package com.alexxx2k.thread;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class ApiCaller implements Runnable {
    private final String apiUrl;
    private final BlockingQueue<String> responseQueue;
    private CloseableHttpClient httpClient = HttpClients.createDefault();

    public ApiCaller(String apiUrl, BlockingQueue<String> responseQueue) {
        this.apiUrl = apiUrl;
        this.responseQueue = responseQueue;
    }
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void run() {
        try {
            HttpGet request = new HttpGet(apiUrl);
            System.out.println("Making request to: " + apiUrl);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    responseQueue.put(responseBody);
                } else {
                    System.err.println("Request failed: " + response.getStatusLine());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            System.err.println("HTTP request failed: " + apiUrl + " " + e.getMessage());
        }
    }
}