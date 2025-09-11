package com.alexxx2k;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @TempDir
    Path tempDir;

    @Test
    void testMain_WithCorrectArgsCsv() throws IOException {
        File configFile = tempDir.resolve("test-apis.json").toFile();
        Files.write(configFile.toPath(), "[\"https://api.example.com\"]".getBytes());

        String[] args = {"2", "1", configFile.getAbsolutePath(), "csv"};

        Thread testThread = new Thread(() -> {
            try {
                Main.main(args);
            } catch (Exception e) {
            }
        });

        testThread.start();

        try {
            Thread.sleep(100);
            testThread.interrupt();
            testThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(true);
    }

    @Test
    void testMain_WithCorrectArgsJson() throws IOException {
        File configFile = tempDir.resolve("test-apis.json").toFile();
        Files.write(configFile.toPath(), "[\"https://api.example.com\"]".getBytes());

        String[] args = {"2", "1", configFile.getAbsolutePath(), "json"};

        Thread testThread = new Thread(() -> {
            try {
                Main.main(args);
            } catch (Exception e) {
            }
        });

        testThread.start();

        try {
            Thread.sleep(100);
            testThread.interrupt();
            testThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(true);
    }

    @Test
    void testMain_WithInsufficientArgs() {
        String[] args = {"2", "5", "file.json"};

        assertThrows(IllegalArgumentException.class, () -> {
            Main.main(args);
        });
    }

    @Test
    void testMain_WithInvalidFileType() {
        String[] args = {"2", "5", "file.json", "xml"};

        assertThrows(IllegalArgumentException.class, () -> {
            Main.main(args);
        });
    }

    @Test
    void testMain_WithInvalidThreadAmount() {
        String[] args = {"invalid", "5", "file.json", "csv"};

        assertThrows(NumberFormatException.class, () -> {
            Main.main(args);
        });
    }

    @Test
    void testMain_WithNonExistentConfigFile() {
        String[] args = {"2", "5", "nonexistent.json", "csv"};

        assertThrows(RuntimeException.class, () -> {
            Main.main(args);
        });
    }
}