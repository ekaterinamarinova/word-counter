package com.personaltask.wordcounter.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.exception.BlobNotFoundException;
import com.personaltask.wordcounter.exception.InvalidBlobDestinationException;
import com.personaltask.wordcounter.exception.NoSuchBucketException;
import com.personaltask.wordcounter.exception.UnsuccessfulBlobDeletionException;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private Storage storage;
    private FileOperations fileOperations;

    public StorageService(Storage storage, FileOperations fileOperations) {
        this.storage = storage;
        this.fileOperations = fileOperations;
    }

    /**
     * Downloads files from google cloud storage by prefix,
     * moves successfully downloaded ones to outbound.
     *
     * @param bucket         - bucket to download/upload to
     * @param fileNamePrefix - the prefix used to list the file
     * @param ext            - the file extension
     * @param destination    - the local folder that's created to store the downloaded file
     * @return - {@link List<Path>} containing all the blob paths on the local system
     * @throws NoSuchBucketException    - if something with the blob/bucket is/goes wrong
     * @throws IOException              - when something goes wrong with creating a directory for local storage
     * @throws InvalidBlobDestinationException - when slash is not found
     */
    public List<Path> downloadFiles(String bucket,
                                    String fileNamePrefix,
                                    String ext,
                                    String destination)
            throws IOException, InvalidBlobDestinationException, NoSuchBucketException {

        if (ObjectUtils.isEmpty(bucket)) {
            throw new NoSuchBucketException("Bucket name is null. Check configuration file.");
        }

        LOGGER.debug("Fetching file " + fileNamePrefix + " from bucket: {}", bucket);
        Page<Blob> blobs = storage.list(
                bucket,
                Storage.BlobListOption.prefix(fileNamePrefix)
        );

        List<Path> blobPathList = new ArrayList<>();

        for (Blob blob : blobs.iterateAll()) {
            if (blob.getSize() > 0 && blob.getName().endsWith(ext)) {
                int indexOfSlash = blob.getName().lastIndexOf(Constants.SLASH);

                if (indexOfSlash == -1) {
                    throw new InvalidBlobDestinationException("No occurrence of " + Constants.SLASH + " in blob name");
                }

                val blobNameExtension = blob.getName().substring(indexOfSlash);
                val directoryPath = Paths.get(destination).toAbsolutePath().normalize();
                val pathToEmptyFile = fileOperations.createFile(
                        directoryPath,
                        blobNameExtension
                );
                val pathToBlob = fileOperations.writeToFile(pathToEmptyFile, new String(blob.getContent()));

                //if no exception occurs, add current blob path to the list to be used on downstream processing
                blobPathList.add(pathToBlob);
            }
        }

        return blobPathList;
    }

    /**
     * Moves a {@link Blob} to a specific bucket and destination in that bucket.
     *
     * @param oldBucket   - target bucket name
     * @param newBlobDest - destination in bucket + (new) blob name + (new) blob extension
     * @throws BlobNotFoundException - if blob to be moved was not found
     */
    public void moveBlob(String oldBucket, String oldBlobDest, String newBucket, String newBlobDest) throws BlobNotFoundException, UnsuccessfulBlobDeletionException {
        BlobId blobId = BlobId.of(oldBucket, oldBlobDest);

        Blob blob = storage.get(blobId);

        if (blob == null) {
            throw new BlobNotFoundException("Blob with destination " + oldBlobDest + " could not be fetched.");
        }

        blob.copyTo(newBucket, newBlobDest);
        try {
            boolean deleted = blob.delete();
            if (!deleted) {
                LOGGER.error("Deleting of blob " + blob.getName() + " was not successful.");
            }
        } catch (StorageException e) {
            throw new UnsuccessfulBlobDeletionException("Failed to delete blob " + blob.getName());
        }
    }

    /**
     * Uploads a {@link Blob} to a destination bucket.
     *
     * @param bucket       - bucket name
     * @param blobFullName - path to blob (directories) + name + extension
     * @param content      - content to be written to blob
     */
    public Blob uploadFile(String bucket, String blobFullName, byte[] content) {
        LOGGER.debug("Uploading file to bucket " + bucket + ", with blob destination " + blobFullName);

        BlobId blobId = BlobId.of(bucket, blobFullName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(Constants.CONTENT_TYPE)
                .build();
        Blob uploadedBlob = storage.create(blobInfo, content);

        LOGGER.debug("Blob " + blobFullName + " uploaded to bucket " + bucket);

        return uploadedBlob;
    }

}
