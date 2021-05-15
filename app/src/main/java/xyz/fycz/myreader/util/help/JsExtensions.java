package xyz.fycz.myreader.util.help;


import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeUrl;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

@SuppressWarnings({"unused", "WeakerAccess"})
public interface JsExtensions {

    /**
     * js实现跨域访问,不能删
     */
    default String ajax(String urlStr) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(urlStr);
            return OkHttpUtils.getStrResponse(analyzeUrl).blockingFirst().body();
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    /**
     * js实现解码,不能删
     */
    default String base64Decoder(String base64) {
        return StringUtils.base64Decode(base64);
    }

    /**
     * 章节数转数字
     */
    default String toNumChapter(String s) {
        if (s == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("(第)(.+?)(章)");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group(1) + StringUtils.stringToInt(matcher.group(2)) + matcher.group(3);
        }
        return s;
    }

    /**
     * js实现重定向拦截,不能删
     */
    default Connection.Response get(String urlStr, Map<String, String> headers) throws IOException {
        return Jsoup.connect(urlStr)
                .sslSocketFactory(SSLSocketClient.createSSLSocketFactory())
                .ignoreContentType(true)
                .followRedirects(false)
                .headers(headers)
                .method(Connection.Method.GET)
                .execute();
    }

    /**
     * js实现重定向拦截,不能删
     */
    default Connection.Response post(String urlStr, String body, Map<String, String> headers) throws IOException {
        return Jsoup.connect(urlStr)
                .sslSocketFactory(SSLSocketClient.createSSLSocketFactory())
                .ignoreContentType(true)
                .followRedirects(false)
                .requestBody(body)
                .headers(headers)
                .method(Connection.Method.POST)
                .execute();
    }

    /**
     * 时间格式化
     */
    default String timeFormat(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return sdf.format(new Date(time));
    }
}
