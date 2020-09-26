package xyz.fycz.myreader.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhouas666 on 2019-03-28
 * Github: https://github.com/zas023
 */
public class TabFragmentPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;
    private List<String> mTitleList;

    public TabFragmentPageAdapter(@NonNull FragmentManager fm) {
        super(fm);
        mFragmentList = new ArrayList<>();
        mTitleList = new ArrayList<>();
    }

    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }

    public void addFragment(Fragment fragment,String title) {
        mFragmentList.add(fragment);
        mTitleList.add(title);
    }

    public void addTitle(String title) {
        mTitleList.add(title);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }
}
