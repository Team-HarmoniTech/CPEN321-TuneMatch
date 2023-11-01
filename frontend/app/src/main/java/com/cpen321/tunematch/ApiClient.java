package com.cpen321.tunematch;

import android.util.Log;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private OkHttpClient client;
    private String baseUrl;
    private Headers customHeader;

    // Partially written by ChatGPT
    public ApiClient(String baseUrl, Headers customHeader) {
        this.baseUrl = baseUrl;
        this.client = new OkHttpClient();
        this.customHeader = customHeader;
    }

    // Fully written by ChatGPT
    public String doGetRequest(String endpoint, Boolean customHeaders) throws IOException {

        String fullUrl = baseUrl + endpoint;
        Log.d("ApiClient", "doGetRequest: " + fullUrl);
        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .get();

        if (customHeaders) {
            requestBuilder.headers(customHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    // Fully written by ChatGPT
    public String doPostRequest(String endpoint, String jsonRequestBody, Boolean customHeaders) throws IOException {
        String fullUrl = baseUrl + endpoint;

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonRequestBody);

        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .post(body);

        if (customHeaders) {
            requestBuilder.headers(customHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    // Fully written by ChatGPT
    public String doPutRequest(String endpoint, String jsonRequestBody, Boolean customHeaders) throws IOException {
        String fullUrl = baseUrl + endpoint;

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonRequestBody);

        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .put(body);

        if (customHeaders) {
            requestBuilder.headers(customHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    // Fully written by ChatGPT
    public String doDeleteRequest(String endpoint, Boolean customHeaders) throws IOException {
        String fullUrl = baseUrl + endpoint;

        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .delete();

        if (customHeaders) {
            requestBuilder.headers(customHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }
}