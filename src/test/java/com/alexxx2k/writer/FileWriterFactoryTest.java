package com.alexxx2k.writer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileWriterFactoryTest {

    @TempDir
    File tempDir;

    @Test
    void testCreateFileWriter_CsvFormat() {
        ResponseFileWriter writer = FileWriterFactory.createFileWriter(true);

        assertNotNull(writer);
        assertTrue(writer instanceof CsvFileWriter);
        assertEquals("csv", writer.getFileType());
    }

    @Test
    void testCreateFileWriter_JsonFormat() {
        ResponseFileWriter writer = FileWriterFactory.createFileWriter(false);

        assertNotNull(writer);
        assertTrue(writer instanceof JsonFileWriter);
        assertEquals("json", writer.getFileType());
    }
}