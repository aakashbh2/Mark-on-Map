package com.example.aakash.realmnkinvey;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class LatlngValues implements ClusterItem{

    public LatlngValues(){}
    public LatlngValues(Double latitude,Double longitude){
    this.latitude=latitude;
        this.longitude=longitude;
        position = new LatLng(latitude,longitude);
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    private Double latitude,longitude;

    private LatLng position;

    @Override
    public LatLng getPosition() {
        return position;
    }

}
