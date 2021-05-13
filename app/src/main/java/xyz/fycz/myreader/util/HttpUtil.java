package xyz.fycz.myreader.util;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import okhttp3.*;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.webapi.callback.HttpCallback;
import xyz.fycz.myreader.webapi.callback.JsonCallback;
import xyz.fycz.myreader.webapi.callback.URLConnectionCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.entity.JsonModel;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static java.lang.String.valueOf;


public class HttpUtil {

    private static String sessionid;
    //最好只使用一个共享的OkHttpClient 实例，将所有的网络请求都通过这个实例处理。
    //因为每个OkHttpClient 实例都有自己的连接池和线程池，重用这个实例能降低延时，减少内存消耗，而重复创建新实例则会浪费资源。
    private static OkHttpClient mClient;

    public static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }
    static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
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

    private static Interceptor getHeaderInterceptor() {
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

    public static synchronized OkHttpClient getOkHttpClient() {
        if (mClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .sslSocketFactory(createSSLSocketFactory(), createTrustAllManager())
                    .hostnameVerifier((hostname, session) -> true)
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .addInterceptor(getHeaderInterceptor());
            mClient = builder
                    .build();
        }
        return mClient;

    }


    /**
     * 图片发送
     *
     * @param address
     * @param callback
     */
    public static void sendBitmapGetRequest(final String address, final HttpCallback callback) {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-type", "application/octet-stream");
                    connection.setRequestProperty("Accept-Charset", "utf-8");
                    connection.setRequestProperty("contentType", "utf-8");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    InputStream in = connection.getInputStream();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e("Http", "网络错误异常！!!!");
                    }
                    Log.d("Http", "connection success");
                    if (callback != null) {
                        callback.onFinish(in);
                    }
                } catch (Exception e) {
                    Log.e("Http", e.toString());
                    if (callback != null) {
                        callback.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

        }).start();
    }

    /**
     * get请求
     *
     * @param address
     * @param callback
     */
    public static void sendGetRequest(final String address, final HttpCallback callback) {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-type", "text/html");
                    connection.setRequestProperty("Accept-Charset", "utf-8");
                    connection.setRequestProperty("contentType", "utf-8");
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e("Http", "网络错误异常！!!!");
                    }
                    InputStream in = connection.getInputStream();
                    Log.d("Http", "connection success");
                    if (callback != null) {
                        callback.onFinish(in);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Http", e.toString());
                    if (callback != null) {
                        callback.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    /**
     * 网络通信测试请求
     *
     * @param address
     * @param callback
     */
    public static void sendTestGetRequest(final String address, final HttpCallback callback) {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-type", "text/html");
                    connection.setRequestProperty("Accept-Charset", "utf-8");
                    connection.setRequestProperty("contentType", "utf-8");
                    connection.setConnectTimeout(3 * 1000);
                    connection.setReadTimeout(3 * 1000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e("Http", "网络错误异常！!!!");
                    }
                    InputStream in = connection.getInputStream();
                    Log.d("Http", "connection success");
                    if (callback != null) {
                        callback.onFinish(in);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Http", e.toString());
                    if (callback != null) {
                        callback.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void sendGetRequest_okHttp(final String address, boolean isRefresh, final HttpCallback callback) {
        App.getApplication().newThread(() -> {
            try {
                OkHttpClient client = getOkHttpClient();
                Request.Builder requestBuilder = new Request.Builder()
                        .addHeader("Accept", "*/*")
                        .addHeader("Connection", "Keep-Alive")
                        //.addHeader("Charsert", "utf-8")
                        .addHeader("Cache-Control", "no-cache")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36");
                if (address.contains("qidian.com")) {
                    SharedPreUtils spu = SharedPreUtils.getInstance();
                    String cookie = spu.getString(App.getmContext().getString(R.string.qdCookie), "");
                    if (cookie.equals("")) {
                        requestBuilder.addHeader("cookie", "_csrfToken=eXRDlZxmRDLvFAmdgzqvwWAASrxxp2WkVlH4ZM7e; newstatisticUUID=1595991935_2026387981");
                    } else {
                        requestBuilder.addHeader("cookie", cookie);
                    }
                }
                requestBuilder.url(address);
                Response response = client.newCall(requestBuilder.build()).execute();
                callback.onFinish(response.body().byteStream());
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e);
            }
        });
    }

    /**
     * post请求
     *
     * @param address
     * @param output
     * @param callback
     */
    public static void sendPostRequest(final String address, final String output, final HttpCallback callback) {
        App.getApplication().newThread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.setRequestProperty("user-agent",
                            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4168.3 Safari/537.36");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    if (output != null) {
                        // 获取URLConnection对象对应的输出流
                        PrintWriter out = new PrintWriter(connection.getOutputStream());
                        // 发送请求参数
                        out.print(output);
                        // flush输出流的缓冲
                        out.flush();
                    }
                    InputStream in = connection.getInputStream();
                    if (callback != null) {
                        callback.onFinish(in);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }

    /**
     * post请求 获取蓝奏云直链
     *
     * @param address
     * @param output
     * @param callback
     */
    public static void sendPostRequest(final String address, final String output, final HttpCallback callback, final String referer) {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.setRequestProperty("Referer", referer);
                    connection.setRequestProperty("user-agent",
                            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4168.3 Safari/537.36");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    if (output != null) {
                        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                        out.writeBytes(output);
                    }
                    InputStream in = connection.getInputStream();
                    if (callback != null) {
                        callback.onFinish(in);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    /**
     * 生成URL
     *
     * @param p_url
     * @param params
     * @return
     */
    public static String makeURL(String p_url, Map<String, Object> params) {
        if (params == null) return p_url;
        StringBuilder url = new StringBuilder(p_url);
        Log.d("http", p_url);
        if (url.indexOf("?") < 0)
            url.append('?');
        for (String name : params.keySet()) {
            Log.d("http", name + "=" + params.get(name));
            url.append('&');
            url.append(name);
            url.append('=');
            try {
                if (URLCONST.isRSA) {
                    if (name.equals("token")) {
                        url.append(valueOf(params.get(name)));
                    } else {
                        url.append(StringHelper.encode(Base64.encodeToString(RSAUtilV2.encryptByPublicKey(valueOf(params.get(name)).getBytes(), APPCONST.publicKey), Base64.DEFAULT).replace("\n", "")));
                    }
                } else {
                    url.append(valueOf(params.get(name)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //不做URLEncoder处理
//			try {
//				url.append(URLEncoder.encode(String.valueOf(params.get(name)), UTF_8));
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        }
        return url.toString().replace("?&", "?");
    }


    /**
     * 生成post输出参数串
     *
     * @param params
     * @return
     */
    public static String makePostOutput(Map<String, Object> params) {
        StringBuilder output = new StringBuilder();
        Iterator<String> it = params.keySet().iterator();
        while (true) {
            String name = it.next();
            Log.d("http", name + "=" + params.get(name));
            output.append(name);
            output.append('=');
            try {
                if (URLCONST.isRSA) {
                    if (name.equals("token")) {
                        output.append(valueOf(params.get(name)));
                    } else {
                        output.append(StringHelper.encode(Base64.encodeToString(RSAUtilV2.encryptByPublicKey(valueOf(params.get(name)).getBytes(), APPCONST.publicKey), Base64.DEFAULT).replace("\n", "")));
                    }
                } else {
                    output.append(valueOf(params.get(name)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!it.hasNext()) {
                break;
            }
            output.append('&');
            //不做URLEncoder处理
//			try {
//				url.append(URLEncoder.encode(String.valueOf(params.get(name)), UTF_8));
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        }
        return output.toString();
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
