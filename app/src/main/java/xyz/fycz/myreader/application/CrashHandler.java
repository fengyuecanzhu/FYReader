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

package xyz.fycz.myreader.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.activity.CrashActivity;
import xyz.fycz.myreader.ui.activity.RestartActivity;
import xyz.fycz.myreader.util.utils.FileUtils;

/**
 * @author fengyue
 * @date 2021/5/16 11:01
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault());
    /**
     * Crash 文件名
     */
    private static final String CRASH_FILE_NAME = "crash_file";
    /**
     * Crash 时间记录
     */
    private static final String KEY_CRASH_TIME = "key_crash_time";

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

        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(CRASH_FILE_NAME, Context.MODE_PRIVATE);
        long currentCrashTime = System.currentTimeMillis();
        long lastCrashTime = sharedPreferences.getLong(KEY_CRASH_TIME, 0);
        // 记录当前崩溃的时间，以便下次崩溃时进行比对
        sharedPreferences.edit().putLong(KEY_CRASH_TIME, currentCrashTime).commit();

        String logFilePath = saveCrashLog(throwable);

        // 致命异常标记：如果上次崩溃的时间距离当前崩溃小于 5 分钟，那么判定为致命异常
        boolean deadlyCrash = currentCrashTime - lastCrashTime < 1000 * 60 * 5;
        // 如果是致命的异常，或者是调试模式下
        if (deadlyCrash || App.isDebug()) {
            CrashActivity.start(mApplication, throwable, logFilePath);
        } else {
            RestartActivity.Companion.start(mApplication);
        }

        // 不去触发系统的崩溃处理（com.android.internal.os.RuntimeInit$KillApplicationHandler）
        if (mNextHandler != null && !mNextHandler.getClass().getName().startsWith("com.android.internal.os")) {
            mNextHandler.uncaughtException(thread, throwable);
        }
        // 杀死进程（这个事应该是系统干的，但是它会多弹出一个崩溃对话框，所以需要我们自己手动杀死进程）
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private String saveCrashLog(Throwable throwable) {
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
        File file = FileUtils.getFile(path);
        FileUtils.writeText(mPhoneInfo + "\n\n" + mStackTrace, file);
        return file.getAbsolutePath();
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
