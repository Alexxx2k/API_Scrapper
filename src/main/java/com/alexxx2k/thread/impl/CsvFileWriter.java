package com.alexxx2k.thread.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class CsvFileWriter implements Runnable, com.alexxx2k.thread.FileWriter {
    private File file;
    private final ReentrantLock lock = new ReentrantLock();
    private BlockingQueue<String> responseQueue;

    public CsvFileWriter(File file, BlockingQueue<String> responseQueue) {
        this.file = file;
        this.responseQueue = responseQueue;
    }

    @Override
    public void run() {
        lock.lock();
        try (FileWriter writer = new FileWriter(file, true)) {
            String response = responseQueue.take();
            JsonNode node = new ObjectMapper().readValue(response, JsonNode.class);

            StringBuilder result = new StringBuilder();

            node.fields().forEachRemaining(entry -> {
                if (!result.isEmpty()) {
                    result.append(", ");
                }
                if (entry.getValue().isArray()) {
                    ArrayNode fields = (ArrayNode) entry.getValue();
                    fields.forEach(field -> {
                        if (!result.isEmpty() && !result.toString().endsWith(", ")) {
                            result.append(", ");
                        }
                        result.append(field.asText());
                    });
                }
                else {
                    result.append(entry.getValue().asText());
                }
            });
            writer.append(result).append("\n");
            System.out.println("Written " + result);
            writer.flush();
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
