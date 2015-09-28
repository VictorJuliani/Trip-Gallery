package com.tripgallery;

import com.parse.Parse;

/**
 * Created by matheus on 9/28/15.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "43YBy74RtUb5ElhlkkaG048nj1zzI7JBacVybyHg", "mKuQfCQT1HXnYwzNLUMuYbaYofCTUeSqyhyWpcPC");
    }
}
