package xyz.fycz.myreader.webapi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.TianLaiReadCrawler;
import xyz.fycz.myreader.webapi.crawler.source.ThirdCrawler;


public class BookApi {

    /**
     * 获取章节列表
     *
     * @param book
     */
    public static Observable<List<Chapter>> getBookChapters(Book book, final ReadCrawler rc) {
        if (rc instanceof ThirdCrawler) {
            return ThirdSourceApi.getBookChaptersByTC(book, (ThirdCrawler) rc);
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


    /**
     * 获取章节正文
     *
     */

    public static Observable<String> getChapterContent(Chapter chapter, Book book,  final ReadCrawler rc) {
        if (rc instanceof ThirdCrawler) {
            return ThirdSourceApi.getChapterContentByTC(chapter, book, (ThirdCrawler) rc);
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


    /**
     * 搜索小说
     *
     * @param key
     */
    public static Observable<ConMVMap<SearchBookBean, Book>> search(String key, final ReadCrawler rc) {
        if (rc instanceof ThirdCrawler) {
            return ThirdSourceApi.searchByTC(key, (ThirdCrawler) rc);
        }
        String charset = "utf-8";
        if (rc instanceof TianLaiReadCrawler) {
            charset = "utf-8";
        } else {
            charset = rc.getCharset();
        }
        if (rc.getSearchCharset() != null && rc.getSearchCharset().toLowerCase().equals("gbk")) {
            try {
                key = URLEncoder.encode(key, rc.getSearchCharset());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String finalCharset = charset;
        String finalKey = key;
        return Observable.create(emitter -> {
            try {
                if (rc.isPost()) {
                    String url = rc.getSearchLink();
                    String[] urlInfo = url.split(",");
                    url = urlInfo[0];
                    String body = makeSearchUrl(urlInfo[1], finalKey);
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody requestBody = RequestBody.create(mediaType, body);
                    emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(url, requestBody, finalCharset, rc.getHeaders())));
                } else {
                    emitter.onNext(rc.getBooksFromSearchHtml(OkHttpUtils.getHtml(makeSearchUrl(rc.getSearchLink(), finalKey), finalCharset, rc.getHeaders())));
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
        if (bic instanceof ThirdCrawler){
            return ThirdSourceApi.getBookInfoByTC(book, (ThirdCrawler) bic);
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
}
