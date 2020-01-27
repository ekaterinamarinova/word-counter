package com.personaltask.wordcounter.service;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

public class WordCountServiceTest {

    private WordCountService service = new WordCountService();

    @Test
    public void testCountWords() {
        String mockContent = "mock .message,! ?\n\r";
        Map<String, Integer> expected = new TreeMap<>();
        expected.put("message", 1);
        expected.put("mock", 1);

        Map<String, Integer> actual = service.countWords(mockContent);

        Assert.assertEquals(expected, actual);
    }

}
