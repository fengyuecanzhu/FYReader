/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.weaction.ddsdk.ad.DdSdkFlowAd;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivityBookDetailBinding;
import xyz.fycz.myreader.entity.ad.AdBean;
import xyz.fycz.myreader.experiment.BookWCEstimate;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.adapter.BookTagAdapter;
import xyz.fycz.myreader.ui.adapter.DetailCatalogAdapter;
import xyz.fycz.myreader.ui.dialog.BookGroupDialog;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.LoadingDialog;
import xyz.fycz.myreader.ui.dialog.SourceExchangeDialog;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.AdUtils;
import xyz.fycz.myreader.util.utils.BlurTransformation;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.util.utils.ShareBookUtil;
import xyz.fycz.myreader.webapi.BookApi;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

/**
 * @author fengyue
 * @date 2020/8/17 11:39
 */
public class BookDetailedActivity extends BaseActivity<ActivityBookDetailBinding> {

    private static final String TAG = BookDetailedActivity.class.getSimpleName();

    private Book mBook;
    private List<Book> aBooks;
    private BookService mBookService;
    private ChapterService mChapterService;
    private ReadCrawler mReadCrawler;
    private DetailCatalogAdapter mCatalogAdapter;
    private List<Chapter> mChapters = new ArrayList<>();
    private List<Chapter> mNewestChapters = new ArrayList<>();
    private boolean isCollected;
    private SourceExchangeDialog mSourceDialog;
    private int sourceIndex;
    private BookGroupDialog mBookGroupDia;
    private List<String> tagList = new ArrayList<>();
    private Disposable chaptersDis;
    private Disposable wcDis;
    private AdBean adBean;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!"本地书籍".equals(mBook.getType())) {
                        initBookInfo();
                        mChapters.clear();
                        mNewestChapters.clear();
                        initChapters(true);
                        mCatalogAdapter.clear();
                    }
                    break;
                case 2:
                    createChangeSourceDia();
                    break;
                case 3:
                    binding.pbLoading.setVisibility(View.GONE);
                    DialogCreator.createTipDialog(BookDetailedActivity.this, "未搜索到该书籍，书源加载失败！");
                    break;
                case 4:
                    binding.pbLoading.setVisibility(View.GONE);
                    initOtherInfo();
                    break;
            }
        }
    };

    @Override
    protected void bindView() {
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        aBooks = mSourceDialog.getaBooks();
        Intent intent = new Intent();
        if (aBooks != null && aBooks.size() > 0) {
            int sourceIndex = mSourceDialog.getSourceIndex();
            if (mSourceDialog.hasCurBookSource())
                aBooks.set(sourceIndex, mBook);
            BitIntentDataManager.getInstance().putData(intent, aBooks);
            intent.putExtra(APPCONST.SOURCE_INDEX, sourceIndex);
        } else {
            BitIntentDataManager.getInstance().putData(intent, mBook);
        }
        intent.putExtra("isCollected", isCollected);
        outState.putParcelable(INTENT, intent);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (chaptersDis != null) chaptersDis.dispose();
        if (mSourceDialog != null) {
            mSourceDialog.stopSearch();
        }
        super.onDestroy();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        if (!initBook()) {
            ToastUtils.showError("无法获取书籍！");
            finish();
            return;
        }
        isCollected = isBookCollected();
        if (isCollected) {
            mChapters = mChapterService.findBookAllChapterByBookId(mBook.getId());
        }
        //Dialog
        mSourceDialog = new SourceExchangeDialog(this, mBook);
        if (isBookSourceNotExist()) {
            DialogCreator.createCommonDialog(this, "未知书源",
                    "当前书籍的书源不存在，是否搜索以切换书源？", false, (dialog, which) -> {
                        mSourceDialog.show();
                    }, null);
        }
        mBookGroupDia = new BookGroupDialog(this);
        mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
    }

    private boolean initBook() {
        Object obj = BitIntentDataManager.getInstance().getData(getIntent());
        sourceIndex = getIntent().getIntExtra(APPCONST.SOURCE_INDEX, 0);
        if (obj == null) {
            return false;
        }
        if (obj instanceof Book) {
            mBook = (Book) obj;
        } else if (obj instanceof List) {
            aBooks = (ArrayList<Book>) obj;
            mBook = aBooks.get(sourceIndex);
        }
        return mBook != null;
    }

    private void initTagList() {
        tagList.clear();
        String type = mBook.getType();
        if (!StringHelper.isEmpty(type))
            tagList.add(type);
        String wordCount = mBook.getWordCount();
        if (!StringHelper.isEmpty(wordCount))
            tagList.add(wordCount);
        String status = mBook.getStatus();
        if (!StringHelper.isEmpty(status))
            tagList.add(status);
        binding.ih.tflBookTag.setAdapter(new BookTagAdapter(this, tagList, 13));
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
        binding.ic.bookDetailRvCatalog.setLayoutManager(new LinearLayoutManager(this));
        binding.ic.bookDetailRvCatalog.setAdapter(mCatalogAdapter);

        initChapters(false);

        mCatalogAdapter.setOnItemClickListener((view, pos) -> {
            mBook.setHisttoryChapterNum(mChapters.size() - pos - 1);
            mBook.setLastReadPosition(0);
            goReadActivity();
        });

        binding.ic.tvDisclaimer.setOnClickListener(v -> DialogCreator.createAssetTipDialog(this, "免责声明", "disclaimer.fy"));
        if (isCollected) {
            binding.ib.bookDetailTvAdd.setText("移除书籍");
            binding.ib.bookDetailTvOpen.setText("继续阅读");
        }

        if (aBooks != null && aBooks.size() > 0) {
            if (isCollected) {
                for (int i = 0; i < aBooks.size(); i++) {
                    Book book = aBooks.get(i);
                    if (book.getSource().equals(mBook.getSource())) {
                        book.setNewestChapterId("true");
                        sourceIndex = i;
                        break;
                    }
                }
            } else {
                aBooks.get(sourceIndex).setNewestChapterId("true");
            }
        }
        mSourceDialog.setABooks(aBooks);
        mSourceDialog.setSourceIndex(sourceIndex);
        adBean = AdUtils.getAdConfig().getDetail();
        initAd();
    }

    private void initAd() {
        AdUtils.checkHasAd().subscribe(new MySingleObserver<Boolean>() {
            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                if (aBoolean && AdUtils.adTime("detail", adBean)) {
                    if (adBean.getStatus() == 1) {
                        AdUtils.getFlowAd(BookDetailedActivity.this, 1,
                                view -> binding.ic.getRoot().addView(view, 2), "detail");
                    } else if (adBean.getStatus() == 2) {
                        AdUtils.showInterAd(BookDetailedActivity.this, "detail");
                    }
                }
            }
        });
    }


    @Override
    protected void initClick() {
        super.initClick();
        binding.ih.bookDetailTvAuthor.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailedActivity.this, SearchBookActivity.class);
            intent.putExtra(APPCONST.SEARCH_KEY, binding.ih.bookDetailTvAuthor.getText());
            startActivity(intent);
        });
        binding.ic.bookDetailTvDesc.setOnClickListener(v -> showMoreDesc());
        binding.ic.bookDetailTvCatalogMore.setOnClickListener(v -> goToMoreChapter());
        binding.ib.flAddBookcase.setOnClickListener(view -> {
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
                binding.ib.bookDetailTvAdd.setText("移除书籍");
            } else {
                mBookService.deleteBookById(mBook.getId());
                isCollected = false;
                mBook.setHisttoryChapterNum(0);
                mBook.setHistoryChapterId("未开始阅读");
                mBook.setLastReadPosition(0);
                ToastUtils.showSuccess("成功移除书籍");
                binding.ib.bookDetailTvAdd.setText("加入书架");
                binding.ib.bookDetailTvOpen.setText("开始阅读");
            }
            invalidateOptionsMenu();
        });
        binding.ib.flOpenBook.setOnClickListener(view -> goReadActivity());

        //换源对话框
        mSourceDialog.setOnSourceChangeListener((bean, pos) -> {
            Book bookTem = mBook.changeSource(bean);
            if (isCollected) {
                mBookService.updateBook(mBook, bookTem);
            }
            mBook = bookTem;
            mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
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
        });
    }

    /**
     * 展开简介
     */
    protected void showMoreDesc() {
        if (binding.ic.bookDetailTvDesc.getMaxLines() == 5)
            binding.ic.bookDetailTvDesc.setMaxLines(15);
        else
            binding.ic.bookDetailTvDesc.setMaxLines(5);
    }

    /**
     * 章节列表
     */
    public void goToMoreChapter() {
        Intent intent = new Intent(this, CatalogActivity.class);
        BitIntentDataManager.getInstance().putData(intent, mBook);
        startActivityForResult(intent, APPCONST.REQUEST_CHAPTER_PAGE);
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
        Book book = null;
        if (!TextUtils.isEmpty(mBook.getId())){
            book = mBookService.getBookById(mBook.getId());
        }
        if (book == null) {
            book = mBookService.findBookByAuthorAndName(mBook.getName(), mBook.getAuthor());
        }
        if (book == null) {
            return false;
        } else {
            mBook = book;
            return true;
        }
    }

    private boolean isBookSourceNotExist() {
        BookSource source = BookSourceManager.getBookSourceByStr(mBook.getSource());
        return source.getSourceEName() != null && "fynovel".equals(source.getSourceEName());
    }

    /**
     * 初始化书籍信息
     */
    private void initBookInfo() {
        binding.ih.bookDetailTvAuthor.setText(mBook.getAuthor());
        if (StringHelper.isEmpty(mBook.getImgUrl())) {
            mBook.setImgUrl("");
        } else {
            if (!App.isDestroy(this)) {
                binding.ih.bookDetailIvCover.load(NetworkUtils.getAbsoluteURL(mReadCrawler.getNameSpace(), mBook.getImgUrl()), mBook.getName(), mBook.getAuthor());
            }
        }
        initTagList();
        if (StringHelper.isEmpty(mBook.getDesc()))
            binding.ic.bookDetailTvDesc.setText("");
        else
            binding.ic.bookDetailTvDesc.setText(String.format("\t\t\t\t%s", mBook.getDesc()));
        BookSource source = BookSourceManager.getBookSourceByStr(mBook.getSource());
        binding.ih.bookDetailSource.setText(String.format("书源：%s", source.getSourceName()));
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(source);
        if ((rc instanceof BookInfoCrawler && StringHelper.isEmpty(mBook.getImgUrl()))) {
            binding.pbLoading.setVisibility(View.VISIBLE);
            BookInfoCrawler bic = (BookInfoCrawler) rc;
            BookApi.getBookInfo(mBook, bic).compose(RxUtils::toSimpleSingle).subscribe(new MyObserver<Book>() {
                @Override
                public void onSubscribe(Disposable d) {
                    addDisposable(d);
                }

                @Override
                public void onNext(@NotNull Book book) {
                    if (!App.isDestroy(BookDetailedActivity.this)) {
                        mHandler.sendMessage(mHandler.obtainMessage(4));
                    }

                }

                @Override
                public void onError(Throwable e) {
                    if (!App.isDestroy(BookDetailedActivity.this)) {
                        ToastUtils.showError("书籍详情加载失败！");
                        binding.pbLoading.setVisibility(View.GONE);
                    }
                    if (App.isDebug()) e.printStackTrace();
                }
            });

        } else {
            initOtherInfo();
            //predictBookCount();
        }
    }

    /**
     * 初始化其他书籍信息
     */
    private void initOtherInfo() {
        binding.ic.bookDetailTvDesc.setText(String.format("\t\t\t\t%s", mBook.getDesc()));
        initTagList();
        if (!App.isDestroy(this)) {
            binding.ih.bookDetailIvCover.load(NetworkUtils.getAbsoluteURL(mReadCrawler.getNameSpace(), mBook.getImgUrl()), mBook.getName(), mBook.getAuthor());
        }
    }

    private RequestBuilder<Drawable> defaultCover() {
        return Glide.with(this)
                .load(R.mipmap.default_cover)
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
    }

    /**
     * 初始化章节目录
     */
    private void initChapters(boolean isChangeSource) {
        if (mChapters.size() == 0 && !"本地书籍".equals(mBook.getType())) {
            if (isCollected) {
                mChapters = mChapterService.findBookAllChapterByBookId(mBook.getId());
            }
            if (chaptersDis != null) chaptersDis.dispose();
            BookApi.getBookChapters(mBook, mReadCrawler)
                    .flatMap((Function<List<Chapter>, ObservableSource<Boolean>>) chapters -> saveChapters(chapters, isChangeSource)).compose(RxUtils::toSimpleSingle)
                    .subscribe(new MyObserver<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            chaptersDis = d;
                            addDisposable(d);
                        }

                        @Override
                        public void onNext(@NotNull Boolean aBoolean) {
                            mCatalogAdapter.refreshItems(mNewestChapters);
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.pbLoading.setVisibility(View.GONE);
                            mCatalogAdapter.clear();
                            if (App.isDebug()) e.printStackTrace();
                            ToastUtils.showError("最新章节加载失败！\n" + e.getLocalizedMessage());
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

    private Observable<Boolean> saveChapters(List<Chapter> chapters, boolean isChangeSource) {
        return Observable.create(emitter -> {
            mBook.setNewestChapterTitle(chapters.get(chapters.size() - 1).getTitle());
            if (isCollected) {
                int noReadNum = chapters.size() - mBook.getChapterTotalNum();
                mBook.setNoReadNum(Math.max(noReadNum, 0));
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
            emitter.onNext(true);
            emitter.onComplete();
        });
    }

    /**
     * 前往阅读界面
     */
    private void goReadActivity() {
        if (!isCollected) {
            mBookService.addBook(mBook);
        }
        Intent intent = new Intent(this, ReadActivity.class);
        aBooks = mSourceDialog.getaBooks();
        if (aBooks != null && aBooks.size() > 0) {
            int sourceIndex = mSourceDialog.getSourceIndex();
            if (mSourceDialog.hasCurBookSource())
                aBooks.set(sourceIndex, mBook);
            BitIntentDataManager.getInstance().putData(intent, aBooks);
            intent.putExtra(APPCONST.SOURCE_INDEX, sourceIndex);
        } else {
            BitIntentDataManager.getInstance().putData(intent, mBook);
        }
//        BitIntentDataManager.getInstance().putData(intent, mBook);
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
            getMenuInflater().inflate(R.menu.menu_book_detail_local, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if ("本地书籍".equals(mBook.getType())) {
            MenuItem groupSetting = menu.findItem(R.id.action_group_setting);
            MenuItem edit = menu.findItem(R.id.action_edit);
            groupSetting.setVisible(isCollected);
            edit.setVisible(isCollected);
        } else {
            MenuItem isUpdate = menu.findItem(R.id.action_is_update);
            MenuItem groupSetting = menu.findItem(R.id.action_group_setting);
            MenuItem edit = menu.findItem(R.id.action_edit);
            if (isCollected) {
                isUpdate.setVisible(true);
                groupSetting.setVisible(true);
                edit.setVisible(true);
                isUpdate.setChecked(!mBook.getIsCloseUpdate());
            } else {
                isUpdate.setVisible(false);
                groupSetting.setVisible(false);
                edit.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 导航栏菜单点击事件
     *
     * @param item
     * @return
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_source:  //换源
                if (!NetworkUtils.isNetWorkAvailable()) {
                    ToastUtils.showWarring("无网络连接！");
                    return true;
                }
                mSourceDialog.show();
                break;
            case R.id.action_share:
                ShareBookUtil.shareBook(this, mBook, binding.ih.bookDetailIvCover);
                break;
            case R.id.action_edit:
                Intent editIntent = new Intent(this, BookInfoEditActivity.class);
                BitIntentDataManager.getInstance().putData(editIntent, mBook);
                startActivityForResult(editIntent, APPCONST.REQUEST_EDIT_BOOK);
                break;
            case R.id.action_reload:  //重新加载
                mHandler.sendEmptyMessage(1);
                break;
            case R.id.action_is_update://是否更新
                mBook.setIsCloseUpdate(!mBook.getIsCloseUpdate());
                mBookService.updateEntity(mBook);
                break;
            case R.id.action_open_link:  //打开链接
                Uri uri = Uri.parse(NetworkUtils.getAbsoluteURL(mReadCrawler.getNameSpace(), mBook.getChapterUrl()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.action_group_setting:
                mBookGroupDia.addGroup(mBook, null);
                break;
            case R.id.action_edit_source:
                BookSource source = BookSourceManager.getBookSourceByStr(mBook.getSource());
                if (TextUtils.isEmpty(source.getSourceType())) {
                    ToastUtils.showWarring("内置书源无法编辑！");
                } else {
                    Intent sourceIntent = new Intent(this, SourceEditActivity.class);
                    sourceIntent.putExtra(APPCONST.BOOK_SOURCE, source);
                    startActivity(sourceIntent);
                }
                break;
            case R.id.action_word_count:
                getWordCount();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getWordCount() {

        LoadingDialog dialog = new LoadingDialog(this, "正在计算", () -> {
            if (wcDis != null) {
                wcDis.dispose();
            }
        });
        dialog.show();
        Single.create((SingleOnSubscribe<Integer>) emitter -> {
            BookWCEstimate bookWCEstimate = new BookWCEstimate();
            emitter.onSuccess(bookWCEstimate.getWordCount(mBook));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                addDisposable(d);
                wcDis = d;
            }

            @Override
            public void onSuccess(@NonNull Integer count) {
                dialog.dismiss();
                if (count == -1) {
                    ToastUtils.showWarring("书籍源文件不存在，无法计算字数");
                } else if (count == -2) {
                    ToastUtils.showWarring("已缓存章节数量不足20，无法计算字数");
                } else if (count == -3) {
                    ToastUtils.showWarring("epub书籍暂不支持计算字数");
                } else {
                    String tip = "\n";
                    if (count > 0) {
                        tip += "注：当前为网络书籍，无法获取准确字数，软件采用简单线性回归模型进行字数预测，仅供参考";
                    } else {
                        tip += "注：当前为本地书籍，字数统计自书籍源文件";
                    }
                    count = count < 0 ? -count : count;
                    String countTag = count > 100000 ? count / 10000 + "万" : count + "";
                    mBook.setWordCount(countTag);
                    initTagList();
                    mBookService.updateEntity(mBook);
                    DialogCreator.createTipDialog(BookDetailedActivity.this, "书籍字数(实验性功能)",
                            "书籍字数计算成功，本书字数：" + count + tip);
                }
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showError("书籍字数计算失败！\n" + e.getLocalizedMessage());
                dialog.dismiss();
            }
        });
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
        super.onActivityResult(requestCode, resultCode, data);
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
                        binding.ib.bookDetailTvAdd.setText("移除书籍");
                        binding.ib.bookDetailTvOpen.setText("继续阅读");
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
                case APPCONST.REQUEST_EDIT_BOOK:
                    mBook = BookService.getInstance().getBookById(mBook.getId());
                    initBookInfo();
                    break;
            }
        }
    }

}
