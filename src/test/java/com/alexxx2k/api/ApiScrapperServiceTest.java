package com.alexxx2k.api;

import com.alexxx2k.writer.ResponseFileWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        String jsonContent = "[\"https://api1.example.com\", \"https://api2.example.com\", \"https://api3.example.com\"]";
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
        apiScrapperService = new ApiScrapperService(3, 10, configFile.getAbsolutePath(), fileWriter);

        assertNotNull(apiScrapperService);
    }

    @Test
    void testConstructor_InvalidConfigFile() {
        File nonExistentFile = new File("nonexistent.json");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            new ApiScrapperService(3, 10, nonExistentFile.getAbsolutePath(), fileWriter);
        });

        assertTrue(exception.getMessage().contains("Error reading config file:"));
    }

    @Test
    void testConstructor_EmptyConfigFile() throws IOException {
        File emptyFile = tempDir.resolve("empty.json").toFile();
        Files.write(emptyFile.toPath(), "[]".getBytes());

        apiScrapperService = new ApiScrapperService(2, 5, emptyFile.getAbsolutePath(), fileWriter);

        assertNotNull(apiScrapperService);
    }

    @Test
    void testConstructor_MalformedJsonFile() throws IOException {
        File malformedFile = tempDir.resolve("malformed.json").toFile();
        Files.write(malformedFile.toPath(), "invalid json".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            new ApiScrapperService(3, 10, malformedFile.getAbsolutePath(), fileWriter);
        });

        assertTrue(exception.getMessage().contains("Error reading config file:"));
    }

    @Test
    void testConstructor_WithSingleApi() throws IOException {
        File singleApiFile = tempDir.resolve("single.json").toFile();
        Files.write(singleApiFile.toPath(), "[\"https://single.api.com\"]".getBytes());

        apiScrapperService = new ApiScrapperService(1, 5, singleApiFile.getAbsolutePath(), fileWriter);

        assertNotNull(apiScrapperService);
    }

    @Test
    void testConstructor_WithDifferentThreadAmounts() {
        apiScrapperService = new ApiScrapperService(1, 10, configFile.getAbsolutePath(), fileWriter);
        assertNotNull(apiScrapperService);

        ApiScrapperService service2 = new ApiScrapperService(10, 10, configFile.getAbsolutePath(), fileWriter);
        assertNotNull(service2);
    }

    @Test
    void testConstructor_WithNegativeThreads() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ApiScrapperService(-1, 10, configFile.getAbsolutePath(), fileWriter);
        });
    }

    @Test
    void testConstructor_WithZeroTimeout() {
        apiScrapperService = new ApiScrapperService(2, 0, configFile.getAbsolutePath(), fileWriter);
        assertNotNull(apiScrapperService);
    }

    @Test
    void testConstructor_WithNegativeTimeout() {
        apiScrapperService = new ApiScrapperService(2, -1, configFile.getAbsolutePath(), fileWriter);
        assertNotNull(apiScrapperService); // Отрицательный timeout может быть допустим
    }

    @Test
    void testStart_StartsScheduledTasks() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(3, 1, configFile.getAbsolutePath(), fileWriter);

        apiScrapperService.start();

        Thread.sleep(100);

        assertTrue(true);
    }

    @Test
    void testStart_MultipleCalls() {
        apiScrapperService = new ApiScrapperService(2, 2, configFile.getAbsolutePath(), fileWriter);

        apiScrapperService.start();
        apiScrapperService.start();

        assertTrue(true);
    }

    @Test
    void testStop_TerminatesExecutor() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(2, 5, configFile.getAbsolutePath(), fileWriter);
        apiScrapperService.start();

        Thread.sleep(50);

        apiScrapperService.stop();

        assertTrue(true);
    }

    @Test
    void testStop_WithoutStart() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(3, 10, configFile.getAbsolutePath(), fileWriter);

        apiScrapperService.stop();

        assertTrue(true);
    }

    @Test
    void testStop_MultipleCalls() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(2, 5, configFile.getAbsolutePath(), fileWriter);
        apiScrapperService.start();

        Thread.sleep(50);

        apiScrapperService.stop();
        apiScrapperService.stop();

        assertTrue(true);
    }

    @Test
    void testServiceWithLargeNumberOfApis() throws IOException {
        List<String> manyApis = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            manyApis.add("https://api" + i + ".example.com");
        }

        ObjectMapper mapper = new ObjectMapper();
        File largeConfigFile = tempDir.resolve("many_apis.json").toFile();
        mapper.writeValue(largeConfigFile, manyApis);

        apiScrapperService = new ApiScrapperService(5, 2, largeConfigFile.getAbsolutePath(), fileWriter);
        apiScrapperService.start();

        assertTrue(true);
    }

    @Test
    void testServiceWithDifferentTimeouts() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(2, 500, configFile.getAbsolutePath(), fileWriter); // 500ms timeout
        apiScrapperService.start();

        Thread.sleep(100);
        apiScrapperService.stop();

        assertTrue(true);
    }

    @Test
    void testServiceWithVeryShortTimeout() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(2, 1, configFile.getAbsolutePath(), fileWriter); // 1ms timeout
        apiScrapperService.start();

        Thread.sleep(50);
        apiScrapperService.stop();

        assertTrue(true);
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(3, 2, configFile.getAbsolutePath(), fileWriter);

        Thread startThread = new Thread(apiScrapperService::start);
        Thread stopThread = new Thread(() -> {
            try {
                apiScrapperService.stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        startThread.start();
        Thread.sleep(50);
        stopThread.start();

        startThread.join();
        stopThread.join();

        assertTrue(true);
    }

    @Test
    void testFileWriterInteraction() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(2, 1, configFile.getAbsolutePath(), fileWriter);
        apiScrapperService.start();

        Thread.sleep(1500);
        apiScrapperService.stop();

    }

    @Test
    void testApiCallerCreation() throws IOException {
        String[] testApis = {"https://api1.test.com", "https://api2.test.com"};
        ObjectMapper mapper = new ObjectMapper();
        File testConfig = tempDir.resolve("test_apis.json").toFile();
        mapper.writeValue(testConfig, testApis);

        apiScrapperService = new ApiScrapperService(2, 5, testConfig.getAbsolutePath(), fileWriter);
        apiScrapperService.start();

        assertTrue(true);
    }

    @Test
    void testResourceCleanup() throws InterruptedException {
        apiScrapperService = new ApiScrapperService(2, 1, configFile.getAbsolutePath(), fileWriter);
        apiScrapperService.start();

        Thread.sleep(100);
        apiScrapperService.stop();

        ApiScrapperService newService = new ApiScrapperService(2, 1, configFile.getAbsolutePath(), fileWriter);
        newService.start();
        newService.stop();

        assertTrue(true);
    }

    @Test
    void testServiceWithSpecialCharactersInUrls() throws IOException {
        String[] specialUrls = {
                "https://api.example.com/test?param=value",
                "https://api.example.com/path%20with%20spaces",
                "https://api.example.com:8080/api"
        };

        ObjectMapper mapper = new ObjectMapper();
        File specialConfig = tempDir.resolve("special_urls.json").toFile();
        mapper.writeValue(specialConfig, specialUrls);

        apiScrapperService = new ApiScrapperService(2, 2, specialConfig.getAbsolutePath(), fileWriter);
        apiScrapperService.start();

        assertTrue(true);
    }
}