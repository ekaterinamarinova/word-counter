package com.personaltask.wordcounter.service;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOperationsTest {

    private static final String DIRECTORY = "test";
    private static final String FILE_NAME = "/FileOperationsTest.txt";

    private FileOperations fileOperations = new FileOperations();

    @Test
    public void testCreateFile() throws IOException {
        Path directoryPath = Paths.get(DIRECTORY);
        String fileName = FILE_NAME;

        Path result = fileOperations.createFile(directoryPath, fileName);

        Assert.assertTrue(Files.exists(result));
        Assert.assertTrue(Files.isWritable(result));

        Files.delete(Paths.get(directoryPath + fileName));
        Files.delete(directoryPath);
    }

    @Test
    public void testWriteToFile() throws IOException {
        Path dir = Files.createDirectory(Paths.get(DIRECTORY));
        Path pathToFile = Files.createFile(Paths.get(FILE_NAME.substring(1)));
        String content = "content 123";

        Path result = fileOperations.writeToFile(pathToFile, content);

        String resultContent = new String(Files.readAllBytes(result));

        Assert.assertTrue(Files.exists(result));
        Assert.assertTrue(Files.isWritable(result));
        Assert.assertEquals(resultContent, content);

        Files.delete(pathToFile);
        Files.delete(dir);
    }

    @Test
    public void testReadFromFile() throws IOException {
        Path dir = Files.createDirectory(Paths.get(DIRECTORY));
        Path pathToFile = Files.createFile(Paths.get(FILE_NAME.substring(1)));
        String content = "content 123";
        Files.write(pathToFile, content.getBytes());

        String result = fileOperations.readFromFile(pathToFile);

        Assert.assertEquals(content, result);

        Files.delete(pathToFile);
        Files.delete(dir);
    }

    @Test
    public void testDeleteFile() throws IOException {
        Path dir = Files.createDirectory(Paths.get(DIRECTORY));
        Path pathToFile = Files.createFile(Paths.get(FILE_NAME.substring(1)));

        boolean isFileDeleted = fileOperations.deleteFile(pathToFile);
        boolean isDirDeleted = fileOperations.deleteFile(dir);

        Assert.assertTrue(isFileDeleted);
        Assert.assertTrue(isDirDeleted);
        Assert.assertFalse(Files.exists(pathToFile));
        Assert.assertFalse(Files.exists(dir));
    }
}
