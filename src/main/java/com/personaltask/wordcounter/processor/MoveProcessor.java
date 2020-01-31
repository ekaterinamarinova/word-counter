package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.service.StorageService;
import lombok.val;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Processor used to move blobs to specific destinations.
 *
 * @author EMarinova
 */
@Component
public class MoveProcessor implements Processor {

    public static final String NAME = "moveProcessor";

    private StorageService storageService;
    private GoogleCloudProperties properties;

    public MoveProcessor(StorageService storageService, GoogleCloudProperties properties) {
        this.storageService = storageService;
        this.properties = properties;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        val oldBlobDest = properties.getInbound() + exchange.getProperty(Constants.BLOB_NAME_KEY, String.class);
        val newBlobDest = exchange.getProperty(Constants.BLOB_DEST_KEY, String.class);
        val bucket = exchange.getProperty(Constants.BUCKET_KEY, String.class);

        storageService.moveBlob(bucket, oldBlobDest, bucket, newBlobDest);
    }
}
