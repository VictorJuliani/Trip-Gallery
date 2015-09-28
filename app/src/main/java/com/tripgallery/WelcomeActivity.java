package com.tripgallery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class WelcomeActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "cq5PCmd3snLt0xT7OHJ9YRREd";
    private static final String TWITTER_SECRET = "72Fuoj06jzNGUWQCOBymHuplrRjhQUhsMJM4FHGRIcNAZAWei1";

    private LoginButton facebookLoginButton;
    private TwitterLoginButton twitterLoginButton;
    private CallbackManager callbackManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences       = getSharedPreferences(getString(R.string.auth_prefs), MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");
        if (!userId.equals("")) { // user already logged in
            // TODO: send user to Main Activity
            Log.d(BuildVars.LOG_TAG, "user already logged in; user id: " + userId);
            finish();
        }

        sharedPreferencesEditor = sharedPreferences.edit();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_welcome);

        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("public_profile,email,user_location");

        // Callback registration
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(final Result<TwitterSession> result) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
                View imageView   =  findViewById(R.id.imageView); // Cast to ImageView in unnecessary
                final View progressBar =  findViewById(R.id.progress_bar); // Same

                twitterLoginButton.setVisibility(View.INVISIBLE);
                facebookLoginButton.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                Log.d(BuildVars.LOG_TAG, "twitter auth: success; user id: " + result.data.getUserId());

                query.whereEqualTo("twitter_user_id", result.data.getUserId());
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) { // This twitter_user_id already exists in our database
                            Log.d(BuildVars.LOG_TAG, "user already signed up before with Twitter");

                            sharedPreferencesEditor.putString("user_id", object.getObjectId());
                            sharedPreferencesEditor.apply();

                            Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            // TODO: send user to Main Activity

                        } else {
                            if (e.getCode() == ParseException.OBJECT_NOT_FOUND) { // User is new to our app with this twitter user id
                                Log.d(BuildVars.LOG_TAG, "user is new to our app");

                                final ParseObject user = new ParseObject("User");
                                user.put("twitter_user_id", result.data.getUserId());
                                user.put("name", "Foo Bar");
                                /** TODO: get the user name (username != user name)
                                 *  TODO: get profile picture
                                 *  Obs: Fabric doesn't provide this out of the box :(
                                 */
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Log.d(BuildVars.LOG_TAG, "save user @ parse: success; user id: " + user.getObjectId());
                                            sharedPreferencesEditor.putString("user_id", user.getObjectId());
                                            sharedPreferencesEditor.apply();

                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                                            // TODO: send user to Main Activity
                                        } else {
                                            Log.e(BuildVars.LOG_TAG, "can't save the new user to parse");
                                            e.printStackTrace();
                                            Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                // TODO: figure out when an error has occurred and when the user has cancelled/denied the authentication
                Toast.makeText(getApplicationContext(), ":(", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }
}
