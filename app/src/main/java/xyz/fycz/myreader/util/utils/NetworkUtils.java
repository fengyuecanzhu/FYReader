//Copyright (c) 2017. 章钦豪. All rights reserved.
package xyz.fycz.myreader.util.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import xyz.fycz.myreader.application.MyApplication;


import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {
    public static final int SUCCESS = 10000;
    public static final int ERROR_CODE_NONET = 10001;
    public static final int ERROR_CODE_OUTTIME = 10002;
    public static final int ERROR_CODE_ANALY = 10003;
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, String> errorMap = new HashMap<>();

    static {
        errorMap.put(ERROR_CODE_NONET, "没有网络");
        errorMap.put(ERROR_CODE_OUTTIME, "网络连接超时");
        errorMap.put(ERROR_CODE_ANALY, "数据解析失败");
    }

    public static String getErrorTip(int code) {
        return errorMap.get(code);
    }

    public static boolean isNetWorkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) MyApplication.getmContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } else {
            return false;
        }
    }



    public static boolean isUrl(String urlStr) {
        String regex = "^(https?)://.+$";//设置正则表达式
        return urlStr.matches(regex);
    }
}
