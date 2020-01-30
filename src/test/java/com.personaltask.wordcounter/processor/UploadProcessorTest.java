package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.service.FileOperations;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UploadProcessorTest extends CamelTestSupport {

    @Mock
    private StorageService storageService;

    @Mock
    private GoogleCloudProperties properties;

    @Mock
    private FileOperations fileOperations;

    private UploadProcessor uploadProcessor;

    private Exchange exchange;

    private Path mockPath1;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        mockPath1 = Paths.get("mockPath1");

        exchange = createExchangeWithBody(mockPath1);
        exchange.setProperty(Constants.BLOB_NAME_KEY, "/MockBlobName.txt");
    }

    @Test
    public void testProcess() throws Exception {
        when(properties.getBucket()).thenReturn("bucket");
        when(properties.getOutbound()).thenReturn("");
        when(properties.getDone()).thenReturn("done");

        when(fileOperations.readFromFile(mockPath1)).thenReturn("mock content 123");

        when(storageService.uploadFile(anyString(), anyString(), any()))
                .thenReturn(null);

        uploadProcessor = new UploadProcessor(storageService, properties, fileOperations);

        uploadProcessor.process(exchange);

        Assert.assertEquals("bucket", exchange.getProperty(Constants.BUCKET_KEY));
        Assert.assertEquals("done/MockBlobName.txt", exchange.getProperty(Constants.BLOB_DEST_KEY));

        verify(storageService, times(1)).uploadFile(anyString(), anyString(), any());
        verify(fileOperations, times(1)).readFromFile(mockPath1);
    }
}
