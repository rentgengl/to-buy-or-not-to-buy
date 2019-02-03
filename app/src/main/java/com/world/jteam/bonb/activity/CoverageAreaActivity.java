package com.world.jteam.bonb.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoverageAreaActivity extends FragmentActivity implements OnMapReadyCallback {
    private final FragmentActivity mThis=this;

    private GoogleMap mMap;

    private Switch mUseAutoPosView;
    private EditText mRadiusAreaView;
    private Circle mCircleArea;

    private static final int GEO_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_coverage_area);

        mUseAutoPosView=(Switch) findViewById(R.id.use_auto_position);
        mUseAutoPosView.setChecked(AppInstance.isAutoGeoPosition());
        mUseAutoPosView.setOnCheckedChangeListener(new UseAutoPosOnCheckedChangeListener());

        mRadiusAreaView=(EditText) findViewById(R.id.radius_area);
        mRadiusAreaView.setText(Integer.toString(AppInstance.getRadiusArea()));
        mRadiusAreaView.setFilters(new InputFilter[] {new RadiusAreaFormatInputFilter()});
        mRadiusAreaView.addTextChangedListener(new RadiusAreaTextChangedListener());

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
        } else {
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
        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //mMap.setMyLocationEnabled(true);

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

    private class RadiusAreaTextChangedListener implements TextWatcher{
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mCircleArea != null)
                mCircleArea.setRadius(getRadiusArea()*1000);
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
        String radiusArea = mRadiusAreaView.getText().toString();
        if (radiusArea.equals("")){
            return Constants.DEFAULT_RADIUS_AREA;
        } else {
            return Integer.parseInt(radiusArea);
        }
    }

    private boolean isAutoGeoPosition(){
        return mUseAutoPosView.isChecked();
    }

    private class RadiusAreaFormatInputFilter implements InputFilter {

        Pattern mPattern = Pattern.compile("^|([1-9]{1,1}[0-9]{0,2})");

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String result =
                    dest.subSequence(0, dstart)
                            + source.toString()
                            + dest.subSequence(dend, dest.length());

            Matcher matcher = mPattern.matcher(result);

            if (!matcher.matches()) return dest.subSequence(dstart, dend);

            return null;
        }
    }

}
