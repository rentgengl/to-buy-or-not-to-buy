package com.world.jteam.bonb.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.world.jteam.bonb.AppInstance;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.geo.GeoManager;

public class CoverageAreaActivity extends FragmentActivity implements OnMapReadyCallback {
    private final FragmentActivity mThis=this;

    private GoogleMap mMap;

    private Switch mUseAutoPosView;
    private Spinner mRadiusAreaView;
    private Circle mCircleArea;

    private static final int GEO_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_coverage_area);

        mUseAutoPosView=(Switch) findViewById(R.id.use_auto_position);
        mUseAutoPosView.setChecked(AppInstance.isAutoGeoPosition());
        mUseAutoPosView.setOnCheckedChangeListener(new UseAutoPosOnCheckedChangeListener());

        mRadiusAreaView=(Spinner) findViewById(R.id.radius_area);
        mRadiusAreaView.setSelection(GeoManager.getRadiusAreaItemByValue(AppInstance.getRadiusArea()));
        mRadiusAreaView.setOnItemSelectedListener(new RadiusAreaItemSelectedListener());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.coverage_area);

        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        boolean useAutoGeoPos = isAutoGeoPosition();
        boolean useAutoGeoPosSave = AppInstance.isAutoGeoPosition();

        AppInstance.setAutoGeoPosition(useAutoGeoPos);
        AppInstance.setRadiusArea(getRadiusArea());

        if (useAutoGeoPos && useAutoGeoPos != useAutoGeoPosSave) {
            GeoManager.starGeoPositionTrace();
        } else if (mCircleArea!=null){
            AppInstance.setGeoPosition(mCircleArea.getCenter());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case GEO_REQUEST:
                //Обработка права доступа на геолокацию
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    moveToCurrentPosition();
                } else {
                    mUseAutoPosView.setChecked(false);
                }
                break;

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new MapClickListener());
        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng position = AppInstance.getGeoPosition();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,10));

        CircleOptions circleOptions = new CircleOptions()
                .center(position)
                .radius(getRadiusArea()*1000)
                .fillColor(getResources().getColor(R.color.colorGeoArea));
        mCircleArea = mMap.addCircle(circleOptions);

    }

    private void moveToCurrentPosition(){
        LocationManager locationManager =
                (LocationManager) mThis.getSystemService(mThis.LOCATION_SERVICE);

        GeoManager.detectGeoPosition(locationManager,new GeoPositionLocationListener(),null);
    }

    //Слушатели
    private class UseAutoPosOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                //Проверим права доступа и если надо запросим повторно
                int rc = ActivityCompat.checkSelfPermission(mThis, Manifest.permission.ACCESS_FINE_LOCATION);
                if (rc == PackageManager.PERMISSION_GRANTED){
                    moveToCurrentPosition(); //Спозиционируем карту на текущей позиции
                } else {
                    final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                    ActivityCompat.requestPermissions(mThis, permissions, GEO_REQUEST);
                }
            }
        }
    }

    private class RadiusAreaItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (mCircleArea != null)
                mCircleArea.setRadius(getRadiusArea()*1000);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class MapClickListener implements GoogleMap.OnMapClickListener{
        @Override
        public void onMapClick(LatLng latLng) {
            if (!isAutoGeoPosition())
                mCircleArea.setCenter(latLng);
        }
    }

    private class GeoPositionLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            mCircleArea.setCenter(position);
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

    //Вспомогательные
    private int getRadiusArea(){
        String radiusArea = mRadiusAreaView.getSelectedItem().toString();
        return Integer.parseInt(radiusArea);
    }

    private boolean isAutoGeoPosition(){
        return mUseAutoPosView.isChecked();
    }

}
