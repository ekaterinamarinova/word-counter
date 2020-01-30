package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import com.personaltask.wordcounter.service.FileOperations;
import com.personaltask.wordcounter.service.WordCountService;
import org.apache.camel.Exchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WordCountProcessorTest extends CamelTestSupport {

    private static final String FILE_NAME = "/WordCounterProcessorTest.txt";

    @Mock
    private WordCountService wordCountService;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private FileOperations fileOperations;

    @InjectMocks
    private WordCountProcessor wordCountProcessor;

    private Exchange exchange;

    private Path mockPath1;

    private Map<String, Integer> wordMap;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockPath1 = Paths.get("test");
        wordMap = new TreeMap<>();
        wordMap.put("content", 1);
        wordMap.put("mock", 1);
    }

    @Test
    public void testProcess() throws Exception {
        Path dir = Paths.get(mockPath1 + FILE_NAME);
        exchange = createExchangeWithBody(dir);

        //mock properties
        when(properties.getFileDestinationLocal()).thenReturn("");
        when(properties.getCounted()).thenReturn("");

        //mock file operations
        when(fileOperations.readFromFile(dir)).thenReturn("mock content");
        when(fileOperations.createFile(any(), anyString())).thenReturn(dir);
        when(fileOperations.writeToFile(any(), anyString())).thenReturn(dir);

        //mock service
        doReturn(wordMap).when(wordCountService).countWords("mock content");

        wordCountProcessor = new WordCountProcessor(wordCountService, properties, fileOperations);

        wordCountProcessor.process(exchange);
        
        verify(fileOperations, times(1)).readFromFile(dir);
        verify(fileOperations, times(1)).createFile(any(), anyString());
        verify(fileOperations, times(1)).writeToFile(any(), anyString());

        verify(wordCountService, times(1)).countWords("mock content");

        Assert.assertEquals(exchange.getIn().getBody(), dir);
    }
}
