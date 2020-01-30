package com.personaltask.wordcounter.service;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

/**
 * Used for different types of operations with files:
 * creating, deleting, writing to and reading from files.
 * <p>
 * Basically wraps the functionality of the {@link Files} class,
 * but has a few checks added in order to ease the work.
 *
 * @author EMarinova
 */
@Component
public class FileOperations {

    /**
     * Creates a new file, returning the same path if already exists.
     *
     * @param directoryPath - path to the directory where where the new file will be located
     * @param fileName      - name of the new file + file extension
     * @return - {@link Path} to the new file OR to the old one if a file
     * with the same name already exists in that directory.
     * @throws IOException - if something goes wrong on creation
     */
    public Path createFile(Path directoryPath, String fileName) throws IOException {
        Path fullPathToFile = Paths.get(
                directoryPath.toAbsolutePath().toString() + FileSystems.getDefault().getSeparator() + fileName
        );

        if (Files.exists(fullPathToFile)) {
            return fullPathToFile;
        }

        if (Files.isDirectory(directoryPath)) {
            return Files.createFile(fullPathToFile);
        }

        Files.createDirectories(directoryPath);
        return Files.createFile(fullPathToFile);
    }

    /**
     * Writes content to an already existing file.
     * If such file already exists, the content is being overridden.
     *
     * @param fullPathToFile - full path to the file, including directory, file name and extension
     * @param content        - stuff to be written to the file
     * @return - {@link Path} to the file with content
     * @throws IOException - if something goes wrong during writing /security exception for example/
     */
    public Path writeToFile(Path fullPathToFile, String content) throws IOException {
        return Files.write(fullPathToFile, content.getBytes(), StandardOpenOption.CREATE);
    }

    /**
     * Reads content with the default charset set to UTF-8 from an already existent file.
     *
     * @param fullPathToFile - full path to the file, including directory, file name and extension
     * @return - {@link String} holding the content
     * @throws IOException - if something goes wrong during reading
     *                     /file is encoded with different charset or does not exist/
     */
    public String readFromFile(Path fullPathToFile) throws IOException {
        return new String(Files.readAllBytes(fullPathToFile));
    }

    /**
     * Deletes a file or directory.
     * <p>
     * If trying to delete a non-empty directory, will throw an exception.
     *
     * @param path - {@link Path} to the file to delete, including directory, file name and extension
     * @return - {@code true} if file exists and it was deleted successfully or
     * {@code false} mostly if file does not exist/was not found.
     * @throws IOException - if trying to delete a non-empty directory or if you have no rights to delete current file
     */
    public boolean deleteFile(Path path) throws IOException {
        return Files.deleteIfExists(path.toAbsolutePath());
    }

    /**
     * Deletes a directory recursively with its content.
     *
     * @param pathToDirectory - path to the directory to delete
     * @return - {@code true} if deleted, {@code false} if does not exist
     * @throws IOException - if an I/O exception occurs, such as {@link java.nio.file.AccessDeniedException}
     */
    public boolean deleteDirWithContent(Path pathToDirectory) throws IOException {
        if (Files.exists(pathToDirectory)) {
            Files.walk(pathToDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

            return true;
        }
        return false;
    }

}
