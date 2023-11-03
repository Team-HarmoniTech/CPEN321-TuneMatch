package com.cpen321.tunematch;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public abstract class ApiClient<T> {
    protected abstract String getBaseUrl();
    private Retrofit retrofit;
    protected T api;

    public ApiClient(Class<T> serviceClass) {
        retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(serviceClass);
    }

    public interface ApiResponseCallback {
        void onSuccess(JsonElement result);
        void onError(ApiException exception);
    }

    private JsonElement parseJsonObject(String json) {
        JsonParser parser = new JsonParser();
        return parser.parse(json);
    }

    protected ArrayList<String> getAsStringList(JsonArray arr) {
        ArrayList<String> stringList = new ArrayList<String>();
        for (int i = 0; i < arr.size(); i++) {
            JsonElement jsonElement = arr.get(i);
            stringList.add(jsonElement.getAsString());
        }
        return stringList;
    }

    protected JsonElement call(Call<String> call) throws ApiException {
        try {
            Response<String> response = call.execute();
            Log.d("APICLIENT", response.toString());
            if (response.isSuccessful()) {
                if (response.body() != null && !response.body().isEmpty()) {
                    return parseJsonObject(response.body());
                } else {
                    return null;
                }
            } else {
                throw new ApiException(response.code(), response.message());
            }
        } catch (Exception e) {
            throw new ApiException(-1, e.getMessage());
        }
    }
}

