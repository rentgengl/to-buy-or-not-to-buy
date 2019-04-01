package com.world.jteam.bonb.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.R;

public class MarketMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        Intent mIntent = getIntent();

        String[] arrName = mIntent.getStringArrayExtra("name");
        double[] arrLat = mIntent.getDoubleArrayExtra("lat");
        double[] arrLng = mIntent.getDoubleArrayExtra("lng");
        String arrLogo[] = mIntent.getStringArrayExtra("logo");

        Picasso picasso = Picasso.with(this);

        for (int i = 0; i<arrName.length; i++ ) {

            //Добавлю маркер на карту
            final Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(arrLat[i], arrLng[i]))
                    .title(arrName[i]));

            //Подгрузим иконку
            picasso.load(Constants.SERVICE_GET_IMAGE +arrLogo[i])
                    .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(
                                        Bitmap.createScaledBitmap(bitmap,80,80,false)));
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                });

        }

        // Спозиционируем камеру
        mMap.moveCamera(CameraUpdateFactory.newLatLng(AppInstance.getGeoPosition()));
        mMap.setMinZoomPreference(11.0f);

    }
}
