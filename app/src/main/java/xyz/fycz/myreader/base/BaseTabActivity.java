package xyz.fycz.myreader.base;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import xyz.fycz.myreader.R;

/**
 * @author fengyue
 * @date 2020/8/12 20:02
 */

public abstract class BaseTabActivity extends BaseActivity {
    /**************View***************/
    protected TabLayout mTlIndicator;
    protected ViewPager mVp;

    /************Params*******************/
    private List<Fragment> mFragmentList;
    private List<String> mTitleList;

    /**************abstract***********/
    protected abstract List<Fragment> createTabFragments();
    protected abstract List<String> createTabTitles();

    @Override
    protected void bindView() {
        mTlIndicator = findViewById(R.id.tab_tl_indicator);
        mVp = findViewById(R.id.tab_vp);
    }

    /*****************rewrite method***************************/

    @Override
    protected void initWidget() {
        super.initWidget();
        setUpTabLayout();
    }

    private void setUpTabLayout(){
        mFragmentList = createTabFragments();
        mTitleList = createTabTitles();

        checkParamsIsRight();

        TabFragmentPageAdapter adapter = new TabFragmentPageAdapter(getSupportFragmentManager());
        mVp.setAdapter(adapter);
        mVp.setOffscreenPageLimit(3);
        mTlIndicator.setupWithViewPager(mVp);
    }

    /**
     * 检查输入的参数是否正确。即Fragment和title是成对的。
     */
    private void checkParamsIsRight(){
        if (mFragmentList == null || mTitleList == null){
            throw new IllegalArgumentException("fragmentList or titleList doesn't have null");
        }

        if (mFragmentList.size() != mTitleList.size())
            throw new IllegalArgumentException("fragment and title size must equal");
    }


    /******************inner class*****************/
    class TabFragmentPageAdapter extends FragmentPagerAdapter {

        public TabFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

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
}
