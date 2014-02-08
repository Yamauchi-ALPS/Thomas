package com.yamauchi.thomas;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ThomasSharedPreferences {
	// Pref Key ===================================================================
    private static final String PREFKEY_DEVICE_UID = "device_uid";
    private static final String PREFKEY_TIME_COUNT = "time_count";
    private static final String PREFKEY_SUBWAY_CHECK = "subway_check";
    private static final String PREFKEY_GET_NEWS = "get_news";
    private SharedPreferences mPref = null;

    public ThomasSharedPreferences(Context context){
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setTimeCountOn(boolean onoff){
        if(mPref != null){
            mPref.edit().putBoolean(PREFKEY_TIME_COUNT, onoff).commit();
        }
    }
    
    public boolean isTimeCountOn(){
        boolean onoff = false;
        if(mPref != null){
        	onoff = mPref.getBoolean(PREFKEY_TIME_COUNT, false);
        }
        return onoff;
    }
    
    public void setSubwayCheckOn(boolean onoff){
        if(mPref != null){
            mPref.edit().putBoolean(PREFKEY_SUBWAY_CHECK, onoff).commit();
        }
    }
    
    public boolean isSubwayCheckOn(){
        boolean onoff = false;
        if(mPref != null){
        	onoff = mPref.getBoolean(PREFKEY_SUBWAY_CHECK, false);
        }
        return onoff;
    }
    
    public void setGetNewsOn(boolean onoff){
        if(mPref != null){
            mPref.edit().putBoolean(PREFKEY_GET_NEWS, onoff).commit();
        }
    }
    
    public boolean isGetNewsOn(){
        boolean onoff = false;
        if(mPref != null){
        	onoff = mPref.getBoolean(PREFKEY_GET_NEWS, false);
        }
        return onoff;
    }
}