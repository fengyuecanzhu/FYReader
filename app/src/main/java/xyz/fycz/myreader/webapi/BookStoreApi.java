package xyz.fycz.myreader.webapi;


import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler3;

public class BookStoreApi {


    /**
     * 获取书城小说分类列表
     * @param findCrawler3
     * @param callback
     */
    public static void getBookTypeList(FindCrawler3 findCrawler3, final ResultCallback callback){
        Single.create((SingleOnSubscribe<List<BookType>>) emitter -> {
            String html = OkHttpUtils.getHtml(findCrawler3.getFindUrl(), findCrawler3.getCharset());
            emitter.onSuccess(findCrawler3.getBookTypes(html));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<List<BookType>>() {
            @Override
            public void onSuccess(@NotNull List<BookType> bookTypes) {
                callback.onFinish(bookTypes, 0);
            }

            @Override
            public void onError(Throwable e) {
                callback.onError((Exception) e);
            }
        });
    }


    /**
     * 获取某一分类小说排行榜列表
     * @param findCrawler3
     * @param callback
     */
    public static void getBookRankList(BookType bookType, FindCrawler3 findCrawler3, final ResultCallback callback){
        Single.create((SingleOnSubscribe<List<Book>>) emitter -> {
            String html = OkHttpUtils.getHtml(bookType.getUrl(), findCrawler3.getCharset());
            emitter.onSuccess(findCrawler3.getFindBooks(html, bookType));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<List<Book>>() {
            @Override
            public void onSuccess(@NotNull List<Book> books) {
                callback.onFinish(books, 0);
            }

            @Override
            public void onError(Throwable e) {
                callback.onError((Exception) e);
            }
        });
    }

}
