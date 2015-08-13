package org.zarroboogs.smartzpn.utis;

import android.content.Context;
import android.content.SharedPreferences;

import org.zarroboogs.smartzpn.SmartZpnApplication;

/**
 * Created by wangdiyuan on 15-8-13.
 */
public class TokenUtils {
    private static final String TOKEN = "token";
    private static final String SPEC = "spec";

    private static SharedPreferences mSharedPreferences = SmartZpnApplication.getContext().getSharedPreferences(SmartZpnApplication.getContext().getPackageName(), Context.MODE_PRIVATE);

    public static String getToken() {
        return mSharedPreferences.getString(TOKEN, "");
    }

    public static void setToken(String token) {
        mSharedPreferences.edit().putString(TOKEN, token).commit();
    }

    public static String getSpec(){
        return mSharedPreferences.getString(SPEC, "");
    }

    public static void setSpec(String spec){
        mSharedPreferences.edit().putString(SPEC,spec).commit();
    }
}
