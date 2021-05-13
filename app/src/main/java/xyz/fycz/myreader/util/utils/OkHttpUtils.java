package xyz.fycz.myreader.util.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import okhttp3.*;
import xyz.fycz.myreader.application.App;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static xyz.fycz.myreader.util.help.SSLSocketClient.createSSLSocketFactory;
import static xyz.fycz.myreader.util.help.SSLSocketClient.createTrustAllManager;
import static xyz.fycz.myreader.util.help.SSLSocketClient.getHeaderInterceptor;

public class OkHttpUtils {

    public static OkHttpClient mClient;

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
     * 同步获取html文件，默认编码utf-8
     */
    public static String getHtml(String url) throws IOException {
        return getHtml(url, "utf-8");
    }

    public static String getHtml(String url, String encodeType) throws IOException {
        return getHtml(url, null, encodeType);
    }

    public static String getHtml(String url, String encodeType, Map<String, String> headers) throws IOException {
        return getHtml(url, null, encodeType, headers);
    }

    public static String getHtml(String url, RequestBody requestBody, String encodeType) throws IOException {
        return getHtml(url, requestBody, encodeType, null);
    }

    public static String getHtml(String url, RequestBody requestBody, String encodeType, Map<String, String> headers) throws IOException {

        Request.Builder builder = new Request.Builder()
                .addHeader("Accept", "*/*")
                .addHeader("Connection", "Keep-Alive")
                //.addHeader("Charsert", "utf-8")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36");
        if (headers != null) {
            for (String name : headers.keySet()) {
                builder.addHeader(name, headers.get(name));
            }
        }
        if (requestBody != null) {
            builder.post(requestBody);
            Log.d("HttpPost URl", url);
        } else {
            Log.d("HttpGet URl", url);
        }
        Request request = builder
                .url(url)
                .build();
        Response response = getOkHttpClient()
                .newCall(request)
                .execute();
        ResponseBody body = response.body();
        if (body == null) {
            return "";
        } else {
            String bodyStr = new String(body.bytes(), encodeType);
            Log.d("Http: read finish", bodyStr);
            return bodyStr;
        }
    }

    public static InputStream getInputStream(String url) throws IOException {
        Request.Builder builder = new Request.Builder()
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4168.3 Safari/537.36");
        Request request = builder
                .url(url)
                .build();
        Response response = getOkHttpClient()
                .newCall(request)
                .execute();
        if (response.body() == null) {
            return null;
        }
        return response.body().byteStream();
    }

    public static String getUpdateInfo() throws IOException, JSONException {
        String key = "ryvwiq";
        if (App.isDebug()) {
            key = "sgak2h";
        }
        String url = "https://www.yuque.com/api/docs/" + key + "?book_id=19981967&include_contributors=true&include_hits=true&include_like=true&include_pager=true&include_suggests=true";
        String referer = "https://www.yuque.com/books/share/bf61f5fb-6eff-4740-ab38-749300e79306/" + key;
        Request.Builder builder = new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4168.3 Safari/537.36")
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", referer);
        Request request = builder
                .url(url)
                .build();
        Response response = getOkHttpClient()
                .newCall(request)
                .execute();
        ResponseBody body = response.body();
        if (body == null) {
            return "";
        } else {
            String bodyStr = new String(body.bytes(), StandardCharsets.UTF_8);
            JSONObject jsonObj = new JSONObject(bodyStr);
            jsonObj = jsonObj.getJSONObject("data");
            String content = jsonObj.getString("content");
            Document doc = Jsoup.parse(content);
            content = doc.text();
            Log.d("Http: UpdateInfo", content);
            return content;
        }
    }
}
