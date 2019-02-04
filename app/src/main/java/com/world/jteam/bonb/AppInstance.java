package com.world.jteam.bonb;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import com.world.jteam.bonb.media.BarcodeManager;
import com.world.jteam.bonb.model.ModelUser;

import java.io.IOException;

public class AppInstance extends Application {

    private static Context sContext;
    private static boolean sFirstStart;
    private ModelUser user;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        Thread thread=new Thread(new AppInitialisation());
        thread.start();
    }

    private class AppInitialisation implements Runnable{
        @Override
        public void run() {
            //Первый запуск
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(sContext);
            sFirstStart=sharedPreferences.getBoolean("first_start",true);
            if (sFirstStart){
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("first_start", false);
                edit.commit();
            }

            //Штрихкодер
            if (sFirstStart){
                BarcodeManager.firstInitBarcodeDetector(sContext);
            }

            //БД

            try {
                DatabaseApp.initDatabaseApp(sContext);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Категории
            Product.categoryInitialisation();
        }
    }

    public static Context getAppContext() {
        return sContext;
    }

    public static ModelUser getUser(){
        return null;
    }

    public static void setUser(ModelUser user){

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}
