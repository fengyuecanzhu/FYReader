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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivityMainBinding;
import xyz.fycz.myreader.entity.SharedBook;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.BookGroup;
import xyz.fycz.myreader.greendao.service.BookGroupService;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.model.storage.BackupRestoreUi;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.ui.fragment.BookcaseFragment;
import xyz.fycz.myreader.ui.fragment.FindFragment;
import xyz.fycz.myreader.ui.fragment.MineFragment;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.AdUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.webapi.LanZouApi;
import xyz.fycz.myreader.widget.NoScrollViewPager;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

/**
 * @author fengyue
 * @date 2020/9/13 13:03
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {
    public static final String TAG = MainActivity.class.getSimpleName();

    private List<Fragment> mFragments = new ArrayList<>();
    private String[] titles;
    private String groupName;
    private File appFile;
    private boolean isForceUpdate;
    private BookcaseFragment mBookcaseFragment;
    private FindFragment mFindFragment;
    private MineFragment mMineFragment;
    private Animation mBottomInAnim;
    private Animation mBottomOutAnim;

    @Override
    protected void bindView() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        boolean startFromSplash = getIntent().getBooleanExtra("startFromSplash", false);
        if (!startFromSplash && BookGroupService.getInstance().curGroupIsPrivate()) {
            SharedPreUtils.getInstance().putString(getString(R.string.curBookGroupId), "");
            SharedPreUtils.getInstance().putString(getString(R.string.curBookGroupName), "");
        }
        super.onCreate(savedInstanceState);
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
    protected boolean initSwipeBackEnable() {
        return false;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        groupName = SharedPreUtils.getInstance().getString(getString(R.string.curBookGroupName), "");
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
        binding.viewPagerMain.setOffscreenPageLimit(2);
        binding.viewPagerMain.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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

        mToolbar.setOnLongClickListener(v -> {
            if (binding.viewPagerMain.getCurrentItem() == 0
                    && (mBookcaseFragment.getmBookcasePresenter() != null
                    && !mBookcaseFragment.getmBookcasePresenter().ismEditState())) {
                if (BookGroupService.getInstance().curGroupIsPrivate()) {
                    goBackNormalBookcase();
                } else {
                    goPrivateBookcase();
                }
                return true;
            }
            return false;
        });

        //BottomNavigationView 点击事件监听
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            int menuId = menuItem.getItemId();
            // 跳转指定页面：Fragment
            switch (menuId) {
                case R.id.menu_bookshelf:
                    binding.viewPagerMain.setCurrentItem(0);
                    break;
                case R.id.menu_find_book:
                    binding.viewPagerMain.setCurrentItem(1);
                    break;
                case R.id.menu_my_config:
                    binding.viewPagerMain.setCurrentItem(2);
                    break;
            }
            return false;
        });

        // ViewPager 滑动事件监听
        binding.viewPagerMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                //将滑动到的页面对应的 menu 设置为选中状态
                binding.bottomNavigationView.getMenu().getItem(i).setChecked(true);
                getSupportActionBar().setTitle(titles[i]);
                if (i == 0) {
                    getSupportActionBar().setSubtitle(groupName);
                } else {
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
            if (settingVersion < APPCONST.SETTING_VERSION) {
                SysManager.resetSetting();
                Log.d(TAG, "resetSetting");
            }
        } catch (Exception e) {
            ToastUtils.showError(e.getLocalizedMessage());
            e.printStackTrace();
        }
        try {
            int sourceVersion = SysManager.getSetting().getSourceVersion();
            if (sourceVersion < APPCONST.SOURCE_VERSION) {
                SysManager.resetSource();
                Log.d(TAG, "resetSource");
            }
        } catch (Exception e) {
            ToastUtils.showError(e.getLocalizedMessage());
            e.printStackTrace();
        }
        firstInit();
        LanZouApi.INSTANCE.checkSubscribeUpdate(this);
        AdUtils.adRecord("Usage", "usTimes");
    }

    private void firstInit() {
        SharedPreUtils sru = SharedPreUtils.getInstance();
        if (!sru.getBoolean("firstInit")) {
            BookSourceManager.initDefaultSources();
            DialogCreator.createCommonDialog(this, "首次使用书源订阅提醒",
                    "感谢您选择风月读书，当前应用没有任何书源，" +
                            "建议前往书源订阅界面获取书源(也可自行前往书源管理导入书源)，是否前往订阅书源？",
                    false, (dialog, which) -> startActivity(new Intent(this, SourceSubscribeActivity.class)),
                    null);
            sru.putBoolean("firstInit", true);
        }
    }

    private void reLoadFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        mBookcaseFragment = (BookcaseFragment) fragments.get(0);
        mFindFragment = (FindFragment) fragments.get(1);
        mMineFragment = (MineFragment) fragments.get(2);
    }

    public NoScrollViewPager getViewPagerMain() {
        return binding.viewPagerMain;
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
        if (binding.viewPagerMain.getCurrentItem() == 0) {
            if (mBookcaseFragment.getmBookcasePresenter() != null && mBookcaseFragment.getmBookcasePresenter().ismEditState()) {
                menu.findItem(R.id.action_finish).setVisible(true);
                menu.setGroupVisible(R.id.bookcase_menu, false);
            } else {
                menu.setGroupVisible(R.id.bookcase_menu, true);
                menu.findItem(R.id.action_finish).setVisible(false);
                menu.findItem(R.id.action_change_group).setVisible(SharedPreUtils
                        .getInstance().getBoolean("openGroup", true));
            }
        } else {
            menu.setGroupVisible(R.id.bookcase_menu, false);
            menu.findItem(R.id.action_finish).setVisible(false);
        }
        menu.setGroupVisible(R.id.find_menu, binding.viewPagerMain.getCurrentItem() == 1);
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
        int itemId = item.getItemId();
        if (itemId == R.id.action_search) {
            Intent searchBookIntent = new Intent(this, SearchBookActivity.class);
            startActivity(searchBookIntent);
            return true;
        } else if (itemId == R.id.action_finish) {
            cancelEdit();
            return true;
        } else if (itemId == R.id.action_change_group || itemId == R.id.action_group_man) {
            if (!mBookcaseFragment.getmBookcasePresenter().hasOnGroupChangeListener()) {
                mBookcaseFragment.getmBookcasePresenter().addOnGroupChangeListener(() -> {
                    groupName = SharedPreUtils.getInstance().getString(getString(R.string.curBookGroupName), "所有书籍");
                    getSupportActionBar().setSubtitle(groupName);
                });
            }
        } else if (itemId == R.id.action_edit) {
            if (mBookcaseFragment.getmBookcasePresenter().canEditBookcase()) {
                invalidateOptionsMenu();
                initMenuAnim();
                binding.bottomNavigationView.setVisibility(View.GONE);
                binding.bottomNavigationView.startAnimation(mBottomOutAnim);
            }
        } else if (itemId == R.id.action_qr_scan) {
            Intent intent = new Intent(this, QRCodeScanActivity.class);
            startActivityForResult(intent, APPCONST.REQUEST_QR_SCAN);
        } else if (itemId == R.id.action_refresh_find) {
            mFindFragment.refreshFind();
            return true;
        }
        return mBookcaseFragment.getmBookcasePresenter().onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mBookcaseFragment.getmBookcasePresenter() != null && mBookcaseFragment.getmBookcasePresenter().ismEditState()) {
            cancelEdit();
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
        String curBookGroupId = SharedPreUtils.getInstance().getString(this.getString(R.string.curBookGroupId), "");
        BookGroup bookGroup = BookGroupService.getInstance().getGroupById(curBookGroupId);
        if (bookGroup == null) {
            groupName = "";
        } else {
            groupName = bookGroup.getName();
        }
        if (binding.viewPagerMain.getCurrentItem() == 0) {
            getSupportActionBar().setSubtitle(groupName);
        }
//        App.checkVersionByServer(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BackupRestoreUi.INSTANCE.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK || resultCode == RESULT_CANCELED) && requestCode == APPCONST.APP_INSTALL_CODE) {
            installProcess(appFile, isForceUpdate);//再次执行安装流程，包含权限判等
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case APPCONST.REQUEST_LOGIN:
                    if (mMineFragment.isRecreate()) {
                        reLoadFragment();
                    }
                    mMineFragment.onActivityResult(requestCode, resultCode, data);
                    break;
                case APPCONST.REQUEST_QR_SCAN:
                    if (data != null) {
                        String result = data.getStringExtra("result");
                        if (!StringHelper.isEmpty(result)) {
                            String[] string = result.split("#", 2);
                            if (string.length == 2) {
                                SharedBook sharedBook = GsonExtensionsKt.getGSON().fromJson(string[1], SharedBook.class);
                                if (sharedBook != null && !StringHelper.isEmpty(sharedBook.getChapterUrl())) {
                                    Book book = SharedBook.sharedBookToBook(sharedBook);
                                    Intent intent = new Intent(this, BookDetailedActivity.class);
                                    BitIntentDataManager.getInstance().putData(intent, book);
                                    startActivity(intent);
                                } else {
                                    ToastUtils.showError("书籍加载失败");
                                }
                            } else {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri.parse(result);
                                    intent.setData(uri);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    ToastUtils.showError(e.getLocalizedMessage());
                                }
                            }
                        }
                    }
                    break;
                case APPCONST.REQUEST_GROUP_MANAGER:
                    invalidateOptionsMenu();
                    break;
            }
        }
    }


    @Override
    protected void onDestroy() {
        App.getApplication().shutdownThreadPool();
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

    private void goPrivateBookcase() {
        MyAlertDialog.showPrivateVerifyDia(this, needGoTo -> {
            if (needGoTo) showPrivateBooks();
        });
    }

    private void goBackNormalBookcase() {
        DialogCreator.createCommonDialog(this, "退出私密书架",
                "确定要退出私密书架吗？", true, (dialog, which) -> {
                    groupName = "";
                    SharedPreUtils.getInstance().putString(getString(R.string.curBookGroupId), "");
                    SharedPreUtils.getInstance().putString(getString(R.string.curBookGroupName), groupName);
                    getSupportActionBar().setSubtitle("");
                    if (mBookcaseFragment.isRecreate()) {
                        reLoadFragment();
                    }
                    mBookcaseFragment.init();
                }, null);
    }

    /**
     * 显示私密书架
     */
    private void showPrivateBooks() {
        BookGroup bookGroup = BookGroupService.getInstance().
                getGroupById(SharedPreUtils.getInstance().getString("privateGroupId"));
        groupName = bookGroup.getName();
        SharedPreUtils.getInstance().putString(getString(R.string.curBookGroupId), bookGroup.getId());
        SharedPreUtils.getInstance().putString(getString(R.string.curBookGroupName), groupName);
        getSupportActionBar().setSubtitle(groupName);
        if (mBookcaseFragment.isRecreate()) {
            reLoadFragment();
        }
        mBookcaseFragment.init();
    }

    /**
     * 取消编辑状态
     */
    private void cancelEdit() {
        mBookcaseFragment.getmBookcasePresenter().cancelEdit();
        invalidateOptionsMenu();
        initMenuAnim();
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
        binding.bottomNavigationView.startAnimation(mBottomInAnim);
    }

    //初始化菜单动画
    public void initMenuAnim() {
        if (mBottomInAnim != null) return;
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
    }

    public Animation getmBottomInAnim() {
        return mBottomInAnim;
    }

    public Animation getmBottomOutAnim() {
        return mBottomOutAnim;
    }

    public interface OnGroupChangeListener {
        void onChange();
    }
}
