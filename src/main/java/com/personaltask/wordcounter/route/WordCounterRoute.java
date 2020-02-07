package com.personaltask.wordcounter.route;

import com.personaltask.wordcounter.exception.*;
import com.personaltask.wordcounter.processor.*;
import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Component, holding the route to be executed
 *
 * @author EMarinova
 */
@Component
public class WordCounterRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordCounterRoute.class);

    private ApplicationProperties applicationProperties;
    private GoogleCloudProperties googleCloudProperties;

    public WordCounterRoute(ApplicationProperties applicationProperties, GoogleCloudProperties googleCloudProperties) {
        this.applicationProperties = applicationProperties;
        this.googleCloudProperties = googleCloudProperties;
    }

    public void configure() throws Exception {

        onException(NoSuchBucketException.class)
                .log(LoggingLevel.ERROR, LOGGER, "Bucket name is null or empty.");

        onException(BlobNotFoundException.class)
                .handled(true)
                .process(ExceptionLoggingProcessor.NAME);

        onException(UnsuccessfulBlobMovingException.class,
                    UnsuccessfulBlobCreationException.class,
                    UnsuccessfulBlobDeletionException.class,
                    UnsuccessfulBlobFetchingException.class)
                .handled(true)
                .maximumRedeliveries(5)
                .process(ExceptionLoggingProcessor.NAME);

        onException(IOException.class)
                .handled(true)
                .process(ExceptionLoggingProcessor.NAME);

        from("quartz2://simpleCron?cron=" + applicationProperties.getCronExpressionWorkdaysEachMinute())
                .process(DownloadProcessor.NAME)
                .split(body())
                    .process(WordCountProcessor.NAME)
                    .process(UploadProcessor.NAME)
                    .process(MoveProcessor.NAME)
                .end();

        onCompletion().process(CleanLocalDirProcessor.NAME);
    }
}
