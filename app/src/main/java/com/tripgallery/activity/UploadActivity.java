package com.tripgallery.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
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


    @ViewById(R.id.photo)
    protected ImageView photoView;

    @Pref
    protected PreferenceManager_ preferences;

    @AfterViews
    protected void start() {
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("FILE_PATH");
        if(filePath == null) {
            // TODO: emoji support starts on KitKat
            Toast.makeText(this, "Well... that's awkward. Something bad happened \uD83D\uDE30 Please try again later.", Toast.LENGTH_LONG).show();
            finish();
        }

        Bitmap bitMap = BitmapFactory.decodeFile(filePath);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final ParseFile file;
        double factor = 1080.0 / bitMap.getWidth();
        int scaledHeight = (int) (bitMap.getHeight() * factor);

        Bitmap.createScaledBitmap(bitMap, 1080, scaledHeight, false)
                .compress(Bitmap.CompressFormat.JPEG, 50, stream);
        file = new ParseFile(uuid, stream.toByteArray());

//        ParseGeoPoint point = null;
////		if (!TextUtils.isEmpty(cityName) && !cityName.equals(locTxt.getText().toString()))
//        if (!TextUtils.isEmpty(cityName) && !cityName.equals(""))
//        {
//            Address addr = getLocationByCity();
//
//            if (addr == null)
//                showLocErrorMsg();
//            else
//                point = new ParseGeoPoint(addr.getLatitude(), addr.getLongitude());
//        }
//
//        if (point == null && currentLocation != null)
//            point = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
//        else
//            showLocErrorMsg();

//        final ParseGeoPoint finalPoint = point;
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Picasso.with(getApplicationContext()).load(file.getUrl()).into(photoView);

//                ParseObject object = new ParseObject("Post");
//                object.put("ownerId", preferences.userId().get());
//                object.put("file", file);
//
//                object.put("geopoint", finalPoint);
////				if (!TextUtils.isEmpty(tagTxt.getText()))
////					object.put("tags", tagTxt.getText().toString());
//                if (!TextUtils.isEmpty(""))
//                    object.put("tags", "");
//                else
//                    object.put("tags", "");
//
//                object.saveInBackground(new SaveCallback()
//                {
//                    @Override
//                    public void done(ParseException e)
//                    {
//                        if(e != null) {
//                            e.printStackTrace();
//                            Toast.makeText(getApplicationContext(), R.string.something_went_wrong,
//                                    Toast.LENGTH_LONG).show();
//
//                        } else {
//                            Log.d(BuildVars.LOG_TAG, getResources().getString(R.string.photo_upload_success));
//                            Log.d(BuildVars.LOG_TAG, file.getUrl());
//                        }
//                    }
//                });

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

}
