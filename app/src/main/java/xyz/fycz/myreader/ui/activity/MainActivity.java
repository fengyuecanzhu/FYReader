package xyz.fycz.myreader.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseActivity2;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.ui.fragment.BookStoreFragment;
import xyz.fycz.myreader.ui.fragment.BookcaseFragment;
import xyz.fycz.myreader.ui.fragment.FindFragment;
import xyz.fycz.myreader.ui.fragment.MineFragment;
import xyz.fycz.myreader.ui.presenter.BookcasePresenter;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static xyz.fycz.myreader.application.MyApplication.checkVersionByServer;

/**
 * @author fengyue
 * @date 2020/9/13 13:03
 */
public class MainActivity extends BaseActivity2 {
    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigation;
    @BindView(R.id.view_pager_main)
    ViewPager viewPagerMain;

    private List<Fragment> mFragments = new ArrayList<>();
    private String[] titles;
    private String groupName;
    private File appFile;
    private boolean isForceUpdate;
    private BookcaseFragment mBookcaseFragment;
    private FindFragment mFindFragment;
    private MineFragment mMineFragment;

    @Override
    protected int getContentId() {
        return R.layout.activity_main;
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        getSupportActionBar().setTitle(titles[0]);
        getSupportActionBar().setSubtitle(groupName);
        setStatusBarColor(R.color.colorPrimary, true);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        groupName = SharedPreUtils.getInstance().getString("curBookGroupName", "");
        titles = new String[]{"书架", "发现", "我的"};
        mBookcaseFragment = new BookcaseFragment();
        mFindFragment = new FindFragment();
        mMineFragment = new MineFragment();
        mFragments.add(mBookcaseFragment);
        mFragments.add(mFindFragment);
        mFragments.add(mMineFragment);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        viewPagerMain.setOffscreenPageLimit(2);
        viewPagerMain.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @Override
            public int getCount() {
                return mFragments.size();
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }
        });

    }

    @Override
    protected void initClick() {
        super.initClick();
        //BottomNavigationView 点击事件监听
        bottomNavigation.setOnNavigationItemSelectedListener(menuItem -> {
            int menuId = menuItem.getItemId();
            // 跳转指定页面：Fragment
            switch (menuId) {
                case R.id.menu_bookshelf:
                    viewPagerMain.setCurrentItem(0);
                    break;
                case R.id.menu_find_book:
                    viewPagerMain.setCurrentItem(1);
                    break;
                case R.id.menu_my_config:
                    viewPagerMain.setCurrentItem(2);
                    break;
            }
            return false;
        });

        // ViewPager 滑动事件监听
        viewPagerMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                //将滑动到的页面对应的 menu 设置为选中状态
                bottomNavigation.getMenu().getItem(i).setChecked(true);
                getSupportActionBar().setTitle(titles[i]);
                if (i == 0)  {
                    getSupportActionBar().setSubtitle(groupName);
                }else {
                    getSupportActionBar().setSubtitle("");
                }
                invalidateOptionsMenu();
                /*if (i == 1){
                    ((BookStoreFragment) mFragments.get(i)).lazyLoad();
                }*/
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    @Override
    protected void processLogic() {
        super.processLogic();
        try {
            int settingVersion = SysManager.getSetting().getSettingVersion();
            if (settingVersion < APPCONST.SETTING_VERSION){
                SysManager.resetSetting();
            }
        }catch (Exception e){
            SysManager.resetSetting();
        }
    }

    private void reLoadFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        mBookcaseFragment = (BookcaseFragment) fragments.get(0);
        mFindFragment = (FindFragment) fragments.get(1);
        mMineFragment = (MineFragment) fragments.get(2);
    }

    public ViewPager getViewPagerMain() {
        return viewPagerMain;
    }

    /********************************Event***************************************/
    /**
     * 创建菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (viewPagerMain.getCurrentItem() == 0) {

            if (mBookcaseFragment.getmBookcasePresenter() != null && mBookcaseFragment.getmBookcasePresenter().ismEditState()) {
                menu.findItem(R.id.action_finish).setVisible(true);
                menu.setGroupVisible(R.id.bookcase_menu, false);
            } else {
                menu.setGroupVisible(R.id.bookcase_menu, true);
                menu.findItem(R.id.action_finish).setVisible(false);
            }
        } else {
            menu.setGroupVisible(R.id.bookcase_menu, false);
            menu.findItem(R.id.action_finish).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 导航栏菜单点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mBookcaseFragment.isRecreate()) {
            reLoadFragment();
        }
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent searchBookIntent = new Intent(this, SearchBookActivity.class);
                startActivity(searchBookIntent);
                return true;
            case R.id.action_finish:
                mBookcaseFragment.getmBookcasePresenter().cancelEdit();
                invalidateOptionsMenu();
                return true;
            case R.id.action_change_group:
                mBookcaseFragment.getmBookcasePresenter()
                        .showBookGroupMenu(findViewById(R.id.action_change_group), () -> {
                            groupName = SharedPreUtils.getInstance().getString("curBookGroupName", "所有书籍");
                            getSupportActionBar().setSubtitle(groupName);
                        });
            case R.id.action_edit:
                invalidateOptionsMenu();
                break;
        }
        return mBookcaseFragment.getmBookcasePresenter().onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mBookcaseFragment.getmBookcasePresenter() != null && mBookcaseFragment.getmBookcasePresenter().ismEditState()) {
            mBookcaseFragment.getmBookcasePresenter().cancelEdit();
            invalidateOptionsMenu();
            return;
        }
        if (System.currentTimeMillis() - APPCONST.exitTime > APPCONST.exitConfirmTime) {
            ToastUtils.showExit("再按一次退出");
            APPCONST.exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        MyApplication.checkVersionByServer(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode == RESULT_OK || resultCode == RESULT_CANCELED) && requestCode == APPCONST.APP_INSTALL_CODE) {
            installProcess(appFile, isForceUpdate);//再次执行安装流程，包含权限判等
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case APPCONST.REQUEST_LOGIN:
                    if (mMineFragment.isRecreate()){
                        reLoadFragment();
                    }
                    mMineFragment.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case APPCONST.PERMISSIONS_REQUEST_STORAGE: {
                // 如果取消权限，则返回的值为0
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //跳转到 FileSystemActivity
                    Intent intent = new Intent(this, FileSystemActivity.class);
                    startActivity(intent);

                } else {
                    ToastUtils.showWarring("用户拒绝开启读写权限");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        MyApplication.getApplication().shutdownThreadPool();
        super.onDestroy();
    }

    //安装应用的流程
    public void installProcess(File file, boolean isForceUpdate) {
        if (appFile == null || !this.isForceUpdate) {
            appFile = file;
            this.isForceUpdate = isForceUpdate;
        }
        boolean haveInstallPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //先获取是否有安装未知来源应用的权限
            haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {//没有权限
                DialogCreator.createCommonDialog(this, "安装应用",
                        "安装应用需要打开未知来源权限，请去设置中开启权限", true,
                        "确定", (dialog, which) -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startInstallPermissionSettingActivity();
                            }
                        });
                return;
            }
        }
        //有权限，开始安装应用程序
        installApk(file, isForceUpdate);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        Uri packageURI = Uri.parse("package:" + getPackageName());
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, APPCONST.APP_INSTALL_CODE);
    }

    /**
     * 安装应用
     *
     * @param file
     * @param isForceUpdate
     */
    public void installApk(File file, boolean isForceUpdate) {
        String authority = getApplicationContext().getPackageName() + ".fileprovider";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri apkUri = FileProvider.getUriForFile(this, authority, file);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        startActivity(intent);
        if (isForceUpdate) {
            finish();
        }
    }

    public interface OnGroupChangeListener{
        void onChange();
    }
}
