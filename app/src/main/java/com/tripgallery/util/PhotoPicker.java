package com.tripgallery.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.tripgallery.R;

/**
 * @author Victor
 */
public class PhotoPicker implements ImageChooserListener
{
	public static final int CAMERA = 0;
	public static final int PICK = 1;

	private final Activity _activity;
	private final ImageSelectedCallback _callback;
	private ImageChooserManager imageChooserManager;
	private String filePath;
	protected int chooserType;

	public PhotoPicker(Activity activity, ImageSelectedCallback callback)
	{
		_activity = activity;
		_callback = callback;
	}

	public void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("chooser_type", chooserType);
		outState.putString("media_path", filePath);
	}

	public void restoreInstance(Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
		{
			if (savedInstanceState.containsKey("chooser_type"))
				chooserType = savedInstanceState.getInt("chooser_type");

			if (savedInstanceState.containsKey("media_path"))
				filePath = savedInstanceState.getString("media_path");
		}
	}

	public void setMode(int mode)
	{
		chooserType = (mode == CAMERA ? ChooserType.REQUEST_CAPTURE_PICTURE : ChooserType.REQUEST_PICK_PICTURE);
		imageChooserManager = new ImageChooserManager(_activity, chooserType, false);
		imageChooserManager.setImageChooserListener(this);
		try
		{
			filePath = imageChooserManager.choose();
		}
		catch (Exception e)
		{
			// TODO handle
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE))
		{
			if (imageChooserManager == null)
				reinitializeImageChooser();
			imageChooserManager.submit(requestCode, data);
		}
	}

	@Override
	public void onImageChosen(final ChosenImage image)
	{
		_activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				_callback.handleImage(image);
			}
		});
	}

	@Override
	public void onError(final String reason)
	{
		_activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(_activity, reason, Toast.LENGTH_LONG).show();
			}
		});
	}

	// Should be called if for some reason the ImageChooserManager is null (Due
	// to destroying of activity for low memory situations)
	private void reinitializeImageChooser()
	{
		imageChooserManager = new ImageChooserManager(_activity, chooserType, false);
		imageChooserManager.setImageChooserListener(this);
		imageChooserManager.reinitialize(filePath);
	}

	public static void showPhotoPickDialog(final Context owner, final PhotoPicker picker)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(owner);
		builder.setPositiveButton(R.string.take_picture, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				picker.setMode(CAMERA);
			}
		});

		builder.setNeutralButton(R.string.choose_picture, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				picker.setMode(PICK);
			}
		});
		builder.show();
	}
}
