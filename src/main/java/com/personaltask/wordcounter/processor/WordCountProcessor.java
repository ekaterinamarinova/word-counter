package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import com.personaltask.wordcounter.service.FileOperations;
import com.personaltask.wordcounter.service.WordCountService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Processor used for counting the words from the downloaded files.
 *
 * @author EMarinova
 */
@Component
public class WordCountProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(WordCountProcessor.class);
    public static final String NAME = "wordCountProcessor";

    private final WordCountService service;
    private final ApplicationProperties properties;
    private final FileOperations fileOperations;

    public WordCountProcessor(WordCountService service, ApplicationProperties properties, FileOperations fileOperations) {
        this.service = service;
        this.properties = properties;
        this.fileOperations = fileOperations;
    }

    @Override
    public void process(Exchange exchange) throws IOException {
        LOGGER.debug("Beginning of processing file with path: " + exchange.getIn().getBody());

        var pathToFileForProcessing = exchange.getIn().getBody(Path.class);
        var downloadedBlobName = pathToFileForProcessing.getFileName().toString();
        var content = fileOperations.readFromFile(pathToFileForProcessing);

        Map<String, Integer> result = service.countWords(content);

        var pathToEmptyFile = fileOperations.createFile(
                Paths.get(properties.getFileDestinationLocal() + properties.getCounted()),
                properties.getGeneratedFileNamePrefix() + downloadedBlobName
        );

        var pathToFileWithContent = fileOperations.writeToFile(pathToEmptyFile, result.toString());

        exchange.getIn().setBody(pathToFileWithContent);
        exchange.setProperty(Constants.BLOB_NAME, downloadedBlobName);

        LOGGER.debug("Processing of file " + pathToFileForProcessing + " resulted in generating file with path: " + pathToFileWithContent);
    }
}
