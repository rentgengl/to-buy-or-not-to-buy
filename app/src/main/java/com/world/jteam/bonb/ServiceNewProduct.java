package com.world.jteam.bonb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServiceNewProduct extends Service {

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        ModelProduct newProduct = (ModelProduct) intent.getSerializableExtra("newProduct");
        newProduct.createOnServer();
        return super.onStartCommand(intent, flags, startId);

    }

    public void onDestroy() {
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

}
