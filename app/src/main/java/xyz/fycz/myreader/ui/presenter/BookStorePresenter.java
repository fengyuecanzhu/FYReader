package xyz.fycz.myreader.ui.presenter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.dialog.ChangeSourceDialog;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.entity.bookstore.RankBook;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.ui.activity.BookDetailedActivity;
import xyz.fycz.myreader.ui.activity.MainActivity;
import xyz.fycz.myreader.ui.adapter.BookStoreBookAdapter;
import xyz.fycz.myreader.ui.adapter.BookStoreBookTypeAdapter;
import xyz.fycz.myreader.ui.fragment.BookStoreFragment;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.webapi.crawler.find.QiDianMobileRank;


import java.util.ArrayList;
import java.util.List;


public class BookStorePresenter implements BasePresenter {

    private BookStoreFragment mBookStoreFragment;
    private MainActivity mMainActivity;
    private LinearLayoutManager mLinearLayoutManager;
    private BookStoreBookTypeAdapter mBookStoreBookTypeAdapter;
    private List<BookType> mBookTypes;

    private BookStoreBookAdapter mBookStoreBookAdapter;
    private List<Book> bookList = new ArrayList<>();

    private BookType curType;
    private QiDianMobileRank findCrawler;

    private int page = 1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initTypeList();
                    mBookStoreFragment.getmRlRefresh().showFinish();
                    break;
                case 2:
                    List<Book> bookList = (List<Book>) msg.obj;
                    initBookList(bookList);
                    mBookStoreFragment.getSrlBookList().setEnableRefresh(true);
                    mBookStoreFragment.getSrlBookList().setEnableLoadMore(true);
                    mBookStoreFragment.getPbLoading().setVisibility(View.GONE);
                    break;
                case 3:
                    mBookStoreFragment.getPbLoading().setVisibility(View.VISIBLE);
                    break;
                case 4:
                    mBookStoreFragment.getPbLoading().setVisibility(View.GONE);
                    mBookStoreFragment.getSrlBookList().finishRefresh(false);
                    mBookStoreFragment.getSrlBookList().finishLoadMore(false);
                    break;
                case 5:
                    mBookStoreFragment.getmRlRefresh().showError();
                    break;
            }
        }
    };

    public BookStorePresenter(BookStoreFragment bookStoreFragment) {
        mBookStoreFragment = bookStoreFragment;
        mMainActivity = ((MainActivity) (mBookStoreFragment.getActivity()));
    }

    @Override
    public void start() {
        mBookStoreFragment.getSrlBookList().setEnableRefresh(false);
        mBookStoreFragment.getSrlBookList().setEnableLoadMore(false);
        findCrawler = new QiDianMobileRank(true);
        //小说列表下拉加载更多事件
        mBookStoreFragment.getSrlBookList().setOnLoadMoreListener(refreshLayout -> {
            page++;
            BookStorePresenter.this.getBooksData();
        });

        //小说列表上拉刷新事件
        mBookStoreFragment.getSrlBookList().setOnRefreshListener(refreshLayout -> {
            page = 1;
            BookStorePresenter.this.getBooksData();
        });


        mBookStoreBookAdapter = new BookStoreBookAdapter(findCrawler.hasImg(), mMainActivity);
        mBookStoreFragment.getRvBookList().setLayoutManager(new LinearLayoutManager(mMainActivity));
        mBookStoreFragment.getRvBookList().setAdapter(mBookStoreBookAdapter);
        mBookStoreBookAdapter.setOnItemClickListener((view, pos) -> {
            Book book = bookList.get(pos);
            if (!findCrawler.needSearch()) {
                Intent intent = new Intent(mMainActivity, BookDetailedActivity.class);
                intent.putExtra(APPCONST.BOOK, book);
                mMainActivity.startActivity(intent);
            } else {
                mHandler.sendMessage(mHandler.obtainMessage(3));
                ChangeSourceDialog csd = new ChangeSourceDialog(mMainActivity, bookList.get(pos));
                csd.initOneBook(new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        Book searchBook = (Book) o;
                        book.setChapterUrl(searchBook.getChapterUrl());
                        book.setSource(searchBook.getSource());
                        Intent intent = new Intent(mMainActivity, BookDetailedActivity.class);
                        intent.putExtra(APPCONST.BOOK, book);
                        mMainActivity.startActivity(intent);
                        mHandler.sendMessage(mHandler.obtainMessage(4));
                    }

                    @Override
                    public void onError(Exception e) {
                        DialogCreator.createTipDialog(mMainActivity,"未搜索到该书籍，无法进入书籍详情！");
                    }
                });
            }
        });

        getData();

        mBookStoreFragment.getmRlRefresh().setOnReloadingListener(this::getData);

    }


    /**
     * 获取页面数据
     */
    private void getData() {
        /*BookStoreApi.getBookTypeList(findCrawler, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                mBookTypes = (ArrayList<BookType>) o;
                curType = mBookTypes.get(0);
                mHandler.sendMessage(mHandler.obtainMessage(1));
                page = 1;
                getBooksData();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(5);
            }
        });*/
        mBookTypes = findCrawler.getBookTypes();
        curType = mBookTypes.get(0);
        mHandler.sendMessage(mHandler.obtainMessage(1));
        page = 1;
        getBooksData();



    }

    /**
     * 获取小数列表数据
     */
    private void getBooksData() {
        if (findCrawler.getTypePage(curType, page)) {
            mBookStoreFragment.getSrlBookList().finishLoadMoreWithNoMoreData();
            return;
        }
        mHandler.sendEmptyMessage(3);
        /*BookStoreApi.getBookRankList(curType.getUrl(), findCrawler, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                mHandler.sendMessage(mHandler.obtainMessage(2, o));
            }

            @Override
            public void onError(Exception e) {
                mHandler.sendMessage(mHandler.obtainMessage(4));
                ToastUtils.showError("数据加载失败！\n" + e.getMessage());
                e.printStackTrace();
            }
        });*/
        findCrawler.getRankBooks(curType, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                List<Book> books = new ArrayList<>();
                for (RankBook rb : (List<RankBook>) o){
                    Book book = new Book();
                    book.setName(rb.getbName());
                    book.setAuthor(rb.getbAuth());
                    book.setImgUrl(rb.getImg());
                    String cat = rb.getCat();
                    book.setType(cat.contains("小说") || cat.length() >= 4 ? cat : cat + "小说");
                    book.setNewestChapterTitle(rb.getDesc());
                    book.setDesc(rb.getDesc());
                    book.setUpdateDate(rb.getCnt());
                    books.add(book);
                }
                mHandler.sendMessage(mHandler.obtainMessage(2, books));
            }

            @Override
            public void onError(Exception e) {
                mHandler.sendMessage(mHandler.obtainMessage(4));
                ToastUtils.showError("数据加载失败！\n" + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    /**
     * 初始化类别列表
     */
    private void initTypeList() {

        //设置布局管理器
        mLinearLayoutManager = new LinearLayoutManager(mBookStoreFragment.getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBookStoreFragment.getRvTypeList().setLayoutManager(mLinearLayoutManager);
        mBookStoreBookTypeAdapter = new BookStoreBookTypeAdapter(mBookStoreFragment.getActivity(), mBookTypes);
        mBookStoreFragment.getRvTypeList().setAdapter(mBookStoreBookTypeAdapter);

        //点击事件
        mBookStoreBookTypeAdapter.setOnItemClickListener((pos, view) -> {
            if (curType.equals(mBookTypes.get(pos))) {
                return;
            }
            page = 1;
            curType = mBookTypes.get(pos);
            mBookStoreFragment.getSrlBookList().resetNoMoreData();
            BookStorePresenter.this.getBooksData();
        });


    }


    /**
     * 初始化小说列表
     */
    private void initBookList(List<Book> bookList) {
        if (page == 1) {
            mBookStoreBookAdapter.refreshItems(bookList);
            this.bookList.clear();
            this.bookList.addAll(bookList);
            mBookStoreFragment.getRvBookList().scrollToPosition(0);
        } else {
            this.bookList.addAll(bookList);
            mBookStoreBookAdapter.addItems(bookList);
        }

        //刷新动作完成
        mBookStoreFragment.getSrlBookList().finishRefresh();
        //加载更多完成
        mBookStoreFragment.getSrlBookList().finishLoadMore();

    }


}
