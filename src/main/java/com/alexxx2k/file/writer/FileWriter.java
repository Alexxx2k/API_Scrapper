package com.alexxx2k.file.writer;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public interface FileWriter extends Runnable {
    void setFile(File file);
    File getFile();
    void setResponseQueue(BlockingQueue<String> responseQueue);
    BlockingQueue<String> getResponseQueue();
}