package com.tripgallery.activity;

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
import android.util.Log;
import android.widget.ImageView;

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
	protected FloatingActionButton fab;

    @ViewById
    protected ImageView imageView2;

	private PhotoPicker picker;

    @Pref
    protected PreferenceManager_ preferences;

    private Location currentLocation;

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
        Bitmap bitMap = BitmapFactory.decodeFile(image.getFilePathOriginal());
        imageView2.setImageBitmap(BitmapFactory.decodeFile(image.getFileThumbnailSmall()));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final ParseFile file;

        bitMap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        file = new ParseFile(uuid, stream.toByteArray());

        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParseObject object = new ParseObject("Photo");
                object.put("ownerId", preferences.userId().get());
                object.put("file", file);
                object.put("location", new ParseGeoPoint(currentLocation.getLatitude(),
                                                         currentLocation.getLongitude()));
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d(BuildVars.LOG_TAG, getResources().getString(R.string.photo_upload_success));
                        Log.d(BuildVars.LOG_TAG, file.getUrl());
                    }
                });

            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer integer) {
                Log.d(BuildVars.LOG_TAG, integer.toString());
            }
        });

    }

	public void onLocationChanged(Location location)
	{
        this.currentLocation = location;
		String cityName = null;
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		List<Address> addressList;
		try {
			addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if(addressList.size() > 0) {
				Log.d("TG", addressList.get(0).getLocality());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
