package com.personaltask.wordcounter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This application downloads files with a specific prefix
 * from Google Storage service in a scheduled amount of time.
 * After that it "reads" every word in the current file,
 * and if all processing has been successful -> uploads the
 * result file in a success folder.
 * <p>
 * If any issues are encountered during file reading or processing ->
 * "broken" file is uploaded to an error folder.
 *
 * @author Mladen Ivanov
 */
@SpringBootApplication
public class WordCounterApp {
    public static void main(String[] args) {
        SpringApplication.run(WordCounterApp.class, args);
    }
}
