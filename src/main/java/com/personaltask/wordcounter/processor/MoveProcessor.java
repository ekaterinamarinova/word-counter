package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.service.StorageService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Processor used to move blobs to specific destinations.
 *
 * @author EMarinova
 */
@Component
public class MoveProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadProcessor.class);

    public static final String NAME = "moveProcessor";

    private final StorageService storageService;
    private final GoogleCloudProperties properties;

    public MoveProcessor(StorageService storageService, GoogleCloudProperties properties) {
        this.storageService = storageService;
        this.properties = properties;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        var bucket = exchange.getProperty(Constants.BUCKET, String.class);
        var oldBlobDest = properties.getInbound() + exchange.getProperty(Constants.BLOB_NAME, String.class);
        var newBlobDest = exchange.getProperty(Constants.BLOB_DESTINATION, String.class);

        LOGGER.debug("Initiating moving of blob from: " + oldBlobDest + " to: " + newBlobDest);
        storageService.moveBlob(bucket, oldBlobDest, bucket, newBlobDest);
        LOGGER.debug("Blob moved successfully");
    }
}
