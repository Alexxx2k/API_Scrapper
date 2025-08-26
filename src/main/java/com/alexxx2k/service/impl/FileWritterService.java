package com.alexxx2k.service.impl;

import com.alexxx2k.service.Service;
import com.alexxx2k.thread.impl.CsvFileWriter;
import com.alexxx2k.thread.impl.JsonFileWriter;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public class FileWritterService extends Service {
    private final boolean fileType;
    private final BlockingQueue<String> responseQueue;

    public FileWritterService(int threadAmount, BlockingQueue<String> responseQueue, boolean fileType) {
        super(responseQueue, Executors.newFixedThreadPool(threadAmount));
        this.fileType = fileType;
        this.responseQueue = responseQueue;

        System.out.println("File Writer Service is starting with " + threadAmount + " threads");
    }

    @Override
    public void start() {
        File outputFile = fileType
                ? new File("src\\main\\java\\com\\nikitzainc\\output\\output.csv")
                : new File("src\\main\\java\\com\\nikitzainc\\output\\output.json");

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
