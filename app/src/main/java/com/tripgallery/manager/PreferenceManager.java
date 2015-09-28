package com.tripgallery.manager;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * @author Victor
 */
@SharedPref
public interface PreferenceManager
{
	public String userId();
}
