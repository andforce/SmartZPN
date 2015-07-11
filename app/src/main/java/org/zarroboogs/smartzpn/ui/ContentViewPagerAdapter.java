package org.zarroboogs.smartzpn.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by andforce on 15/7/11.
 */
public class ContentViewPagerAdapter extends FragmentPagerAdapter {


    private ArrayList<Fragment> mFragments = new ArrayList<>();

    public ContentViewPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments.add(new MainFragment());
        mFragments.add(new AboutFragment());
    }

    @Override
    public Fragment getItem(int position) {

        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
