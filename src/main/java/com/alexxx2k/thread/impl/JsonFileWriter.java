package com.alexxx2k.thread.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class JsonFileWriter implements Runnable, com.alexxx2k.thread.FileWriter {
    private File file;
    private final ReentrantLock lock = new ReentrantLock();
    private BlockingQueue<String> responseQueue;

    public JsonFileWriter(File file, BlockingQueue<String> responseQueue) {
        this.file = file;
        this.responseQueue = responseQueue;
    }

    @Override
    public void run() {
        lock.lock();
        ObjectMapper mapper = new ObjectMapper();
        try {
            ArrayNode array = file.exists() && file.length() > 0
                    ? (ArrayNode)mapper.readTree(file) : mapper.createArrayNode();
            JsonNode jsonNode = mapper.readTree(responseQueue.take());
            array.add(jsonNode);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, array);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setResponseQueue(BlockingQueue<String> responseQueue) {
        this.responseQueue = responseQueue;
    }

    @Override
    public BlockingQueue<String> getResponseQueue() {
        return responseQueue;
    }
}
