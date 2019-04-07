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
import com.google.android.gms.maps.model.internal.zzp;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.model.ModelMarket;

import java.util.ArrayList;

public class MarketMapActivity extends FragmentActivity implements OnMapReadyCallback {
    MarketMapActivity mThis = this;
    private GoogleMap mMap;
    private ArrayList<ModelMarket> mMarkets;

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

        Intent intent = getIntent();
        mMarkets = intent.getParcelableArrayListExtra("markets");

        Picasso picasso = Picasso.with(this);

        int i=0;
        for (ModelMarket market : mMarkets) {

            //Добавлю маркер на карту
            final Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(market.latitude, market.longitude))
                    .title(market.name));
            marker.setTag(Integer.toString(i));

            //Подгрузим иконку
            picasso.load(Constants.SERVICE_GET_IMAGE +market.logo_link)
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

            i++;
        }

        // Спозиционируем камеру
        mMap.moveCamera(CameraUpdateFactory.newLatLng(AppInstance.getGeoPosition()));
        mMap.setMinZoomPreference(11.0f);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                ModelMarket market = mMarkets.get(Integer.parseInt(marker.getTag().toString()));

                Intent intent = new Intent(mThis, MainActivity.class);
                intent.putExtra("market_id", market.id);
                intent.putExtra("market_group_id", market.market_group_id);
                intent.putExtra("market_name", market.name);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });
    }
}
