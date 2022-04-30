/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.lang.reflect.Method;
import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import xyz.fycz.myreader.ActivityManage;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.application.CrashHandler;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.ui.activity.SplashActivity;
import xyz.fycz.myreader.util.StatusBarUtil;
import xyz.fycz.myreader.util.utils.AdUtils;

/**
 * @author fengyue
 * @date 2020/8/12 20:02
 */
public abstract class BaseActivity<VB> extends SwipeBackActivity {
    private static final int INVALID_VAL = -1;

    protected VB binding;

    protected static final String INTENT = "intent";

    protected CompositeDisposable mDisposable;

    protected Toolbar mToolbar;


    /****************************abstract area*************************************/
    /**
     * 绑定视图
     */
    protected abstract void bindView();

    /************************init area************************************/
    public void addDisposable(Disposable d) {
        if (mDisposable == null) {
            mDisposable = new CompositeDisposable();
        }
        mDisposable.add(d);
    }


    /**
     * 配置Toolbar
     *
     * @param toolbar
     */
    protected void setUpToolbar(Toolbar toolbar) {
    }

    /**
     * 是否开启左滑手势
     *
     * @return
     */
    protected boolean initSwipeBackEnable() {
        return true;
    }

    protected void initData(Bundle savedInstanceState) {
    }

    /**
     * 初始化零件
     */
    protected void initWidget() {

    }

    /**
     * 初始化点击事件
     */
    protected void initClick() {
    }

    /**
     * 逻辑使用区
     */
    protected void processLogic() {
    }


    /**
     * @return 是否夜间模式
     */
    protected boolean isNightTheme() {
        return !SysManager.getSetting().isDayStyle();
    }

    /**
     * 设置夜间模式
     *
     * @param isNightMode
     */
    protected void setNightTheme(boolean isNightMode) {
        Setting setting = SysManager.getSetting();
        setting.setDayStyle(!isNightMode);
        App.getApplication().initNightTheme();
    }


    /*************************lifecycle area*****************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initIntent(savedInstanceState);
        initTheme();
        ActivityManage.addActivity(this);
        bindView();
        setSwipeBackEnable(initSwipeBackEnable());
        initData(savedInstanceState);
        initToolbar();
        initWidget();
        initClick();
        processLogic();
    }

    private void initIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Intent intent = savedInstanceState.getParcelable(INTENT);
            if (intent != null) {
                setIntent(intent);
            }
        }
    }

    private void initToolbar() {
        //更严谨是通过反射判断是否存在Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            supportActionBar(mToolbar);
            setUpToolbar(mToolbar);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        AdUtils.backTime();
        ActivityManage.mResumeActivityCount--;
        if (ActivityManage.mResumeActivityCount <= 0
                && !App.isBackground){
            App.isBackground = true;
            Log.d("FYReader", "onActivityStarted: 应用进入后台");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActivityManage.mResumeActivityCount++;
        if (ActivityManage.mResumeActivityCount == 1 &&
                App.isBackground) {
            App.isBackground = false;
            Log.d("FYReader", "onActivityStarted: 应用进入前台");
            if (!(this instanceof SplashActivity) && AdUtils.backSplashAd()) {
                SplashActivity.start(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManage.removeActivity(this);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    /**
     * 初始化主题
     */
    public void initTheme() {
        //if (isNightTheme()) {
        //setTheme(R.style.AppNightTheme);
        /*} else {
            //curNightMode = false;
            //setTheme(R.style.AppDayTheme);
        }*/
    }

    /**************************used method area*******************************************/

    protected void startActivity(Class<? extends AppCompatActivity> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    protected ActionBar supportActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(
                (v) -> finish()
        );
        return actionBar;
    }

    protected void setStatusBarColor(int statusColor, boolean dark) {
        //沉浸式代码配置
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        StatusBarUtil.setStatusBarColor(this, getResources().getColor(statusColor));

        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!dark) {
            if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
                //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
                //这样半透明+白=灰, 状态栏的文字能看得清
                StatusBarUtil.setStatusBarColor(this, 0x55000000);
            }
        }
    }

    /**
     * 设置MENU图标颜色
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.textPrimary), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("PrivateApi")
    @SuppressWarnings("unchecked")
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            //展开菜单显示图标
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                    method = menu.getClass().getDeclaredMethod("getNonActionItems");
                    ArrayList<MenuItem> menuItems = (ArrayList<MenuItem>) method.invoke(menu);
                    if (!menuItems.isEmpty()) {
                        for (MenuItem menuItem : menuItems) {
                            Drawable drawable = menuItem.getIcon();
                            if (drawable != null) {
                                drawable.mutate();
                                drawable.setColorFilter(getResources().getColor(R.color.textPrimary), PorterDuff.Mode.SRC_ATOP);
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }

        }
        return super.onMenuOpened(featureId, menu);
    }

}
