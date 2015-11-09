package com.tripgallery.activity;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.kbeanie.imagechooser.api.ChosenImage;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.tripgallery.App;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private App app;

	@AfterViews
	protected void setup()
	{
		picker = new PhotoPicker(this, this);

        app = (App) getApplication();

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
        Intent i = new Intent(this, UploadActivity_.class);
        i.putExtra("FILE_PATH", image.getFilePathOriginal());
        startActivity(i);
	}

	public void onLocationChanged(Location location) {
        app.setLocation(location);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        return super.onCreateOptionsMenu(menu);	}
}
