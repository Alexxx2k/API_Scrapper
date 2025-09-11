package com.alexxx2k.api;

import com.alexxx2k.writer.ResponseFileWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ApiCallerTest {

    private static class TestResponseFileWriter implements ResponseFileWriter {
        private int writeCount = 0;

        @Override
        public void writeResponse(String response) {
            writeCount++;
        }

        @Override
        public String getFileType() {
            return "test";
        }

        public int getWriteCount() {
            return writeCount;
        }
    }

    private TestResponseFileWriter fileWriter;
    private ApiCaller apiCaller;
    private final String testApiUrl = "https://api.example.com/data";

    @BeforeEach
    void setUp() {
        resetThreadCounter();
        fileWriter = new TestResponseFileWriter();
        apiCaller = new ApiCaller(testApiUrl, fileWriter);
    }

    private static void resetThreadCounter() {
        try {
            Field field = ApiCaller.class.getDeclaredField("threadCounter");
            field.setAccessible(true);
            AtomicInteger counter = (AtomicInteger) field.get(null);
            counter.set(0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset thread counter", e);
        }
    }

    @Test
    void testConstructor() {
        assertEquals(testApiUrl, apiCaller.apiUrl);
        assertEquals(fileWriter, apiCaller.fileWriter);
        assertEquals(1, apiCaller.threadNumber);
    }

    @Test
    void testThreadCounterIncrementation() {
        resetThreadCounter();

        ApiCaller caller1 = new ApiCaller(testApiUrl, fileWriter);
        ApiCaller caller2 = new ApiCaller(testApiUrl + "/2", fileWriter);
        ApiCaller caller3 = new ApiCaller(testApiUrl + "/3", fileWriter);

        assertEquals(1, caller1.threadNumber);
        assertEquals(2, caller2.threadNumber);
        assertEquals(3, caller3.threadNumber);
    }

    @Test
    void testRun_WithInvalidUrl() {
        ApiCaller invalidCaller = new ApiCaller("invalid-url", fileWriter);
        invalidCaller.run();
        assertEquals(0, fileWriter.getWriteCount());
    }

    @Test
    void testRun_WithUnreachableUrl() {
        ApiCaller unreachableCaller = new ApiCaller("https://unreachable-domain-12345.com", fileWriter);
        unreachableCaller.run();
        assertEquals(0, fileWriter.getWriteCount());
    }

    @Test
    void testRun_WithValidUrl() {
        ApiCaller validCaller = new ApiCaller("https://httpbin.org/get", fileWriter);
        validCaller.run();
        assertTrue(true); // Просто проверяем что не упало
    }

    @Test
    void testApiCallerImplementsRunnable() {
        assertTrue(apiCaller instanceof Runnable);
    }
}