package com.tripgallery;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.parse.Parse;

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

    public void setLocation(Location location) {
        this.location = location;

        List<Address> list;

        try {
            currentCity = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1).get(0).getLocality();
        } catch (IOException e) {
            currentCity = null;
        }
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public Location getLocation() {
        return location;
    }
}
