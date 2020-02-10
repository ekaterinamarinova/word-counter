package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import com.personaltask.wordcounter.service.FileOperations;
import lombok.val;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class CleanLocalDirProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanLocalDirProcessor.class);
    public static final String NAME = "cleanLocalDirProcessor";

    private FileOperations fileOperations;
    private ApplicationProperties properties;

    public CleanLocalDirProcessor(FileOperations fileOperations, ApplicationProperties properties) {
        this.fileOperations = fileOperations;
        this.properties = properties;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOGGER.debug("Deleting local temporary directory for " +
                "file storage: " + properties.getFileDestinationLocal());

        val pathToDirectory = Paths.get(properties.getFileDestinationLocal());
        val isDirectoryDeleted = fileOperations.deleteDirWithContent(pathToDirectory);

        if (isDirectoryDeleted) {
            LOGGER.debug("Directory " + pathToDirectory.toString() +
                    " and its content successfully deleted.");
        } else {
            LOGGER.error("Failed to delete local directory " + pathToDirectory.toString() +
                    " because it does not exist.");
        }

    }
}
