package xyz.fycz.myreader.webapi.crawler.base;


import io.reactivex.Observable;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.greendao.entity.Book;

/**
 * @author fengyue
 * @date 2020/5/19 19:50
 */
public interface BookInfoCrawler {
    String getNameSpace();
    String getCharset();
    Book getBookInfo(String html, Book book);
    default Observable<Book> getBookInfo(StrResponse strResponse, Book book){
        return Observable.create(emitter -> {
           emitter.onNext(getBookInfo(strResponse.body(), book));
           emitter.onComplete();
        });
    }
}
