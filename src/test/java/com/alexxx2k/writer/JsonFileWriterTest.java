package com.alexxx2k.writer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileWriterTest {

    @TempDir
    File tempDir;

    @Test
    void testWriteResponse_NewFile() throws Exception {
        // Arrange
        File outputFile = new File(tempDir, "test.json");
        JsonFileWriter writer = new JsonFileWriter(outputFile);
        String jsonResponse = "{\"name\": \"test\", \"value\": 123}";

        writer.writeResponse(jsonResponse);

        assertTrue(outputFile.exists());
        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("test"));
        assertTrue(content.contains("123"));
    }

    @Test
    void testWriteResponse_AppendToExisting() throws Exception {
        File outputFile = new File(tempDir, "test.json");
        JsonFileWriter writer = new JsonFileWriter(outputFile);
        String jsonResponse1 = "{\"name\": \"test1\", \"value\": 123}";
        String jsonResponse2 = "{\"name\": \"test2\", \"value\": 456}";

        writer.writeResponse(jsonResponse1);
        writer.writeResponse(jsonResponse2);

        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("test1"));
        assertTrue(content.contains("test2"));
        assertTrue(content.contains("123"));
        assertTrue(content.contains("456"));
    }

    @Test
    void testGetFileType() {
        File outputFile = new File(tempDir, "test.json");
        JsonFileWriter writer = new JsonFileWriter(outputFile);

        assertEquals("json", writer.getFileType());
    }
}