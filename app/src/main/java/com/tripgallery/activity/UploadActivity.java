package com.tripgallery.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.tripgallery.App;
import com.tripgallery.BuildVars;
import com.tripgallery.R;
import com.tripgallery.manager.PreferenceManager_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * Created by matheus on 10/29/15.
 */

@EActivity(R.layout.activity_upload)
public class UploadActivity extends AppCompatActivity {
    private App app;

    @ViewById(R.id.photo)
    protected ImageView photoView;

    @ViewById
    protected Toolbar toolbar;

    @ViewById
    protected TextView noLocationWarning;

    @ViewById
    protected EditText locationET;

    @ViewById
    protected EditText hashtagsET;


    @Pref
    protected PreferenceManager_ preferences;

    private ParseFile file;

    @AfterViews
    protected void start() {
        app = (App) getApplication();
        setSupportActionBar(toolbar);

        noLocationWarning.setText(getString(R.string.no_location_warning, app.getCurrentCity()));

        hashtagsET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            int lastLength = 0;

            @Override
            public void afterTextChanged(Editable s) {
                String text = hashtagsET.getText().toString();

                if (text.length() < lastLength) {
                    lastLength = text.length();
                    return;
                }
                lastLength = text.length();

                if (text.length() != 0 && text.charAt(text.length() - 1) == ' ') {
                    if (!text.contains("# ")) {
                        hashtagsET.setText(text + "#");
                        hashtagsET.setSelection(hashtagsET.length());
                    }
                }
            }
        });

        hashtagsET.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
	        @Override
	        public void onFocusChange(View v, boolean hasFocus)
	        {
		        if (hasFocus)
		        {
			        if (hashtagsET.getText().toString().trim().length() == 0)
			        {
				        hashtagsET.setText("#");
				        hashtagsET.setSelection(1);
			        }
		        }
	        }
        });

        Intent intent = getIntent();
        String filePath = intent.getStringExtra("FILE_PATH");
        if(filePath == null) {
            // TODO: emoji support starts on KitKat
            Toast.makeText(this, "Well... that's awkward. Something bad happened \uD83D\uDE30 Please try again later.", Toast.LENGTH_LONG).show();
            finish();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        String uuid = UUID.randomUUID().toString().replace("-", "");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        double factor;
        int scaledHeight;

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        factor = 1080.0 / options.outWidth;
        scaledHeight = (int) (options.outHeight * factor);
        int inSampleSize = 1;

        if (options.outWidth > 1080) {
            final int halfWidth = options.outWidth / 2;

            while ((halfWidth / inSampleSize) > 1080) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap.createScaledBitmap(BitmapFactory.decodeFile(filePath, options),1080,
                scaledHeight, false).compress(Bitmap.CompressFormat.JPEG, 50, stream);
        file = new ParseFile(uuid, stream.toByteArray());

        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Picasso.with(getApplicationContext()).load(file.getUrl()).into(photoView);

                ParseObject object = new ParseObject("Post");
                object.put("ownerId", preferences.userId().get());
                object.put("file", file);

                ParseGeoPoint geoPoint = new ParseGeoPoint(app.getLocation().getLatitude(),
                        app.getLocation().getLongitude());

                object.put("geopoint", geoPoint);
                object.put("location", app.getCurrentCity());
            }
        }, new ProgressCallback()
        {
            @Override
            public void done(Integer integer) {
                Log.d(BuildVars.LOG_TAG, integer.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	    getMenuInflater().inflate(R.menu.upload, menu);
	    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionDone) {
            String locationLabel = locationET.getText().toString().trim();
            ParseGeoPoint point;
            Address address;
            if (locationLabel.length() != 0) {
                address = app.getLocationByCity(locationLabel);
                point = new ParseGeoPoint(address.getLatitude(), address.getLongitude());
            } else {
                point = new ParseGeoPoint(app.getLocation().getLatitude(), app.getLocation().getLongitude());
                locationLabel = app.getCurrentCity();
            }

            ParseObject post = new ParseObject("Post");
            post.put("location", point);
            post.put("locationLabel", locationLabel);
            post.put("ownerId", preferences.userId().get());
            post.put("hashtags", hashtagsET.getText().toString().trim());
            post.put("file", file);

            post.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(UploadActivity.this,
                                getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                    } else {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }
            });


            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
