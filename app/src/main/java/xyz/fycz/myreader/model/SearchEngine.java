package xyz.fycz.myreader.model;


import androidx.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.webapi.BookApi;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.source.ThirdCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SearchEngine {

    private static final String TAG = "SearchEngine";

    //线程池
    private ExecutorService executorService;

    private Scheduler scheduler;
    private CompositeDisposable compositeDisposable;

    private List<ReadCrawler> mSourceList = new ArrayList<>();

    private int threadsNum;
    private int searchSiteIndex;
    private int searchSuccessNum;
    private int searchFinishNum;

    private OnSearchListener searchListener;

    public SearchEngine() {
        threadsNum = SharedPreUtils.getInstance().getInt(App.getmContext().getString(R.string.threadNum), 8);
    }

    public void setOnSearchListener(OnSearchListener searchListener) {
        this.searchListener = searchListener;
    }

    /**
     * 搜索引擎初始化
     */
    public void initSearchEngine(@NonNull List<ReadCrawler> sourceList) {
        mSourceList.clear();
        mSourceList.addAll(sourceList);
        executorService = Executors.newFixedThreadPool(threadsNum);
        scheduler = Schedulers.from(executorService);
        compositeDisposable = new CompositeDisposable();
    }

    public void stopSearch() {
        if (compositeDisposable != null) compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        searchListener.loadMoreFinish(true);
    }

    /**
     * 刷新引擎
     *
     * @param sourceList
     */
    public void refreshSearchEngine(@NonNull List<ReadCrawler> sourceList) {
        mSourceList.clear();
        mSourceList.addAll(sourceList);
    }


    /**
     * 关闭引擎
     */
    public void closeSearchEngine() {
        executorService.shutdown();
        if (!compositeDisposable.isDisposed())
            compositeDisposable.dispose();
        compositeDisposable = null;
    }

    /**
     * 搜索关键字(模糊搜索)
     *
     * @param keyword
     */
    public void search(String keyword) {
        if (mSourceList.size() == 0) {
            ToastUtils.showWarring("当前书源已全部禁用，无法搜索！");
            searchListener.loadMoreFinish(true);
            return;
        }
        searchSuccessNum = 0;
        searchSiteIndex = -1;
        searchFinishNum = 0;
        for (int i = 0; i < Math.min(mSourceList.size(), threadsNum); i++) {
            searchOnEngine(keyword);
        }
    }


    /**
     * 根据书名和作者搜索书籍
     *
     * @param title
     * @param author
     */
    public void search(String title, String author) {
        if (mSourceList.size() == 0) {
            ToastUtils.showWarring("当前书源已全部禁用，无法搜索！");
            searchListener.loadMoreFinish(true);
            return;
        }
        searchSuccessNum = 0;
        searchSiteIndex = -1;
        searchFinishNum = 0;
        for (int i = 0; i < Math.min(mSourceList.size(), threadsNum); i++) {
            searchOnEngine(title, author);
        }
    }

    private synchronized void searchOnEngine(String keyword) {
        searchSiteIndex++;
        if (searchSiteIndex < mSourceList.size()) {
            ReadCrawler crawler = mSourceList.get(searchSiteIndex);
            BookApi.search(keyword, crawler)
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ConMVMap<SearchBookBean, Book>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(ConMVMap<SearchBookBean, Book> bookSearchBeans) {
                            searchFinishNum++;
                            if (bookSearchBeans != null) {
                                searchSuccessNum++;
                                searchListener.loadMoreSearchBook(bookSearchBeans);
                            }
                            searchOnEngine(keyword);
                        }

                        @Override
                        public void onError(Throwable e) {
                            searchFinishNum++;
                            searchOnEngine(keyword);
                            if (App.isDebug()) e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            if (searchFinishNum >= mSourceList.size()) {
                if (searchSuccessNum == 0) {
                    searchListener.searchBookError(new Throwable("未搜索到内容"));
                }
                searchListener.loadMoreFinish(true);
            }
        }

    }

    private synchronized void searchOnEngine(final String title, final String author) {
        searchSiteIndex++;
        if (searchSiteIndex < mSourceList.size()) {
            ReadCrawler crawler = mSourceList.get(searchSiteIndex);
            String searchKey = title;
            BookApi.search(searchKey, crawler)
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ConMVMap<SearchBookBean, Book>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(ConMVMap<SearchBookBean, Book> bookSearchBeans) {
                            searchFinishNum++;
                            if (bookSearchBeans != null) {
                                List<Book> books = bookSearchBeans.getValues(new SearchBookBean(title, author));
                                if (books != null) {
                                    searchSuccessNum++;
                                    searchListener.loadMoreSearchBook(books);
                                }
                            }
                            searchOnEngine(title, author);
                        }

                        @Override
                        public void onError(Throwable e) {
                            searchFinishNum++;
                            searchOnEngine(title, author);
                            if (App.isDebug()) e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            if (searchFinishNum >= mSourceList.size()) {
                if (searchSuccessNum == 0) {
                    searchListener.searchBookError(new Throwable("未搜索到内容"));
                }
                searchListener.loadMoreFinish(true);

            }
        }

    }

    public void getBookInfo(Book book, BookInfoCrawler bic, OnGetBookInfoListener listener) {
        BookApi.getBookInfo(book, bic)
                .subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Book>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Book book) {
                        listener.loadFinish(true);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        listener.loadFinish(false);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /************************************************************************/
    public interface OnSearchListener {

        void loadMoreFinish(Boolean isAll);

        void loadMoreSearchBook(ConMVMap<SearchBookBean, Book> items);

        void loadMoreSearchBook(List<Book> items);

        void searchBookError(Throwable throwable);

    }

    public interface OnGetBookInfoListener {
        void loadFinish(Boolean isSuccess);
    }

    public interface OnGetBookChaptersListener {
        void loadFinish(List<Chapter> chapters, Boolean isSuccess);
    }

    public interface OnGetChapterContentListener {
        void loadFinish(String content, Boolean isSuccess);
    }
}
