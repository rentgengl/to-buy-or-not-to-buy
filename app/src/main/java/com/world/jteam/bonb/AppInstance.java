package com.world.jteam.bonb;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import com.google.android.gms.maps.model.LatLng;
import com.world.jteam.bonb.media.BarcodeManager;
import com.world.jteam.bonb.model.ModelUser;

import java.io.IOException;

public class AppInstance extends Application {
    private static Context sContext;
    private static boolean sFirstStart;
    private static ModelUser user;

    private static boolean sAutoGeoPosition=true;
    private static int sRadiusArea=Constants.DEFAULT_RADIUS_AREA;
    private static LatLng sGeoPosition=new LatLng(55.755814,37.617635); //Москва по дефолту

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        Thread thread=new Thread(new AppInitialisation());
        thread.start();
    }

    //Основная инициализация
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

            //Геолокация
            sAutoGeoPosition=sharedPreferences.getBoolean("auto_geo_position",sAutoGeoPosition);
            sRadiusArea=sharedPreferences.getInt("radius_area",sRadiusArea);
            /*sGeoPosition=new LatLng(
                    sharedPreferences.getFloat("latitude_area",(float) sGeoPosition.latitude),
                    sharedPreferences.getFloat("longitude_area",(float) sGeoPosition.longitude));*/


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

    //Получение контекста
    public static Context getAppContext() {
        return sContext;
    }

    //Мультидекс - необходимый фикс для запуска приложения
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    //Гео
    public static boolean isAutoGeoPosition() {
        return sAutoGeoPosition;
    }

    public static int getRadiusArea() {
        return sRadiusArea;
    }

    public static void setAutoGeoPosition(boolean sAutoGeoPosition) {
        AppInstance.sAutoGeoPosition = sAutoGeoPosition;
    }

    public static void setRadiusArea(int sRadiusArea) {
        AppInstance.sRadiusArea = sRadiusArea;
    }

    public static LatLng getGeoPosition() {
        return sGeoPosition;
    }
}
