// Class written by ChatGPT
package com.cpen321.tunematch;

import okhttp3.*;
import java.io.IOException;

public class ApiClient {
    private OkHttpClient client;
    private String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        client = new OkHttpClient();
    }

    public String doGetRequest(String endpoint) throws IOException {
        String fullUrl = baseUrl + endpoint;
        Request request = new Request.Builder()
                .url(fullUrl)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public String doPostRequest(String endpoint, String jsonRequestBody) throws IOException {
        String fullUrl = baseUrl + endpoint;
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonRequestBody);

        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public String doPutRequest(String endpoint, String jsonRequestBody) throws IOException {
        String fullUrl = baseUrl + endpoint;
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonRequestBody);

        Request request = new Request.Builder()
                .url(fullUrl)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }
}

