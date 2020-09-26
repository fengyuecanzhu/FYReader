package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseActivity2;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.MyAlertDialog;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.BlurTransformation;
import xyz.fycz.myreader.webapi.crawler.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.creator.ChangeSourceDialog;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.ui.adapter.DetailCatalogAdapter;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.webapi.CommonApi;

import java.util.ArrayList;

/**
 * @author fengyue
 * @date 2020/8/17 11:39
 */
public class BookDetailedActivity extends BaseActivity2 {
    @BindView(R.id.book_detail_iv_cover)
    ImageView mIvCover;
   /* @BindView(R.id.book_detail_iv_blur_cover)
    ImageView mIvBlurCover;*/
    @BindView(R.id.book_detail_tv_author)
    TextView mTvAuthor;
    @BindView(R.id.book_detail_tv_type)
    TextView mTvType;
    @BindView(R.id.book_detail_source)
    TextView mTvSource;
    @BindView(R.id.book_detail_tv_add)
    TextView bookDetailTvAdd;
    @BindView(R.id.book_detail_tv_open)
    TextView bookDetailTvOpen;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.book_detail_tv_desc)
    TextView mTvDesc;
    @BindView(R.id.tv_disclaimer)
    TextView mTvDisclaimer;
    @BindView(R.id.fl_add_bookcase)
    FrameLayout flAddBookcase;
    @BindView(R.id.fl_open_book)
    FrameLayout flOpenBook;
    @BindView(R.id.book_detail_rv_catalog)
    RecyclerView bookDetailRvCatalog;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;


    private Book mBook;
    private ArrayList<Book> aBooks;
    private BookService mBookService;
    private ChapterService mChapterService;
    private ReadCrawler mReadCrawler;
    private DetailCatalogAdapter mCatalogAdapter;
    private ArrayList<Chapter> mChapters = new ArrayList<>();
    private ArrayList<Chapter> mNewestChapters = new ArrayList<>();
    private boolean isCollected;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!"本地书籍".equals(mBook.getType())) {
                        mChapters.clear();
                        mNewestChapters.clear();
                        initBookInfo();
                        initChapters(true);
                        mCatalogAdapter.notifyDataSetChanged();
                    }
                    break;
                case 2:
                    createChangeSourceDia();
                    break;
                case 3:
                    pbLoading.setVisibility(View.GONE);
                    DialogCreator.createTipDialog(BookDetailedActivity.this, "未搜索到该书籍，书源加载失败！");
                    break;
                case 4:
                    pbLoading.setVisibility(View.GONE);
                    initOtherInfo();
                    break;
            }
        }
    };

    @Override
    protected int getContentId() {
        return R.layout.activity_book_detail;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        aBooks = (ArrayList<Book>) getIntent().getSerializableExtra(APPCONST.SEARCH_BOOK_BEAN);
        if (aBooks != null) {
            mBook = aBooks.get(0);
        } else {
            mBook = (Book) getIntent().getSerializableExtra(APPCONST.BOOK);
        }
        isCollected = isBookCollected();
        if (isCollected) {
            mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(mBook.getId());
        }
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle(mBook.getName());
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        initBookInfo();

        //catalog
        mCatalogAdapter = new DetailCatalogAdapter();
        bookDetailRvCatalog.setLayoutManager(new LinearLayoutManager(this));
        bookDetailRvCatalog.setAdapter(mCatalogAdapter);

        initChapters(false);

        mCatalogAdapter.setOnItemClickListener((view, pos) -> {
            mBook.setHisttoryChapterNum(mChapters.size() - pos - 1);
            mBook.setLastReadPosition(0);
            goReadActivity();
        });

        mTvDisclaimer.setOnClickListener(v -> DialogCreator.createAssetTipDialog(this, "免责声明", "disclaimer.fy"));
        if (isCollected) {
            bookDetailTvAdd.setText("移除书籍");
            bookDetailTvOpen.setText("继续阅读");
        }

    }

    @Override
    protected void initClick() {
        super.initClick();
        flAddBookcase.setOnClickListener(view -> {
            if (!isCollected) {
                mBook.setNoReadNum(mChapters.size());
                mBook.setChapterTotalNum(0);
                mBookService.addBook(mBook);
                for (Chapter chapter : mChapters) {
                    chapter.setId(StringHelper.getStringRandom(25));
                    chapter.setBookId(mBook.getId());
                }
                mChapterService.addChapters(mChapters);
                isCollected = true;
                ToastUtils.showSuccess("成功加入书架");
                bookDetailTvAdd.setText("移除书籍");
            } else {
                mBookService.deleteBookById(mBook.getId());
                isCollected = false;
                mBook.setHisttoryChapterNum(0);
                mBook.setHistoryChapterId("未开始阅读");
                mBook.setLastReadPosition(0);
                ToastUtils.showSuccess("成功移除书籍");
                bookDetailTvAdd.setText("加入书架");
                bookDetailTvOpen.setText("开始阅读");
            }
        });
        flOpenBook.setOnClickListener(view -> goReadActivity());

    }

    @Override
    protected void processLogic() {
        super.processLogic();
    }

    /**
     * 判断是否在书架
     *
     * @return
     */
    private boolean isBookCollected() {
        Book book = mBookService.findBookByAuthorAndName(mBook.getName(), mBook.getAuthor());
        if (book == null) {
            return false;
        } else {
            mBook = book;
            return true;
        }
    }

    /**
     * 初始化书籍信息
     */
    private void initBookInfo() {
        mTvAuthor.setText(mBook.getAuthor());
        if (StringHelper.isEmpty(mBook.getImgUrl())) {
            mBook.setImgUrl("");
        }
        assert mBook.getNewestChapterTitle() != null;
        mTvDesc.setText("");
        if (mBook.getType() != null) {
            mTvType.setText(mBook.getType());
        } else {
            mTvType.setText("");
        }
        if (!"null".equals(mBook.getSource())) {
            mTvSource.setText("书源：" + BookSource.fromString(mBook.getSource()).text);
        }
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
        if (rc instanceof BookInfoCrawler && StringHelper.isEmpty(mBook.getImgUrl())) {
            pbLoading.setVisibility(View.VISIBLE);
            BookInfoCrawler bic = (BookInfoCrawler) rc;
            CommonApi.getBookInfo(mBook, bic, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    if (!MyApplication.isDestroy(BookDetailedActivity.this)) {
                        mHandler.sendMessage(mHandler.obtainMessage(4));
                    }
                }

                @Override
                public void onError(Exception e) {
                    ToastUtils.showError("书籍详情加载失败！");
                }
            });
        } else {
            initOtherInfo();
        }
    }

    /**
     * 初始化其他书籍信息
     */
    private void initOtherInfo() {
        mTvDesc.setText("\t\t\t\t" + mBook.getDesc());
        mTvType.setText(mBook.getType());
        if (!MyApplication.isDestroy(this)) {
            Glide.with(this)
                    .load(mBook.getImgUrl())
                    .error(R.mipmap.no_image)
                    .placeholder(R.mipmap.no_image)
                    //设置圆角
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                    .into(mIvCover);
            /*Glide.with(this)
                    .load(mBook.getImgUrl())
                    .transition(DrawableTransitionOptions.withCrossFade(1500))
                    .thumbnail(defaultCover())
                    .centerCrop()
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(this, 25)))
                    .into(mIvBlurCover);*/
        }
    }

    private RequestBuilder<Drawable> defaultCover() {
        return Glide.with(this)
                .load(R.mipmap.no_image)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(this, 25)));
    }

    /**
     * 创建换源对话框
     */
    private void createChangeSourceDia() {
        if (aBooks == null) {
            mHandler.sendMessage(mHandler.obtainMessage(3));
            return;
        }
        pbLoading.setVisibility(View.GONE);
        CharSequence[] sources = new CharSequence[aBooks.size()];
        int checkedItem = 0;
        for (int i = 0; i < sources.length; i++) {
            sources[i] = BookSource.fromString(aBooks.get(i).getSource()).text
                    + "\n" + aBooks.get(i).getNewestChapterTitle();
            if (sources[i].equals(BookSource.fromString(mBook.getSource()).text
                    + "\n" + aBooks.get(i).getNewestChapterTitle())) {
                checkedItem = i;
            }
        }
        final int finalCheckedItem = checkedItem;
        AlertDialog dialog = MyAlertDialog.build(this)
                .setTitle("切换书源")
                .setCancelable(true)
                .setSingleChoiceItems(sources, checkedItem, (dialog1, which) -> {
                    if (finalCheckedItem == which) {
                        dialog1.dismiss();
                        return;
                    }
                    Book book = aBooks.get(which);
                    Book bookTem = new Book(mBook);
                    bookTem.setChapterUrl(book.getChapterUrl());
                    bookTem.setImgUrl(book.getImgUrl());
                    bookTem.setType(book.getType());
                    bookTem.setDesc(book.getDesc());
                    bookTem.setSource(book.getSource());
                    if (isCollected) {
                        mBookService.updateBook(mBook, bookTem);
                    }
                    mBook = bookTem;
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                    if (isCollected) {
                        String tip = null;
                        if (SysManager.getSetting().isMatchChapter()) {
                            tip = getString(R.string.change_source_tip1);
                        } else {
                            tip = getString(R.string.change_source_tip2);
                        }
                        DialogCreator.createTipDialog(this, tip);
                    }
                    dialog1.dismiss();
                }).create();
        dialog.show();
    }

    /**
     * 初始化章节目录
     */
    private void initChapters(boolean isChangeSource) {
        if (mChapters.size() == 0 && !"本地书籍".equals(mBook.getType())) {
            mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
            if (isCollected) {
                mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(mBook.getId());
            }
            CommonApi.getBookChapters(mBook.getChapterUrl(), mReadCrawler, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                    if (isCollected) {
                        int noReadNum = chapters.size() - mBook.getChapterTotalNum();
                        mBook.setNoReadNum(Math.max(noReadNum, 0));
                        mBook.setNewestChapterTitle(chapters.get(chapters.size() - 1).getTitle());
                        mChapterService.updateAllOldChapterData(mChapters, chapters, mBook.getId());
                        mBookService.updateEntity(mBook);
                        if (isChangeSource && SysManager.getSetting().isMatchChapter()) {
                            if (mBookService.matchHistoryChapterPos(mBook, chapters)) {
                                ToastUtils.showSuccess("历史阅读章节匹配成功！");
                            } else {
                                ToastUtils.showError("历史阅读章节匹配失败！");
                            }
                        }
                    }
                    mChapters = chapters;
                    int end = Math.max(0, mChapters.size() - 6);
                    for (int i = mChapters.size() - 1; i >= end; i--) {
                        mNewestChapters.add(mChapters.get(i));
                    }
                    MyApplication.runOnUiThread(() -> mCatalogAdapter.refreshItems(mNewestChapters));
                }

                @Override
                public void onError(Exception e) {
                    ToastUtils.showError("最新章节加载失败！");
                }
            });
        } else {
            int end = Math.max(0, mChapters.size() - 6);
            for (int i = mChapters.size() - 1; i >= end; i--) {
                mNewestChapters.add(mChapters.get(i));
                mCatalogAdapter.refreshItems(mNewestChapters);
            }
        }
    }

    /**
     * 前往阅读界面
     */
    private void goReadActivity() {
        if (!isCollected) {
            mBookService.addBook(mBook);
        }
        Intent intent = new Intent(this, ReadActivity.class);
        intent.putExtra(APPCONST.BOOK, mBook);
        intent.putExtra("isCollected", isCollected);
        startActivityForResult(intent, APPCONST.REQUEST_READ);
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
        if ("本地书籍".equals(mBook.getType())) {
            return false;
        }
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem isUpdate = menu.findItem(R.id.action_is_update);
        if (isCollected) {
            isUpdate.setVisible(true);
            isUpdate.setChecked(!mBook.getIsCloseUpdate());
        } else {
            isUpdate.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
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
            case R.id.action_change_source:  //换源
                if (!NetworkUtils.isNetWorkAvailable()) {
                    ToastUtils.showWarring("无网络连接！");
                    return true;
                }
                pbLoading.setVisibility(View.VISIBLE);
                if (aBooks == null) {
                    ChangeSourceDialog csd = new ChangeSourceDialog(this, mBook);
                    csd.init(new ResultCallback() {
                        @Override
                        public void onFinish(Object o, int code) {
                            aBooks = (ArrayList<Book>) o;
                            mHandler.sendMessage(mHandler.obtainMessage(2));
                        }

                        @Override
                        public void onError(Exception e) {
                            mHandler.sendMessage(mHandler.obtainMessage(3));
                        }
                    });
                } else {
                    createChangeSourceDia();
                }
                break;
            case R.id.action_reload:  //重新加载
                mChapters.clear();
                mNewestChapters.clear();
                initWidget();
                processLogic();
                break;
            case R.id.action_is_update://是否更新
                mBook.setIsCloseUpdate(!mBook.getIsCloseUpdate());
                mBookService.updateEntity(mBook);
                break;
            case R.id.action_open_link:  //打开链接
                Uri uri = Uri.parse(mBook.getChapterUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 展开简介
     */
    @OnClick(R.id.book_detail_tv_desc)
    protected void showMoreDesc() {
        if (mTvDesc.getMaxLines() == 5)
            mTvDesc.setMaxLines(15);
        else
            mTvDesc.setMaxLines(5);
    }

    /**
     * 章节列表
     */
    @OnClick(R.id.book_detail_tv_catalog_more)
    public void goToMoreChapter() {
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.putExtra(APPCONST.BOOK, mBook);
        startActivityForResult(intent, APPCONST.REQUEST_CHAPTER_PAGE);
    }

    /**
     * 阅读/章节界面反馈结果处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case APPCONST.REQUEST_READ:
                    if (data == null) {
                        return;
                    }
                    boolean isCollected = data.getBooleanExtra(APPCONST.RESULT_IS_COLLECTED, false);
                    int lastReadPosition = data.getIntExtra(APPCONST.RESULT_LAST_READ_POSITION, 0);
                    int historyChapterPos = data.getIntExtra(APPCONST.RESULT_HISTORY_CHAPTER, 0);
                    if (isCollected) {
                        bookDetailTvAdd.setText("移除书籍");
                        bookDetailTvOpen.setText("继续阅读");
                        this.isCollected = true;
                        if (mChapters != null && mChapters.size() != 0) {
                            mBook.setHistoryChapterId(mChapters.get(historyChapterPos).getTitle());
                        }
                        mBook.setHisttoryChapterNum(historyChapterPos);
                        mBook.setLastReadPosition(lastReadPosition);
                    } else {
                        mBook.setHisttoryChapterNum(0);
                        mBook.setHistoryChapterId("未开始阅读");
                        mBook.setLastReadPosition(0);
                    }
                    mCatalogAdapter.notifyDataSetChanged();
                    break;
                case APPCONST.REQUEST_CHAPTER_PAGE:
                    int[] chapterAndPage = data.getIntArrayExtra(APPCONST.CHAPTER_PAGE);
                    mBook.setHisttoryChapterNum(chapterAndPage[0]);
                    mBook.setLastReadPosition(chapterAndPage[1]);
                    goReadActivity();
                    break;
            }
        }
    }
}
