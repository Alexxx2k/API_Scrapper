package com.alexxx2k;

import com.alexxx2k.api.ApiScrapperService;
import com.alexxx2k.writer.ResponseFileWriter;
import com.alexxx2k.writer.FileWriterFactory;

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

        ResponseFileWriter fileWriter = FileWriterFactory.createFileWriter(outputFileFormat);

        ApiScrapperService apiService = new ApiScrapperService(
                threadAmount, timeout, inputFilePath, fileWriter);

        apiService.start();

        try {
            TimeUnit.SECONDS.sleep(25);
            apiService.stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}