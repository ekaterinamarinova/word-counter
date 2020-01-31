package com.personaltask.wordcounter.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.exception.BlobNotFoundException;
import com.personaltask.wordcounter.exception.NoSuchBucketException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ObjectUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
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
    private FileOperations fileOperations;

    @Mock
    private Blob blob;

    @InjectMocks
    private StorageService service;

    private Iterable<Blob> iterable;

    private Path testPath;

    private String testFileName;

    @Before
    public void setup() {
        iterable = new ArrayList<>();
        testPath = Paths.get("test");
        testFileName = "StorageServiceTest.txt";
    }

    @Test
    public void testDownloadFile() throws Exception {
        byte[] mockContent = new byte[]{1,0,1,0};
        when(blob.getSize()).thenReturn(Long.valueOf(12345));
        when(blob.getName()).thenReturn("/BlobName.txt");
        when(blob.getContent()).thenReturn(mockContent);
        ((ArrayList<Blob>) iterable).add(blob);
        when(page.iterateAll()).thenReturn(iterable);
        when(storage.list(anyString(), any())).thenReturn(page);
        when(fileOperations.createFile(any(), anyString()))
                .thenReturn(testPath);
        when(fileOperations.writeToFile(any(), anyString()))
                .thenReturn(Paths.get(testPath + testFileName));

        List<Path> result = service.downloadFiles("bucket", "prefix", "txt", "testTemp");

        verify(storage, times(1)).list(anyString(), any());
        Assert.assertFalse(ObjectUtils.isEmpty(result));
    }

    @Test(expected = NoSuchBucketException.class)
    public void testDownloadFile_withNullBucket() throws Exception {
        service.downloadFiles(null, "prefix", "txt", "testTemp");
    }

    @Test(expected = Exception.class)
    public void testDownloadFile_withNoSlash() throws Exception {
        when(blob.getSize()).thenReturn(Long.valueOf(12345));
        when(blob.getName()).thenReturn("/BlobName.txt");
        ((ArrayList<Blob>) iterable).add(blob);
        when(page.iterateAll()).thenReturn(iterable);
        when(storage.list(anyString(), any())).thenReturn(page);

        service.downloadFiles("bucket", "prefix", "txt", "testTemp");

    }

    @Test
    public void testDownloadFile_withEmptyFile() throws Exception {
        when(blob.getSize()).thenReturn(Long.valueOf(0));
        ((ArrayList<Blob>) iterable).add(blob);
        when(page.iterateAll()).thenReturn(iterable);
        when(storage.list(anyString(), any())).thenReturn(page);

        List<Path> result = service.downloadFiles("bucket", "prefix", "txt", "testTemp");

        Assert.assertTrue(ObjectUtils.isEmpty(result));
    }

    @Test
    public void testMoveBlob() throws BlobNotFoundException {
        BlobId blobId = BlobId.of("test", "test");
        when(storage.get(blobId)).thenReturn(blob);
        when(blob.copyTo("test", "test")).thenReturn(null);
        when(blob.delete()).thenReturn(true);

        service.moveBlob("test", "test", "test", "test");

        verify(storage, times(1)).get(blobId);
        verify(blob, times(1)).delete();
    }

    @Test(expected = BlobNotFoundException.class)
    public void testMoveBlob_withBlobNotDeleted() throws BlobNotFoundException {
        BlobId blobId = BlobId.of("test", "test");
        when(storage.get(blobId)).thenReturn(blob);
        when(blob.copyTo("test", "test")).thenReturn(null);
        when(blob.delete()).thenReturn(false);

        service.moveBlob("test", "test", "test", "test");

        verify(storage, times(1)).get(blobId);
        verify(blob, times(1)).delete();
    }

    @Test
    public void testUploadFile() {
        BlobId blobId = BlobId.of("test", "test");
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(Constants.CONTENT_TYPE)
                .build();

        when(storage.create(blobInfo, "content".getBytes())).thenReturn(blob);

        Blob result = service.uploadFile("test", "test", "content".getBytes());

        Assert.assertEquals(blob, result);
        verify(storage, times(1)).create(blobInfo, "content".getBytes());
    }

}
