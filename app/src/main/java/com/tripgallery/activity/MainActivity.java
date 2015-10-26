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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kbeanie.imagechooser.api.ChosenImage;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.tripgallery.BuildVars;
import com.tripgallery.R;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements LocationListener, ImageSelectedCallback
{
	@ViewById
	protected Button publishBtn;

	@ViewById
	protected ImageView publishImg;

	@ViewById
	protected LinearLayout photoInfo;

	@ViewById
	protected EditText tagTxt;

	@ViewById
	protected EditText locTxt;

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

		LocManager loc = new LocManager(this, this);
		loc.start();

        Log.d(BuildVars.LOG_TAG, "---");
        Log.d(BuildVars.LOG_TAG, preferences.userId().get());
        Log.d(BuildVars.LOG_TAG, "---");
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

	@Click(R.id.publishBtn)
	protected void pickPhoto()
	{
		PhotoPicker.showPhotoPickDialog(this, picker);
	}

	@Override
	public void handleImage(ChosenImage image)
	{
		photoInfo.setVisibility(View.VISIBLE);
		Bitmap bitMap = BitmapFactory.decodeFile(image.getFilePathOriginal());
		publishImg.setImageBitmap(BitmapFactory.decodeFile(image.getFileThumbnailSmall()));
		String uuid = UUID.randomUUID().toString().replace("-", "");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final ParseFile file;

		bitMap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
		file = new ParseFile(uuid, stream.toByteArray());

		ParseGeoPoint point = null;
		if (!TextUtils.isEmpty(cityName) && !cityName.equals(locTxt.getText().toString()))
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
				ParseObject object = new ParseObject("Photo");
				object.put("ownerId", preferences.userId().get());
				object.put("file", file);

				object.put("cityName", finalPoint);
				if (TextUtils.isEmpty(tagTxt.getText()))
					object.put("tags", tagTxt.getText());
				else
					object.put("tags", "");

				object.saveInBackground(new SaveCallback()
				{
					@Override
					public void done(ParseException e)
					{
						Log.d(BuildVars.LOG_TAG, getResources().getString(R.string.photo_upload_success));
						Log.d(BuildVars.LOG_TAG, file.getUrl());
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
		if (TextUtils.isEmpty(locTxt.getText()))
		{
			cityName = getCityByLocation();
			locTxt.setText(cityName);
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
		if (TextUtils.isEmpty(locTxt.getText()))
			return null;

		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		List<Address> addressList;
		try
		{
			addressList = geocoder.getFromLocationName(locTxt.getText().toString(), 1);
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
