package com.alexxx2k.service;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class Service {
    protected final BlockingQueue<String> responseQueue;
    protected ExecutorService executor;
    protected ArrayList<ScheduledFuture<?>> scheduledTasks;
    protected volatile boolean active = true;

    protected Service(BlockingQueue<String> responseQueue, ExecutorService executor) {
        this.responseQueue = responseQueue;
        this.executor = executor;
    }

    public abstract void start() throws InterruptedException;
    public void stop() throws InterruptedException {
        active = false;
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        System.out.println(this.getClass().getName() + " stopped");
    }

    public ExecutorService getExecutor() {
        return executor;
    }
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public ArrayList<ScheduledFuture<?>> getScheduledTasks() {
        return scheduledTasks;
    }

    public boolean isActive() {
        return active;
    }
}
