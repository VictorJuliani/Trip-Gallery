package com.tripgallery.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.text.TextUtils;

import com.tripgallery.BuildVars;
import com.tripgallery.R;

/**
 * @author Victor
 */
public class LocManager
{
	private final Activity _activity;
	private final LocationManager _locationManager;
	private final LocationListener _listener;

	public LocManager(final Activity ctx, LocationListener listener)
	{
		_activity = ctx;
		_listener = listener;
		_locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
	}

	public boolean start()
	{
		if (!_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(_activity);
			dialog.setMessage(R.string.turn_on_gps);
			dialog.setPositiveButton(R.string.activate, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt)
				{
					_activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 100);
				}
			});
			dialog.setNegativeButton(R.string.cancel, null);
			dialog.show();
		}

		Criteria crit = new Criteria();
		crit.setPowerRequirement(Criteria.POWER_LOW);
		crit.setAccuracy(Criteria.ACCURACY_COARSE);

		String provider = _locationManager.getBestProvider(crit, true);
		if (!TextUtils.isEmpty(provider))
		{
			Location location = _locationManager.getLastKnownLocation(provider);

			if (location != null)
			{
				_locationManager.requestLocationUpdates(provider, BuildVars.LOCATION_INTERVAL, 0, _listener);
				_listener.onLocationChanged(location);
				return true;
			}
		}

		return false;
	}

	public void stop()
	{
		_locationManager.removeUpdates(_listener);
	}
}
