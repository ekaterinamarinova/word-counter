package com.personaltask.wordcounter.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


@Component
public class FileOperations {

    public Path createFile(Path directoryPath, String fileName) throws IOException {
        Path fullPathToFile = Paths.get(
                directoryPath.toAbsolutePath().toString() + fileName
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

    public Path writeToFile(Path fullPathToFile, String content) throws IOException {
        return Files.write(fullPathToFile, content.getBytes(), StandardOpenOption.CREATE);
    }

    public String readFromFile(Path fullPathToFile) throws IOException {
        return new String(Files.readAllBytes(fullPathToFile));
    }

    public boolean deleteFile(Path path) throws IOException {
        return Files.deleteIfExists(path.toAbsolutePath());
    }

}
