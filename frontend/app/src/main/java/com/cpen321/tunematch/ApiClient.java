// Class written by ChatGPT
package com.cpen321.tunematch;

import okhttp3.*;
import java.io.IOException;

import okhttp3.*;
import java.io.IOException;

import okhttp3.*;
import java.io.IOException;

public class ApiClient {
    private OkHttpClient client;
    private String baseUrl;

    public ApiClient() {
        this.baseUrl = "https://zphy19my7b.execute-api.us-west-2.amazonaws.com/v1";
        client = new OkHttpClient();
    }

    public String doGetRequest(String endpoint, Headers customHeaders) throws IOException {
        String fullUrl = baseUrl + endpoint;

        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .get();

        if (customHeaders != null) {
            requestBuilder.headers(customHeaders);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public String doPostRequest(String endpoint, String jsonRequestBody, Headers customHeaders) throws IOException {
        String fullUrl = baseUrl + endpoint;

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonRequestBody);

        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .post(body);

        if (customHeaders != null) {
            requestBuilder.headers(customHeaders);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public String doPutRequest(String endpoint, String jsonRequestBody, Headers customHeaders) throws IOException {
        String fullUrl = baseUrl + endpoint;

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonRequestBody);

        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .put(body);

        if (customHeaders != null) {
            requestBuilder.headers(customHeaders);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public String doDeleteRequest(String endpoint, Headers customHeaders) throws IOException {
        String fullUrl = baseUrl + endpoint;

        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .delete();

        if (customHeaders != null) {
            requestBuilder.headers(customHeaders);
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