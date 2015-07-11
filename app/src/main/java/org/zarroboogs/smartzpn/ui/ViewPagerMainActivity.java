package org.zarroboogs.smartzpn.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.WindowManager;


import org.zarroboogs.smartzpn.R;
import org.zarroboogs.smartzpn.jellyviewpager.widget.JellyViewPager;
import org.zarroboogs.smartzpn.liangfeizc.RubberIndicator;

public class ViewPagerMainActivity extends ActionBarActivity {

    private JellyViewPager mViewPager;
    private RubberIndicator mRubberIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.viewpager_main_activity);
        mRubberIndicator = (RubberIndicator) findViewById(R.id.rubber);
        mRubberIndicator.setCount(2);

        mViewPager = (JellyViewPager) findViewById(R.id.contentViewPager);
        mViewPager.setAdapter(new ContentViewPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mRubberIndicator.move();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


}
