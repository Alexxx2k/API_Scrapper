package com.alexxx2k.writer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class CsvFileWriterTest {

    @TempDir
    File tempDir;

    @Test
    void testWriteResponse_JsonArrayRoot() throws Exception {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);
        String jsonResponse = "[{\"name\": \"test1\"}, {\"name\": \"test2\"}]";

        writer.writeResponse(jsonResponse);

        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("name"));
        assertTrue(content.contains("test1"));
        assertTrue(content.contains("test2"));
    }

    @Test
    void testGetFileType() {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);

        assertEquals("csv", writer.getFileType());
    }
}