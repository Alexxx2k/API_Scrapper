package com.alexxx2k.core;


import com.alexxx2k.api.service.ApiScrapperService;
import com.alexxx2k.file.service.FileWriterService;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        if (args.length != 4 || !(args[3].equals("csv") || args[3].equals("json"))) {
            throw new IllegalArgumentException(
                    "Wrong arguments! Correct usage: threadAmount, timeout, input file path, output file type[csv|json]");
        }
        int threadAmount = Integer.parseInt(args[0]);
        int timeout = Integer.parseInt(args[1]);
        String inputFilePath = args[2];
        boolean outputFileFormat = args[3].equals("csv");

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(threadAmount * 2);

        ApiScrapperService apiService = new ApiScrapperService(threadAmount, timeout, queue, inputFilePath);

        FileWriterService fileService = new FileWriterService(threadAmount, queue, outputFileFormat);


        Thread apiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    apiService.start();
                } catch (Exception e) {
                    System.out.println("API Service failed: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread fileThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fileService.start();
                } catch (Exception e) {
                    System.out.println("File Service failed: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        });

        apiThread.start();
        fileThread.start();


        try {
            TimeUnit.SECONDS.sleep(25);
            apiService.stop();
            fileService.stop();

            apiThread.interrupt();
            apiThread.join();
            fileThread.interrupt();
            fileThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
