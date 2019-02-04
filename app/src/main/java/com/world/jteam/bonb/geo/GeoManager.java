package com.world.jteam.bonb.geo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.Constants;

public class GeoManager {
    private static int mNumberGeoTrace=0; //Для регулирования конфликтов потоков

    private static final String SETTINGS_NAME="geo";

    public static boolean getAutoGeoPositionFromSettings(boolean defval){
        SharedPreferences sp = AppInstance.getAppContext().getSharedPreferences(SETTINGS_NAME,0);
        return sp.getBoolean("auto_geo_position",defval);
    }

    public static void setAutoGeoPositionInSettings(boolean flag){
        if (flag==AppInstance.isAutoGeoPosition())
            return;

        SharedPreferences sp = AppInstance.getAppContext().getSharedPreferences(SETTINGS_NAME,0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("auto_geo_position", flag);
        editor.commit();
    }

    public static int getRadiusAreaFromSettings(int defval){
        SharedPreferences sp = AppInstance.getAppContext().getSharedPreferences(SETTINGS_NAME,0);
        return sp.getInt("radius_area",defval);
    }

    public static void setRadiusAreaInSettings(int radius){
        if (radius==AppInstance.getRadiusArea())
            return;

        SharedPreferences sp = AppInstance.getAppContext().getSharedPreferences(SETTINGS_NAME,0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("radius_area", radius);
        editor.commit();
    }

    public static LatLng getGeoPositionFromSettings(LatLng defval){
        SharedPreferences sp = AppInstance.getAppContext().getSharedPreferences(SETTINGS_NAME,0);

        return new LatLng(
                sp.getFloat("latitude_area", (float) defval.latitude),
                sp.getFloat("longitude_area", (float) defval.longitude));

    }

    public static void setGeoPositionInSettings(LatLng position){
        LatLng currentpos=AppInstance.getGeoPosition();
        if (currentpos.latitude==position.latitude && currentpos.longitude==position.longitude)
            return;

        SharedPreferences sp = AppInstance.getAppContext().getSharedPreferences(SETTINGS_NAME,0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat("latitude_area", (float) position.latitude);
        editor.putFloat("longitude_area", (float) position.longitude);
        editor.commit();
    }

    public static void starGeoPositionTrace(){
        if (!AppInstance.isAutoGeoPosition())
            return;

        mNumberGeoTrace = mNumberGeoTrace + 1;
        final HandlerThread traceHandlerThread=new HandlerThread("GeoPositionTrace"+mNumberGeoTrace);
        traceHandlerThread.start();

        Thread traceThread=new Thread(new Runnable() {
            @Override
            public void run() {
                int numberGeoTrace = mNumberGeoTrace;

                Context context = AppInstance.getAppContext();

                LocationManager locationManager =
                        (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

                while (AppInstance.isAutoGeoPosition() && numberGeoTrace == mNumberGeoTrace){

                    //Проверим права, а то могли поменятся
                    int rc = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
                    if (rc == PackageManager.PERMISSION_DENIED) {
                        AppInstance.setAutoGeoPosition(false);
                        break;
                    }

                    //Обнаружим
                    detectGeoPosition(locationManager,new GeoPositionLocationListener(),traceHandlerThread.getLooper());

                    //Таймаут
                    try {
                        Thread.currentThread().sleep(Constants.UPDATE_RATE_GEO_POSITION);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                traceHandlerThread.quit();
            }
        });
        traceThread.start();
    }

    @SuppressWarnings("MissingPermission")
    public static void  detectGeoPosition(LocationManager locationManager,
                                          LocationListener locationListener,
                                          Looper looper){
        //Получим провайдера с приоритетом на GPS
        String currentProvider=null;
        if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER))
            currentProvider = locationManager.GPS_PROVIDER;
        else if (locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER))
            currentProvider = locationManager.NETWORK_PROVIDER;

        //Есть активный провайдер, получим координаты
        if (currentProvider!=null) {
            locationManager.requestSingleUpdate(
                    currentProvider,locationListener, looper);
        }
    }

    private static class GeoPositionLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            AppInstance.setGeoPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
