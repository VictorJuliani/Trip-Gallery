package com.tripgallery.activity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.kbeanie.imagechooser.api.ChosenImage;
import com.tripgallery.R;
import com.tripgallery.manager.LocManager;
import com.tripgallery.util.ImageSelectedCallback;
import com.tripgallery.util.PhotoPicker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements LocationListener, ImageSelectedCallback
{
	@ViewById
	protected FloatingActionButton fab;

	@ViewById(R.id.lbl_longitude)
	protected TextView longitude;

	@ViewById(R.id.lbl_latidude)
	protected TextView latitude;

	@ViewById
	protected TextView provider;

	private PhotoPicker picker;

	@AfterViews
	protected void setup()
	{
		picker = new PhotoPicker(this, this);

		LocManager loc = new LocManager(this, this);
		loc.start();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		picker.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		picker.restoreInstance(savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		picker.onActivityResult(requestCode, resultCode, data);
	}

	@Click(R.id.fab)
	protected void pickPhoto()
	{
		PhotoPicker.showPhotoPickDialog(this, picker);
	}

	@Override
	public void handleImage(ChosenImage image)
	{
		// TODO
	}

	public void onLocationChanged(Location location)
	{
		this.provider.setText(location.getProvider());
		longitude.setText(String.valueOf(location.getLongitude()));
		latitude.setText(String.valueOf(location.getLatitude()));
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// TODO
	}

	public void onProviderEnabled(String provider)
	{
		// TODO
	}

	public void onProviderDisabled(String provider)
	{
		// TODO
	}
}
