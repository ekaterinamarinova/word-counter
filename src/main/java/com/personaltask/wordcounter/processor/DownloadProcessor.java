package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.storage.StorageService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;


/**
 * Processor used for downloading files from Google Cloud
 *
 * @author EMarinova
 */
@Component
public class DownloadProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadProcessor.class);

    private StorageService storageService;
    private GoogleCloudProperties properties;

    public DownloadProcessor(StorageService storageService, GoogleCloudProperties properties) {
        this.storageService = storageService;
        this.properties = properties;
    }

    public void process(Exchange exchange) throws Exception {
        LOGGER.debug("Attempting download...");
        List<Path> content = storageService.downloadFile(
                properties.getBucket(),
                properties.getInbound() + properties.getFileNamePrefix(),
                properties.getExt(),
                properties.getFileDestination()
        );
        LOGGER.debug("File successfully downloaded.");
        exchange.getIn().setBody(content);
    }
}
