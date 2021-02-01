package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import xyz.fycz.myreader.BuildConfig;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.databinding.ActivityBookDetailBinding;
import xyz.fycz.myreader.entity.SharedBook;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.ui.adapter.DetailCatalogAdapter;
import xyz.fycz.myreader.ui.dialog.BookGroupDialog;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.SourceExchangeDialog;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.BitmapUtil;
import xyz.fycz.myreader.util.utils.BlurTransformation;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.util.utils.StringUtils;
import xyz.fycz.myreader.webapi.CommonApi;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

/**
 * @author fengyue
 * @date 2020/8/17 11:39
 */
public class BookDetailedActivity extends BaseActivity {

    private ActivityBookDetailBinding binding;

    private Book mBook;
    private ArrayList<Book> aBooks;
    private BookService mBookService;
    private ChapterService mChapterService;
    private ReadCrawler mReadCrawler;
    private DetailCatalogAdapter mCatalogAdapter;
    private ArrayList<Chapter> mChapters = new ArrayList<>();
    private ArrayList<Chapter> mNewestChapters = new ArrayList<>();
    private boolean isCollected;
    private SourceExchangeDialog mSourceDialog;
    private int sourceIndex;
    private BookGroupDialog mBookGroupDia;

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
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        aBooks = (ArrayList<Book>) getIntent().getSerializableExtra(APPCONST.SEARCH_BOOK_BEAN);
        sourceIndex = getIntent().getIntExtra(APPCONST.SOURCE_INDEX, 0);
        if (aBooks != null) {
            mBook = aBooks.get(sourceIndex);
        } else {
            mBook = (Book) getIntent().getSerializableExtra(APPCONST.BOOK);
        }
        isCollected = isBookCollected();
        if (isCollected) {
            mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(mBook.getId());
        }
        mBookGroupDia = new BookGroupDialog(this);
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

        //Dialog
        mSourceDialog = new SourceExchangeDialog(this, mBook);
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
    }

    @Override
    protected void initClick() {
        super.initClick();
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
        });
        binding.ib.flOpenBook.setOnClickListener(view -> goReadActivity());

        //换源对话框
        mSourceDialog.setOnSourceChangeListener((bean, pos) -> {
            Book bookTem = (Book) mBook.clone();
            bookTem.setChapterUrl(bean.getChapterUrl());
            if (!StringHelper.isEmpty(bean.getImgUrl())) {
                bookTem.setImgUrl(bean.getImgUrl());
            }
            if (!StringHelper.isEmpty(bean.getType())) {
                bookTem.setType(bean.getType());
            }
            if (!StringHelper.isEmpty(bean.getDesc())) {
                bookTem.setDesc(bean.getDesc());
            }
            bookTem.setSource(bean.getSource());
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
        intent.putExtra(APPCONST.BOOK, mBook);
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
        binding.ih.bookDetailTvAuthor.setText(mBook.getAuthor());
        if (StringHelper.isEmpty(mBook.getImgUrl())) {
            mBook.setImgUrl("");
        }
        assert mBook.getNewestChapterTitle() != null;
        binding.ic.bookDetailTvDesc.setText("");
        if (mBook.getType() != null) {
            binding.ih.bookDetailTvType.setText(mBook.getType());
        } else {
            binding.ih.bookDetailTvType.setText("");
        }
        if (!"null".equals(mBook.getSource())) {
            binding.ih.bookDetailSource.setText("书源：" + BookSource.fromString(mBook.getSource()).text);
        }
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
        if (rc instanceof BookInfoCrawler && StringHelper.isEmpty(mBook.getImgUrl())) {
            binding.pbLoading.setVisibility(View.VISIBLE);
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
        binding.ic.bookDetailTvDesc.setText("\t\t\t\t" + mBook.getDesc());
        binding.ih.bookDetailTvType.setText(mBook.getType());
        if (!MyApplication.isDestroy(this)) {
            binding.ih.bookDetailIvCover.load(mBook.getImgUrl(), mBook.getName(), mBook.getAuthor());
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
        /*pbLoading.setVisibility(View.GONE);
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
        dialog.show();*/

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
            CommonApi.getBookChapters(mBook.getChapterUrl(), mReadCrawler, isChangeSource, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
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
                    MyApplication.runOnUiThread(() -> mCatalogAdapter.refreshItems(mNewestChapters));
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
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
        MenuItem groupSetting = menu.findItem(R.id.action_group_setting);
        if (isCollected) {
            isUpdate.setVisible(true);
            //groupSetting.setVisible(true);
            isUpdate.setChecked(!mBook.getIsCloseUpdate());
        } else {
            isUpdate.setVisible(false);
            groupSetting.setVisible(false);
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
                /*pbLoading.setVisibility(View.VISIBLE);
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
                }*/
                mSourceDialog.show();
                break;
            case R.id.action_share:
                shareBook();
                break;
            case R.id.action_reload:  //重新加载
                mHandler.sendEmptyMessage(1);
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
            case R.id.action_group_setting:
                mBookGroupDia.addGroup(mBook, new BookGroupDialog.OnGroup() {
                    @Override
                    public void change() {

                    }

                    @Override
                    public void addGroup() {

                    }
                });
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
            }
        }
    }

    /**
     * 分享书籍
     */
    private void shareBook() {
        ToastUtils.showInfo("正在生成分享图片");
        Single.create((SingleOnSubscribe<File>) emitter -> {
            // 使用url
            String url = SharedPreUtils.getInstance().getString(getString(R.string.downloadLink), URLCONST.LAN_ZOUS_URL);
            if (url == null)
                url = "";

            int maxLength = 1273 - 1 - url.length();

            SharedBook sharedBook = SharedBook.bookToSharedBook(mBook);

            url = url + "#" + GsonExtensionsKt.getGSON().toJson(sharedBook);

            Log.d("QRcode", "Length=" + url.length() + "\n" + url);

            Bitmap bitmap;
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            bitmap = QRCodeEncoder.syncEncodeQRCode(url, 360);
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            File share = makeShareFile(bitmap);
            if (share == null) {
                ToastUtils.showError("分享图片生成失败");
                return;
            }
            emitter.onSuccess(share);
        }).compose(RxUtils::toSimpleSingle)
          .subscribe(new MySingleObserver<File>() {
              @Override
              public void onSuccess(@NonNull File File) {
                  share(File);
              }
          });
    }

    /**
     * 生成分享图片
     * @param QRCode
     * @return
     */

    private File makeShareFile(Bitmap QRCode) {
        FileOutputStream fos = null;
        try {
            Bitmap back = BitmapFactory.decodeStream(getResources().getAssets().open("share.png")).copy(Bitmap.Config.ARGB_8888, true);
            int backWidth = back.getWidth();
            int backHeight = back.getHeight();

            int margin = 60;

            int marginTop = 24;

            binding.ih.bookDetailIvCover.setDrawingCacheEnabled(true);
            Bitmap img = Bitmap.createBitmap(binding.ih.bookDetailIvCover.getDrawingCache()).copy(Bitmap.Config.ARGB_8888, true);
            binding.ih.bookDetailIvCover.setDrawingCacheEnabled(false);
            img = BitmapUtil.getBitmap(img, 152, 209);

            Canvas cv = new Canvas(back);
            cv.drawBitmap(img, margin, margin + marginTop * 2, null);

            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setFilterBitmap(true);
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(40);

            String name = TextUtils.ellipsize(mBook.getName(), textPaint, backWidth - margin + marginTop * 3 - img.getWidth(), TextUtils.TruncateAt.END).toString();
            cv.drawText(name, margin + marginTop + img.getWidth(), margin + marginTop * 4, textPaint);


            textPaint.setColor(getResources().getColor(R.color.origin));
            textPaint.setTextSize(32);
            cv.drawText(mBook.getAuthor(), margin + marginTop + img.getWidth(), margin + marginTop * 6, textPaint);

            textPaint.setColor(Color.BLACK);
            cv.drawText(mBook.getType() == null ? "" : mBook.getType(), margin + marginTop + img.getWidth(), margin + marginTop * 8, textPaint);
            assert mBook.getSource() != null;
            cv.drawText("书源：" + BookSource.fromString(mBook.getSource()).text, margin + marginTop + img.getWidth(), margin + marginTop * 10, textPaint);

            int textSize = 35;
            int textInterval = textSize / 2;
            textPaint.setTextSize(textSize);

            drawDesc(getDescLines(backWidth - margin * 2, textPaint), textPaint, cv, margin + marginTop * 4 + img.getHeight(), margin, textInterval);

            cv.drawBitmap(QRCode, backWidth - QRCode.getWidth(), backHeight - QRCode.getHeight(), null);

            cv.save();// 保存
            cv.restore();// 存储

            File share = FileUtils.getFile(APPCONST.SHARE_FILE_DIR + mBook.getName() + "_share.png");
            fos = new FileOutputStream(share);
            back.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Log.i("tag", "saveBitmap success: " + share.getAbsolutePath());

            back.recycle();
            img.recycle();
            QRCode.recycle();

            return share;
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showError(e.getLocalizedMessage() + "");
            return null;
        } finally {
            IOUtils.close(fos);
        }
    }

    /**
     * 分享生成的图片
     * @param share
     */
    private void share(File share) {
        //noinspection ResultOfMethodCallIgnored
        share.setReadable(true, false);
        Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", share);
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, "分享书籍"));
    }

    /**
     * 绘制简介
     * @param lines
     * @param textPaint
     * @param canvas
     * @param top
     * @param left
     * @param textInterval
     */
    private void drawDesc(List<String> lines, TextPaint textPaint, Canvas canvas, int top, int left, int textInterval) {
        float interval = textInterval + textPaint.getTextSize();
        for (String line : lines) {
            canvas.drawText(line, left, top, textPaint);
            top += interval;
        }
    }

    /**
     * 生成简介lines
     * @param width
     * @param textPaint
     * @return
     */

    private List<String> getDescLines(int width, TextPaint textPaint) {
        List<String> lines = new ArrayList<>();
        String desc = StringUtils.halfToFull("  ") + mBook.getDesc();
        int i = 0;
        int wordCount = 0;
        String subStr = null;
        while (desc.length() > 0) {
            if (i == 9) {
                lines.add(TextUtils.ellipsize(desc, textPaint, width / 1.8f, TextUtils.TruncateAt.END).toString());
                break;
            }
            wordCount = textPaint.breakText(desc, true, width, null);
            subStr = desc.substring(0, wordCount);
            lines.add(subStr);
            desc = desc.substring(wordCount);
            i++;
        }
        return lines;
    }
}
