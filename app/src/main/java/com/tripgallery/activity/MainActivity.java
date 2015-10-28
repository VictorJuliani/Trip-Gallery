package com.tripgallery.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChosenImage;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.tripgallery.BuildVars;
import com.tripgallery.Post;
import com.tripgallery.R;
import com.tripgallery.RecyclerViewAdapter;
import com.tripgallery.manager.LocManager;
import com.tripgallery.manager.PreferenceManager_;
import com.tripgallery.util.ImageSelectedCallback;
import com.tripgallery.util.PhotoPicker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements LocationListener, ImageSelectedCallback
{
	@ViewById
	protected FloatingActionButton fab;

	@ViewById
	protected RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerViewAdapter recyclerViewAdapter;

	@Pref
	protected PreferenceManager_ preferences;

	private PhotoPicker picker;

    @InstanceState
	protected Location currentLocation;

    @InstanceState
	protected String cityName;

	@AfterViews
	protected void setup()
	{
		picker = new PhotoPicker(this, this);

		final LocManager loc = new LocManager(this, this);
		loc.start();

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    List<Post> posts = new ArrayList<Post>();
                    for (ParseObject object : list) {
                        String url = object.getParseFile("file").getUrl();
                        int likes = 23;
                        String hashtgs = object.getString("tags");
                        String location = object.getString("locationText");
                        Post post = new Post(url, likes, hashtgs, location);
                        posts.add(post);
                    }

                    recyclerViewAdapter = new RecyclerViewAdapter(posts);
                    recyclerView.setAdapter(recyclerViewAdapter);

                } else {
                    e.printStackTrace();
                }
            }
        });

//        recyclerViewAdapter = new RecyclerViewAdapter();
//        recyclerView.setAdapter(recyclerViewAdapter);
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
		Bitmap bitMap = BitmapFactory.decodeFile(image.getFilePathOriginal());
//		publishImg.setImageBitmap(BitmapFactory.decodeFile(image.getFileThumbnailSmall()));
		String uuid = UUID.randomUUID().toString().replace("-", "");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final ParseFile file;
        double factor = 1080.0 / bitMap.getWidth();
        int scaledHeight = (int) (bitMap.getHeight() * factor);

        Bitmap.createScaledBitmap(bitMap, 1080, scaledHeight, false)
              .compress(Bitmap.CompressFormat.JPEG, 50, stream);
		file = new ParseFile(uuid, stream.toByteArray());

		ParseGeoPoint point = null;
//		if (!TextUtils.isEmpty(cityName) && !cityName.equals(locTxt.getText().toString()))
		if (!TextUtils.isEmpty(cityName) && !cityName.equals(""))
		{
			Address addr = getLocationByCity();

			if (addr == null)
				showLocErrorMsg();
			else
				point = new ParseGeoPoint(addr.getLatitude(), addr.getLongitude());
		}

		if (point == null && currentLocation != null)
			point = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
		else
			showLocErrorMsg();

		final ParseGeoPoint finalPoint = point;
		file.saveInBackground(new SaveCallback()
		{
			@Override
			public void done(ParseException e)
			{
				ParseObject object = new ParseObject("Post");
				object.put("ownerId", preferences.userId().get());
				object.put("file", file);

				object.put("location", finalPoint);
//				if (!TextUtils.isEmpty(tagTxt.getText()))
//					object.put("tags", tagTxt.getText().toString());
				if (!TextUtils.isEmpty(""))
					object.put("tags", "");
				else
					object.put("tags", "");

				object.saveInBackground(new SaveCallback()
				{
					@Override
					public void done(ParseException e)
					{
                        if(e != null) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),R.string.something_went_wrong,
                                           Toast.LENGTH_LONG).show();

                        } else {
                            Log.d(BuildVars.LOG_TAG, getResources().getString(R.string.photo_upload_success));
                            Log.d(BuildVars.LOG_TAG, file.getUrl());
                        }
					}
				});

			}
		}, new ProgressCallback()
		{
			@Override
			public void done(Integer integer)
			{
				Log.d(BuildVars.LOG_TAG, integer.toString());
			}
		});
	}

	public void onLocationChanged(Location location)
	{
		this.currentLocation = location;
//		if (TextUtils.isEmpty(locTxt.getText()))
		if (TextUtils.isEmpty(""))
		{
			cityName = getCityByLocation();
//			locTxt.setText(cityName);
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// ignore?
	}

	public void onProviderEnabled(String provider)
	{
		// ignore
	}

	public void onProviderDisabled(String provider)
	{
		// ignore
	}

	private String getCityByLocation()
	{
		if (currentLocation == null)
			return null;

		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		List<Address> addressList;
		try
		{
			addressList = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
			if (addressList.size() > 0)
				return addressList.get(0).getLocality();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private Address getLocationByCity()
	{
		if (TextUtils.isEmpty(""))
			return null;

		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		List<Address> addressList;
		try
		{
			addressList = geocoder.getFromLocationName("", 1);
			if (addressList.size() > 0)
				return addressList.get(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private void showLocErrorMsg()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(true).setMessage(R.string.badLoc);
		builder.create().show();
	}
}
