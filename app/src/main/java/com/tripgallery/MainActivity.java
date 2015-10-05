package com.tripgallery;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tripgallery.manager.LocManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements LocationListener
{
	@ViewById
	protected FloatingActionButton fab;

	@ViewById(R.id.lbl_longitude)
	protected TextView longitude;

	@ViewById(R.id.lbl_latidude)
	protected TextView latitude;

	@ViewById
	protected TextView provider;

	@AfterViews
	protected void setup()
	{
		LocManager loc = new LocManager(this, this);
		loc.start();
	}

	public void onLocationChanged(Location location)
	{
		this.provider.setText(location.getProvider());
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
}
