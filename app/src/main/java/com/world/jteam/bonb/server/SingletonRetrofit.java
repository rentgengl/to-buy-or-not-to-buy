package com.world.jteam.bonb.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.world.jteam.bonb.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SingletonRetrofit {
    private static SingletonRetrofit instance = null;
    private DataApi dataApi;

    public static SingletonRetrofit getInstance() {
        if (instance == null) {
            instance = new SingletonRetrofit();
        }

        return instance;
    }

    // Build retrofit once when creating a single instance
    private SingletonRetrofit() {
        // Implement a method to build your retrofit
        buildRetrofit();
    }

    private void buildRetrofit() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HTTP_SERVER)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Build your services once
        this.dataApi = retrofit.create(DataApi.class);
    }

    public DataApi getDataApi() {
        return this.dataApi;
    }

}