/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.util.help;

import android.annotation.SuppressLint;
import android.util.Log;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.Request;


public class SSLSocketClient {

    public static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{createTrustAllManager()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    public static X509TrustManager createTrustAllManager() {
        X509TrustManager tm = null;
        try {
            tm = new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    //do nothing，接受任意客户端证书
                }

                @SuppressLint("TrustAllX509TrustManager")
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    //do nothing，接受任意服务端证书
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
        } catch (Exception ignored) {
        }
        return tm;
    }

    public static Interceptor getHeaderInterceptor() {
        return chain -> {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("Keep-Alive", "300")
                    .addHeader("Connection", "Keep-Alive")
                    .addHeader("Cache-Control", "no-cache")
                    .build();
            return chain.proceed(request);
        };
    }

    /**
     * Trust every server - dont check for any certificate
     */
    public static void trustAllHosts() {
        final String TAG = "trustAllHosts";
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkClientTrusted");
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkServerTrusted");
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
