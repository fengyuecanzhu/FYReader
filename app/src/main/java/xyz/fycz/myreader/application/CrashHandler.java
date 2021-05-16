package xyz.fycz.myreader.application;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.activity.SplashActivity;
import xyz.fycz.myreader.util.utils.FileUtils;

/**
 * @author fengyue
 * @date 2021/5/16 11:01
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    /**
     * 注册 Crash 监听
     */
    public static void register(Application application) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(application));
    }

    private final Application mApplication;
    private final Thread.UncaughtExceptionHandler mNextHandler;

    private CrashHandler(Application application) {
        mApplication = application;
        mNextHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (getClass().getName().equals(mNextHandler.getClass().getName())) {
            // 请不要重复注册 Crash 监听
            throw new IllegalStateException("are you ok?");
        }
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {

        saveCrashLog(throwable);

        // 不去触发系统的崩溃处理（com.android.internal.os.RuntimeInit$KillApplicationHandler）
        if (mNextHandler != null && !mNextHandler.getClass().getName().startsWith("com.android.internal.os")) {
            mNextHandler.uncaughtException(thread, throwable);
        }

        // 杀死进程（这个事应该是系统干的，但是它会多弹出一个崩溃对话框，所以需要我们自己手动杀死进程）
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private void saveCrashLog(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        if (cause != null) {
            cause.printStackTrace(printWriter);
        }
        String mStackTrace = stringWriter.toString();

        Resources res = mApplication.getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        String targetResource;
        if (displayMetrics.densityDpi > 480) {
            targetResource = "xxxhdpi";
        } else if (displayMetrics.densityDpi > 320) {
            targetResource = "xxhdpi";
        } else if (displayMetrics.densityDpi > 240) {
            targetResource = "xhdpi";
        } else if (displayMetrics.densityDpi > 160) {
            targetResource = "hdpi";
        } else if (displayMetrics.densityDpi > 120) {
            targetResource = "mdpi";
        } else {
            targetResource = "ldpi";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("设备品牌：\t").append(Build.BRAND)
                .append("\n设备型号：\t").append(Build.MODEL)
                .append("\n设备类型：\t").append(isTablet() ? "平板" : "手机");

        builder.append("\n屏幕宽高：\t").append(screenWidth).append(" x ").append(screenHeight)
                .append("\n屏幕密度：\t").append(displayMetrics.densityDpi)
                .append("\n目标资源：\t").append(targetResource);

        builder.append("\n安卓版本：\t").append(Build.VERSION.RELEASE)
                .append("\nAPI 版本：\t").append(Build.VERSION.SDK_INT)
                .append("\nCPU 架构：\t").append(Build.SUPPORTED_ABIS[0]);

        builder.append("\n应用版本：\t").append(App.getStrVersionName())
                .append("\n版本代码：\t").append(App.getVersionCode());

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            PackageInfo packageInfo = mApplication.getPackageManager().getPackageInfo(mApplication.getPackageName(), PackageManager.GET_PERMISSIONS);
            builder.append("\n首次安装：\t").append(dateFormat.format(new Date(packageInfo.firstInstallTime)))
                    .append("\n最近安装：\t").append(dateFormat.format(new Date(packageInfo.lastUpdateTime)))
                    .append("\n崩溃时间：\t").append(dateFormat.format(new Date()));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        String mPhoneInfo = builder.toString();

        long timestamp = System.currentTimeMillis();
        String time = formatter.format(new Date());
        String fileName = "crash-" + time + "-" + timestamp + ".log";
        String path = APPCONST.LOG_DIR + fileName;
        FileUtils.writeText(mPhoneInfo + "\n\n" + mStackTrace, FileUtils.getFile(path));
    }

    /**
     * 判断当前设备是否是平板
     */
    public boolean isTablet() {
        return (mApplication.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
