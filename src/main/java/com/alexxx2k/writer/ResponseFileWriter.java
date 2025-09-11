package com.alexxx2k.writer;

public interface ResponseFileWriter {
    void writeResponse(String response) throws Exception;
    String getFileType();
}