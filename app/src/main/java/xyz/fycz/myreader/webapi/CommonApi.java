package xyz.fycz.myreader.webapi;

import android.text.TextUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.third.analyzeRule.AnalyzeUrl;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.model.third.content.BookChapterList;
import xyz.fycz.myreader.model.third.content.BookContent;
import xyz.fycz.myreader.model.third.content.BookInfo;
import xyz.fycz.myreader.model.third.content.BookList;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.TianLaiReadCrawler;
import xyz.fycz.myreader.webapi.crawler.source.ThirdCrawler;

import static xyz.fycz.myreader.common.APPCONST.JS_PATTERN;


public class CommonApi {

    /**
     * 获取章节列表
     *
     * @param book
     */
    public static Observable<List<Chapter>> getBookChapters(Book book, final ReadCrawler rc) {
        if (rc instanceof ThirdCrawler) {
            return getBookChaptersByTC(book, (ThirdCrawler) rc);
        }
        String url = book.getChapterUrl();
        String charset = rc.getCharset();
        url = NetworkUtils.getAbsoluteURL(rc.getNameSpace(), url);
        String finalUrl = url;
        return Observable.create(emitter -> {
            emitter.onNext(rc.getChaptersFromHtml(OkHttpUtils.getHtml(finalUrl, charset, rc.getHeaders())));
            emitter.onComplete();
        });
    }

    private static Observable<List<Chapter>> getBookChaptersByTC(Book book, ThirdCrawler rc) {
        BookSource source = rc.getSource();
        BookChapterList bookChapterList = new BookChapterList(source.getSourceUrl(), source, true);
        /*if (!TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterListHtml())) {
            return bookChapterList.analyzeChapterList(bookShelfBean.getBookInfoBean().getChapterListHtml(), bookShelfBean, headerMap);
        }*/
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(book.getChapterUrl(), null, book.getInfoUrl());
            return OkHttpUtils.getStrResponse(analyzeUrl)
                    //.flatMap(response -> setCookie(response, tag))
                    .flatMap(response -> bookChapterList.analyzeChapterList(response.body(), book, null))
                    .flatMap(chapters -> Observable.create(emitter -> {
                        for (int i = 0; i < chapters.size(); i++) {
                            Chapter chapter = chapters.get(i);
                            chapter.setNumber(i);
                        }
                        emitter.onNext(chapters);
                        emitter.onComplete();
                    }));
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", book.getChapterUrl())));
        }
    }

    /**
     * 获取章节正文
     *
     */

    public static Observable<String> getChapterContent(Chapter chapter, Book book,  final ReadCrawler rc) {
        if (rc instanceof ThirdCrawler) {
            return getChapterContentByTC(chapter, book, (ThirdCrawler) rc);
        }
        String url = chapter.getUrl();
        String charset = rc.getCharset();
        url = NetworkUtils.getAbsoluteURL(rc.getNameSpace(), url);
        String finalUrl = url;
        return Observable.create(emitter -> {
            emitter.onNext(rc.getContentFormHtml(OkHttpUtils.getHtml(finalUrl, charset, rc.getHeaders())));
            emitter.onComplete();
        });
    }

    private static Observable<String> getChapterContentByTC(Chapter chapter, Book book, ThirdCrawler rc) {
        BookSource source = rc.getSource();
        BookContent bookContent = new BookContent(source.getSourceUrl(), source);
        /*if (Objects.equals(chapterBean.getDurChapterUrl(), bookShelfBean.getBookInfoBean().getChapterUrl())
                && !TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterListHtml())) {
            return bookContent.analyzeBookContent(bookShelfBean.getBookInfoBean().getChapterListHtml(), chapterBean, nextChapterBean, bookShelfBean, headerMap);
        }*/
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapter.getUrl(), null, book.getChapterUrl());
            String contentRule = source.getContentRule().getContent();
            if (contentRule.startsWith("$") && !contentRule.startsWith("$.")) {
                //动态网页第一个js放到webView里执行
                contentRule = contentRule.substring(1);
                String js = null;
                Matcher jsMatcher = JS_PATTERN.matcher(contentRule);
                if (jsMatcher.find()) {
                    js = jsMatcher.group();
                    if (js.startsWith("<js>")) {
                        js = js.substring(4, js.lastIndexOf("<"));
                    } else {
                        js = js.substring(4);
                    }
                }
                return OkHttpUtils.getAjaxString(analyzeUrl, source.getSourceUrl(), js)
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapter, null, book, null));
            } else {
                return OkHttpUtils.getStrResponse(analyzeUrl)
                        //.flatMap(response -> setCookie(response, tag))
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapter, null, book, null));
            }
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", chapter.getUrl())));
        }
    }


    /**
     * 搜索小说
     *
     * @param key
     */
    public static Observable<ConcurrentMultiValueMap<SearchBookBean, Book>> search(String key, final ReadCrawler rc) {
        if (rc instanceof ThirdCrawler) {
            return searchByTC(key, (ThirdCrawler) rc);
        }
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
                    String body = makeSearchUrl(urlInfo[1], key);
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody requestBody = RequestBody.create(mediaType, body);
                    emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(url, requestBody, finalCharset, rc.getHeaders())));
                } else {
                    emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(makeSearchUrl(rc.getSearchLink(), key), finalCharset, rc.getHeaders())));
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
     * 第三方书源搜索
     *
     * @param key
     * @param rc
     * @return
     */
    public static Observable<ConcurrentMultiValueMap<SearchBookBean, Book>> searchByTC(String key, final ThirdCrawler rc) {
        try {
            BookSource source = rc.getSource();
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(source.getSearchRule().getSearchUrl(),
                    key, 1, null, source.getSourceUrl());
            BookList bookList = new BookList(source.getSourceUrl(), source.getSourceName(), source, false);
            return OkHttpUtils.getStrResponse(analyzeUrl).flatMap(bookList::analyzeSearchBook)
                    .flatMap((Function<List<Book>, ObservableSource<ConcurrentMultiValueMap<SearchBookBean, Book>>>) books -> Observable.create((ObservableOnSubscribe<ConcurrentMultiValueMap<SearchBookBean, Book>>) emitter -> {
                        emitter.onNext(rc.getBooks(books));
                        emitter.onComplete();
                    }));
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    /**
     * 获取小说详细信息
     *
     * @param book
     */
    public static Observable<Book> getBookInfo(final Book book, final BookInfoCrawler bic) {
        if (bic instanceof ThirdCrawler){
            return getBookInfoByTC(book, (ThirdCrawler) bic);
        }
        String url;
        url = book.getInfoUrl();
        url = NetworkUtils.getAbsoluteURL(bic.getNameSpace(), url);
        String finalUrl = url;
        return Observable.create(emitter -> {
            emitter.onNext(bic.getBookInfo(OkHttpUtils.getHtml(finalUrl, bic.getCharset(),
                    ((ReadCrawler) bic).getHeaders()), book));
            emitter.onComplete();
        });
    }

    private static Observable<Book> getBookInfoByTC(Book book, ThirdCrawler rc) {
        BookSource source = rc.getSource();
        BookInfo bookInfo = new BookInfo(source.getSourceUrl(), source.getSourceName(), source);
        /*if (!TextUtils.isEmpty(book.getBookInfoBean().getBookInfoHtml())) {
            return bookInfo.analyzeBookInfo(book.getBookInfoBean().getBookInfoHtml(), bookShelfBean);
        }*/
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(book.getInfoUrl(), null, source.getSourceUrl());
            return OkHttpUtils.getStrResponse(analyzeUrl)
                    //.flatMap(response -> setCookie(response, tag))
                    .flatMap(response -> bookInfo.analyzeBookInfo(response.body(), book));
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", book.getInfoUrl())));
        }
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
