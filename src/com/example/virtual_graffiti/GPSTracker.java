package com.example.virtual_graffiti;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;

public class GPSTracker extends Service implements LocationListener {
	
	private GeoFix geoFix;
	
	public double getLongitude() {
		return 45.234234;
	}
	public double getLatitude() {
		return 123.1234342;
	}
	public double getAccuracy() {
		return 10.1242424;
	}
	public GeoFix getGeoFix() {
		return geoFix;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
