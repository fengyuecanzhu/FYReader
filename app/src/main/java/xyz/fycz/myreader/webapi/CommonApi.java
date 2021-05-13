package xyz.fycz.myreader.webapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.TianLaiReadCrawler;


public class CommonApi {


    /**
     * 获取章节列表
     *
     * @param url
     */
    public static Observable<List<Chapter>> getBookChapters(String url, final ReadCrawler rc) {
        String charset = rc.getCharset();
        url = NetworkUtils.getAbsoluteURL(rc.getNameSpace(), url);
        String finalUrl = url;
        return Observable.create(emitter -> {
            emitter.onNext(rc.getChaptersFromHtml(OkHttpUtils.getHtml(finalUrl, charset)));
            emitter.onComplete();
        });
    }

    /**
     * 获取章节正文
     *
     * @param url
     */

    public static Observable<String> getChapterContent(String url, final ReadCrawler rc) {
        String charset = rc.getCharset();
        url = NetworkUtils.getAbsoluteURL(rc.getNameSpace(), url);
        String finalUrl = url;
        return Observable.create(emitter -> {
            emitter.onNext(rc.getContentFormHtml(OkHttpUtils.getHtml(finalUrl, charset)));
            emitter.onComplete();
        });
    }


    /**
     * 搜索小说
     *
     * @param key
     */

    public static Observable<ConcurrentMultiValueMap<SearchBookBean, Book>> search(String key, final ReadCrawler rc) {
        String charset = "utf-8";
        if (rc instanceof TianLaiReadCrawler) {
            charset = "utf-8";
        } else {
            charset = rc.getCharset();
        }
        String finalCharset = charset;
        return Observable.create(emitter -> {
            try {
                if (rc.isPost()) {
                    String url = rc.getSearchLink();
                    String[] urlInfo = url.split(",");
                    url = urlInfo[0];
                    /*String[] bodies = makeSearchUrl(urlInfo[1], key).split("&");
                    FormBody.Builder formBody = new FormBody.Builder();
                    for (String body : bodies) {
                        String[] kv = body.split("=");
                        formBody.add(kv[0], kv[1]);
                    }
                    RequestBody requestBody = formBody.build();*/
                    String body = makeSearchUrl(urlInfo[1], key);
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody requestBody = RequestBody.create(mediaType, body);
                    if (rc.getNameSpace().contains("soxs.cc") ||
                            rc.getNameSpace().contains("xinshuhaige.org") ||
                            rc.getNameSpace().contains("bxwxorg.com") ||
                            rc.getNameSpace().contains("soshuw.com")) {
                        String cookie = "Hm_lvt_46329db612a10d9ae3a668a40c152e0e=1612793811,1612795781,1613200980,1613218588; "
                                + "__cfduid=d0ebd0275436b7b0c3ccf4c9eb7394abd1619231977 ";
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", cookie);
                        emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(url, requestBody, finalCharset, headers)));
                    } else {
                        emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(url, requestBody, finalCharset)));
                    }
                } else {
                    emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(makeSearchUrl(rc.getSearchLink(), key), finalCharset)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }

    public static String makeSearchUrl(String url, String key) {
        return url.replace("{key}", key);
    }

    /**
     * 获取小说详细信息
     *
     * @param book
     */
    public static Observable<Book> getBookInfo(final Book book, final BookInfoCrawler bic) {
        String url;
        if (StringHelper.isEmpty(book.getInfoUrl())) {
            url = book.getChapterUrl();
        } else {
            url = book.getInfoUrl();
        }
        url = NetworkUtils.getAbsoluteURL(bic.getNameSpace(), url);
        String finalUrl = url;
        return Observable.create(emitter -> {
            emitter.onNext(bic.getBookInfo(OkHttpUtils.getHtml(finalUrl, bic.getCharset()), book));
            emitter.onComplete();
        });
    }

    /**
     * 通过api获取蓝奏云可下载直链
     *
     * @param lanZouUrl
     * @param callback
     */
    public static void getUrl(final String lanZouUrl, final ResultCallback callback) {
        LanZousApi.getUrl1(lanZouUrl, new ResultCallback() {
            @Override
            public void onFinish(final Object o, int code) {
                LanZousApi.getKey((String) o, new ResultCallback() {
                    final String referer = (String) o;

                    @Override
                    public void onFinish(Object o, int code) {
                        LanZousApi.getUrl2((String) o, new ResultCallback() {
                            @Override
                            public void onFinish(Object o, int code) {
                                LanZousApi.getRedirectUrl((String) o, callback);
                            }

                            @Override
                            public void onError(Exception e) {
                                callback.onError(e);
                            }
                        }, referer);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });

    }


}
