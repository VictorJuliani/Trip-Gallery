package com.tripgallery.manager;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * @author Victor
 */
@SharedPref(value = SharedPref.Scope.APPLICATION_DEFAULT)
public interface PreferenceManager
{
	public String userId();
}
