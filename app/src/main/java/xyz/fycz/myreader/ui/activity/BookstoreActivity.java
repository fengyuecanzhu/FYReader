package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActiityBookstoreBinding;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.entity.bookstore.QDBook;
import xyz.fycz.myreader.entity.bookstore.RankBook;
import xyz.fycz.myreader.entity.bookstore.SortBook;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.ui.adapter.BookStoreBookAdapter;
import xyz.fycz.myreader.ui.adapter.BookStoreBookTypeAdapter;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.SourceExchangeDialog;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.webapi.BookStoreApi;
import xyz.fycz.myreader.webapi.ResultCallback;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.QiDianMobileRank;

/**
 * @author fengyue
 * @date 2020/9/13 21:11
 */
public class BookstoreActivity extends BaseActivity {
    private ActiityBookstoreBinding binding;

    private FindCrawler findCrawler;
    private LinearLayoutManager mLinearLayoutManager;
    private BookStoreBookTypeAdapter mBookStoreBookTypeAdapter;
    private List<BookType> mBookTypes;

    private BookStoreBookAdapter mBookStoreBookAdapter;
    private List<Book> bookList = new ArrayList<>();
    private SourceExchangeDialog mSourceDia;

    private BookType curType;

    private int page = 1;

    private String title = "";

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initTypeList();
                    binding.refreshLayout.showFinish();
                    break;
                case 2:
                    List<Book> bookList = (List<Book>) msg.obj;
                    initBookList(bookList);
                    binding.srlBookList.setEnableRefresh(true);
                    binding.srlBookList.setEnableLoadMore(true);
                    binding.pbLoading.setVisibility(View.GONE);
                    break;
                case 3:
                    binding.pbLoading.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.srlBookList.finishRefresh(false);
                    binding.srlBookList.finishLoadMore(false);
                    break;
                case 5:
                    binding.refreshLayout.showError();
                    break;
                case 6:
                    DialogCreator.createTipDialog(BookstoreActivity.this,
                            getResources().getString(R.string.top_sort_tip, title));
                    break;
            }
        }
    };

    @Override
    protected void bindView() {
        binding = ActiityBookstoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        String subTitle = "";
        if (findCrawler != null) {
            String name = findCrawler.getFindName();
            title = name.substring(0, name.indexOf("["));
            subTitle = name.substring(name.indexOf("[") + 1, name.length() - 1);
        }
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(subTitle);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        findCrawler = (FindCrawler) getIntent().getSerializableExtra(APPCONST.FIND_CRAWLER);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        binding.srlBookList.setEnableRefresh(false);
        binding.srlBookList.setEnableLoadMore(false);
        //小说列表下拉加载更多事件
        binding.srlBookList.setOnLoadMoreListener(refreshLayout -> {
            page++;
            getBooksData();
        });

        //小说列表上拉刷新事件
        binding.srlBookList.setOnRefreshListener(refreshLayout -> {
            page = 1;
            getBooksData();
        });

        mBookStoreBookAdapter = new BookStoreBookAdapter(findCrawler.hasImg(), this);
        binding.rvBookList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBookList.setAdapter(mBookStoreBookAdapter);
        binding.refreshLayout.setOnReloadingListener(this::getData);
    }

    @Override
    protected void initClick() {
        super.initClick();
        mBookStoreBookAdapter.setOnItemClickListener((view, pos) -> {
            Book book = bookList.get(pos);
            if (!findCrawler.needSearch()) {
                goToBookDetail(book);
            } else {
                if (BookService.getInstance().isBookCollected(book)) {
                    goToBookDetail(book);
                    return;
                }
                mSourceDia = new SourceExchangeDialog(this, book);
                mSourceDia.setOnSourceChangeListener((bean, pos1) -> {
                    Intent intent = new Intent(this, BookDetailedActivity.class);
                    intent.putExtra(APPCONST.SEARCH_BOOK_BEAN, (ArrayList<Book>) mSourceDia.getaBooks());
                    intent.putExtra(APPCONST.SOURCE_INDEX, pos1);
                    BookstoreActivity.this.startActivity(intent);
                    mSourceDia.dismiss();
                });
                mSourceDia.show();
            }
        });
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        getData();
        if (findCrawler.needSearch()) {
            SharedPreUtils spu = SharedPreUtils.getInstance();
            boolean isReadTopTip = spu.getBoolean(getString(R.string.isReadTopTip), false);
            if (!isReadTopTip) {
                DialogCreator.createCommonDialog(this, "提示", getResources().getString(R.string.top_sort_tip, title),
                        true, "知道了", "不再提示", null,
                        (dialog, which) -> spu.putBoolean(getString(R.string.isReadTopTip), true));
            }
        }
    }

    /**
     * 获取页面数据
     */
    private void getData() {
        if (findCrawler instanceof QiDianMobileRank) {
            SharedPreUtils spu = SharedPreUtils.getInstance();
            if (spu.getString(getString(R.string.qdCookie), "").equals("")) {
                ((QiDianMobileRank) findCrawler).initCookie(this, new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        if (App.isDestroy(BookstoreActivity.this)) return;
                        spu.putString(getString(R.string.qdCookie), (String) o);
                        mBookTypes = findCrawler.getBookTypes();
                        initBooks();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (App.isDestroy(BookstoreActivity.this)) return;
                        binding.refreshLayout.showError();
                        e.printStackTrace();
                    }
                });
            } else {
                mBookTypes = findCrawler.getBookTypes();
                initBooks();
            }
        } else if ((mBookTypes = findCrawler.getBookTypes()) != null) {
            initBooks();
        } else {
            BookStoreApi.getBookTypeList(findCrawler, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    if (App.isDestroy(BookstoreActivity.this)) return;
                    mBookTypes = (ArrayList<BookType>) o;
                    initBooks();
                }

                @Override
                public void onError(Exception e) {
                    if (App.isDestroy(BookstoreActivity.this)) return;
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(5);
                }
            });
        }
    }

    private void initBooks() {
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
            binding.srlBookList.finishLoadMoreWithNoMoreData();
            return;
        }

        mHandler.sendEmptyMessage(3);
        if (findCrawler instanceof QiDianMobileRank) {
            ((QiDianMobileRank) findCrawler).getRankBooks(curType, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    if (App.isDestroy(BookstoreActivity.this)) return;
                    List<Book> books = new ArrayList<>();
                    for (QDBook rb : (List<QDBook>) o) {
                        Book book = new Book();
                        book.setName(rb.getbName());
                        book.setAuthor(rb.getbAuth());
                        book.setImgUrl(rb.getImg());
                        String cat = rb.getCat();
                        book.setType(cat.contains("小说") || cat.length() >= 4 ? cat : cat + "小说");
                        book.setNewestChapterTitle(rb.getDesc());
                        book.setDesc(rb.getDesc());
                        if (rb instanceof RankBook) {
                            boolean hasRankCnt = !((RankBook) rb).getRankCnt().equals("null");
                            book.setUpdateDate(hasRankCnt ? book.getType() + "-" + rb.getCnt() : rb.getCnt());
                            book.setNewestChapterId(hasRankCnt ? ((RankBook) rb).getRankCnt() : book.getType());
                        } else if (rb instanceof SortBook) {
                            book.setUpdateDate(rb.getCnt());
                            book.setNewestChapterId(((SortBook) rb).getState());
                        }
                        books.add(book);
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(2, books));
                }

                @Override
                public void onError(Exception e) {
                    if (App.isDestroy(BookstoreActivity.this)) return;
                    mHandler.sendMessage(mHandler.obtainMessage(4));
                    ToastUtils.showError("数据加载失败！\n" + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            BookStoreApi.getBookRankList(curType, findCrawler, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    if (App.isDestroy(BookstoreActivity.this)) return;
                    mHandler.sendMessage(mHandler.obtainMessage(2, o));
                }

                @Override
                public void onError(Exception e) {
                    if (App.isDestroy(BookstoreActivity.this)) return;
                    mHandler.sendMessage(mHandler.obtainMessage(4));
                    ToastUtils.showError("数据加载失败！\n" + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }


    /**
     * 初始化类别列表
     */
    private void initTypeList() {

        //设置布局管理器
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rvTypeList.setLayoutManager(mLinearLayoutManager);
        mBookStoreBookTypeAdapter = new BookStoreBookTypeAdapter(this, mBookTypes);
        binding.rvTypeList.setAdapter(mBookStoreBookTypeAdapter);

        //点击事件
        mBookStoreBookTypeAdapter.setOnItemClickListener((pos, view) -> {
            if (curType.equals(mBookTypes.get(pos))) {
                return;
            }
            page = 1;
            curType = mBookTypes.get(pos);
            binding.srlBookList.resetNoMoreData();
            getBooksData();
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
            binding.rvBookList.scrollToPosition(0);
        } else {
            this.bookList.addAll(bookList);
            this.bookList = new ArrayList<>(new LinkedHashSet<>(this.bookList));//去重
            mBookStoreBookAdapter.refreshItems(this.bookList);
        }

        //刷新动作完成
        binding.srlBookList.finishRefresh();
        //加载更多完成
        binding.srlBookList.finishLoadMore();

    }

    /**
     * 前往书籍详情
     *
     * @param book
     */
    private void goToBookDetail(Book book) {
        Intent intent = new Intent(this, BookDetailedActivity.class);
        intent.putExtra(APPCONST.BOOK, book);
        BookstoreActivity.this.startActivity(intent);
    }

    /********************************Event***************************************/
    /**
     * 创建菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_store, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (findCrawler.needSearch()) {
            menu.findItem(R.id.action_tip).setVisible(true);
        }
        return true;
    }

    /**
     * 导航栏菜单点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tip:
                mHandler.sendEmptyMessage(6);
                return true;
            case R.id.action_refresh:
                binding.refreshLayout.showLoading();
                getData();
                return true;
        }
        return false;
    }
}
