package com.personaltask.wordcounter.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.personaltask.wordcounter.constant.Constants;
import com.personaltask.wordcounter.exception.*;
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
    private static final String SLASH = "/";

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
     * @throws NoSuchBucketException - if something with the blob/bucket is/goes wrong
     * @throws IOException           - when something goes wrong with creating a directory for local storage
     */
    public List<Path> downloadFiles(String bucket,
                                    String fileNamePrefix,
                                    String ext,
                                    String destination)
            throws IOException, NoSuchBucketException {

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
                int indexOfSlash = blob.getName().lastIndexOf(SLASH);

                val blobNameExtension = blob.getName().substring(indexOfSlash);
                val directoryPath = Paths.get(destination).toAbsolutePath();
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
     * @param oldBucket     - bucket to be moved from
     * @param oldBlobDest   - destination in bucket to be moved from
     * @param newBucket     - bucket to be moved to
     * @param newBlobDest   - destination in bucket + (new) blob name + (new) blob extension
     * @throws BlobNotFoundException - if blob is null
     * @throws UnsuccessfulBlobMovingException   - if a {@link StorageException} occurs while moving
     * @throws UnsuccessfulBlobDeletionException - if a {@link StorageException} occurs while deleting
     * @throws UnsuccessfulBlobFetchingException - if a {@link StorageException} occurs while fetching
     */
    public void moveBlob(String oldBucket,
                         String oldBlobDest,
                         String newBucket,
                         String newBlobDest)
            throws BlobNotFoundException, UnsuccessfulBlobMovingException, UnsuccessfulBlobDeletionException, UnsuccessfulBlobFetchingException {
        BlobId blobId = BlobId.of(oldBucket, oldBlobDest);

        try {
            val blob = fetchBlob(blobId);
            blob.copyTo(newBucket, newBlobDest);
            boolean deleted = deleteBlob(blob);

            if (!deleted) {
                LOGGER.error("Deleting of blob " + blob.getName() + " was not successful.");
            }
        } catch (StorageException e) {
            throw new UnsuccessfulBlobMovingException("Failed to move blob " + blobId.getName());
        }
    }

    /**
     * Fetches a blob from the Storage.
     *
     * @param blobId - contains blob info that's needed in order to get the object.
     * @return - fetched {@link Blob} object
     * @throws BlobNotFoundException             - if blob is {@code null}
     * @throws UnsuccessfulBlobFetchingException - if a {@link StorageException} occurs
     */
    protected Blob fetchBlob(BlobId blobId) throws BlobNotFoundException, UnsuccessfulBlobFetchingException {
        try {
            val blob = storage.get(blobId);

            if (blob == null) {
                throw new BlobNotFoundException("Blob with name: " + blobId.getName() + " could not be found.");
            }

            return blob;
        } catch (StorageException e) {
            throw new UnsuccessfulBlobFetchingException("Fetching blob with name: " + blobId.getName() + " failed.");
        }
    }

    /**
     * Deletes a blob from the Storage.
     *
     * @param blob - {@link Blob} to be deleted
     * @return - {@code true} if deleted successfully, {@code false} if not.
     * @throws UnsuccessfulBlobDeletionException - if a {@link StorageException} occurs
     */
    protected boolean deleteBlob(Blob blob) throws UnsuccessfulBlobDeletionException {
        try {
            return blob.delete();
        } catch (StorageException e) {
            throw new UnsuccessfulBlobDeletionException("Deleting blob with name: " + blob.getName() + " failed.");
        }
    }

    /**
     * Uploads a {@link Blob} to a destination bucket.
     *
     * @param bucket       - bucket name
     * @param blobFullName - path to blob (directories) + name + extension
     * @param content      - content to be written to blob
     */
    public Blob uploadFile(String bucket,
                           String blobFullName,
                           byte[] content) throws UnsuccessfulBlobCreationException {
        LOGGER.debug("Uploading file to bucket " + bucket + ", with blob destination " + blobFullName);

        BlobId blobId = BlobId.of(bucket, blobFullName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(Constants.CONTENT_TYPE)
                .build();

        try {
            val uploadedBlob = storage.create(blobInfo, content);
            LOGGER.debug("Blob " + blobFullName + " uploaded to bucket " + bucket);
            return uploadedBlob;
        } catch (StorageException e) {
            throw new UnsuccessfulBlobCreationException("Creating blob: " + blobFullName +
                    " in bucket: " + bucket + " failed.");
        }
    }

}
