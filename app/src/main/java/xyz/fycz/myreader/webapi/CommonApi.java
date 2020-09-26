package xyz.fycz.myreader.webapi;

import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.webapi.crawler.*;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.greendao.entity.Book;

import java.util.HashMap;
import java.util.Map;


public class CommonApi extends BaseApi {

    /**
     * 获取章节列表
     *
     * @param url
     * @param callback
     */
    public static void getBookChapters(String url, final ReadCrawler rc, final ResultCallback callback) {
        String charset = rc.getCharset();
        getCommonReturnHtmlStringApi(url, null, charset, new ResultCallback() {
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
        getCommonReturnHtmlStringApi(url, null, charset, new ResultCallback() {
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
     * 搜索小说
     *
     * @param key
     * @param callback
     */

    public static void search(String key, final ReadCrawler rc, final ResultCallback callback) {
        Map<String, Object> params = new HashMap<>();
        String charset = "utf-8";
        if (rc instanceof TianLaiReadCrawler) {
            charset = "utf-8";
        } else {
            charset = rc.getCharset();
        }
        params.put(rc.getSearchKey(), key);
        if (rc instanceof PinShuReadCrawler) {
            params.put("SearchClass", 1);
        }else if (rc instanceof QB5ReadCrawler){
            params.put("submit", "%CB%D1%CB%F7");
        }
        getCommonReturnHtmlStringApi(rc.getSearchLink(), params, charset, new ResultCallback() {
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
     * 获取小说详细信息
     *
     * @param book
     * @param callback
     */
    public static void getBookInfo(final Book book, final BookInfoCrawler bic, final ResultCallback callback) {
        getCommonReturnHtmlStringApi(book.getChapterUrl(), null, bic.getCharset(), new ResultCallback() {
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

    public static void getNewestAppVersion(final ResultCallback callback) {
        getCommonReturnStringApi(URLCONST.method_getCurAppVersion, null, callback);
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
