// Class written by ChatGPT
package com.cpen321.tunematch;

import android.util.Log;

import okhttp3.*;
import java.io.IOException;

public class ApiClient {
    private OkHttpClient client;
    private String baseUrl;
    private Headers customHeader;

    public ApiClient() {
        this.baseUrl = "https://zphy19my7b.execute-api.us-west-2.amazonaws.com/v1";
        client = new OkHttpClient();
        customHeader = new Headers.Builder()
                        .add("user-id", "queryTestId2")
                        .build();
    }

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