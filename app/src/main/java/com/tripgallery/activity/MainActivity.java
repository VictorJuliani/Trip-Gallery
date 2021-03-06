package com.tripgallery.activity;

import android.app.Activity;
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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.kbeanie.imagechooser.api.ChosenImage;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
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
	protected Toolbar toolbar;

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
	private boolean init = false;
    private boolean search;

    private SearchView searchView;

	@AfterViews
	protected void setup()
	{
		picker = new PhotoPicker(this, this);

		app = (App) getApplication();
		setSupportActionBar(toolbar);

		final LocManager loc = new LocManager(this, this);
		loc.start();

		layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);

		if (!init && app.getLocation() != null)
			initFeed();
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

        if (requestCode == 666) {
            if (resultCode == Activity.RESULT_OK) {
                this.recyclerViewAdapter = new RecyclerViewAdapter();
                this.recyclerView.swapAdapter(this.recyclerViewAdapter, true);
                this.init = false;
                this.initFeed();
            }
        } else {
            picker.onActivityResult(requestCode, resultCode, data);
        }
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
		startActivityForResult(i, 666);
	}

	public void onLocationChanged(Location location)
	{
		app.setLocation(location);
		if (!init)
		{
			initFeed();
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

	@Override
	protected void onNewIntent(Intent intent)
	{
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
            this.search = true;
			String query = intent.getStringExtra(SearchManager.QUERY);
			loadImages(null, query, false);
			Address loc = getLocationByCity(query);
			if (loc != null)
			{
				ParseGeoPoint geoPoint = new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
				loadImages(geoPoint, null, true);
			}

            this.searchView.clearFocus();
		}
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

	private Address getLocationByCity(String name)
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

	private void showLocErrorMsg()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(true).setMessage(R.string.badLoc);
		builder.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);

		MenuItem searchItem = menu.findItem(R.id.search);

		SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

		SearchView searchView = null;
		if (searchItem != null)
		{
			searchView = (SearchView) searchItem.getActionView();
		}
		if (searchView != null)
		{
			searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
		}

        this.searchView = searchView;

		return super.onCreateOptionsMenu(menu);
	}

	private List<Post> loadImages(ParseGeoPoint geoPoint, String hashtags, final boolean append)
	{
		final List<Post> posts = new ArrayList<Post>();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
		if (geoPoint != null)
			query.whereWithinKilometers("location", geoPoint, 10);
		if (hashtags != null)
			query.whereContains("hashtags", hashtags);
		query.findInBackground(new FindCallback<ParseObject>()
		{
			@Override
			public void done(List<ParseObject> list, ParseException e)
			{
				if (e == null)
				{
					for (ParseObject object : list)
					{
						String url = object.getParseFile("file").getUrl();
						int likes = 23;
						String hashtags = object.getString("hashtags");
						String location = object.getString("locationLabel");
						Post post = new Post(url, likes, hashtags, location);
						posts.add(post);
					}

                    recyclerViewAdapter.put(posts, append);

				}
			}
		});

		return posts;
	}

	private void initFeed()
	{
		ParseGeoPoint geoPoint = new ParseGeoPoint(app.getLocation().getLatitude(), app.getLocation().getLongitude());
        recyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);
		loadImages(geoPoint, null, false);
		init = true;
	}

    @Override
    public void onBackPressed() {
        if (this.search) {
            this.search = false;
            this.recyclerViewAdapter.restore();
            this.searchView.setQuery("", false);
            this.searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }
}
