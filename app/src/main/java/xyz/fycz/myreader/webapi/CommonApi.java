package xyz.fycz.myreader.webapi;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.FYReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.TianLaiReadCrawler;

import java.io.IOException;
import java.util.List;


public class CommonApi extends BaseApi {

    /**
     * 获取章节列表
     *
     * @param url
     * @param callback
     */
    public static void getBookChapters(String url, final ReadCrawler rc,  boolean isRefresh, final ResultCallback callback) {
        String charset = rc.getCharset();
        getCommonReturnHtmlStringApi(url, null, charset, isRefresh, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(rc.getChaptersFromHtml((String) o), 0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }



    /**
     * 获取章节列表
     *
     * @param url
     */
    public static Observable<List<Chapter>> getBookChapters(String url, final ReadCrawler rc) {
        String charset = rc.getCharset();

        return Observable.create(emitter -> {
            try {
                emitter.onNext(rc.getChaptersFromHtml(OkHttpUtils.getHtml(url, charset)));
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }

    /**
     * 获取章节正文
     *
     * @param url
     * @param callback
     */

    public static void getChapterContent(String url, final ReadCrawler rc, final ResultCallback callback) {
        int tem = url.indexOf("\"");
        if (tem != -1) {
            url = url.substring(0, tem);
        }
        String charset = rc.getCharset();
        if (rc instanceof FYReadCrawler) {
            if (url.contains("47.105.152.62")) {
                url.replace("47.105.152.62", "novel.fycz.xyz");
            }
            if (!url.contains("novel.fycz.xyz")) {
                url = URLCONST.nameSpace_FY + url;
            }
        }
        getCommonReturnHtmlStringApi(url, null, charset, true, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(rc.getContentFormHtml((String) o), 0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 获取章节正文
     *
     * @param url
     */

    public static Observable<String> getChapterContent(String url, final ReadCrawler rc) {
        String charset = rc.getCharset();
        return Observable.create(emitter -> {
            try {
                emitter.onNext(rc.getContentFormHtml(OkHttpUtils.getHtml(url, charset)));
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }

    /**
     * 搜索小说
     *
     * @param key
     * @param callback
     */

    public static void search(String key, final ReadCrawler rc, final ResultCallback callback) {
        String charset = "utf-8";
        if (rc instanceof TianLaiReadCrawler) {
            charset = "utf-8";
        } else {
            charset = rc.getCharset();
        }
        getCommonReturnHtmlStringApi(makeSearchUrl(rc.getSearchLink(), key), null, charset, false, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(rc.getBooksFromSearchHtml((String) o), code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
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
                if (rc.isPost()){
                    String url = rc.getSearchLink();
                    String[] urlInfo = url.split(",");
                    url = urlInfo[0];
                    String body = makeSearchUrl(urlInfo[1], key);
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody requestBody = RequestBody.create(mediaType, body);
                    emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(url, requestBody, finalCharset)));
                }else {
                    emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(makeSearchUrl(rc.getSearchLink(), key), finalCharset)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }

    public static String makeSearchUrl(String url, String key){
        return url.replace("{key}", key);
    }

    /**
     * 获取小说详细信息
     *
     * @param book
     */
    public static Observable<Book> getBookInfo(final Book book, final BookInfoCrawler bic) {
        String url;
        if (StringHelper.isEmpty(book.getInfoUrl())){
            url = book.getChapterUrl();
        }else {
            url = book.getInfoUrl();
        }
        return Observable.create(emitter -> {
            try {
                emitter.onNext(bic.getBookInfo(OkHttpUtils.getHtml(url, bic.getCharset()), book));
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }

    /**
     * 获取小说详细信息
     *
     * @param book
     * @param callback
     */
    public static void getBookInfo(final Book book, final BookInfoCrawler bic, final ResultCallback callback) {
        String url = book.getInfoUrl();
        if (StringHelper.isEmpty(url)){
            url = book.getChapterUrl();
        }
        getCommonReturnHtmlStringApi(url, null, bic.getCharset(), false, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(bic.getBookInfo((String) o, book), 0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
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
