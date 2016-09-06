package org.zarroboogs.smartzpn;

import android.app.Application;
import android.content.Context;

/**
 * Created by wangdiyuan on 15-8-13.
 */
public class SmartZpnApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
