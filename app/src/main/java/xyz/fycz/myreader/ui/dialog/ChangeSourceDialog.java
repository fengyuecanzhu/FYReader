package xyz.fycz.myreader.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.webapi.crawler.*;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.CommonApi;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * @author fengyue
 * @date 2020/5/19 17:35
 */
public class ChangeSourceDialog {
    private Context context;
    private Book mBook;
    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks = new ConcurrentMultiValueMap<>();
    private ArrayList<Book> aBooks;
    private SearchBookBean sbb;
    private boolean isSearchSuccess;
    private int threadCount;
    private ResultCallback rc;
    private boolean isGetOneBook = false;
    private Book oneBook;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (isGetOneBook){
                        rc.onFinish(oneBook, 0);
                    }else {
                        if (threadCount == 0) {
                            createaBooks();
                        }
                    }
                    break;
                case 2:
                    if (!isSearchSuccess && threadCount == 0) {
                        if (!isGetOneBook|| oneBook == null) {
                            rc.onError(new Exception());
                        }
                    }else if (threadCount == 0){
                        if (isGetOneBook){
                            rc.onFinish(oneBook, 0);
                        }else {
                            createaBooks();
                        }
                    }
                    break;
            }
        }
    };

    public ChangeSourceDialog(Context context, Book mBook) {
        this.context = context;
        this.mBook = mBook;
        sbb = new SearchBookBean(mBook.getName(), mBook.getAuthor());
    }

    public void init(ResultCallback rc) {
        this.rc = rc;
        getData();
    }

    public void initOneBook(ResultCallback rc){
        this.rc = rc;
        getData();
        isGetOneBook = true;
    }

    /**
     * 获取搜索数据
     */
    private void getData() {
        mBooks.clear();
        ArrayList<ReadCrawler> readCrawlers = ReadCrawlerUtil.getReadCrawlers();
        threadCount = readCrawlers.size();
        if (threadCount == 0){
            if (isGetOneBook){
                ToastUtils.showWarring("当前书源已全部禁用，无法搜索！");
            }else {
                ToastUtils.showWarring("当前书源已全部禁用，无法换源！");
            }
            rc.onError(new Exception());
            return;
        }
        isSearchSuccess = false;
        for (ReadCrawler readCrawler : readCrawlers){
            searchBookByCrawler(readCrawler, readCrawler.getSearchCharset());
        }
    }

    private void searchBookByCrawler(final ReadCrawler rc, String charset) {
        String searchKey = mBook.getName();
        if (charset.toLowerCase().equals("gbk")) {
            try {
                searchKey = URLEncoder.encode(mBook.getName(), charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        CommonApi.search(searchKey, rc, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                final ConcurrentMultiValueMap<SearchBookBean, Book> cmvm =
                        (ConcurrentMultiValueMap<SearchBookBean, Book>) o;
                if (isGetOneBook){
                    if (oneBook != null){
                        return;
                    }
                    oneBook = cmvm.getValue(sbb, 0);
                    threadCount--;
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                    App.getApplication().shutdownThreadPool();
                    return;
                }
                if (rc instanceof BookInfoCrawler) {
                    BookInfoCrawler bic = (BookInfoCrawler) rc;
                    final List<Book> aBooks = cmvm.getValues(sbb);
                    if (aBooks != null) {
                        for (int i = 0; i < aBooks.size(); i++) {
                            Book book = aBooks.get(i);
                            final int finalI = i;
                            CommonApi.getBookInfo(book, bic, new ResultCallback() {
                                @Override
                                public void onFinish(Object o, int code) {
                                    if (finalI == aBooks.size() - 1) {
                                        mBooks.addAll(cmvm);
                                        isSearchSuccess = true;
                                        threadCount--;
                                        mHandler.sendMessage(mHandler.obtainMessage(1));
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    threadCount--;
                                    mHandler.sendMessage(mHandler.obtainMessage(2));
                                }
                            });

                        }
                    } else {
                        isSearchSuccess = true;
                        threadCount--;
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                } else {
                    mBooks.addAll(cmvm);
                    isSearchSuccess = true;
                    threadCount--;
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                }
            }

            @Override
            public void onError(Exception e) {
                threadCount--;
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }
        });
    }

    private void createaBooks() {
        aBooks = (ArrayList<Book>) mBooks.getValues(sbb);
        rc.onFinish(aBooks, 1);
    }

}
