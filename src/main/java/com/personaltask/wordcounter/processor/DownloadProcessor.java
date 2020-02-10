package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.service.StorageService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.nio.file.Path;
import java.util.List;


/**
 * Processor used for downloading files from Google Cloud.
 *
 * @author EMarinova
 */
@Component
public class DownloadProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadProcessor.class);
    public static final String NAME = "downloadProcessor";

    private StorageService storageService;
    private GoogleCloudProperties googleCloudProperties;
    private ApplicationProperties applicationProperties;

    public DownloadProcessor(StorageService storageService, GoogleCloudProperties googleCloudProperties, ApplicationProperties applicationProperties) {
        this.storageService = storageService;
        this.googleCloudProperties = googleCloudProperties;
        this.applicationProperties = applicationProperties;
    }

    public void process(Exchange exchange) throws Exception {
        LOGGER.debug("Attempting download...");
        List<Path> pathList = storageService.downloadFiles(
                googleCloudProperties.getBucket(),
                googleCloudProperties.getInbound() + googleCloudProperties.getFileNamePrefix(),
                googleCloudProperties.getExt(),
                applicationProperties.getFileDestinationLocal() + applicationProperties.getDownloaded()
        );

        if (pathList.isEmpty()) {
            LOGGER.debug("Blob list is empty. Retrying...");
        }

        exchange.getIn().setBody(pathList);
    }
}
