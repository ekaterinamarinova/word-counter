package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.exception.NoSuchFileException;
import com.personaltask.wordcounter.property.yml.ApplicationProperties;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.service.StorageService;
import org.apache.camel.Exchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DownloadProcessorTest extends CamelTestSupport {

    @Mock
    private StorageService downloadService;

    @Mock
    private GoogleCloudProperties properties;

    @Mock
    private ApplicationProperties applicationProperties;

    private DownloadProcessor downloadProcessor;

    private List<Path> paths;

    private Exchange exchange;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        paths = new ArrayList<>(2);

        Path mockPath1 = Paths.get("mockPath1");
        Path mockPath2 = Paths.get("mockPath2");

        paths.add(mockPath1);
        paths.add(mockPath2);

        exchange = createExchangeWithBody(paths);
    }

    @Test
    public void testProcess() throws Exception {
        when(properties.getBucket()).thenReturn("");
        when(properties.getExt()).thenReturn("");
        when(applicationProperties.getFileDestination()).thenReturn("");
        when(properties.getFileNamePrefix()).thenReturn("");
        when(downloadService.downloadFiles(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(paths);

        downloadProcessor = new DownloadProcessor(downloadService, properties, applicationProperties);

        downloadProcessor.process(exchange);

        Assert.assertEquals(paths, exchange.getIn().getBody());
        verify(downloadService, times(1)).downloadFiles(anyString(), anyString(), anyString(), anyString());
    }

    @Test(expected = NoSuchFileException.class)
    public void test_withException() throws Exception {
        when(properties.getBucket()).thenReturn(null);
        when(properties.getExt()).thenReturn("");
        when(applicationProperties.getFileDestination()).thenReturn("");
        when(properties.getFileNamePrefix()).thenReturn("");
        when(downloadService.downloadFiles(eq(null), anyString(), anyString(), anyString()))
                .thenThrow(new NoSuchFileException("random message"));

        downloadProcessor = new DownloadProcessor(downloadService, properties, applicationProperties);

        downloadProcessor.process(exchange);
        verify(downloadService, times(1)).downloadFiles(eq(null), anyString(), anyString(), anyString());
    }

}
