package xyz.fycz.myreader.ui.home;

import android.content.DialogInterface;
import android.content.Intent;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.ui.home.bbs.BBSFragment;
import xyz.fycz.myreader.ui.home.bookcase.BookcaseFragment;
import xyz.fycz.myreader.ui.home.bookcase.BookcasePresenter;
import xyz.fycz.myreader.ui.home.bookstore.BookStoreFragment;
import xyz.fycz.myreader.ui.search.SearchBookActivity;
import xyz.fycz.myreader.ui.user.LoginActivity;
import xyz.fycz.myreader.util.AppInfoUtils;
import xyz.fycz.myreader.util.TextHelper;

import java.io.File;
import java.util.ArrayList;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static xyz.fycz.myreader.application.MyApplication.checkVersionByServer;


public class MainPresenter implements BasePresenter {

    private MainActivity mMainActivity;
    private ArrayList<Fragment> mFragments = new ArrayList<>();

    private String[] tabTitle = {"书架"};

    private BookcaseFragment mBookcaseFragment;

    public MainPresenter(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public void start() {
        try {
            Setting mSetting = SysManager.getSetting();
            int settingVersion = mSetting.getSettingVersion();
            if (settingVersion < APPCONST.SETTING_VERSION){
                SysManager.resetSetting();
                DialogCreator.createTipDialog(mMainActivity, "因组件升级，已恢复默认设置！");
            }
        }catch (Exception e){
            SysManager.resetSetting();
            DialogCreator.createTipDialog(mMainActivity, "因组件升级，已恢复默认设置！");
        }
        init();
        checkVersionByServer(mMainActivity, false, (BookcaseFragment) mFragments.get(0));
        mMainActivity.getIvSearch().setOnClickListener(view -> {
            Intent intent = new Intent(mMainActivity, SearchBookActivity.class);
            mMainActivity.startActivity(intent);
        });
        mMainActivity.getCivAvatar().setOnClickListener(v -> {
            final File file = MyApplication.getApplication().getFileStreamPath("userConfig.fy");
            if (file.exists()) {
                DialogCreator.createCommonDialog(mMainActivity, "退出登录", "确定要退出登录吗？"
                        , true, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (file.delete()) {
                                    TextHelper.showText("退出成功");
                                    Intent intent = new Intent(mMainActivity, LoginActivity.class);
                                    mMainActivity.startActivity(intent);
                                } else {
                                    TextHelper.showText("退出失败");
                                }
                            }
                        }, (dialog, which) -> dialog.dismiss());
            } else {
                Intent intent = new Intent(mMainActivity, LoginActivity.class);
                mMainActivity.startActivity(intent);
            }
        });
    }

    /**
     * 初始化
     */
    private void init() {
        mBookcaseFragment = new BookcaseFragment();
        mFragments.add(mBookcaseFragment);
        mMainActivity.getVpContent().setAdapter(new FragmentPagerAdapter(mMainActivity.getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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

        });
        mMainActivity.getTlTabMenu().setupWithViewPager(mMainActivity.getVpContent());
        mMainActivity.getVpContent().setCurrentItem(0);
    }

    /**
     * 添加本地书籍
     * @param path
     */
    public void addLocalBook(String path){
        mBookcaseFragment.getmBookcasePresenter().addLocalBook(path);
    }

    /**
     * 取消编辑状态
     */
    public void cancelEdit(){
        mBookcaseFragment.getmBookcasePresenter().cancelEdit();
    }

    /**
     * 判断是否处于编辑状态
     * @return
     */
    public boolean ismEditState(){
        return mBookcaseFragment.getmBookcasePresenter().ismEditState();
    }
}
