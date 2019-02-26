package com.world.jteam.bonb.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.R; //Временно до объединения пакетов

public class ProductPriceMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_price_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        Intent mIntent = getIntent();

        String[] arrName = mIntent.getStringArrayExtra("name");
        double[] arrLat = mIntent.getDoubleArrayExtra("lat");
        double[] arrLng = mIntent.getDoubleArrayExtra("lng");
        int[] arrPrice = mIntent.getIntArrayExtra("price");


        for (int i = 0; i<arrName.length; i++ ) {

            //Сгенерю иконку с ценой
            IconGenerator mBubble = new IconGenerator(this);
            mBubble.setStyle(IconGenerator.STYLE_GREEN);

            Bitmap mapIcon = mBubble.makeIcon("" + arrPrice[i] + "\u20BD");

            //Добавлю маркер иконкой на карту
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(mapIcon))
                    .position(new LatLng(arrLat[i], arrLng[i]))
                    .title(arrName[i]));
        }


        // Спозиционируем камеру
        mMap.moveCamera(CameraUpdateFactory.newLatLng(AppInstance.getGeoPosition()));
        mMap.setMinZoomPreference(11.0f);
    }
}
