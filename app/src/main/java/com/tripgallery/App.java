package com.tripgallery;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.parse.Parse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by matheus on 9/28/15.
 */
public class App extends Application
{
	private Location location;
	private String currentCity;
	private Geocoder geocoder;

	@Override
	public void onCreate()
	{
		super.onCreate();

		Parse.enableLocalDatastore(this);
		Parse.initialize(this, "43YBy74RtUb5ElhlkkaG048nj1zzI7JBacVybyHg", "mKuQfCQT1HXnYwzNLUMuYbaYofCTUeSqyhyWpcPC");

		geocoder = new Geocoder(this, Locale.getDefault());

	}

	public void setLocation(Location location)
	{
		this.location = location;

		List<Address> list;

		try
		{
			currentCity = geocoder.getFromLocation(location.getLatitude(),
					location.getLongitude(), 1).get(0).getLocality();
		}
		catch (IOException e)
		{
			currentCity = null;
		}
	}

	public String getCurrentCity()
	{
		return currentCity;
	}

	public Location getLocation()
	{
		return location;
	}

	public static Intent sharePhoto(Context ctx, Bitmap image)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = MediaStore.Images.Media.insertImage(ctx.getContentResolver(), image, "Title", null);

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		// Add data to the intent, the receiving app will decide what to do with it.
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));

		return intent;
	}

	public Address getLocationByCity(String name)
	{
		if (TextUtils.isEmpty(name))
			return null;

		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		List<Address> addressList;
		try
		{
			addressList = geocoder.getFromLocationName(name, 1);
			if (addressList.size() > 0)
				return addressList.get(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
