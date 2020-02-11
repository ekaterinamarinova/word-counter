package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import com.personaltask.wordcounter.service.FileOperations;
import lombok.var;
import org.apache.camel.Exchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CleanLocalDirProcessorTest extends CamelTestSupport {
    @Mock
    private FileOperations fileOperations;

    @Mock
    private ApplicationProperties properties;

    private Exchange exchange;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        exchange = createExchangeWithBody("mock body");
    }

    @Test
    public void testProcess() throws Exception {
        var dir = Files.createDirectory(Paths.get("test"));
        var file = Files.createFile(Paths.get(dir + "/NewTestFile.txt"));

        when(properties.getFileDestinationLocal()).thenReturn(file.toString());
        when(fileOperations.deleteDirWithContent(any())).thenReturn(true);

        CleanLocalDirProcessor cleanLocalDirProcessor = new CleanLocalDirProcessor(fileOperations, properties);
        cleanLocalDirProcessor.process(exchange);

        verify(fileOperations, times(1)).deleteDirWithContent(any());

        Files.deleteIfExists(file);
        Files.deleteIfExists(dir);
    }
}
