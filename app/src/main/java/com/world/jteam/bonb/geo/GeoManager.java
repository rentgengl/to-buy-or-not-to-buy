package com.world.jteam.bonb.geo;

import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.Constants;

public class GeoManager {

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

        Thread traceThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (AppInstance.isAutoGeoPosition()){
                    try {
                        Thread.currentThread().sleep(Constants.UPDATE_RATE_GEO_POSITION);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        traceThread.start();
    }
}
