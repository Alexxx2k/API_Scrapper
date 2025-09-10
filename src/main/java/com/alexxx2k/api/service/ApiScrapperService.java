package com.alexxx2k.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alexxx2k.core.Service;
import com.alexxx2k.api.client.ApiCaller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ApiScrapperService extends Service {
    private final int timeout;
    private final ArrayList<String> apis;

    public ApiScrapperService(int threadAmount, int timeout, BlockingQueue<String> responseQueue, String filepath) {
        super(responseQueue, Executors.newScheduledThreadPool(threadAmount));
        this.timeout = timeout;
        this.scheduledTasks = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        try {
            apis = mapper.readValue(new File(filepath), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error reading config file:", e);
        }
        System.out.println("Api Scrapper Service created with " + threadAmount + " threads");
    }

    @Override
    public void start() {
        for (String url : apis) {
            ScheduledFuture<?> future = ((ScheduledExecutorService) executor).scheduleAtFixedRate(
                    new ApiCaller(url, responseQueue), 0, timeout, TimeUnit.SECONDS);
            scheduledTasks.add(future);
        }
    }

    @Override
    public void stop() throws InterruptedException {
        active = false;
        for (ScheduledFuture<?> scheduledTask : scheduledTasks) {
            scheduledTask.cancel(true);
        }
        executor.shutdown();
        if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        System.out.println(this.getClass().getName() + " stopped");
    }
}
