package com.alexxx2k.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alexxx2k.writer.ResponseFileWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ApiScrapperService {
    private final int timeout;
    private final ArrayList<String> apis;
    private final ScheduledExecutorService executor;
    private final ArrayList<ScheduledFuture<?>> scheduledTasks;
    private final ResponseFileWriter fileWriter;

    public ApiScrapperService(int threadAmount, int timeout,
                              String filepath, ResponseFileWriter fileWriter) {
        this.timeout = timeout;
        this.fileWriter = fileWriter;
        this.executor = Executors.newScheduledThreadPool(threadAmount);
        this.scheduledTasks = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        try {
            apis = mapper.readValue(new File(filepath), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading config file:", e);
        }
        System.out.println("Api Scrapper Service created with " + threadAmount + " threads");
    }

    public void start() {
        for (String url : apis) {
            ScheduledFuture<?> future = executor.scheduleAtFixedRate(
                    new ApiCaller(url, fileWriter), 0, timeout, TimeUnit.SECONDS);
            scheduledTasks.add(future);
        }
    }

    public void stop() throws InterruptedException {
        for (ScheduledFuture<?> scheduledTask : scheduledTasks) {
            scheduledTask.cancel(true);
        }
        executor.shutdown();
        if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        System.out.println("ApiScrapperService stopped");
    }
}