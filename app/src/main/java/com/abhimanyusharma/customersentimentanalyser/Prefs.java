package com.abhimanyusharma.customersentimentanalyser;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by ANIMATOR ABHI on 16/03/2017.
 */


public class Prefs {


    //  private static final String IS_PERSISTENCE = "is_persistance";
    private static final String USERSPEECH = "userspeech";


    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(context.getPackageName(), 0);
    }



    @NonNull
    public static String getUserspeech(Context context) {
        return getPrefs(context).getString(USERSPEECH, "");
    }

    public static void setUserspeech(Context context, String value) {
        getPrefs(context).edit().putString(USERSPEECH, value).commit();
    }


}
