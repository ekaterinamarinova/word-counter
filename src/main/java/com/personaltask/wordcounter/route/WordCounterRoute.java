package com.personaltask.wordcounter.route;

import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.exception.BlobNotFoundException;
import com.personaltask.wordcounter.exception.NoSuchBucketException;
import com.personaltask.wordcounter.processor.*;
import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import org.apache.camel.Expression;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.language.ExpressionDefinition;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Component, holding the route to be executed
 *
 * @author EMarinova
 */
@Component
public class WordCounterRoute extends RouteBuilder {

    private ApplicationProperties applicationProperties;
    private GoogleCloudProperties googleCloudProperties;

    public WordCounterRoute(ApplicationProperties applicationProperties, GoogleCloudProperties googleCloudProperties) {
        this.applicationProperties = applicationProperties;
        this.googleCloudProperties = googleCloudProperties;
    }

    public void configure() throws Exception {

        onException(NoSuchBucketException.class)
                .log(LoggingLevel.ERROR, "Bucket is null, check configuration file.")
                .stop();

        onException(BlobNotFoundException.class)
                .log("Couldn't move blob because it was not found")
                .handled(true);

        onException(IOException.class)
                .log("I/O exception")
//                .setProperty()
                .process(MoveProcessor.NAME);

        from("quartz2://simpleCron?cron=" + applicationProperties.getCronExpressionWorkdaysEachMinute())
                .process(DownloadProcessor.NAME)
                .split(body())
                    .process(WordCountProcessor.NAME)
                    .process(UploadProcessor.NAME)
                    .process(MoveProcessor.NAME)
                .end()
                .process(CleanLocalDirProcessor.NAME);

    }
}
