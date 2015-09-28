package com.tripgallery;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity
{
	@ViewById
	protected Toolbar toolbar;
	@ViewById
	protected FloatingActionButton fab;

	@AfterViews
	protected void setToolbar()
	{
		setSupportActionBar(toolbar);
	}

	@Click(R.id.fab)
	protected void snackAction()
	{
		Snackbar.make(fab, "^~^", Snackbar.LENGTH_LONG).setAction("Action", null).show();
	}
}
