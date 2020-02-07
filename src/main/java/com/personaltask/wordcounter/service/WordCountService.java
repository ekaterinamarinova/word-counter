package com.personaltask.wordcounter.service;

import org.springframework.stereotype.Service;

import java.util.TreeMap;

@Service
public class WordCountService {

    private static final String WHITE_SPACE = " ";

    /**
     * A method that counts the words in a given message string.
     * The algorithm saves each word and how many times it has
     * occurred in the current string.
     *
     * @param message - working string
     * @return - a {@link TreeMap} containing the word itself as a {@link String} key
     * and how many times it has been repeated as an {@link Integer} value
     */
    public TreeMap<String, Integer> countWords(String message) {
        TreeMap<String, Integer> differentWords = new TreeMap<>();

        String[] words = message.toLowerCase()
                .replaceAll("[,.\\-!?\"\n\r]", WHITE_SPACE)
                .split("\\s+");

        for (String word : words) {
            Integer count = differentWords.get(word);
            differentWords.put(word, count == null ? 1 : count + 1);
        }

        return differentWords;
    }
}
