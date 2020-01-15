package com.personaltask.wordcounter.storage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.personaltask.wordcounter.exception.NoSuchFileException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ObjectUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageServiceTest {

    @Mock
    private Storage storage;

    @Mock
    private Page<Blob> page;

    @Mock
    private Blob blob;

    @InjectMocks
    private StorageService service;

    private Iterable<Blob> iterable;

    @Before
    public void setup() {
        iterable = new ArrayList<>();

    }

    @Test
    public void testDownloadFile() throws Exception {
        when(blob.getSize()).thenReturn(Long.valueOf(12345));
        when(blob.getName()).thenReturn("/BlobName.txt");
        ((ArrayList<Blob>) iterable).add(blob);
        when(page.iterateAll()).thenReturn(iterable);
        when(storage.list(anyString(), any())).thenReturn(page);

        List<Path> result = service.downloadFile("bucket", "prefix", "txt", "testTemp");

        verify(storage, times(1)).list(anyString(), any());
        Assert.assertFalse(ObjectUtils.isEmpty(result));
    }

    @Test(expected = NoSuchFileException.class)
    public void testDownloadFile_withNullBucket() throws Exception {
        service.downloadFile(null, "prefix", "txt", "testTemp");
    }

    @Test(expected = Exception.class)
    public void testDownloadFile_withNoSlash() throws Exception {
        when(blob.getSize()).thenReturn(Long.valueOf(12345));
        when(blob.getName()).thenReturn("BlobName.txt");
        ((ArrayList<Blob>) iterable).add(blob);
        when(page.iterateAll()).thenReturn(iterable);
        when(storage.list(anyString(), any())).thenReturn(page);

        service.downloadFile("bucket", "prefix", "txt", "testTemp");

    }

    @Test(expected = NoSuchFileException.class)
    public void testDownloadFile_withEmptyFile() throws Exception {
        when(blob.getSize()).thenReturn(Long.valueOf(0));
        ((ArrayList<Blob>) iterable).add(blob);
        when(page.iterateAll()).thenReturn(iterable);
        when(storage.list(anyString(), any())).thenReturn(page);

        service.downloadFile("bucket", "prefix", "txt", "testTemp");
    }

}
