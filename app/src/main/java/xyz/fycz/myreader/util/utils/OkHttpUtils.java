package xyz.fycz.myreader.util.utils;

import android.util.Log;
import okhttp3.*;
import xyz.fycz.myreader.application.TrustAllCerts;
import xyz.fycz.myreader.util.HttpUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

public class OkHttpUtils {

    public static OkHttpClient okHttpClient = HttpUtil.getOkHttpClient();

    /**
     * 同步获取html文件，默认编码utf-8
     */
    public static String getHtml(String url) throws IOException {
        return getHtml(url, "utf-8");
    }
    public static String getHtml(String url, String encodeType) throws IOException {
        return getHtml(url, null, encodeType);
    }

    public static String getHtml(String url, RequestBody requestBody, String encodeType) throws IOException {

        Request.Builder builder = new Request.Builder()
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                //.addHeader("Charsert", "utf-8")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4168.3 Safari/537.36");
        if (requestBody != null) {
            builder.post(requestBody);
            Log.d("HttpPost URl", url);
        }else {
            Log.d("HttpGet URl", url);
        }
        Request request = builder
                .url(url)
                .build();
        Response response = okHttpClient
                .newCall(request)
                .execute();
        ResponseBody body=response.body();
        if (body == null) {
            return "";
        } else {
            String bodyStr = new String(body.bytes(), encodeType);
            Log.d("Http: read finish", bodyStr);
            return bodyStr;
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    public static InputStream getInputStream(String url) throws IOException {
        Request.Builder builder = new Request.Builder()
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4168.3 Safari/537.36");
        Request request = builder
                .url(url)
                .build();
        Response response = okHttpClient
                .newCall(request)
                .execute();
        if (response.body() == null){
            return null;
        }
        return response.body().byteStream();
    }
}
