package com.personaltask.wordcounter.route;

import com.personaltask.wordcounter.exception.*;
import com.personaltask.wordcounter.processor.*;
import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import com.personaltask.wordcounter.property.yml.CamelProperties;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
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
    private CamelProperties camelProperties;
    private ConfigurableApplicationContext context;

    @Autowired
    public WordCounterRoute(ApplicationProperties applicationProperties, CamelProperties camelProperties, ConfigurableApplicationContext context) {
        this.applicationProperties = applicationProperties;
        this.camelProperties = camelProperties;
        this.context = context;
    }

    public void configure() throws Exception {
        onException(NoSuchBucketException.class)
                .process(ExceptionLoggingProcessor.NAME)
                .process(exchange -> stop());

        onException(UnsuccessfulBlobCreationException.class,
                    UnsuccessfulBlobMovingException.class,
                    UnsuccessfulBlobDeletionException.class,
                    UnsuccessfulBlobFetchingException.class,
                    BlobNotFoundException.class)
                .handled(true)
                .maximumRedeliveries(camelProperties.getMaximumRedeliveries())
                .delay(camelProperties.getDelayInMilliseconds())
                .process(ExceptionLoggingProcessor.NAME);

        onException(IOException.class)
                .handled(true)
                .process(ExceptionLoggingProcessor.NAME);

        onCompletion()
                .process(CleanLocalDirProcessor.NAME);

        from("quartz2://simpleCron?cron=" + applicationProperties.getCronExpressionWorkdaysEachMinute())
                .process(DownloadProcessor.NAME)
                .split(body())
                    .process(WordCountProcessor.NAME)
                    .process(UploadProcessor.NAME)
                    .process(MoveProcessor.NAME)
                .end();
    }

    private void stop() throws Exception {
        getContext().getShutdownStrategy().setTimeout(1);
        getContext().stop();
        context.close();
        System.exit(1);
    }
}
