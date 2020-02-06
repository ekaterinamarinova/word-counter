package com.personaltask.wordcounter.route;

import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.exception.BlobNotFoundException;
import com.personaltask.wordcounter.exception.InvalidBlobDestinationException;
import com.personaltask.wordcounter.exception.NoSuchBucketException;
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
                .log(LoggingLevel.ERROR, LOGGER, "Bucket name is null or empty.")
                .stop();

        onException(BlobNotFoundException.class)
                .log(LoggingLevel.ERROR, LOGGER,"Couldn't move blob because it was not found")
                .process(CleanLocalDirProcessor.NAME)
                .stop();

        onException(InvalidBlobDestinationException.class)
                .log(LoggingLevel.ERROR, LOGGER,"Invalid Blob Destination")
                .process(CleanLocalDirProcessor.NAME)
                .stop();

        onException(IOException.class)
                .handled(true)
                .log(LoggingLevel.ERROR, LOGGER, "I/O exception")
                .setProperty(
                        Constants.BLOB_DESTINATION,
                        constant(googleCloudProperties.getError() + exchangeProperty(Constants.BLOB_NAME).toString())
                )
                .process(MoveProcessor.NAME)
                .process(CleanLocalDirProcessor.NAME);

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
