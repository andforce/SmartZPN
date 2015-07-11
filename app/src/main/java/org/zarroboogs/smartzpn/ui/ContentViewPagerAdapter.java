package org.zarroboogs.smartzpn.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by andforce on 15/7/11.
 */
public class ContentViewPagerAdapter extends FragmentPagerAdapter {


    public ContentViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public int getCount() {
        return 1;
    }
}
