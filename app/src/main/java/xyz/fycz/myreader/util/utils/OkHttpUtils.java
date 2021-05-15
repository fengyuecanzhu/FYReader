package xyz.fycz.myreader.util.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.reactivex.Observable;
import okhttp3.*;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.CookieBean;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.third.analyzeRule.AnalyzeUrl;

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
        Response response = getResponse(url, requestBody, headers);
        ResponseBody body = response.body();
        if (body == null) {
            return "";
        } else {
            String bodyStr = new String(body.bytes(), encodeType);
            Log.d("Http: read finish", bodyStr);
            return bodyStr;
        }
    }

    /**
     * 同步获取StrResponse
     */
    public static StrResponse getStrResponse(String url, String encodeType, Map<String, String> headers) throws IOException {
        return getStrResponse(url, null, encodeType, headers);
    }

    public static StrResponse getStrResponse(String url, RequestBody requestBody, String encodeType, Map<String, String> headers) throws IOException {
        StrResponse strResponse = new StrResponse();
        strResponse.setEncodeType(encodeType);
        strResponse.setResponse(getResponse(url, requestBody, headers));
        return strResponse;
    }

    public static Response getResponse(String url, RequestBody requestBody, Map<String, String> headers) throws IOException {
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
        return getOkHttpClient()
                .newCall(request)
                .execute();
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

    public static Observable<StrResponse> getStrResponse(AnalyzeUrl analyzeUrl) {
        return Observable.create(emitter -> {
            StringBuilder sb = new StringBuilder();
            for (String key : analyzeUrl.getQueryMap().keySet()) {
                sb.append(key).append("=").append(analyzeUrl.getQueryMap().get(key));
                sb.append("&");
            }
            int index = sb.lastIndexOf("&");
            if (index != -1) {
                sb.deleteCharAt(index);
            }
            String body = sb.toString();
            StrResponse strResponse = new StrResponse();
            //strResponse.setEncodeType(analyzeUrl.getCharCode() != null ? analyzeUrl.getCharCode() : "UTF-8");
            switch (analyzeUrl.getUrlMode()) {
                case GET:
                    String url = analyzeUrl.getUrl() + "?" + body;
                    strResponse.setResponse(OkHttpUtils.getResponse(url, null, analyzeUrl.getHeaderMap()));
                    break;
                case POST:
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody requestBody = RequestBody.create(mediaType, body);
                    strResponse.setResponse(OkHttpUtils.getResponse(analyzeUrl.getUrl(), requestBody, analyzeUrl.getHeaderMap()));
                    break;
                default:
                    strResponse.setResponse(OkHttpUtils.getResponse(analyzeUrl.getUrl(), null, analyzeUrl.getHeaderMap()));
                    break;
            }
            emitter.onNext(strResponse);
        });
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    public static Observable<String> getAjaxString(AnalyzeUrl analyzeUrl, String tag, String js) {
        final Web web = new Web("加载超时");
        if (!TextUtils.isEmpty(js)) {
            web.js = js;
        }
        return Observable.create(e -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Runnable timeoutRunnable;
                WebView webView = new WebView(App.getmContext());
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setUserAgentString(analyzeUrl.getHeaderMap().get("User-Agent"));
                CookieManager cookieManager = CookieManager.getInstance();
                Runnable retryRunnable = new Runnable() {
                    @Override
                    public void run() {
                        webView.evaluateJavascript(web.js, value -> {
                            if (!TextUtils.isEmpty(value)) {
                                web.content = StringEscapeUtils.unescapeJson(value);
                                e.onNext(web.content);
                                e.onComplete();
                                webView.destroy();
                                handler.removeCallbacks(this);
                            } else {
                                handler.postDelayed(this, 1000);
                            }
                        });
                    }
                };
                timeoutRunnable = () -> {
                    if (!e.isDisposed()) {
                        handler.removeCallbacks(retryRunnable);
                        e.onNext(web.content);
                        e.onComplete();
                        webView.destroy();
                    }
                };
                handler.postDelayed(timeoutRunnable, 30000);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        DbManager.getDaoSession().getCookieBeanDao()
                                .insertOrReplace(new CookieBean(tag, cookieManager.getCookie(webView.getUrl())));
                        handler.postDelayed(retryRunnable, 1000);
                    }
                });
                switch (analyzeUrl.getUrlMode()) {
                    case POST:
                        webView.postUrl(analyzeUrl.getUrl(), analyzeUrl.getPostData());
                        break;
                    case GET:
                        webView.loadUrl(String.format("%s?%s", analyzeUrl.getUrl(), analyzeUrl.getQueryStr()), analyzeUrl.getHeaderMap());
                        break;
                    default:
                        webView.loadUrl(analyzeUrl.getUrl(), analyzeUrl.getHeaderMap());
                }
            });
        });
    }

    public static Observable<StrResponse> setCookie(StrResponse response, String tag) {
        return Observable.create(e -> {
            if (!response.getResponse().headers("Set-Cookie").isEmpty()) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (String s : response.getResponse().headers("Set-Cookie")) {
                    String[] x = s.split(";");
                    for (String y : x) {
                        if (!TextUtils.isEmpty(y)) {
                            cookieBuilder.append(y).append(";");
                        }
                    }
                }
                String cookie = cookieBuilder.toString();
                if (!TextUtils.isEmpty(cookie)) {
                    DbManager.getDaoSession().getCookieBeanDao().insertOrReplace(new CookieBean(tag, cookie));
                }
            }
            e.onNext(response);
            e.onComplete();
        });
    }

    private static class Web {
        private String content;
        private String js = "document.documentElement.outerHTML";

        Web(String content) {
            this.content = content;
        }
    }
}
