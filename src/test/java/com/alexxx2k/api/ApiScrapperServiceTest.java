package com.alexxx2k.api;

import com.alexxx2k.writer.ResponseFileWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ApiScrapperServiceTest {

    @TempDir
    Path tempDir;

    private ResponseFileWriter fileWriter;
    private File configFile;
    private ApiScrapperService apiScrapperService;

    @BeforeEach
    void setUp() throws IOException {
        configFile = tempDir.resolve("apis.json").toFile();
        String jsonContent = "[\"https://api1.example.com\"]";
        Files.write(configFile.toPath(), jsonContent.getBytes());
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (apiScrapperService != null) {
            apiScrapperService.stop();
        }
    }

    @Test
    void testConstructor_ValidConfigFile() {
        apiScrapperService = new ApiScrapperService(1, 5, configFile.getAbsolutePath(), fileWriter);
        assertNotNull(apiScrapperService);
    }

    @Test
    void testConstructor_InvalidConfigFile() {
        File nonExistentFile = new File("nonexistent.json");

        assertThrows(RuntimeException.class, () -> {
            new ApiScrapperService(1, 5, nonExistentFile.getAbsolutePath(), fileWriter);
        });
    }

    @Test
    void testConstructor_MalformedJsonFile() throws IOException {
        File malformedFile = tempDir.resolve("malformed.json").toFile();
        Files.write(malformedFile.toPath(), "invalid json".getBytes());

        assertThrows(RuntimeException.class, () -> {
            new ApiScrapperService(1, 5, malformedFile.getAbsolutePath(), fileWriter);
        });
    }

    @Test
    void testStartAndStop() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(1, 1, configFile.getAbsolutePath(), fileWriter);
        apiScrapperService.start();
        Thread.sleep(100);
        apiScrapperService.stop();
        assertTrue(true);
    }

    @Test
    void testStop_WithoutStart() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(1, 5, configFile.getAbsolutePath(), fileWriter);
        apiScrapperService.stop();
        assertTrue(true);
    }
}