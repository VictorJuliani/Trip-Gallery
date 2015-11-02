package com.tripgallery.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tripgallery.R;

public class ImageFullSizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_size);
        Intent intent = getIntent();
        String postUrl = intent.getExtras().getString("url");

        ImageView fullSize = (ImageView) findViewById(R.id.imageView2);

        Picasso.with(this)
                .load(postUrl)
                .into(fullSize);
    }
}
