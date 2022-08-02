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

package xyz.fycz.myreader.webapi;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;

/**
 * @author fengyue
 * @date 2020/7/1 8:48
 */
public class LanZousApi {

    /**
     * 通过api获取蓝奏云可下载直链
     *
     * @param lanZouUrl
     * @param callback
     */
    public static void getUrl(final String lanZouUrl, final ResultCallback callback) {
        getUrl(lanZouUrl, "", callback);
    }

    public static void getUrl(final String lanZouUrl, String password, final ResultCallback callback) {
        LanZouApi.INSTANCE.getFileUrl(lanZouUrl, "")
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new MyObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        callback.onFinish(s, 1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError((Exception) e);
                    }
                });
    }


    /**
     * 获取蓝奏云含有key的url
     *
     * @param lanZouUrl
     * @param callback
     */
    public static void getUrl1(String lanZouUrl, final ResultCallback callback) {
        Single.create((SingleOnSubscribe<String>) emitter -> {
            emitter.onSuccess(getUrl1(OkHttpUtils.getHtml(lanZouUrl)));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<String>() {
            @Override
            public void onSuccess(@NotNull String s) {
                callback.onFinish(s, 0);
            }

            @Override
            public void onError(Throwable e) {
                callback.onError((Exception) e);
            }
        });

    }

    /**
     * 获取key
     *
     * @param url
     * @param callback
     */
    public static void getKey(String url, final ResultCallback callback) {
        Single.create((SingleOnSubscribe<String>) emitter -> {
            emitter.onSuccess(getKey(OkHttpUtils.getHtml(url)));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<String>() {
            @Override
            public void onSuccess(@NotNull String s) {
                callback.onFinish(s, 0);
            }

            @Override
            public void onError(Throwable e) {
                callback.onError((Exception) e);
            }
        });
    }

    /**
     * 获取蓝奏云直链
     *
     * @param callback
     */
    public static void getUrl2(String key, final ResultCallback callback, final String referer) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("action", "downprocess");
        params.put("sign", key);
        params.put("ves", 1);
        Single.create((SingleOnSubscribe<String>) emitter -> {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String body = "action=downprocess&sign=" + key + "&ves=1";
            RequestBody requestBody = RequestBody.create(mediaType, body);

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Referer", referer);

            String html = OkHttpUtils.getHtml(URLCONST.LAN_ZOU_URL + "/ajaxm.php", requestBody,
                    "UTF-8", headers);
            emitter.onSuccess(getUrl2(html));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<String>() {
            @Override
            public void onSuccess(@NotNull String s) {
                callback.onFinish(s, 1);
            }

            @Override
            public void onError(Throwable e) {
                callback.onError((Exception) e);
            }
        });
    }

    private static String getUrl1(String html) {
        Document doc = Jsoup.parse(html);
        return URLCONST.LAN_ZOU_URL + doc.getElementsByClass("ifr2").attr("src");
    }

    private static String getKey(String html) {
        String lanzousKeyStart = "var pposturl = '";
        String keyName = StringHelper.getSubString(html, "'sign':", ",");
        if (keyName.endsWith("'")) {
            lanzousKeyStart = "'sign':'";
        } else {
            lanzousKeyStart = "var " + keyName + " = '";
        }
        //lanzousKeyStart = spu.getString(App.getmContext().getString(R.string.lanzousKeyStart));
        return StringHelper.getSubString(html, lanzousKeyStart, "'");
    }

    private static String getUrl2(String o) {
        String[] info = o.split(",");
        String zt = info[0].substring(info[0].indexOf(":") + 1);
        if (!"1".endsWith(zt)) {
            return null;
        }
        String dom = info[1].substring(info[1].indexOf(":") + 2, info[1].lastIndexOf("\""));
        String url = info[2].substring(info[2].indexOf(":") + 2, info[2].lastIndexOf("\""));
        dom = dom.replace("\\", "");
        url = url.replace("\\", "");
        return dom + "/file/" + url;
    }

    /**
     * 获取重定向地址
     *
     * @param path
     */
    public static void getRedirectUrl(final String path, final ResultCallback callback) {
        App.getApplication().newThread(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(path)
                        .openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(5000);
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                conn.setRequestProperty("Accept-Language", "zh-cn");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/x-silverlight, */*");
                conn.connect();
                String redirectUrl = conn.getHeaderField("Location");
//                    Log.d("D/Http: RedirectUrl", redirectUrl);
                callback.onFinish(redirectUrl, 1);
            } catch (IOException e) {
                callback.onError(e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

        });

    }
}
