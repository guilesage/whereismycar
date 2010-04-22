package com.piche.wimc;


import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Helper for pref values storage.
 */
public class PrefsValues {

    private SharedPreferences mPrefs;

	public PrefsValues(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public SharedPreferences getPrefs() {
        return mPrefs;
    }
	
    public GeoPoint getSavedLocation(GeoPoint location) {
        try {
        	int iLatitude = mPrefs.getInt("saved-loc-lat", 0);
            int iLongitude = mPrefs.getInt("saved-loc-long", 0);

            if (location == null)
            	location = new GeoPoint(iLatitude, iLongitude);
            
            return location;

        } catch (NullPointerException e) {
            return null;
        } catch (NumberFormatException e2) {
            return null;
        }
    }

    public void setSavedLocation(GeoPoint location) {
        Editor e = mPrefs.edit();
        try {
        	e.putInt("saved-loc-lat", location == null ? null : location.getLatitudeE6());
            e.putInt("saved-loc-long", location == null ? null : location.getLongitudeE6());
        } finally {
            e.commit();
        }
    }
    
    public void deleteLocation() {
        Editor e = mPrefs.edit();
        try {
        	e.remove("saved-loc-lat");
        	e.remove("saved-loc-long");
        } finally {
            e.commit();
        }
    }
}
