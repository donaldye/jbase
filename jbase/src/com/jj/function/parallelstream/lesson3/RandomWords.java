package com.jj.function.parallelstream.lesson3;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Class to generate a list of random words
 *
 * @author Simon Ritter (@speakjava)
 */
public class RandomWords {
    private final List<String> sourceWords;

    /**
     * Constructor
     *
     * @throws IOException If the source words file cannot be read
     */
    public RandomWords() throws IOException {
        String current = new java.io.File( "." ).getCanonicalPath();
        System.out.println("Current dir:"+current);
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current dir using System:" +currentDir);
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("src/com/jj/parallelstream/lesson3/words.txt"))) {
            sourceWords = //null;    // YOUR CODE HERE
                reader.lines().collect(Collectors.toList());
            System.out.println("Loaded " + sourceWords.size() + " words");
        }
    }

    /**
     * Create a list of a given size containing random words
     *
     * @param listSize The size of the list to create
     * @return The created list
     */
    public List<String> createList(int listSize) {
        Random rand = new Random();
        List<String> wordList = //null; // YOUR CODE HERE
            //rand.ints(listSize, 0, sourceWords.size()).mapToObj(i -> sourceWords.get(i)).collect(Collectors.toList());
                rand.ints(listSize, 0, sourceWords.size())
                        .mapToObj(sourceWords::get)
                        .collect(Collectors.toList());
        return wordList;
    }

    /**
     * Return the list of all source words, which cannot be modified
     *
     * @return The unmodifiable list of all source words
     */
    public List<String> allWords() {
        return Collections.unmodifiableList(sourceWords);
    }
}

