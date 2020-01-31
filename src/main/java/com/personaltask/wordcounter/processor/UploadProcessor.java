package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.service.FileOperations;
import com.personaltask.wordcounter.service.StorageService;
import lombok.val;
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
        val pathToProcessedFile = exchange.getIn().getBody(Path.class);
        val contentFromProcessedFile = fileOperations.readFromFile(pathToProcessedFile);
        val blobName = exchange.getProperty(Constants.BLOB_NAME_KEY);

        LOGGER.debug("Begin uploading blob with path " +
                properties.getOutbound() + blobName + "and content: <" + contentFromProcessedFile + ">.");

        storageService.uploadFile(
                properties.getBucket(),
                properties.getOutbound() + blobName,
                contentFromProcessedFile.getBytes()
        );

        exchange.setProperty(Constants.BUCKET_KEY, properties.getBucket());
        exchange.setProperty(Constants.BLOB_DEST_KEY, properties.getDone() + blobName);

        LOGGER.debug("Uploading to bucket " + properties.getBucket() + " complete.");
    }
}