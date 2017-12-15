package com.jj.concurrent;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

/**
 * Created by yejaz on 12/21/2016.
 */
public class MyThread {

    static class Sum implements Callable<Long> {
        private final long from;
        private final long to;
        Sum(long from, long to) {
            this.from = from;
            this.to = to;
        }
        @Override
        public Long call() {
            long acc = 0;
            for (long i = from; i <= to; i++) {
                acc = acc + i;
            }
            return acc;
        }
    }

    public static void main(String[] args) throws Exception {
        //compute sums of long integers
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<Long>> results = executor.invokeAll(Arrays.asList(new Sum(0, 10), new Sum(100, 1_100), new Sum(10_000, 1_000_000)));
        //this is needed as it will block the program from exiting
        executor.shutdown();

        //need to wait for the result, not yet in asynchronous fashion.
        //can't check each individually and cancel
        for (Future<Long> result: results) {
            System.out.println(result.get());
        }

        //divide and conquer => map and reduce/functional lang
        //split phase: map phase
        //collect the result: reduce phase

    }
}

class Document {
    private final List<String> lines;

    Document(List<String> lines) {
        this.lines = lines;
    }

    List<String> getLines() {
        return this.lines;
    }

    static Document fromFile(File file) throws IOException {
        //infer String type
        List<String> lines = new LinkedList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }//only try block
        return new Document(lines);
    }
}

class Folder {
    private final List<Folder> subFolders;
    private final List<Document> documents;

    Folder(List<Folder> subFolders, List<Document> documents) {
        this.subFolders = subFolders;
        this.documents = documents;
    }

    List<Folder> getSubFolders() {
        return this.subFolders;
    }

    List<Document> getDocuments() {
        return this.documents;
    }

    static Folder fromDirectory(File dir) throws IOException {
        List<Document> documents = new LinkedList<>();
        List<Folder> subFolders = new LinkedList<>();
        for (File entry : dir.listFiles()) {
            if (entry.isDirectory()) {
                subFolders.add(Folder.fromDirectory(entry));
            } else {
                documents.add(Document.fromFile(entry));
            }
        }
        return new Folder(subFolders, documents);
    }
}

//We will implement two types of fork/join tasks.
// Intuitively, the number of occurrences of a word in a folder is the sum of those
// in each of its subfolders and documents.
// Hence, we will have one task for counting the occurrences in a document and one for counting them in a folder.
// The latter type forks children tasks and then joins them to collect their findings.
class WordCounter {

    String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    //The occurrencesCount method returns the number of occurrences of a word in a document,
    Long occurrencesCount(Document document, String searchedWord) {
        long count = 0;
        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                if (searchedWord.equals(word)) {
                    count = count + 1;
                }
            }
        }
        return count;
    }
}