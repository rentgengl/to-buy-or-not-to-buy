package com.world.jteam.bonb;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import com.google.android.gms.maps.model.LatLng;
import com.world.jteam.bonb.geo.GeoManager;
import com.world.jteam.bonb.media.BarcodeManager;
import com.world.jteam.bonb.model.ModelUser;

import java.io.IOException;

public class AppInstance extends Application {
    private static Context sContext;
    private static boolean sFirstStart;
    private static ModelUser sUser;

    private static boolean sAutoGeoPosition = true;
    private static int sRadiusArea = Constants.DEFAULT_RADIUS_AREA;
    private static LatLng sGeoPosition = new LatLng(55.755814, 37.617635); //Москва по дефолту

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        Thread thread = new Thread(new AppInitialisation());
        thread.start();
    }

    //Основная инициализация
    private class AppInitialisation implements Runnable {
        @Override
        public void run() {
            //Первый запуск
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(sContext);
            sFirstStart = sharedPreferences.getBoolean("first_start", true);
            if (sFirstStart) {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("first_start", false);
                edit.commit();
            }

            //Геолокация
            sAutoGeoPosition = GeoManager.getAutoGeoPositionFromSettings(sAutoGeoPosition);
            sRadiusArea = GeoManager.getRadiusAreaFromSettings(sRadiusArea);
            sGeoPosition = GeoManager.getGeoPositionFromSettings(sGeoPosition);

            //Штрихкодер
            if (sFirstStart) {
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
    public static ModelUser getUser(){
        return AppInstance.sUser;
    }

    public static void setUser(ModelUser user){
        AppInstance.sUser = user;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    //Гео
    public static boolean isAutoGeoPosition() {
        return sAutoGeoPosition;
    }

    public static void setAutoGeoPosition(boolean flag) {
        if (flag == AppInstance.sAutoGeoPosition)
            return;

        GeoManager.setAutoGeoPositionInSettings(flag);
        AppInstance.sAutoGeoPosition = flag;
    }

    public static int getRadiusArea() {
        return sRadiusArea;
    }

    public static void setRadiusArea(int radius) {
        if (radius == AppInstance.sRadiusArea)
            return;

        GeoManager.setRadiusAreaInSettings(radius);
        AppInstance.sRadiusArea = radius;
    }

    public static LatLng getGeoPosition() {
        return sGeoPosition;
    }

    public static void setGeoPosition(LatLng position) {
        if (position.latitude == AppInstance.sGeoPosition.latitude
                && position.longitude == AppInstance.sGeoPosition.longitude)
            return;

        GeoManager.setGeoPositionInSettings(position);
        AppInstance.sGeoPosition = position;
    }
}
