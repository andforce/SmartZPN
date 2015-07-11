package org.zarroboogs.smartzpn.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.WindowManager;


import org.zarroboogs.smartzpn.R;
import org.zarroboogs.smartzpn.jellyviewpager.widget.JellyViewPager;

public class ViewPagerMainActivity extends ActionBarActivity {

    private JellyViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.viewpager_main_activity);

        mViewPager = (JellyViewPager) findViewById(R.id.contentViewPager);
        mViewPager.setAdapter(new ContentViewPagerAdapter(getSupportFragmentManager()));

    }


}
