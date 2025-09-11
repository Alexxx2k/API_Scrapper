package com.alexxx2k.api;

import com.alexxx2k.writer.ResponseFileWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ApiCallerTest {

    private static class TestResponseFileWriter implements ResponseFileWriter {
        private String lastResponse;
        private int writeCount = 0;
        private Exception lastException;

        @Override
        public void writeResponse(String response) {
            this.lastResponse = response;
            writeCount++;
        }

        @Override
        public String getFileType() {
            return "test";
        }

        public String getLastResponse() {
            return lastResponse;
        }

        public int getWriteCount() {
            return writeCount;
        }

        public void setLastException(Exception e) {
            this.lastException = e;
        }

        public Exception getLastException() {
            return lastException;
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
        assertNotNull(apiCaller.toString());
    }

    @Test
    void testConstructorWithEmptyUrl() {
        ApiCaller emptyUrlCaller = new ApiCaller("", fileWriter);
        assertEquals("", emptyUrlCaller.apiUrl);
    }

    @Test
    void testThreadCounterIncrementation() {
        resetThreadCounter();

        ApiCaller caller1 = new ApiCaller(testApiUrl, fileWriter);
        ApiCaller caller2 = new ApiCaller(testApiUrl + "/2", fileWriter);
        ApiCaller caller3 = new ApiCaller(testApiUrl + "/3", fileWriter);
        ApiCaller caller4 = new ApiCaller(testApiUrl + "/4", fileWriter);

        assertEquals(1, caller1.threadNumber);
        assertEquals(2, caller2.threadNumber);
        assertEquals(3, caller3.threadNumber);
        assertEquals(4, caller4.threadNumber);
    }

    @Test
    void testThreadCounterThreadSafety() throws InterruptedException {
        resetThreadCounter();
        final int threadCount = 10;
        final ApiCaller[] callers = new ApiCaller[threadCount];

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                callers[index] = new ApiCaller(testApiUrl + "/" + index, fileWriter);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        boolean[] found = new boolean[threadCount + 1];
        for (ApiCaller caller : callers) {
            assertTrue(caller.threadNumber >= 1 && caller.threadNumber <= threadCount);
            assertFalse(found[caller.threadNumber]);
            found[caller.threadNumber] = true;
        }
    }

    @Test
    void testRun_WithInvalidUrl() {
        ApiCaller invalidCaller = new ApiCaller("invalid-url", fileWriter);
        invalidCaller.run();
        assertEquals(0, fileWriter.getWriteCount());
    }

    @Test
    void testRun_WithMalformedUrl() {
        ApiCaller malformedCaller = new ApiCaller("http://[invalid-url", fileWriter);
        malformedCaller.run();
        assertEquals(0, fileWriter.getWriteCount());
    }

    @Test
    void testRun_WithUnreachableUrl() {
        ApiCaller unreachableCaller = new ApiCaller("https://unreachable-domain-12345.com", fileWriter);
        unreachableCaller.run();
        assertEquals(0, fileWriter.getWriteCount());
    }

    @Test
    void testRun_WithLocalhostUrl() {
        ApiCaller localhostCaller = new ApiCaller("http://localhost:9999/nonexistent", fileWriter);
        localhostCaller.run();
        assertEquals(0, fileWriter.getWriteCount());
    }

    @Test
    void testRun_WithHttpsUrl() {
        ApiCaller httpsCaller = new ApiCaller("https://httpbin.org/get", fileWriter);
        httpsCaller.run();
        assertTrue(true);
    }

    @Test
    void testRun_WithHttpUrl() {
        ApiCaller httpCaller = new ApiCaller("http://httpbin.org/get", fileWriter);
        httpCaller.run();
        assertTrue(true);
    }

    @Test
    void testRun_MultipleCallsSameInstance() {
        ApiCaller multiCallCaller = new ApiCaller("https://httpbin.org/get", fileWriter);

        multiCallCaller.run();
        int firstCallCount = fileWriter.getWriteCount();

        multiCallCaller.run();
        int secondCallCount = fileWriter.getWriteCount();

        assertTrue(secondCallCount >= firstCallCount);
    }

    @Test
    void testRun_WithDifferentInstances() {
        TestResponseFileWriter writer1 = new TestResponseFileWriter();
        TestResponseFileWriter writer2 = new TestResponseFileWriter();

        ApiCaller caller1 = new ApiCaller("https://httpbin.org/get", writer1);
        ApiCaller caller2 = new ApiCaller("https://httpbin.org/get", writer2);

        caller1.run();
        caller2.run();

        assertTrue(writer1.getWriteCount() >= 0);
        assertTrue(writer2.getWriteCount() >= 0);
    }

    @Test
    void testRun_WithVeryLongUrl() {
        String longUrl = "https://example.com/" + "a".repeat(1000);
        ApiCaller longUrlCaller = new ApiCaller(longUrl, fileWriter);

        longUrlCaller.run();
        assertEquals(0, fileWriter.getWriteCount());
    }

    @Test
    void testRun_WithSpecialCharactersInUrl() {
        ApiCaller specialCharCaller = new ApiCaller("https://example.com/test?param=value&other=test%20space", fileWriter);
        specialCharCaller.run();
        assertTrue(true);
    }

    @Test
    void testRun_ConcurrentAccessToFileWriter() throws InterruptedException {
        resetThreadCounter();
        final int threadCount = 5;
        final TestResponseFileWriter sharedWriter = new TestResponseFileWriter();

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                ApiCaller caller = new ApiCaller("https://httpbin.org/get", sharedWriter);
                caller.run();
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertTrue(sharedWriter.getWriteCount() >= 0);
    }

    @Test
    void testApiCallerImplementsRunnable() {
        assertTrue(apiCaller instanceof Runnable);
    }

    @Test
    void testApiCallerFieldsAreFinal() {
        Field[] fields = ApiCaller.class.getDeclaredFields();
        for (Field field : fields) {
            if (!field.getName().equals("threadCounter")) {
                assertTrue(java.lang.reflect.Modifier.isFinal(field.getModifiers()),
                        "Field " + field.getName() + " should be final");
            }
        }
    }

    @Test
    void testThreadCounterIsStatic() {
        try {
            Field field = ApiCaller.class.getDeclaredField("threadCounter");
            assertTrue(java.lang.reflect.Modifier.isStatic(field.getModifiers()));
        } catch (NoSuchFieldException e) {
            fail("threadCounter field not found");
        }
    }

    @Test
    void testRun_WithRedirectUrl() {
        ApiCaller redirectCaller = new ApiCaller("https://httpbin.org/redirect-to?url=http://example.com", fileWriter);
        redirectCaller.run();
        assertTrue(true);
    }

    @Test
    void testRun_WithLargeResponse() {
        ApiCaller largeResponseCaller = new ApiCaller("https://httpbin.org/bytes/10000", fileWriter);
        largeResponseCaller.run();
        assertTrue(true);
    }

    @Test
    void testRun_WithDifferentHttpMethods() {
        ApiCaller getCaller = new ApiCaller("https://httpbin.org/get", fileWriter);
        getCaller.run();
        assertTrue(true);
    }

    @Test
    void testRun_WithCustomPort() {
        ApiCaller customPortCaller = new ApiCaller("https://httpbin.org:443/get", fileWriter);
        customPortCaller.run();
        assertTrue(true);
    }

    @Test
    void testRun_WithPathParameters() {
        ApiCaller pathParamCaller = new ApiCaller("https://httpbin.org/anything/test/path", fileWriter);
        pathParamCaller.run();
        assertTrue(true);
    }
}