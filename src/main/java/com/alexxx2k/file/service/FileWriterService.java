package com.alexxx2k.file.service;

import com.alexxx2k.core.Service;
import com.alexxx2k.file.writer.CsvFileWriter;
import com.alexxx2k.file.writer.JsonFileWriter;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public class FileWriterService extends Service {
    private final boolean fileType;
    private final BlockingQueue<String> responseQueue;

    public FileWriterService(int threadAmount, BlockingQueue<String> responseQueue, boolean fileType) {
        super(responseQueue, Executors.newFixedThreadPool(threadAmount));
        this.fileType = fileType;
        this.responseQueue = responseQueue;

        System.out.println("File Writer Service is starting with " + threadAmount + " threads");
    }

    @Override
    public void start() {
        File outputFile = fileType
                ? new File("output/output.csv")
                : new File("output/output.json");

        Runnable writer = fileType
                ? new CsvFileWriter(outputFile, responseQueue)
                : new JsonFileWriter(outputFile, responseQueue);

        while (active && !Thread.currentThread().isInterrupted()){
            if (!responseQueue.isEmpty()) {
                executor.execute(writer);
            }
        }
    }
}
