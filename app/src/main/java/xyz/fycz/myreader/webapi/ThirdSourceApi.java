package xyz.fycz.myreader.webapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.CookieBean;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.model.third.analyzeRule.AnalyzeUrl;
import xyz.fycz.myreader.model.third.content.BookChapterList;
import xyz.fycz.myreader.model.third.content.BookContent;
import xyz.fycz.myreader.model.third.content.BookInfo;
import xyz.fycz.myreader.model.third.content.BookList;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.webapi.crawler.source.ThirdCrawler;

import static xyz.fycz.myreader.common.APPCONST.JS_PATTERN;

/**
 * @author fengyue
 * @date 2021/5/15 9:56
 */
public class ThirdSourceApi {

    /**
     * 第三方书源搜索
     *
     * @param key
     * @param rc
     * @return
     */
    public static Observable<ConMVMap<SearchBookBean, Book>> searchByTC(String key, final ThirdCrawler rc) {
        try {
            BookSource source = rc.getSource();
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(source.getSearchRule().getSearchUrl(),
                    key, 1, getCookies(source), source.getSourceUrl());
            BookList bookList = new BookList(source.getSourceUrl(), source.getSourceName(), source, false);
            return OkHttpUtils.getStrResponse(analyzeUrl).flatMap(bookList::analyzeSearchBook)
                    .flatMap((Function<List<Book>, ObservableSource<ConMVMap<SearchBookBean, Book>>>) books -> Observable.create((ObservableOnSubscribe<ConMVMap<SearchBookBean, Book>>) emitter -> {
                        emitter.onNext(rc.getBooks(books));
                        emitter.onComplete();
                    }));
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    protected static Observable<Book> getBookInfoByTC(Book book, ThirdCrawler rc) {
        BookSource source = rc.getSource();
        BookInfo bookInfo = new BookInfo(source.getSourceUrl(), source.getSourceName(), source);
        /*if (!TextUtils.isEmpty(book.getBookInfoBean().getBookInfoHtml())) {
            return bookInfo.analyzeBookInfo(book.getBookInfoBean().getBookInfoHtml(), bookShelfBean);
        }*/
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(book.getInfoUrl(), getCookies(source), source.getSourceUrl());
            return OkHttpUtils.getStrResponse(analyzeUrl)
                    .flatMap(response -> OkHttpUtils.setCookie(response, source.getSourceUrl()))
                    .flatMap(response -> bookInfo.analyzeBookInfo(response.body(), book));
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", book.getInfoUrl())));
        }
    }

    protected static Observable<List<Chapter>> getBookChaptersByTC(Book book, ThirdCrawler rc) {
        BookSource source = rc.getSource();
        BookChapterList bookChapterList = new BookChapterList(source.getSourceUrl(), source, true);
        /*if (!TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterListHtml())) {
            return bookChapterList.analyzeChapterList(bookShelfBean.getBookInfoBean().getChapterListHtml(), bookShelfBean, headerMap);
        }*/
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(book.getChapterUrl(), getCookies(source), book.getInfoUrl());
            return OkHttpUtils.getStrResponse(analyzeUrl)
                    .flatMap(response -> OkHttpUtils.setCookie(response, source.getSourceUrl()))
                    .flatMap(response -> bookChapterList.analyzeChapterList(response.body(), book, getCookies(source)))
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

    protected static Observable<String> getChapterContentByTC(Chapter chapter, Book book, ThirdCrawler rc) {
        BookSource source = rc.getSource();
        BookContent bookContent = new BookContent(source.getSourceUrl(), source);
        /*if (Objects.equals(chapterBean.getDurChapterUrl(), bookShelfBean.getBookInfoBean().getChapterUrl())
                && !TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterListHtml())) {
            return bookContent.analyzeBookContent(bookShelfBean.getBookInfoBean().getChapterListHtml(), chapterBean, nextChapterBean, bookShelfBean, headerMap);
        }*/
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapter.getUrl(), getCookies(source), book.getChapterUrl());
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
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapter, null, book, getCookies(source)));
            } else {
                return OkHttpUtils.getStrResponse(analyzeUrl)
                        .flatMap(response -> OkHttpUtils.setCookie(response, source.getSourceUrl()))
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapter, null, book, getCookies(source)));
            }
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", chapter.getUrl())));
        }
    }

    private static Map<String, String> getCookies(BookSource bookSource){
        Map<String, String> cookieMap = new HashMap<>();
        if (bookSource != null){
            CookieBean cookie = DbManager.getDaoSession().getCookieBeanDao().load(bookSource.getSourceUrl());
            if (cookie != null) {
                cookieMap.put("Cookie", cookie.getCookie());
            }
        }
        return cookieMap;
    }
}
