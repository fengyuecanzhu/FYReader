package xyz.fycz.myreader.util.help;


import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import xyz.fycz.myreader.model.analyzeRule.AnalyzeUrl;
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
            String res = Observable.create((ObservableOnSubscribe<String>) emitter -> {
                StringBuilder sb = new StringBuilder();
                for (String key : analyzeUrl.getQueryMap().keySet()){
                    sb.append(key).append("=").append(analyzeUrl.getQueryMap().get(key));
                    sb.append("&");
                }
                sb.deleteCharAt(sb.lastIndexOf("&"));
                String body = sb.toString();
                switch (analyzeUrl.getUrlMode()){
                    case GET:
                        String url = analyzeUrl.getUrl() + "?" + body;
                        emitter.onNext(OkHttpUtils.getHtml(url, analyzeUrl.getCharCode(),
                                analyzeUrl.getHeaderMap()));
                        break;
                    case POST:
                        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                        RequestBody requestBody = RequestBody.create(mediaType, body);
                        emitter.onNext(OkHttpUtils.getHtml(analyzeUrl.getUrl(), requestBody,
                                analyzeUrl.getCharCode(), analyzeUrl.getHeaderMap()));
                        break;
                    default:
                        emitter.onNext(OkHttpUtils.getHtml(analyzeUrl.getUrl(),
                                analyzeUrl.getCharCode(), analyzeUrl.getHeaderMap()));
                        break;
                }
            }).blockingFirst();
            return res;
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


}
