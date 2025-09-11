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
    void testWriteResponse_NewFile() throws Exception {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);
        String jsonResponse = "{\"name\": \"test\", \"age\": 25, \"city\": \"Moscow\"}";

        writer.writeResponse(jsonResponse);

        assertTrue(outputFile.exists());
        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("name, age, city"));
        assertTrue(content.contains("test, 25, Moscow"));
    }

    @Test
    void testWriteResponse_AppendToExisting() throws Exception {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);
        String jsonResponse1 = "{\"name\": \"test1\", \"age\": 25}";
        String jsonResponse2 = "{\"name\": \"test2\", \"age\": 30}";

        writer.writeResponse(jsonResponse1);
        writer.writeResponse(jsonResponse2);

        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("name, age"));
        assertTrue(content.contains("test1, 25"));
        assertTrue(content.contains("test2, 30"));
    }

    @Test
    void testWriteResponse_WithArray() throws Exception {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);
        String jsonResponse = "{\"tags\": [\"java\", \"test\"], \"name\": \"test\"}";

        writer.writeResponse(jsonResponse);

        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("tags, name"));
        assertTrue(content.contains("java, test, test") || content.contains("test, java, test"));
    }

    @Test
    void testGetFileType() {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);

        assertEquals("csv", writer.getFileType());
    }
}