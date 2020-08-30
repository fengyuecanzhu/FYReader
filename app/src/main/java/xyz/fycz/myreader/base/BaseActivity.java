package xyz.fycz.myreader.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import xyz.fycz.myreader.ActivityManage;
import xyz.fycz.myreader.util.Anti_hijackingUtils;
import xyz.fycz.myreader.util.StatusBarUtil;



public class BaseActivity extends AppCompatActivity {

    public static int width = 0;
    public static int height = 0;
    public static boolean home;
    public static boolean back;
    private boolean catchHomeKey = false;
    private boolean disallowAntiHijacking;//暂停防界面劫持

    private  static boolean closeAntiHijacking;//关闭防界面劫持


    private InputMethodManager mInputMethodManager; //输入管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //将每一个Activity都加入activity管理器
        ActivityManage.addActivity(this);
        Log.d("ActivityName: ",getLocalClassName());
        DisplayMetrics dm = new DisplayMetrics();
        //获取屏幕宽高
        if(height == 0 || width == 0){
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            width = dm.widthPixels;
            height = dm.heightPixels;
        }

        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static void setCloseAntiHijacking(boolean closeAntiHijacking) {
        BaseActivity.closeAntiHijacking = closeAntiHijacking;
    }

    @Override
    protected void onDestroy() {
        ActivityManage.removeActivity(this);
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        if (!disallowAntiHijacking && !closeAntiHijacking) {
            Anti_hijackingUtils.getinstance().onPause(this);//防界面劫持提示任务
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!closeAntiHijacking) {
            Anti_hijackingUtils.getinstance().onResume(this);//注销防界面劫持提示任务
        }
        BaseActivity.home = false;
        BaseActivity.back = false;
        disallowAntiHijacking = false;
        super.onResume();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            back = true;//以便于判断是否按返回键触发界面劫持提示
        }
        return super.onKeyDown(keyCode, event);
    }


    public void setDisallowAntiHijacking(boolean disallowAntiHijacking) {
        this.disallowAntiHijacking = disallowAntiHijacking;
    }



    /**
     * 设置状态栏颜色
     * @param colorId
     */
    public void setStatusBar(int colorId, boolean dark){
        //沉浸式代码配置
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        if (colorId != 0) {
            StatusBarUtil.setStatusBarColor(this, getResources().getColor(colorId));
        }
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

    public InputMethodManager getmInputMethodManager() {
        return mInputMethodManager;
    }

}
