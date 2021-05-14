package xyz.fycz.myreader.model.third.content;

import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.entity.WebChapterBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.third.analyzeRule.AnalyzeUrl;
import xyz.fycz.myreader.util.utils.OkHttpUtils;

public class AnalyzeNextUrlTask {
    private WebChapterBean webChapterBean;
    private Callback callback;
    private Book bookShelfBean;
    private Map<String, String> headerMap;
    private BookChapterList bookChapterList;

    public AnalyzeNextUrlTask(BookChapterList bookChapterList, WebChapterBean webChapterBean, Book bookShelfBean, Map<String, String> headerMap) {
        this.bookChapterList = bookChapterList;
        this.webChapterBean = webChapterBean;
        this.bookShelfBean = bookShelfBean;
        this.headerMap = headerMap;
    }

    public AnalyzeNextUrlTask setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void analyzeUrl(AnalyzeUrl analyzeUrl) {
        OkHttpUtils.getStrResponse(analyzeUrl)
                .flatMap(stringResponse ->
                        bookChapterList.analyzeChapterList(stringResponse.body(), bookShelfBean, headerMap))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new MyObserver<List<Chapter>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        callback.addDisposable(d);
                    }

                    @Override
                    public void onNext(List<Chapter> bookChapterBeans) {
                        callback.analyzeFinish(webChapterBean, bookChapterBeans);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.onError(throwable);
                    }
                });
    }

    public interface Callback {
        void addDisposable(Disposable disposable);

        void analyzeFinish(WebChapterBean bean, List<Chapter> bookChapterBeans);

        void onError(Throwable throwable);
    }
}
