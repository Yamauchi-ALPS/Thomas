package com.yamauchi.thomas;

import android.app.Application;
import android.content.Context;

public class ThomasApplication extends Application {
	private ThomasSharedPreferences mPref = null;
	
    public static ThomasSharedPreferences getVvmSharedPreference(Context context){
    	ThomasSharedPreferences pref = null;
        if(context != null){
            ThomasApplication app = (ThomasApplication)context.getApplicationContext();
            if(app.mPref == null){
                app.mPref = new ThomasSharedPreferences(context);
            }
            pref = app.mPref;
        }
        return pref;
    }

}
