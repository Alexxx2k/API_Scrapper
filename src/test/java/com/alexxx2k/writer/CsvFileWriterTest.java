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
    void testWriteResponse_SimpleObject() throws Exception {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);
        String jsonResponse = "{\"name\": \"test\", \"age\": 25}";

        writer.writeResponse(jsonResponse);

        assertTrue(outputFile.exists());
        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("name,age"));
        assertTrue(content.contains("test,25"));
    }

    @Test
    void testWriteResponse_AppendMultipleRows() throws Exception {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);

        writer.writeResponse("{\"name\": \"test1\", \"age\": 25}");
        writer.writeResponse("{\"name\": \"test2\", \"age\": 30}");

        String content = Files.readString(outputFile.toPath());
        String[] lines = content.split("\n");

        assertEquals(3, lines.length);
        assertTrue(lines[1].contains("test1,25"));
        assertTrue(lines[2].contains("test2,30"));
    }

    @Test
    void testWriteResponse_WithNestedObject() throws Exception {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);
        String jsonResponse = "{\"user\": {\"name\": \"John\", \"age\": 30}, \"status\": \"active\"}";

        writer.writeResponse(jsonResponse);

        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("user.name,user.age,status"));
        assertTrue(content.contains("John,30,active"));
    }

    @Test
    void testWriteResponse_WithArray() throws Exception {
        File outputFile = new File(tempDir, "test.csv");
        CsvFileWriter writer = new CsvFileWriter(outputFile);
        String jsonResponse = "{\"tags\": [\"java\", \"test\"], \"name\": \"test\"}";

        writer.writeResponse(jsonResponse);

        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("tags,name"));
        assertTrue(content.contains("java; test,test"));
    }

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