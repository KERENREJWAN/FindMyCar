package com.rejwanb.findmycar;

public class LocationModel {
    public double Lat, Long;

    public LocationModel(double Lat, double Long) {
        this.Lat = Lat;
        this.Long = Long;
    }

    public double getLat() {
        return Lat;
    }

    public double getLong() {
        return Long;
    }
}
