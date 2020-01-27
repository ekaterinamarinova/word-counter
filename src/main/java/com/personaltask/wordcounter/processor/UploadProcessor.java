package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.service.StorageService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UploadProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadProcessor.class);

    private StorageService storageService;
    private GoogleCloudProperties properties;

    public UploadProcessor(StorageService storageService, GoogleCloudProperties properties) {
        this.storageService = storageService;
        this.properties = properties;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOGGER.debug("Begin uploading blob with path " +
                properties.getDone() + properties.getFileNamePrefix() + properties.getExt() +
                "and content: <" + exchange.getIn().getBody().toString() + ">.");

        storageService.uploadFile(
                properties.getBucket(),
                properties.getDone() + properties.getFileNamePrefix() + properties.getExt(),
                exchange.getIn().getBody().toString().getBytes()
        );

        LOGGER.debug("Uploading to bucket " + properties.getBucket() + " complete.");
    }
}
