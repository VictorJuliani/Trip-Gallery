package com.tripgallery.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.tripgallery.BuildVars;
import com.tripgallery.R;
import com.tripgallery.manager.PreferenceManager_;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

@EActivity(R.layout.activity_welcome)
public class WelcomeActivity extends AppCompatActivity implements FacebookCallback<LoginResult>
{
	// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
	private static final String TWITTER_KEY = "cq5PCmd3snLt0xT7OHJ9YRREd";
	private static final String TWITTER_SECRET = "72Fuoj06jzNGUWQCOBymHuplrRjhQUhsMJM4FHGRIcNAZAWei1";

	private CallbackManager callbackManager;

	// TODO force fb + twitter buttons to have same size!
	@ViewById
	protected LoginButton facebookLoginButton;
	@ViewById
	protected TwitterLoginButton twitterLoginButton;

	// TODO add multiple size imgs
	@ViewById
	protected ImageView imageView;
	@ViewById
	protected ProgressBar progressBar;

	@Pref
	protected PreferenceManager_ preferences;

	@AfterInject
	protected void loadApis()
	{
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new Twitter(authConfig));

		FacebookSdk.sdkInitialize(getApplicationContext());
		callbackManager = CallbackManager.Factory.create();
	}

	@AfterViews
	protected void setup()
	{
		String userId = preferences.userId().get();
		if (!TextUtils.isEmpty(userId))
		{ // user already logged in
			Log.d(BuildVars.LOG_TAG, "user already logged in; user id: " + userId);
			startActivity(new Intent(this, MainActivity_.class));
			finish();
		}

		facebookLoginButton.setReadPermissions("public_profile, email");

		// Callback registration
		facebookLoginButton.registerCallback(callbackManager, this);
		twitterLoginButton.setCallback(new TwitterCallback());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		callbackManager.onActivityResult(requestCode, resultCode, data);
		twitterLoginButton.onActivityResult(requestCode, resultCode, data);
	}

	private void doLogin(boolean isFacebook, final long userId, final String name)
	{
		final String column = isFacebook ? "facebook_user_id" : "twitter_user_id";
		ParseQuery<ParseObject> query = ParseQuery.getQuery("User");

		twitterLoginButton.setVisibility(View.INVISIBLE);
		facebookLoginButton.setVisibility(View.INVISIBLE);
		imageView.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.VISIBLE);

		query.whereEqualTo(column, userId);
		query.getFirstInBackground(new GetCallback<ParseObject>()
		{
			@Override
			public void done(ParseObject object, ParseException e)
			{
				if (e == null)
				{   // This twitter_user_id already exists in our database
					Log.d(BuildVars.LOG_TAG, "user already signed up before");
					doFinish(object.getObjectId());
				}
				else
				{
					if (e.getCode() == ParseException.OBJECT_NOT_FOUND)
					{ // User is new to our app with this twitter user id
						Log.d(BuildVars.LOG_TAG, "user is new to our app");

						final ParseObject user = new ParseObject("User");
						user.put(column, userId);
						user.put("name", name);
						/**
						 *  TODO: get profile picture
						 *  Obs: Fabric doesn't provide this out of the box :(
						 */
						user.saveInBackground(new SaveCallback()
						{
							@Override
							public void done(ParseException e)
							{
								if (e == null)
								{
									Log.d(BuildVars.LOG_TAG, "save user @ parse: success; user id: " + user.getObjectId());
									doFinish(user.getObjectId());
								}
								else
									doError("can't save the new user to parse", e);
							}
						});
					}
					else
						doError("can't save the new user to parse", e);
				}
			}
		});
	}

	private void doFinish(String objId)
	{
		preferences.userId().put(objId);

		Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
		progressBar.setVisibility(View.GONE);
		startActivity(new Intent(getApplicationContext(), MainActivity_.class));
		finish();
	}

	private void doError(String log, Exception e)
	{
		// TODO: figure out when an error has occurred and when the user has cancelled/denied the authentication
		Log.e(BuildVars.LOG_TAG, log, e);
		Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onSuccess(LoginResult loginResult)
	{
		GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback()
		{
			@Override
			public void onCompleted(JSONObject object, GraphResponse response)
			{
				try
				{
					long userId = object.getLong("id");
					Log.d(BuildVars.LOG_TAG, "facebook auth: success; user id: " + userId);
					doLogin(true, userId, object.getString("name"));
				}
				catch (Exception e)
				{
					doError("couldn't parse facebook response", e);
				}
			}
		}).executeAsync();
	}

	@Override
	public void onCancel()
	{
		// TODO ignore?
	}

	@Override
	public void onError(FacebookException exception)
	{
		doError("failed facebook login", exception);
	}

	private class TwitterCallback extends Callback<TwitterSession>
	{
		@Override
		public void success(final Result<TwitterSession> result)
		{
			long userId = result.data.getUserId();
			Log.d(BuildVars.LOG_TAG, "twitter auth: success; user id: " + userId);
			doLogin(false, userId, "Foo Bar"); //  TODO: get the user name (username != user name)
		}

		@Override
		public void failure(TwitterException exception)
		{
			doError("failed twitter login", exception);
		}
	}
}
