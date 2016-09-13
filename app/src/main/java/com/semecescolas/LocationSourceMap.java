package com.semecescolas;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hildebrandosegundo on 25/02/16.
 */
public class LocationSourceMap implements LocationSource {
    private LocationSource.OnLocationChangedListener listener;
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        this.listener = listener;
    }
    @Override
    public void deactivate() {
        this.listener = null;
    }
    public void setLocation(Location location) {
        if(this.listener != null) {
            this.listener.onLocationChanged(location);
        }
    }
    public void setLocation(LatLng latLng,int tipo_provider) {
        Location location = null;
        switch (tipo_provider) {
           case 1: location = new Location(LocationManager.NETWORK_PROVIDER);
               break;
            case 2: location = new Location(LocationManager.GPS_PROVIDER);
                break;
            default: location = new Location(LocationManager.GPS_PROVIDER);
        }
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        if(this.listener != null) {
            this.listener.onLocationChanged(location);
        }
    }
    public void setLocation(LatLng latLng) {
        Location location = new Location(LocationManager.GPS_PROVIDER);;
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
        if(this.listener != null) {
            this.listener.onLocationChanged(location);
        }
    }
}
