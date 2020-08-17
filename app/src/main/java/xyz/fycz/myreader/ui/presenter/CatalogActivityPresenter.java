package xyz.fycz.myreader.ui.presenter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.ui.activity.CatalogActivity;
import xyz.fycz.myreader.ui.fragment.CatalogFragment;
import xyz.fycz.myreader.ui.fragment.BookMarkFragment;

import java.util.ArrayList;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

/**
 * @author fengyue
 * @date 2020/7/22 8:10
 */
public class CatalogActivityPresenter implements BasePresenter {

    private CatalogActivity mCatalogActivity;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private String[] tabTitle = {"目录", "书签"};
    private boolean isDayStyle;
    private FragmentPagerAdapter tabAdapter;

    public CatalogActivityPresenter(CatalogActivity mCatalogActivity) {
        this.mCatalogActivity = mCatalogActivity;
    }

    @Override
    public void start() {
        Setting setting = SysManager.getSetting();
        isDayStyle = setting.isDayStyle();
        //设置日夜间
        if (!isDayStyle){
            mCatalogActivity.setStatusBar(R.color.sys_dialog_setting_bg);
            mCatalogActivity.getRlCommonTitle().setBackground(mCatalogActivity.getDrawable(R.color.sys_dialog_setting_bg));
            mCatalogActivity.getTvBack().setImageDrawable(mCatalogActivity.getDrawable(R.mipmap.larrow_white2));
            mCatalogActivity.getIvCancel().setImageDrawable(mCatalogActivity.getDrawable(R.mipmap.ic_cha_black));
            mCatalogActivity.getIvSearch().setImageDrawable(mCatalogActivity.getDrawable(R.mipmap.b9));
            mCatalogActivity.getEtSearch().setTextColor(mCatalogActivity.getResources().getColor(R.color.sys_night_word));
            mCatalogActivity.getEtSearch().setHintTextColor(mCatalogActivity.getResources().getColor(R.color.sys_night_word));
        }else {
            mCatalogActivity.getEtSearch().setTextColor(mCatalogActivity.getResources().getColor(setting.getReadWordColor()));
        }
        init();

        InputMethodManager manager = ((InputMethodManager) mCatalogActivity.getSystemService(Context.INPUT_METHOD_SERVICE));

        mCatalogActivity.getTvBack().setOnClickListener(v -> mCatalogActivity.finish());
        mCatalogActivity.getIvSearch().setOnClickListener(v -> {
            mCatalogActivity.getTlTabMenu().setVisibility(View.GONE);
            mCatalogActivity.getIvSearch().setVisibility(View.GONE);
            mCatalogActivity.getEtSearch().setVisibility(View.VISIBLE);
            mCatalogActivity.getIvCancel().setVisibility(View.VISIBLE);
            mCatalogActivity.getEtSearch().setFocusable(true);
            mCatalogActivity.getEtSearch().setFocusableInTouchMode(true);
            mCatalogActivity.getEtSearch().requestFocus();
        });

        mCatalogActivity.getEtSearch().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                if (manager != null) manager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }else {
                if (manager != null) manager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        mCatalogActivity.getIvCancel().setOnClickListener(v -> {
            mCatalogActivity.getTlTabMenu().setVisibility(View.VISIBLE);
            mCatalogActivity.getIvSearch().setVisibility(View.VISIBLE);
            mCatalogActivity.getEtSearch().setVisibility(View.GONE);
            mCatalogActivity.getIvCancel().setVisibility(View.GONE);
            if(!"".equals(mCatalogActivity.getEtSearch().getText().toString())){
                mCatalogActivity.getEtSearch().setText("");
            }
        });

        mCatalogActivity.getEtSearch().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                switch (mCatalogActivity.getVpContent().getCurrentItem()){
                    case 0:
                        ((CatalogFragment) tabAdapter.getItem(0)).getmCatalogPresent().startSearch(s.toString());
                        break;
                    case 1:
                        ((BookMarkFragment) tabAdapter.getItem(1)).getmBookMarkPresenter().startSearch(s.toString());
                        break;
                }
            }
        });
    }

    private void init(){
        mFragments.add(new CatalogFragment());
        mFragments.add(new BookMarkFragment());
        tabAdapter = new FragmentPagerAdapter(mCatalogActivity.getSupportFragmentManager(),BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitle[position];
            }

        };
        mCatalogActivity.getVpContent().setAdapter(tabAdapter);
        mCatalogActivity.getTlTabMenu().setupWithViewPager(mCatalogActivity.getVpContent());
        mCatalogActivity.getVpContent().setCurrentItem(0);
    }
}
