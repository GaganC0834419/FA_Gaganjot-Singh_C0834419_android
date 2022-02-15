package com.project.finalexam;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;



public class SharedPreference {
    public static String My_Prefrences = "final_Preferences";
    static SharedPreferences mPref;
    Editor editor;

    public SharedPreference(Context mContext) {
        mPref = mContext.getSharedPreferences(My_Prefrences, Context.MODE_PRIVATE);
        editor = mPref.edit();
    }




    public static String getLatitude() {
        return mPref.getString("LATITUDE", "");
    }

    public static void setLatitude(String status) {
        Editor editor2 = mPref.edit();
        editor2.putString("LATITUDE", status);
        editor2.apply();
    }

    public static String getLongitude() {
        return mPref.getString("LONGITUDE", "");
    }

    public static void setLongitude(String status) {
        Editor editor2 = mPref.edit();
        editor2.putString("LONGITUDE", status);
        editor2.apply();
    }




}
