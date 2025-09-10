package com.alexxx2k.api.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiCaller implements Runnable {
    private final String apiUrl;
    private final BlockingQueue<String> responseQueue;
    private CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final AtomicInteger threadCounter = new AtomicInteger(0);
    private final int threadNumber;

    public ApiCaller(String apiUrl, BlockingQueue<String> responseQueue) {
        this.apiUrl = apiUrl;
        this.responseQueue = responseQueue;
        this.threadNumber = threadCounter.incrementAndGet();
    }

    @Override
    public void run() {
        String threadInfo = "Поток-" + threadNumber + "[" + Thread.currentThread().getName() + "]";
        System.out.println(threadInfo + " → Начинает запрос к: " + apiUrl);

        try {
            HttpGet request = new HttpGet(apiUrl);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                System.out.println(threadInfo + " ← Получил ответ от: " + apiUrl +
                        " [Status: " + response.getStatusLine().getStatusCode() + "]");

                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    responseQueue.put(responseBody);
                    System.out.println(threadInfo + " ✓ Данные помещены в очередь");
                }
            }
        } catch (Exception e) {
            System.err.println(threadInfo + " ✗ Ошибка: " + e.getMessage());
        }

        System.out.println(threadInfo + " - Завершил работу");
    }
}