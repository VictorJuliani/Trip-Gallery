package com.tripgallery;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity
{
	@ViewById
	protected FloatingActionButton fab;

	@ViewById(R.id.lbl_longitude)
	protected TextView longitude;

	@ViewById(R.id.lbl_latidude)
	protected TextView latitude;

	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener()
	{
		public void onLocationChanged(Location location)
		{
			longitude.setText(String.valueOf(location.getLongitude()));
			latitude.setText(String.valueOf(location.getLatitude()));
		}

		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}

		public void onProviderEnabled(String provider)
		{
		}

		public void onProviderDisabled(String provider)
		{
		}
	};

	@AfterViews
	protected void setup()
	{
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}
}
