package com.world.jteam.bonb.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.geo.GeoManager;

public class CoverageAreaActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private Switch mUseAutoPosView;
    private EditText mRadiusAreaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_coverage_area);

        mUseAutoPosView=(Switch) findViewById(R.id.use_auto_position);
        mUseAutoPosView.setOnCheckedChangeListener(new UseAutoPosOnCheckedChangeListener());
        mUseAutoPosView.setChecked(AppInstance.isAutoGeoPosition());

        mRadiusAreaView=(EditText) findViewById(R.id.radius_area);
        mRadiusAreaView.addTextChangedListener(new RadiusAreaTextChangedListener());
        mRadiusAreaView.setText(Integer.toString(AppInstance.getRadiusArea()));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.coverage_area);

        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        boolean autoGeoPosition=mUseAutoPosView.isChecked();
        int radiusArea=Integer.parseInt(mRadiusAreaView.getText().toString());

        AppInstance.setAutoGeoPosition(autoGeoPosition);
        AppInstance.setRadiusArea(radiusArea);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(AppInstance.getGeoPosition(),10));

    }

    //Слушатели
    private class UseAutoPosOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        }
    }

    private class RadiusAreaTextChangedListener implements TextWatcher{
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

}
