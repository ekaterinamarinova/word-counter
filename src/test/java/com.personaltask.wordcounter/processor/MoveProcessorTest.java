package com.personaltask.wordcounter.processor;

import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.property.yml.GoogleCloudProperties;
import com.personaltask.wordcounter.service.StorageService;
import org.apache.camel.Exchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MoveProcessorTest extends CamelTestSupport {

    @Mock
    private StorageService storageService;

    @Mock
    private GoogleCloudProperties properties;

    private MoveProcessor moveProcessor;

    private Exchange exchange;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        exchange = createExchangeWithBody("mock body");
        exchange.setProperty(Constants.BLOB_DEST_KEY, "mockValue");
        exchange.setProperty(Constants.BLOB_NAME_KEY, "mockValue");
        exchange.setProperty(Constants.BUCKET_KEY, "mockValue");
    }

    @Test
    public void testProcess() throws Exception {
        when(properties.getInbound()).thenReturn("inbound");
        doNothing().when(storageService).moveBlob(anyString(), anyString(), anyString());

        moveProcessor = new MoveProcessor(storageService, properties);
        moveProcessor.process(exchange);

        verify(storageService, times(1)).moveBlob(anyString(), anyString(), any());
    }
}
