package com.world.jteam.bonb.server;

import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.BuildConfig;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.R;

import retrofit2.Call;
import retrofit2.Callback;
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

    public static void enqueue(Call serviceCall, Callback serviceCallback) {
        int minServerAppVersion = AppInstance.getMinServerAppVersion();

        if (minServerAppVersion==0) {
            synchronized (AppInstance.getServerVersion()){
                minServerAppVersion = AppInstance.getMinServerAppVersion();
            }
        }

        if (minServerAppVersion==0)
            return;

        if (Looper.myLooper() == Looper.getMainLooper()) {
            //Вызов идет из главного потока
            if (minServerAppVersion > BuildConfig.VERSION_CODE){
                Toast.makeText(AppInstance.getAppContext(), R.string.need_update_app, Toast.LENGTH_LONG).show();
                return;
            }
        }

        serviceCall.enqueue(serviceCallback);
    }

}