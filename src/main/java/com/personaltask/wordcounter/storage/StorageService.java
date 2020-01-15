package com.personaltask.wordcounter.storage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.personaltask.wordcounter.exception.NoSuchFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);
    private static final String SLASH = "/";

    private Storage storage;

    public StorageService(Storage storage) {
        this.storage = storage;
    }

    public List<Path> downloadFile(String bucket, String fileNamePrefix, String ext, String destination) throws Exception {

        if (bucket == null) {
            throw new NoSuchFileException("Bucket name is null. Check configuration file.");
        }

        LOGGER.debug("Fetching file " + fileNamePrefix + " from bucket: {}", bucket);
        Page<Blob> blobs = storage.list(
                bucket,
                Storage.BlobListOption.prefix(fileNamePrefix)
        );

        List<Path> blobPathList = new ArrayList<>();

        for (Blob blob : blobs.iterateAll()) {
            if (blob.getSize() > 0 && blob.getName().endsWith(ext)) {
                int indexOfSlash = blob.getName().lastIndexOf(SLASH);

                if (indexOfSlash == -1) {
                    throw new Exception("No occurrence of " + SLASH + " in blob name");
                }

                Path directoryPath = FileSystems.getDefault().getPath(destination + SLASH);
                Path pathToBlob = Paths.get(directoryPath.toString() + blob.getName().substring(indexOfSlash));

                if (!(Files.isDirectory(directoryPath) && Files.exists(directoryPath))) {
                    Files.createDirectory(directoryPath);
                }

                blob.downloadTo(pathToBlob);
                //if no exception occurs, add current blob path to the list to be used on downstream processing
                blobPathList.add(pathToBlob);
            } else {
                throw new NoSuchFileException("Trying to fetch non-existing file.");
            }
        }

        return blobPathList;
    }

}
