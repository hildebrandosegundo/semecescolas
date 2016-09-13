package com.semecescolas;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hildebrandosegundo on 17/02/16.
 */
public abstract class BaseActivity extends FragmentActivity {
    private GoogleMap mMap;
    private LatLng teresina = new LatLng(-5.154925, -42.767201);
    protected LocationSourceMap locationSource;
    private ProgressDialog dialog;
    protected int getLayoutId() {
        return R.layout.kml_map;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setUpMapIfNeeded();

    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(teresina, 10));
                locationSource = new LocationSourceMap();
                mMap.setMyLocationEnabled(true);
                mMap.setTrafficEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.setLocationSource(locationSource);
                locationSource.setLocation(teresina);
                if (mMap != null) {
                    startMap();
                }
            }
        });

    }
    protected abstract void startMap();

    protected GoogleMap getMap() {
        setUpMapIfNeeded();
        return mMap;
    }
}
