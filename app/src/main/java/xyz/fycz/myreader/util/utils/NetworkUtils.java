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
package xyz.fycz.myreader.util.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import okhttp3.Response;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.util.help.StringHelper;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NetworkUtils {
    public static final Pattern headerPattern = Pattern.compile("@Header:\\{.+?\\}", Pattern.CASE_INSENSITIVE);

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
        ConnectivityManager manager = (ConnectivityManager) App.getmContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } else {
            return false;
        }
    }

    public static String getUrl(Response response) {
        okhttp3.Response networkResponse = response.networkResponse();
        if (networkResponse != null) {
            return networkResponse.request().url().toString();
        } else {
            return response.request().url().toString();
        }
    }

    /**
     * 获取绝对地址
     */
    public static String getAbsoluteURL(String baseURL, String relativePath) {
        if (StringHelper.isEmpty(relativePath)) return "";
        if (StringHelper.isEmpty(baseURL)) return relativePath;
        if (relativePath.contains("storage")) return relativePath;
        String header = null;
        if (StringUtils.startWithIgnoreCase(relativePath, "@header:")) {
            header = relativePath.substring(0, relativePath.indexOf("}") + 1);
            relativePath = relativePath.substring(header.length());
        }
        try {
            URL absoluteUrl = new URL(baseURL);
            URL parseUrl = new URL(absoluteUrl, relativePath);
            relativePath = parseUrl.toString();
            if (header != null) {
                relativePath = header + relativePath;
            }
            return relativePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return relativePath;
    }

    public static String getAbsoluteURL(URL baseURL, String relativePath) {
        if (baseURL == null) return relativePath;
        String header = null;
        if (StringUtils.startWithIgnoreCase(relativePath, "@header:")) {
            header = relativePath.substring(0, relativePath.indexOf("}") + 1);
            relativePath = relativePath.substring(header.length());
        }
        try {
            URL parseUrl = new URL(baseURL, relativePath);
            relativePath = parseUrl.toString();
            if (header != null) {
                relativePath = header + relativePath;
            }
            return relativePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return relativePath;
    }

    public static boolean isUrl(String urlStr) {
        if (urlStr == null) return false;
        String regex = "^(https?)://.+$";//设置正则表达式
        return urlStr.matches(regex);
    }

    public static String getBaseUrl(String url) {
        if (url == null || !url.startsWith("http")) return null;
        int index = url.indexOf("/", 9);
        if (index == -1) {
            return url;
        } else return url.substring(0, index);
    }

    public static String getSubDomain(String url) {
        String baseUrl = getBaseUrl(url);
        if (baseUrl == null) return "";
        if (baseUrl.indexOf(".") == baseUrl.lastIndexOf(".")) {
            return baseUrl.substring(baseUrl.lastIndexOf("/") + 1);
        } else {
            return baseUrl.substring(baseUrl.indexOf(".") + 1);
        }
    }

    /**
     * Ipv4 address check.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(" + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                    "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    /**
     * Check if valid IPV4 address.
     *
     * @param input the address string to check for validity.
     * @return True if the input parameter is a valid IPv4 address.
     */
    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    /**
     * Get local Ip address.
     */
    public static InetAddress getLocalIPAddress() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = enumeration.nextElement();
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                if (inetAddresses != null) {
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                            return inetAddress;
                        }
                    }
                }
            }
        }
        return null;
    }
}
