package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.service.FileOperations;
import com.personaltask.wordcounter.service.StorageService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Processor used for uploading files to Google Cloud.
 *
 * @author EMarinova
 */
@Component
public class UploadProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadProcessor.class);
    public static final String NAME = "uploadProcessor";

    private StorageService storageService;
    private GoogleCloudProperties properties;
    private FileOperations fileOperations;

    public UploadProcessor(StorageService storageService, GoogleCloudProperties properties, FileOperations fileOperations) {
        this.storageService = storageService;
        this.properties = properties;
        this.fileOperations = fileOperations;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        var pathToProcessedFile = exchange.getIn().getBody(Path.class);
        var contentFromProcessedFile = fileOperations.readFromFile(pathToProcessedFile);
        var blobName = exchange.getProperty(Constants.BLOB_NAME);

        LOGGER.debug("Begin uploading blob with path " + properties.getOutbound() + blobName);

        storageService.uploadFile(
                properties.getBucket(),
                properties.getOutbound() + blobName,
                contentFromProcessedFile.getBytes()
        );

        exchange.setProperty(Constants.BUCKET, properties.getBucket());
        exchange.setProperty(Constants.BLOB_DESTINATION, properties.getDone() + blobName);

        LOGGER.debug("Uploading to bucket: " + properties.getBucket() +
                " with destination: " + properties.getDone() +  " complete.");
    }
}
