package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.gyf.immersionbar.ImmersionBar;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import xyz.fycz.myreader.ActivityManage;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.databinding.ActivityReadBinding;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.enums.Font;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.BookMark;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.ReplaceRuleBean;
import xyz.fycz.myreader.greendao.service.BookGroupService;
import xyz.fycz.myreader.greendao.service.BookMarkService;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.model.audio.ReadAloudService;
import xyz.fycz.myreader.model.storage.Backup;
import xyz.fycz.myreader.ui.dialog.AudioPlayerDialog;
import xyz.fycz.myreader.ui.dialog.CopyContentDialog;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.ui.dialog.ReplaceDialog;
import xyz.fycz.myreader.ui.dialog.SourceExchangeDialog;
import xyz.fycz.myreader.ui.popmenu.AutoPageMenu;
import xyz.fycz.myreader.ui.popmenu.BrightnessEyeMenu;
import xyz.fycz.myreader.ui.popmenu.CustomizeComMenu;
import xyz.fycz.myreader.ui.popmenu.CustomizeLayoutMenu;
import xyz.fycz.myreader.ui.popmenu.ReadSettingMenu;
import xyz.fycz.myreader.util.BrightUtil;
import xyz.fycz.myreader.util.DateHelper;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.SystemUtil;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.notification.NotificationClickReceiver;
import xyz.fycz.myreader.util.notification.NotificationUtil;
import xyz.fycz.myreader.util.utils.ColorUtil;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.ScreenUtils;
import xyz.fycz.myreader.util.utils.StringUtils;
import xyz.fycz.myreader.util.utils.SystemBarUtils;
import xyz.fycz.myreader.webapi.CommonApi;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.widget.BubblePopupView;
import xyz.fycz.myreader.widget.page.LocalPageLoader;
import xyz.fycz.myreader.widget.page.PageLoader;
import xyz.fycz.myreader.widget.page.PageMode;
import xyz.fycz.myreader.widget.page.PageView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static xyz.fycz.myreader.util.UriFileUtil.getPath;

/**
 * @author fengyue
 * @date 2020/10/21 16:46
 */
public class ReadActivity extends BaseActivity implements ColorPickerDialogListener, View.OnTouchListener {
    private static final String TAG = ReadActivity.class.getSimpleName();

    /*****************************View***********************************/
    private ActivityReadBinding binding;

    /***************************variable*****************************/
    private Book mBook;
    private ArrayList<Chapter> mChapters = new ArrayList<>();
    private ChapterService mChapterService;
    private BookService mBookService;
    private BookMarkService mBookMarkService;
    private NotificationUtil notificationUtil;
    private Setting mSetting;

    private boolean isCollected = true;//是否在书架中

    private boolean isPrev;//是否向前翻页

    private boolean autoPage = false;//是否自动翻页

    private boolean loadFinish = false;

    private int curCacheChapterNum = 0;//缓存章节数

    private int needCacheChapterNum;//需要缓存的章节

    private PageLoader mPageLoader;//页面加载器

    private int screenTimeOut;//息屏时间（单位：秒），dengy零表示常亮

    private Runnable keepScreenRunnable;//息屏线程
    private Runnable autoPageRunnable;//自动翻页
    private Runnable sendDownloadNotification;
    private static boolean isStopDownload = true;

    private int tempCacheChapterNum;
    private int tempCount;
    private String downloadingChapter;

    private ReadCrawler mReadCrawler;

    private int upHpbInterval = 30;//更新翻页进度速度

    private int downloadInterval = 150;

    private SourceExchangeDialog mSourceDialog;
    private AudioPlayerDialog mAudioPlayerDialog;

    private boolean hasChangeSource;

    private int pagePos;

    private int chapterPos;

    private Animation mTopInAnim;
    private Animation mTopOutAnim;
    private Animation mBottomInAnim;
    private Animation mBottomOutAnim;
    private int lastX, lastY;

    // 接收电池信息和时间更新的广播
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                try {
                    mPageLoader.updateBattery(level);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 监听分钟的变化
            else if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                try {
                    mPageLoader.updateTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    init();
                    break;
                case 2:
                    try {
                        int chapterPos = msg.arg1;
                        int pagePos = msg.arg2;
                        mPageLoader.skipToChapter(chapterPos);
                        mPageLoader.skipToPage(pagePos);
                    } catch (Exception e) {
                        ToastUtils.showError("章节跳转失败，请截图联系作者。\n" +
                                e.getMessage());
                        e.printStackTrace();
                    }
                    binding.pbLoading.setVisibility(View.GONE);
                    break;
                case 3:
                    break;
                case 4:
                    saveLastChapterReadPosition();
                    screenOffTimerStart();
                    initMenu();
                    break;
                case 5:
                    if (mPageLoader != null) {
                        mPageLoader.refreshUi();
                    }
                    break;
                case 6:
                    mPageLoader.openChapter();
                    if (isPrev) {//判断是否向前翻页打开章节，如果是则打开自己后跳转到最后一页，否则不跳转
                        try {//概率性异常（空指针异常）
                            mPageLoader.skipToPage(mPageLoader.getAllPagePos() - 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 7:
                    ToastUtils.showWarring("无网络连接！");
                    mPageLoader.chapterError();
                    break;
                case 8:
                    binding.pbLoading.setVisibility(View.GONE);
                    break;
                case 9:
                    ToastUtils.showInfo("正在后台缓存书籍，具体进度可查看通知栏！");
                    notificationUtil.requestNotificationPermissionDialog(ReadActivity.this);
                    break;
                case 10:
                    if (mPageLoader != null) {
                        mPageLoader.chapterError();
                    }
            }
        }
    };


    @Override
    protected void bindView() {
        binding = ActivityReadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**************************override***********************************/


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            pagePos = savedInstanceState.getInt("pagePos");
            chapterPos = savedInstanceState.getInt("chapterPos");
        } else {
            pagePos = -1;
            chapterPos = -1;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mBook != null) {
            outState.putInt("pagePos", mBook.getLastReadPosition());
            outState.putInt("chapterPos", mBook.getHisttoryChapterNum());
        }
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        getSupportActionBar().setTitle(mBook.getName());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        mBookMarkService = BookMarkService.getInstance();
        mSetting = SysManager.getSetting();
        if (!loadBook()) {
            finish();
            return;
        }
        if (pagePos != -1 && chapterPos != -1) {
            mBook.setHisttoryChapterNum(chapterPos);
            mBook.setLastReadPosition(pagePos);
        }
        if (SharedPreUtils.getInstance().getBoolean(getString(R.string.isNightFS), false)) {
            mSetting.setDayStyle(!ColorUtil.isColorLight(getResources().getColor(R.color.textPrimary)));
        }
        //息屏时间
        screenTimeOut = mSetting.getResetScreen() * 60;
        //保持屏幕常亮
        keepScreenRunnable = this::unKeepScreenOn;
        autoPageRunnable = this::nextPage;
        sendDownloadNotification = this::sendNotification;

        notificationUtil = NotificationUtil.getInstance();
        isCollected = getIntent().getBooleanExtra("isCollected", true);
        hasChangeSource = getIntent().getBooleanExtra("hasChangeSource", false);

        mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());

        mPageLoader = binding.readPvContent.getPageLoader(mBook, mReadCrawler, mSetting);
        //Dialog
        mSourceDialog = new SourceExchangeDialog(this, mBook);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        ImmersionBar.with(this).fullScreen(true).init();
        //隐藏StatusBar
        binding.readPvContent.post(
                this::hideSystemBar
        );
        if (!mSetting.isBrightFollowSystem()) {
            BrightUtil.setBrightness(this, mSetting.getBrightProgress());
        }
        binding.pbLoading.setVisibility(View.VISIBLE);
        initEyeView();
        initSettingListener();
        initTopMenu();
        initBottomMenu();
        setOrientation(mSetting.isHorizontalScreen());
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initClick() {
        super.initClick();
        binding.readPvContent.setTouchListener(new PageView.TouchListener() {
            @Override
            public boolean onTouch() {
                screenOffTimerStart();
                return !hideReadMenu();
            }

            @Override
            public void center() {
                toggleMenu(true);
            }

            @Override
            public void prePage() {
                isPrev = true;
            }

            @Override
            public void nextPage(boolean hasNextPage) {
                isPrev = false;
                if (!hasNextPage) {
                    if (autoPage) {
                        autoPageStop();
                    }
                }
            }

            @Override
            public void cancel() {
            }

            @Override
            public void onTouchClearCursor() {
                binding.cursorLeft.setVisibility(View.INVISIBLE);
                binding.cursorRight.setVisibility(View.INVISIBLE);
                longPressMenu.hidePopupListWindow();
            }

            @Override
            public void onLongPress() {
                if (!binding.readPvContent.isRunning()) {
                    selectTextCursorShow();
                    showAction();
                }
            }
        });

        mPageLoader.setOnPageChangeListener(
                new PageLoader.OnPageChangeListener() {
                    @Override
                    public void onChapterChange(int pos) {
                        lastLoad(pos);
                        for (int i = 0; i < 5; i++) {
                            preLoad(pos - 1 + i);
                        }
                        mBook.setHistoryChapterId(mChapters.get(pos).getTitle());
                        MyApplication.getApplication().newThread(() -> {
                            if (mPageLoader == null) {
                                return;
                            }
                            if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
                                if (!NetworkUtils.isNetWorkAvailable()) {
                                    mHandler.sendMessage(mHandler.obtainMessage(7));
                                } else {
                                    mHandler.sendMessage(mHandler.obtainMessage(6));
                                }
                            }
                        });
                    }

                    @Override
                    public void requestChapters(List<Chapter> requestChapters) {
                        /*for (final Chapter chapter : requestChapters){
                            getChapterContent(chapter, null);
                        }*/
                    }

                    @Override
                    public void onCategoryFinish(List<Chapter> chapters) {
                    }

                    @Override
                    public void onPageCountChange(int count) {

                    }

                    @Override
                    public void onPageChange(int pos, boolean resetRead) {
                        mHandler.sendMessage(mHandler.obtainMessage(4));
                        if (ReadAloudService.running) {
                            if (mPageLoader.hasChapterData(mChapters.get(mPageLoader.getChapterPos()))) {
                                if (resetRead) {
                                    if (mAudioPlayerDialog != null) {
                                        mHandler.postDelayed(() -> mAudioPlayerDialog.readAloud(), 400);
                                    }
                                    return;
                                }
                                if (pos == 0) {
                                    if (mAudioPlayerDialog != null) {
                                        mHandler.postDelayed(() -> mAudioPlayerDialog.readAloud(), 400);
                                    }
                                }
                            } else {
                                ReadAloudService.stop(ReadActivity.this);
                            }
                        }
                    }
                }
        );

        binding.readSbChapterProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    binding.readTvPageTip.setText((progress + 1) + "/" + (seekBar.getMax() + 1));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //进行切换
                int pagePos = seekBar.getProgress();
                if (pagePos != mPageLoader.getPagePos() && pagePos < mPageLoader.getAllPagePos()) {
                    mPageLoader.skipToPage(pagePos);
                }
            }
        });

        initBottomMenuClick();

        mSourceDialog.setOnSourceChangeListener((bean, pos) -> {
            Book bookTem = (Book) mBook.clone();
            bookTem.setChapterUrl(bean.getChapterUrl());
            bookTem.setSource(bean.getSource());
            if (!StringHelper.isEmpty(bean.getImgUrl())) {
                bookTem.setImgUrl(bean.getImgUrl());
            }
            if (!StringHelper.isEmpty(bean.getType())) {
                bookTem.setType(bean.getType());
            }
            if (!StringHelper.isEmpty(bean.getDesc())) {
                bookTem.setDesc(bean.getDesc());
            }
            if (isCollected) {
                mBookService.updateBook(mBook, bookTem);
            }
            mBook = bookTem;
            toggleMenu(true);
            Intent intent = new Intent(this, ReadActivity.class)
                    .putExtra(APPCONST.BOOK, mBook)
                    .putExtra("hasChangeSource", true);
            if (!isCollected) {
                intent.putExtra("isCollected", false);
            }
            finish();
            startActivity(intent);
        });

        binding.readTvListenBook.setOnClickListener(v -> {
            if (mSetting.getPageMode() == PageMode.SCROLL) {
                ToastUtils.showWarring("朗读暂不支持滚动翻页模式!");
                return;
            }
            toggleMenu(true);
            if (mAudioPlayerDialog == null) {
                mAudioPlayerDialog = new AudioPlayerDialog(this, mPageLoader);
                mAudioPlayerDialog.setOnDismissListener(dialog -> hideSystemBar());
            }
            if (!ReadAloudService.running) {
                mAudioPlayerDialog.aloudStatus = ReadAloudService.Status.STOP;
                SystemUtil.ignoreBatteryOptimization(this);
            }
            mAudioPlayerDialog.show();
        });
        initReadLongPressPop();
        binding.cursorLeft.setOnTouchListener(this);
        binding.cursorRight.setOnTouchListener(this);
        binding.rlContent.setOnTouchListener(this);
    }

    protected void initBottomMenuClick() {
        //设置
        binding.readTvSetting.setOnClickListener(v -> {
            toggleMenu(false);
            binding.readSettingMenu.startAnimation(mBottomInAnim);
            binding.readSettingMenu.setVisibility(VISIBLE);
        });
        //上一章
        binding.readTvPreChapter.setOnClickListener(v -> mPageLoader.skipPreChapter());
        //下一章
        binding.readTvNextChapter.setOnClickListener(v -> mPageLoader.skipNextChapter());
        //护眼
        binding.readTvBrightnessEye.setOnClickListener(v -> {
            hideReadMenu();
            binding.readBrightnessEyeMenu.initWidget();
            binding.readBrightnessEyeMenu.setVisibility(VISIBLE);
            binding.readBrightnessEyeMenu.startAnimation(mBottomInAnim);
        });
        //目录
        binding.readTvCategory.setOnClickListener(v -> {
            //切换菜单
            toggleMenu(true);
            //跳转
            mHandler.postDelayed(() -> {
                Intent intent = new Intent(this, CatalogActivity.class);
                intent.putExtra(APPCONST.BOOK, mBook);
                this.startActivityForResult(intent, APPCONST.REQUEST_CHAPTER_PAGE);
            }, mBottomOutAnim.getDuration());
        });
        //跳转链接
        binding.llChapterView.setOnClickListener(v -> {
            if (mChapters != null && mChapters.size() != 0) {
                Chapter curChapter = mChapters.get(mPageLoader.getChapterPos());
                String url = curChapter.getUrl();
                if (!"本地书籍".equals(mBook.getType()) && !StringHelper.isEmpty(url)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(url);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        });
    }
    @Override
    protected void processLogic() {
        super.processLogic();
        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, intentFilter);
        //当书籍Collected且书籍id不为空的时候保存上次阅读信息
        String privateGroupId = SharedPreUtils.getInstance().getString("privateGroupId");
        if (isCollected && !StringHelper.isEmpty(mBook.getId())
                && !privateGroupId.equals(mBook.getGroupId())) {
            //保存上次阅读信息
            SharedPreUtils.getInstance().putString(getString(R.string.lastRead), mBook.getId());
        }
        //保存最近阅读时间
        mBook.setLastReadTime(DateHelper.getLongDate());
        getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBar();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isVolumeTurnPage = SysManager.getSetting().isVolumeTurnPage();
        if (binding.readAblTopMenu.getVisibility() != View.VISIBLE &&
                binding.readCustomizeLayoutMenu.getVisibility() != View.VISIBLE &&
                binding.readAutoPageMenu.getVisibility() != View.VISIBLE &&
                binding.readCustomizeMenu.getVisibility() != View.VISIBLE &&
                binding.readSettingMenu.getVisibility() != View.VISIBLE &&
                binding.readBrightnessEyeMenu.getVisibility() != View.VISIBLE) {
            if (binding.readPvContent.getSelectMode() != PageView.SelectMode.Normal) {
                clearSelect();
            }
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (ReadAloudService.running) {
                        if (mSetting.isReadAloudVolumeTurnPage()) {
                            return mPageLoader.skipToPrePage();
                        }
                    } else {
                        if (isVolumeTurnPage) {
                            return mPageLoader.skipToPrePage();
                        }
                    }
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (ReadAloudService.running) {
                        if (mSetting.isReadAloudVolumeTurnPage()) {
                            return mPageLoader.skipToNextPage();
                        }
                    } else {
                        if (isVolumeTurnPage) {
                            return mPageLoader.skipToNextPage();
                        }
                    }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (hideReadMenu()) {
            return;
        } else if (ReadAloudService.running && mAudioPlayerDialog.aloudStatus == ReadAloudService.Status.PLAY) {
            ReadAloudService.pause(this);
            ToastUtils.showInfo("朗读暂停！");
            return;
        }
        finish();
    }

    @Override
    public void finish() {
        if (!isCollected) {
            DialogCreator.createCommonDialog(this, "加入书架", "喜欢本书就加入书架吧", true, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveLastChapterReadPosition();
                            isCollected = true;
                            exit();
                        }
                    }
                    , (dialog, which) -> {
                        mBookService.deleteBookById(mBook.getId());
                        exit();
                    });
        } else {
            saveLastChapterReadPosition();
            exit();
        }
    }

    private void exit() {
        // 返回给BookDetail
        Intent result = new Intent();
        result.putExtra(APPCONST.RESULT_IS_COLLECTED, isCollected);
        if (mPageLoader != null) {
            result.putExtra(APPCONST.RESULT_LAST_READ_POSITION, mPageLoader.getPagePos());
            result.putExtra(APPCONST.RESULT_HISTORY_CHAPTER, mPageLoader.getChapterPos());
        }
        setResult(AppCompatActivity.RESULT_OK, result);
        if (!ActivityManage.isExist(MainActivity.class)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        Backup.INSTANCE.autoBack();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mHandler.removeCallbacks(keepScreenRunnable);
        mHandler.removeCallbacks(autoPageRunnable);
        /*mHandler.removeCallbacks(sendDownloadNotification);
        notificationUtil.cancelAll();
        MyApplication.getApplication().shutdownThreadPool();*/
        if (autoPage) {
            autoPageStop();
        }
        ReadAloudService.stop(this);
        for (int i = 0; i < 9; i++) {
            mHandler.removeMessages(i + 1);
        }
        if (mPageLoader != null) {
            mPageLoader.closeBook();
            mPageLoader = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_read, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.action_load_finish, loadFinish);
        if ("本地书籍".equals(mBook.getType())) {
            menu.findItem(R.id.action_change_source).setVisible(false);
            menu.findItem(R.id.action_open_link).setVisible(false);
            menu.findItem(R.id.action_download).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_change_source) {
            mSourceDialog.show();
        } else if (itemId == R.id.action_reload) {
            isPrev = false;
            if (!"本地书籍".equals(mBook.getType())) {
                mChapterService.deleteChapterCacheFile(mChapters.get(mPageLoader.getChapterPos()));
            }
            mPageLoader.refreshChapter(mChapters.get(mPageLoader.getChapterPos()));
        } else if (itemId == R.id.action_add_bookmark) {
            if (mChapters == null || mChapters.size() == 0) {
                if ("本地书籍".equals(mBook.getType())) {
                    ToastUtils.showWarring("请等待章节拆分完成后再添加书签");
                } else {
                    ToastUtils.showError("章节目录为空，添加书签失败!");
                }
                return true;
            }
            Chapter curChapter = mChapters.get(mPageLoader.getChapterPos());
            BookMark bookMark = new BookMark();
            bookMark.setBookId(mBook.getId());
            bookMark.setTitle(curChapter.getTitle());
            bookMark.setBookMarkChapterNum(mPageLoader.getChapterPos());
            bookMark.setBookMarkReadPosition(mPageLoader.getPagePos());
            mBookMarkService.addOrUpdateBookMark(bookMark);
            DialogCreator.createTipDialog(this, "《" + mBook.getName() +
                    "》：" + bookMark.getTitle() + "[" + (bookMark.getBookMarkReadPosition() + 1) +
                    "]\n书签添加成功，书签列表可在目录界面查看！");
            return true;
        } else if (itemId == R.id.action_replace_content) {
            Intent ruleIntent = new Intent(this, RuleActivity.class);
            startActivityForResult(ruleIntent, APPCONST.REQUEST_REFRESH_READ_UI);
        } else if (itemId == R.id.action_copy_content) {
            new CopyContentDialog(this, mPageLoader.getContent()).show();
        } else if (itemId == R.id.action_open_link) {
            Uri uri = Uri.parse(mBook.getChapterUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (itemId == R.id.action_download) {
            download();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 结果回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case APPCONST.REQUEST_FONT:
                    Font font = (Font) data.getSerializableExtra(APPCONST.FONT);
                    mSetting.setFont(font);
//                    init();
                    MyApplication.runOnUiThread(() -> mPageLoader.setFont(font));
                    break;
                case APPCONST.REQUEST_CHAPTER_PAGE:
                    int[] chapterAndPage = data.getIntArrayExtra(APPCONST.CHAPTER_PAGE);
                    /*LLog.i(TAG, "chapterAndPage == null" + (chapterAndPage == null));
                    LLog.i(TAG, "chapterAndPage.length" + (chapterAndPage.length));*/
                    if (chapterAndPage == null) {
                        ToastUtils.showError("章节跳转失败!");
                        return;
                    }
                    try {
                        skipToChapterAndPage(chapterAndPage[0], chapterAndPage[1]);
                    } catch (Exception e) {
                        ToastUtils.showError("章节跳转失败，请截图联系作者。\n" +
                                e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case APPCONST.REQUEST_REFRESH_READ_UI:
                    screenTimeOut = mSetting.getResetScreen() * 60;
                    screenOffTimerStart();
                    boolean needRefresh = data.getBooleanExtra(APPCONST.RESULT_NEED_REFRESH, false);
                    boolean upMenu = data.getBooleanExtra(APPCONST.RESULT_UP_MENU, false);
                    if (needRefresh) {
                        mHandler.sendEmptyMessage(5);
                    }
                    if (upMenu) {
                        initTopMenu();
                    }
                    break;
                case APPCONST.REQUEST_SELECT_BG:
                    String bgPath = getPath(this, data.getData());
                    binding.readCustomizeLayoutMenu.setCustomBg(bgPath);
                    break;
                case APPCONST.REQUEST_IMPORT_LAYOUT:
                    String zipPath = getPath(this, data.getData());
                    binding.readCustomizeLayoutMenu.zip2Layout(zipPath);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Callback that is invoked when a color is selected from the color picker dialog.
     *
     * @param dialogId The dialog id used to create the dialog instance.
     * @param color    The selected color
     */
    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case APPCONST.SELECT_TEXT_COLOR:
                mSetting.setTextColor(color);
                mPageLoader.setTextSize();
                break;
            case APPCONST.SELECT_BG_COLOR:
                mSetting.setBgIsColor(true);
                mSetting.setBgColor(color);
                mPageLoader.refreshUi();
                break;
        }
        SysManager.saveSetting(mSetting);
        binding.readCustomizeLayoutMenu.upColor();
    }

    /**
     * Callback that is invoked when the color picker dialog was dismissed.
     *
     * @param dialogId The dialog id used to create the dialog instance.
     */
    @Override
    public void onDialogDismissed(int dialogId) {

    }

    /**************************method*********************************/
    /**
     * 初始化
     */
    private void init() {
        if (MyApplication.isDestroy(this)) return;
        screenOffTimerStart();
        mPageLoader.init();
        mPageLoader.refreshChapterList();
        loadFinish = true;
        invalidateOptionsMenu();
        mHandler.sendMessage(mHandler.obtainMessage(8));
    }

    private void initMenu() {
        if (mChapters != null && mChapters.size() != 0) {
            Chapter curChapter = mChapters.get(mPageLoader.getChapterPos());
            String url = curChapter.getUrl();
            binding.tvChapterTitleTop.setText(curChapter.getTitle());
            binding.tvChapterUrl.setText(StringHelper.isEmpty(url) ? curChapter.getId() : url);
            binding.readSbChapterProgress.setProgress(mPageLoader.getPagePos());
            binding.readSbChapterProgress.setMax(mPageLoader.getAllPagePos() - 1);
            binding.readTvPageTip.setText(String.format("%s/%s", binding.readSbChapterProgress.getProgress() + 1 ,binding.readSbChapterProgress.getMax() + 1));
        }
    }
    /************************书籍相关******************************/
    /**
     * 进入阅读书籍有三种方式：
     * 1、直接从书架进入，这种方式书籍一定Collected
     * 2、从外部打开txt文件，这种方式会添加进书架
     * 3、从快捷图标打开上次阅读书籍
     *
     * @return 是否加载成功
     */
    private boolean loadBook() {
        //是否直接打开本地txt文件
        String path = null;
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            Uri uri = getIntent().getData();
            if (uri != null) {
                path = getPath(this, uri);
            }
        }
        if (!StringHelper.isEmpty(path)) {
            //本地txt文件路径不为空，添加书籍
            addLocalBook(path);
        } else {
            //路径为空，说明不是直接打开txt文件
            mBook = (Book) getIntent().getSerializableExtra(APPCONST.BOOK);
            //mBook为空，说明是从快捷方式启动
            if (mBook == null) {
                String bookId = SharedPreUtils.getInstance().getString(getString(R.string.lastRead), "");
                if ("".equals(bookId)) {//没有上次阅读信息
                    ToastUtils.showWarring("当前没有阅读任何书籍，无法加载上次阅读书籍！");
                    finish();
                    return false;
                } else {//有信息
                    mBook = mBookService.getBookById(bookId);
                    if (mBook == null) {//上次阅读的书籍不存在
                        ToastUtils.showWarring("上次阅读书籍已不存在/移除书架，无法加载！");
                        finish();
                        return false;
                    }//存在就继续执行
                }
            }
        }
        return true;
    }

    /**
     * 添加本地书籍
     *
     * @param path
     */
    private void addLocalBook(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        Book book = new Book();
        book.setName(file.getName().replace(".txt", ""));
        book.setChapterUrl(path);
        book.setType(getString(R.string.local_book));
        book.setHistoryChapterId("未开始阅读");
        book.setNewestChapterTitle("未拆分章节");
        book.setAuthor(getString(R.string.local_book));
        book.setSource(BookSource.local.toString());
        book.setDesc("无");
        book.setIsCloseUpdate(true);
        //判断书籍是否已经添加
        Book existsBook = mBookService.findBookByAuthorAndName(book.getName(), book.getAuthor());
        if (book.equals(existsBook)) {
            mBook = existsBook;
            return;
        }

        if (BookGroupService.getInstance().curGroupIsPrivate()) {
            mBookService.addBookNoGroup(book);
        } else {
            mBookService.addBook(book);
        }
        mBook = book;
    }

    /**
     * 添加到书架并缓存书籍
     */
    private void addBookToCaseAndDownload() {
        DialogCreator.createCommonDialog(this, this.getString(R.string.tip), this.getString(R.string.download_no_add_tips), true, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadBook();
                isCollected = true;
            }
        }, (dialog, which) -> dialog.dismiss());
    }
    /************************章节相关*************************/
    /**
     * 章节数据网络同步
     */
    private void getData() {
        mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(mBook.getId());
        if (!isCollected || mChapters.size() == 0 || ("本地书籍".equals(mBook.getType()) && !ChapterService.isChapterCached(mBook.getId(), mChapters.get(0).getTitle())
        )) {
            if ("本地书籍".equals(mBook.getType())) {
                if (!new File(mBook.getChapterUrl()).exists()) {
                    ToastUtils.showWarring("书籍缓存为空且源文件不存在，书籍加载失败！");
                    finish();
                    return;
                }
                if (mChapters.size() != 0 && mChapters.get(0).getEnd() > 0) {
                    initChapters();
                    return;
                }
                ((LocalPageLoader) mPageLoader).loadChapters(new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                        mBook.setChapterTotalNum(chapters.size());
                        mBook.setNewestChapterTitle(chapters.get(chapters.size() - 1).getTitle());
                        mBookService.updateEntity(mBook);
                        if (mChapters.size() == 0) {
                            updateAllOldChapterData(chapters);
                        }
                        initChapters();
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                        mChapters.clear();
                        initChapters();
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                });
            } else {
                CommonApi.getBookChapters(mBook.getChapterUrl(), mReadCrawler, false, new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                        updateAllOldChapterData(chapters);
                        initChapters();
                    }

                    @Override
                    public void onError(Exception e) {
//                settingChange = true;
                        initChapters();
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                });
            }
        } else {
            initChapters();
        }
    }

    /**
     * 更新所有章节
     *
     * @param newChapters
     */
    private void updateAllOldChapterData(ArrayList<Chapter> newChapters) {
        for (Chapter newChapter : newChapters) {
            newChapter.setId(StringHelper.getStringRandom(25));
            newChapter.setBookId(mBook.getId());
            mChapters.add(newChapter);
//                mChapterService.addChapter(newChapters.get(j));
        }
        mChapterService.addChapters(mChapters);
    }

    /**
     * 初始化章节
     */
    private void initChapters() {
        mBook.setNoReadNum(0);
        mBook.setChapterTotalNum(mChapters.size());
        if (!StringHelper.isEmpty(mBook.getId())) {
            mBookService.updateEntity(mBook);
        }
        if (mChapters.size() == 0) {
            ToastUtils.showWarring("该书查询不到任何章节");
            mHandler.sendMessage(mHandler.obtainMessage(8));
        } else {
            if (mBook.getHisttoryChapterNum() < 0) {
                mBook.setHisttoryChapterNum(0);
            } else if (mBook.getHisttoryChapterNum() >= mChapters.size()) {
                mBook.setHisttoryChapterNum(mChapters.size() - 1);
            }
            if ("本地书籍".equals(mBook.getType())) {
                mHandler.sendMessage(mHandler.obtainMessage(1));
                return;
            }
            if (hasChangeSource) {
                mBookService.matchHistoryChapterPos(mBook, mChapters);
            }
            getChapterContent(mChapters.get(mBook.getHisttoryChapterNum()), new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
//                            mChapters.get(mBook.getHisttoryChapterNum()).setContent((String) o);
                    mChapterService.saveOrUpdateChapter(mChapters.get(mBook.getHisttoryChapterNum()), (String) o);
                    mHandler.sendMessage(mHandler.obtainMessage(1));
//                        getAllChapterData();
                }

                @Override
                public void onError(Exception e) {
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                }
            });
            initMenu();
        }
    }


    /**
     * 跳转到指定章节的指定页面
     *
     * @param chapterPos
     * @param pagePos
     */
    private void skipToChapterAndPage(final int chapterPos, final int pagePos) {
        isPrev = false;
        if (StringHelper.isEmpty(mChapters.get(chapterPos).getContent())) {
            if ("本地书籍".equals(mBook.getType())) {
                ToastUtils.showWarring("该章节无内容！");
                return;
            }
            binding.pbLoading.setVisibility(View.VISIBLE);
            getChapterContent(mChapters.get(chapterPos), new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    mChapterService.saveOrUpdateChapter(mChapters.get(chapterPos), (String) o);
                    mHandler.sendMessage(mHandler.obtainMessage(2, chapterPos, pagePos));
                }

                @Override
                public void onError(Exception e) {
                    mHandler.sendMessage(mHandler.obtainMessage(2, chapterPos, pagePos));
                    mHandler.sendEmptyMessage(10);
                }
            });
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(2, chapterPos, pagePos));
        }
    }

    /**
     * 获取章节内容
     *
     * @param chapter
     * @param resultCallback
     */
    private void getChapterContent(final Chapter chapter, ResultCallback resultCallback) {
        if (StringHelper.isEmpty(chapter.getBookId())) {
            chapter.setId(mBook.getId());
        }
        if (!StringHelper.isEmpty(chapter.getContent())) {
            if (resultCallback != null) {
                resultCallback.onFinish(mChapterService.getChapterCatheContent(chapter), 0);
            }
        } else {
            if ("本地书籍".equals(mBook.getType())) {
                return;
            }
            if (resultCallback != null) {
                CommonApi.getChapterContent(chapter.getUrl(), mReadCrawler, resultCallback);
            } else {
                CommonApi.getChapterContent(chapter.getUrl(), mReadCrawler, new ResultCallback() {
                    @Override
                    public void onFinish(final Object o, int code) {
//                        chapter.setContent((String) o);
                        mChapterService.saveOrUpdateChapter(chapter, (String) o);
                    }

                    @Override
                    public void onError(Exception e) {

                    }

                });
            }
        }
    }

    /**
     * 预加载下一章
     */
    private void preLoad(int position) {
        if (position + 1 < mChapters.size()) {
            Chapter chapter = mChapters.get(position + 1);
            if (StringHelper.isEmpty(chapter.getContent())) {
                mPageLoader.getChapterContent(chapter);
            }
        }
    }

    /**
     * 预加载上一章
     *
     * @param position
     */
    private void lastLoad(int position) {
        if (position > 0) {
            Chapter chapter = mChapters.get(position - 1);
            if (StringHelper.isEmpty(chapter.getContent())) {
                mPageLoader.getChapterContent(chapter);
            }
        }
    }

    /**
     * 保存最后阅读章节的进度
     */
    public void saveLastChapterReadPosition() {
        if (!StringHelper.isEmpty(mBook.getId()) && mPageLoader.getPageStatus() == PageLoader.STATUS_FINISH) {
            mBook.setLastReadPosition(mPageLoader.getPagePos());
            mBook.setHisttoryChapterNum(mPageLoader.getChapterPos());
            mBookService.updateEntity(mBook);
        }
    }
    /********************菜单相关*************************/
    /**
     * 初始化顶部菜单
     */
    private void initTopMenu() {
        int statusBarHeight = ImmersionBar.getStatusBarHeight(this);
        binding.readAblTopMenu.setPadding(0, statusBarHeight, 0, 0);
        if (mSetting.isNoMenuChTitle()) {
            binding.llChapterView.setVisibility(GONE);
            binding.toolbar.getLayoutParams().height = 60 + ImmersionBar.getStatusBarHeight(this);
        } else {
            binding.llChapterView.setVisibility(VISIBLE);
            binding.toolbar.getLayoutParams().height = 45 + ImmersionBar.getStatusBarHeight(this);
        }
    }

    /**
     * 初始化底部菜单
     */
    private void initBottomMenu() {
        //判断是否全屏
        //if (mSetting.getHideStatusBar()) {
        if (!mSetting.isDayStyle()) {
            binding.btnNightMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_sun));
        }
        binding.btnNightMode.setOnClickListener(v -> changeNightAndDaySetting(mSetting.isDayStyle()));
        if (true) {
            //还需要设置mBottomMenu的底部高度
            if (ImmersionBar.hasNavigationBar(this)) {
                int height = ImmersionBar.getNavigationBarHeight(this);
                binding.vwNavigationBar.getLayoutParams().height = height;
                binding.readSettingMenu.setNavigationBarHeight(height);
                binding.readCustomizeMenu.setNavigationBarHeight(height);
                binding.readCustomizeLayoutMenu.setNavigationBarHeight(height);
                binding.readAutoPageMenu.setNavigationBarHeight(height);
                binding.readBrightnessEyeMenu.setNavigationBarHeight(height);
            }
        } else {
            //设置mBottomMenu的底部距离
            binding.vwNavigationBar.getLayoutParams().height = 0;
        }
    }

    /**
     * 隐藏阅读界面的菜单显示
     *
     * @return 是否隐藏成功
     */
    private boolean hideReadMenu() {
        hideSystemBar();
        boolean flag = false;
        if (binding.readAblTopMenu.getVisibility() == VISIBLE) {
            toggleMenu(true);
            flag = true;
        }
        if (binding.readSettingMenu.getVisibility() == View.VISIBLE) {
            binding.readSettingMenu.setVisibility(GONE);
            binding.readSettingMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        if (binding.readCustomizeMenu.getVisibility() == VISIBLE) {
            binding.readCustomizeMenu.setVisibility(GONE);
            binding.readCustomizeMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        if (binding.readCustomizeLayoutMenu.getVisibility() == VISIBLE) {
            binding.readCustomizeLayoutMenu.setVisibility(GONE);
            binding.readCustomizeLayoutMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        if (binding.readAutoPageMenu.getVisibility() == VISIBLE) {
            binding.readAutoPageMenu.setVisibility(GONE);
            binding.readAutoPageMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        if ( binding.readBrightnessEyeMenu.getVisibility() == VISIBLE) {
            binding.readBrightnessEyeMenu.setVisibility(GONE);
            binding.readBrightnessEyeMenu.startAnimation(mBottomOutAnim);
            flag = true;
        }
        return flag;
    }

    /**
     * 切换菜单栏的可视状态
     * 默认是隐藏的
     */
    private void toggleMenu(boolean hideStatusBar) {
        toggleMenu(hideStatusBar, false);
    }

    public void toggleMenu(boolean hideStatusBar, boolean home) {
        initMenuAnim();
        if (binding.readAblTopMenu.getVisibility() == View.VISIBLE) {
            //关闭
            binding.readAblTopMenu.startAnimation(mTopOutAnim);
            binding.readLlBottomMenu.startAnimation(mBottomOutAnim);
            binding.readAblTopMenu.setVisibility(GONE);
            binding.readLlBottomMenu.setVisibility(GONE);
            if (hideStatusBar) {
                hideSystemBar();
            }
            return;
        }
        if (ReadAloudService.running && !home) {
            if (mAudioPlayerDialog != null) {
                mAudioPlayerDialog.show();
                return;
            }
        }
        if (autoPage) {
            binding.readAutoPageMenu.setVisibility(VISIBLE);
            binding.readAutoPageMenu.startAnimation(mBottomInAnim);
            return;
        }
        binding.readAblTopMenu.setVisibility(View.VISIBLE);
        binding.readLlBottomMenu.setVisibility(View.VISIBLE);
        binding.readAblTopMenu.startAnimation(mTopInAnim);
        binding.readLlBottomMenu.startAnimation(mBottomInAnim);
        showSystemBar();
    }

    //初始化菜单动画
    private void initMenuAnim() {
        if (mTopInAnim != null) return;
        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
    }

    private void showSystemBar() {
        //显示
        SystemBarUtils.showUnStableStatusBar(this);
        SystemBarUtils.showUnStableNavBar(this);
    }

    private void hideSystemBar() {
        //隐藏
        if (binding.readAblTopMenu.getVisibility() != VISIBLE || (mAudioPlayerDialog != null && !mAudioPlayerDialog.isShowing())) {
            if (!mSetting.isShowStatusBar()) {
                SystemBarUtils.hideStableStatusBar(this);
            }
            SystemBarUtils.hideStableNavBar(this);
        }
    }

    /******************设置相关*****************/

    /**
     * 初始化详细设置
     */
    private void initSettingListener() {
        binding.readSettingMenu.setOnClickListener(null);
        binding.readSettingMenu.setListener(this, new ReadSettingMenu.Callback() {
            @Override
            public void onRefreshUI() {
                mHandler.sendEmptyMessage(5);
            }

            @Override
            public void onStyleChange() {
                changeStyle();
            }

            @Override
            public void onTextSizeChange() {
                mPageLoader.setTextSize();
            }

            @Override
            public void onFontClick() {
                hideReadMenu();
                mHandler.postDelayed(() -> {
                    Intent intent = new Intent(ReadActivity.this, FontsActivity.class);
                    startActivityForResult(intent, APPCONST.REQUEST_FONT);
                }, mBottomOutAnim.getDuration());
            }

            @Override
            public void onAutoPageClick() {
                if (ReadAloudService.running) {
                    ToastUtils.showWarring("请先关闭语音朗读！");
                    return;
                }
                hideReadMenu();
                ToastUtils.showInfo("自动翻页开启");
                autoPage = !autoPage;
                autoPage();
            }

            @Override
            public void onHVChange() {
                setOrientation(mSetting.isHorizontalScreen());
            }

            @Override
            public void onMoreSettingClick() {
                hideReadMenu();
                mHandler.postDelayed(() -> {
                    Intent intent = new Intent(ReadActivity.this, MoreSettingActivity.class);
                    startActivityForResult(intent, APPCONST.REQUEST_REFRESH_READ_UI);
                }, mBottomOutAnim.getDuration());
            }
        });
        binding.readCustomizeMenu.setOnClickListener(null);
        binding.readCustomizeMenu.setListener(new CustomizeComMenu.Callback() {
            @Override
            public void onTextPChange() {
                mPageLoader.setTextSize();
                mSetting.setComposition(0);
                SysManager.saveSetting(mSetting);
                binding.readSettingMenu.initComposition();
            }

            @Override
            public void onMarginChange() {
                mPageLoader.upMargin();
                mSetting.setComposition(0);
                SysManager.saveSetting(mSetting);
                binding.readSettingMenu.initComposition();
            }

            @Override
            public void onRefreshUI() {
                mPageLoader.refreshUi();
            }

            @Override
            public void onReset() {
                mPageLoader.setTextSize();
                mPageLoader.upMargin();
                mSetting.setComposition(1);
                SysManager.saveSetting(mSetting);
                binding.readSettingMenu.initComposition();
                ToastUtils.showInfo("已重置各间距为默认值");
            }
        });
        binding.readAutoPageMenu.setOnClickListener(null);
        binding.readAutoPageMenu.setListener(new AutoPageMenu.Callback() {
            @Override
            public void onSpeedChange() {
                binding.readPvContent.autoPageOnSpeedChange();
                autoPage();
            }

            @Override
            public void onExitClick() {
                ToastUtils.showInfo("自动翻页关闭");
                autoPageStop();
                hideReadMenu();
            }
        });
        binding.readCustomizeLayoutMenu.setOnClickListener(null);
        binding.readCustomizeLayoutMenu.setListener(this, new CustomizeLayoutMenu.Callback() {
            @Override
            public void upBg() {
                mPageLoader.refreshUi();
            }

            @Override
            public void upStyle() {
                binding.readSettingMenu.initStyleImage();
            }

        });
        binding.readBrightnessEyeMenu.setOnClickListener(null);
        binding.readBrightnessEyeMenu.setListener(this, new BrightnessEyeMenu.Callback() {
            @Override
            public void onProtectEyeChange() {
                if (mSetting.isProtectEye()) {
                    openEye();
                } else {
                    closeEye();
                }
            }

            @Override
            public void upProtectEye() {
                openEye();
            }
        });
    }

    /**
     * 设置屏幕方向
     */
    @SuppressLint("SourceLockedOrientationActivity")
    public void setOrientation(boolean isHorizontalScreen) {
        if (isHorizontalScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * 白天夜间改变
     */
    private void changeNightAndDaySetting(boolean isNight) {
        mSetting.setDayStyle(!isNight);
        SysManager.saveSetting(mSetting);
        MyApplication.getApplication().setNightTheme(isNight);
        //mPageLoader.setPageStyle(!isCurDayStyle);
    }


    /**
     * 改变阅读风格
     */
    private void changeStyle() {
        if (!mSetting.isDayStyle()) {
            changeNightAndDaySetting(false);
        }
        upBrightnessEye();
        mPageLoader.refreshUi();
    }

    /**
     * 更新亮度和护眼
     */
    private void upBrightnessEye() {
        if (!mSetting.isBrightFollowSystem()) {
            BrightUtil.setBrightness(this, mSetting.getBrightProgress());
        } else {
            BrightUtil.followSystemBright(this);
        }
        if (mSetting.isProtectEye()) {
            openEye();
        } else {
            closeEye();
        }
    }


    protected void download() {
        if (!isCollected) {
            addBookToCaseAndDownload();
        } else {
            downloadBook();
        }
    }

    public void showCustomizeMenu() {
        binding.readCustomizeMenu.initWidget();
        binding.readCustomizeMenu.setVisibility(VISIBLE);
        binding.readCustomizeMenu.startAnimation(mBottomInAnim);
    }

    public void showCustomizeLayoutMenu() {
        hideReadMenu();

        binding.readCustomizeLayoutMenu.upColor();
        binding.readCustomizeLayoutMenu.setVisibility(VISIBLE);
        binding.readCustomizeLayoutMenu.startAnimation(mBottomInAnim);
    }

    /****************息屏相关*****************/
    /**
     * 取消亮屏保持
     */
    private void unKeepScreenOn() {
        keepScreenOn(false);
    }

    /**
     * @param keepScreenOn 是否保持亮屏
     */
    public void keepScreenOn(boolean keepScreenOn) {
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 重置黑屏时间
     */
    private void screenOffTimerStart() {
        if (screenTimeOut <= 0) {
            keepScreenOn(true);
            return;
        }
        int screenOffTime = screenTimeOut * 1000 - SystemUtil.getScreenOffTime(this);
        if (screenOffTime > 0) {
            mHandler.removeCallbacks(keepScreenRunnable);
            keepScreenOn(true);
            mHandler.postDelayed(keepScreenRunnable, screenOffTime);
        } else {
            keepScreenOn(false);
        }
    }

    /***************************缓存相关***************************/
    private int selectedIndex;//对话框选择下标

    protected void downloadBook() {
        if ("本地书籍".equals(mBook.getType())) {
            ToastUtils.showWarring("《" + mBook.getName() + "》是本地书籍，不能缓存");
            return;
        }
        if (!NetworkUtils.isNetWorkAvailable()) {
            ToastUtils.showWarring("无网络连接！");
            return;
        }
        MyApplication.runOnUiThread(() -> {
            MyAlertDialog.build(this)
                    .setTitle("缓存书籍")
                    .setSingleChoiceItems(getResources().getStringArray(R.array.download), selectedIndex,
                            (dialog, which) -> selectedIndex = which).setNegativeButton("取消", ((dialog, which) -> dialog.dismiss())).setPositiveButton("确定",
                    (dialog, which) -> {
                        switch (selectedIndex) {
                            case 0:
                                addDownload(mPageLoader.getChapterPos(), mPageLoader.getChapterPos() + 50);
                                break;
                            case 1:
                                addDownload(mPageLoader.getChapterPos() - 50, mPageLoader.getChapterPos() + 50);
                                break;
                            case 2:
                                addDownload(mPageLoader.getChapterPos(), mChapters.size());
                                break;
                            case 3:
                                addDownload(0, mChapters.size());
                                break;
                        }
                    }).show();
        });
    }

    private void addDownload(int begin, int end) {
        /*//取消之前下载
        if (!isStopDownload) {
            isStopDownload = true;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        if (SysManager.getSetting().getCatheGap() != 0) {
            downloadInterval = SysManager.getSetting().getCatheGap();
        }
        //计算断点章节
        final int finalBegin = Math.max(0, begin);
        final int finalEnd = Math.min(end, mChapters.size());
        needCacheChapterNum = finalEnd - finalBegin;
        curCacheChapterNum = 0;
        isStopDownload = false;
        ArrayList<Chapter> needDownloadChapters = new ArrayList<>();
        for (int i = finalBegin; i < finalEnd; i++) {
            final Chapter chapter = mChapters.get(i);
            if (StringHelper.isEmpty(chapter.getContent())) {
                needDownloadChapters.add(chapter);
            }
        }
        needCacheChapterNum = needDownloadChapters.size();
        if (needCacheChapterNum > 0) {
            mHandler.sendEmptyMessage(9);
            mHandler.postDelayed(sendDownloadNotification, 2 * downloadInterval);
        }
        MyApplication.getApplication().newThread(() -> {
            for (Chapter chapter : needDownloadChapters) {
                getChapterContent(chapter, new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        downloadingChapter = chapter.getTitle();
                        mChapterService.saveOrUpdateChapter(chapter, (String) o);
                        curCacheChapterNum++;
                    }

                    @Override
                    public void onError(Exception e) {
                        curCacheChapterNum++;
                    }
                });
                try {
                    Thread.sleep(downloadInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isStopDownload) {
                    break;
                }
            }
            if (curCacheChapterNum == needCacheChapterNum) {
                ToastUtils.showInfo("《" + mBook.getName() + "》" + getString(R.string.download_already_all_tips));
            }
        });
    }

    /**
     * 发送通知
     */
    private void sendNotification() {
        if (curCacheChapterNum == needCacheChapterNum) {
            notificationUtil.cancel(1001);
            return;
        } else {
            Notification notification = notificationUtil.build(APPCONST.channelIdDownload)
                    .setSmallIcon(R.drawable.ic_download)
                    //通知栏大图标
                    .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getApplication().getResources(), R.mipmap.ic_launcher))
                    .setOngoing(true)
                    //点击通知后自动清除
                    .setAutoCancel(true)
                    .setContentTitle("正在下载：" + mBook.getName() +
                            "[" + curCacheChapterNum + "/" + needCacheChapterNum + "]")
                    .setContentText(downloadingChapter == null ? "  " : downloadingChapter)
                    .addAction(R.drawable.ic_stop_black_24dp, "停止",
                            notificationUtil.getChancelPendingIntent(cancelDownloadReceiver.class))
                    .build();
            notificationUtil.notify(1001, notification);
        }
        if (tempCacheChapterNum < curCacheChapterNum) {
            tempCount = 1500 / downloadInterval;
            tempCacheChapterNum = curCacheChapterNum;
        } else if (tempCacheChapterNum == curCacheChapterNum) {
            tempCount--;
            if (tempCount == 0) {
                notificationUtil.cancel(1001);
                return;
            }
        }
        mHandler.postDelayed(sendDownloadNotification, 2 * downloadInterval);
    }

    public static class cancelDownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //todo 跳转之前要处理的逻辑
            if (NotificationClickReceiver.CANCEL_ACTION.equals(intent.getAction())) {
                isStopDownload = true;
            }
        }
    }


    /*******************************自动翻页相关************************************/
    /**
     * 自动翻页
     */
    private void autoPage() {
        mHandler.removeCallbacks(autoPageRunnable);
        if (autoPage) {
            mPageLoader.setPageMode(PageMode.AUTO);
            mPageLoader.skipToNextPage();
            mHandler.postDelayed(autoPageRunnable, mSetting.getAutoScrollSpeed() * 1000);
        }
    }

    /**
     * 停止自动翻页
     */
    public void autoPageStop() {
        autoPage = false;
        mPageLoader.setPageMode(mSetting.getPageMode());
        autoPage();
    }

    /**
     * 下一页
     */
    private void nextPage() {
        MyApplication.runOnUiThread(() -> {
            screenOffTimerStart();
            autoPage();
        });
    }

    /**************************护眼相关*********************************/
    private View vProtectEye;

    private void initEyeView() {
        ViewGroup content = findViewById(android.R.id.content);
        vProtectEye = new FrameLayout(this);
        vProtectEye.setBackgroundColor(mSetting.isProtectEye() ? getFilterColor(mSetting.getBlueFilterPercent()) : Color.TRANSPARENT);          //设置透明
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL     //不触碰
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE            //不可定焦
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;           //不可触
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        content.addView(vProtectEye, params);
    }


    /**
     * 开启护眼模式
     */
    public void openEye() {
        if (vProtectEye == null) {
            initEyeView();
        }
        vProtectEye.setBackgroundColor(getFilterColor(mSetting.getBlueFilterPercent()));
    }

    /**
     * 关闭护眼模式
     */
    public void closeEye() {
        if (vProtectEye == null) {
            initEyeView();
        }
        vProtectEye.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * 过滤蓝光
     *
     * @param blueFilterPercent 蓝光过滤比例[10-30-80]
     */
    public int getFilterColor(int blueFilterPercent) {
        int realFilter = blueFilterPercent;
        if (realFilter < 10) {
            realFilter = 10;
        } else if (realFilter > 80) {
            realFilter = 80;
        }
        int a = (int) (realFilter / 80f * 180);
        int r = (int) (200 - (realFilter / 80f) * 190);
        int g = (int) (180 - (realFilter / 80f) * 170);
        int b = (int) (60 - realFilter / 80f * 60);
        return Color.argb(a, r, g, b);
    }

    /***********************长按弹出菜单相关*************************/
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.cursor_left || v.getId() == R.id.cursor_right) {
            int ea = event.getAction();
            //final int screenWidth = dm.widthPixels;
            //final int screenHeight = dm.heightPixels;
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                    lastY = (int) event.getRawY();
                    longPressMenu.hidePopupListWindow();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;
                    int l = v.getLeft() + dx;
                    int b = v.getBottom() + dy;
                    int r = v.getRight() + dx;
                    int t = v.getTop() + dy;

                    v.layout(l, t, r, b);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    v.postInvalidate();

                    //移动过程中要画线
                    binding.readPvContent.setSelectMode(PageView.SelectMode.SelectMoveForward);

                    int hh = binding.cursorLeft.getHeight();
                    int ww = binding.cursorLeft.getWidth();

                    if (v.getId() == R.id.cursor_left) {
                        binding.readPvContent.setFirstSelectTxtChar(binding.readPvContent.getCurrentTxtChar(lastX + ww, lastY - hh));
                        if (binding.readPvContent.getFirstSelectTxtChar() != null) {
                            binding.cursorLeft.setX(binding.readPvContent.getFirstSelectTxtChar().getTopLeftPosition().x - ww);
                            binding.cursorLeft.setY(binding.readPvContent.getFirstSelectTxtChar().getBottomLeftPosition().y);
                        }
                    } else {
                        binding.readPvContent.setLastSelectTxtChar(binding.readPvContent.getCurrentTxtChar(lastX - ww, lastY - hh));
                        if (binding.readPvContent.getLastSelectTxtChar() != null) {
                            binding.cursorRight.setX(binding.readPvContent.getLastSelectTxtChar().getBottomRightPosition().x);
                            binding.cursorRight.setY(binding.readPvContent.getLastSelectTxtChar().getBottomRightPosition().y);
                        }
                    }

                    binding.readPvContent.invalidate();

                    break;
                case MotionEvent.ACTION_UP:
                    showAction();
                    //v.layout(l, t, r, b);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    /**
     * 显示长按菜单
     */
    public void showAction() {
        float x, y;
        if (binding.cursorLeft.getX() - binding.cursorRight.getX() > 0){
            x = binding.cursorRight.getX() + (binding.cursorLeft.getX() - binding.cursorRight.getX()) / 2 + ScreenUtils.dpToPx(12);
        }else {
            x = binding.cursorLeft.getX() + (binding.cursorRight.getX() - binding.cursorLeft.getX()) / 2 + ScreenUtils.dpToPx(12);
        }
        if ((binding.cursorLeft.getY() - ScreenUtils.spToPx(mSetting.getReadWordSize()) - ScreenUtils.dpToPx(60)) < 0) {
            longPressMenu.setShowBottom(true);
            y = binding.cursorLeft.getY() + binding.cursorLeft.getHeight() * 3 / 5;
        } else {
            longPressMenu.setShowBottom(false);
            y = binding.cursorLeft.getY() - ScreenUtils.spToPx(mSetting.getReadWordSize()) - ScreenUtils.dpToPx(5);
        }
        longPressMenu.showPopupListWindow(binding.rlContent, 0, x, y,
                longPressMenuItems, longPressMenuListener);
    }

    /**
     * 显示
     */
    private void selectTextCursorShow() {
        if (binding.readPvContent.getFirstSelectTxtChar() == null || binding.readPvContent.getLastSelectTxtChar() == null)
            return;
        //show Cursor on current position
        cursorShow();
        //set current word selected
        binding.readPvContent.invalidate();

//        hideSnackBar();
    }

    /**
     * 显示选择
     */
    private void cursorShow() {
        binding.cursorLeft.setVisibility(View.VISIBLE);
        binding.cursorRight.setVisibility(View.VISIBLE);
        int hh = binding.cursorLeft.getHeight();
        int ww = binding.cursorLeft.getWidth();
        if (binding.readPvContent.getFirstSelectTxtChar() != null) {
            binding.cursorLeft.setX(binding.readPvContent.getFirstSelectTxtChar().getTopLeftPosition().x - ww);
            binding.cursorLeft.setY(binding.readPvContent.getFirstSelectTxtChar().getBottomLeftPosition().y);
            binding.cursorRight.setX(binding.readPvContent.getFirstSelectTxtChar().getBottomRightPosition().x);
            binding.cursorRight.setY(binding.readPvContent.getFirstSelectTxtChar().getBottomRightPosition().y);
        }
    }

    private final List<String> longPressMenuItems = new ArrayList<>();
    private BubblePopupView longPressMenu;
    private BubblePopupView.PopupListListener longPressMenuListener;

    /**
     * 长按选择按钮
     */
    private void initReadLongPressPop() {
        longPressMenuItems.add("拷贝");
        longPressMenuItems.add("替换");
        longPressMenuItems.add("发声");
        longPressMenuItems.add("搜索");
        longPressMenuItems.add("分享");
        longPressMenu = new BubblePopupView(this);
        //是否跟随手指显示，默认false，设置true后翻转高度无效，永远在上方显示
        longPressMenu.setShowTouchLocation(true);
        longPressMenu.setFocusable(false);
        longPressMenuListener = new BubblePopupView.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {
                String selectString;
                switch (position) {
                    case 0:
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(null, binding.readPvContent.getSelectStr());
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clipData);
                            ToastUtils.showInfo("所选内容已经复制到剪贴板");
                        }
                        clearSelect();
                        break;
                    case 1:
                        ReplaceRuleBean oldRuleBean = new ReplaceRuleBean();
                        oldRuleBean.setReplaceSummary("");
                        oldRuleBean.setEnable(true);
                        oldRuleBean.setRegex(binding.readPvContent.getSelectStr().trim());
                        oldRuleBean.setIsRegex(false);
                        oldRuleBean.setReplacement("");
                        oldRuleBean.setSerialNumber(0);
                        oldRuleBean.setUseTo(String.format("%s;%s", mBook.getSource(), mBook.getName() + "-" + mBook.getAuthor()));
                        ReplaceDialog replaceDialog = new ReplaceDialog(ReadActivity.this, oldRuleBean
                                , () -> {
                            ToastUtils.showSuccess("内容替换规则添加成功！");
                            clearSelect();
                            mPageLoader.refreshUi();
                        });
                        replaceDialog.show(getSupportFragmentManager(), "replaceRule");
                        break;
                    case 2:
                        selectString = binding.readPvContent.getSelectStr();
                        speak(ReadActivity.this, selectString);
                        clearSelect();
                        break;
                    case 3:
                        selectString = StringUtils.deleteWhitespace(binding.readPvContent.getSelectStr());
                        MyAlertDialog.build(ReadActivity.this)
                                .setTitle(R.string.search)
                                .setItems(R.array.search_way, (dialog, which) -> {
                                    String url = "";
                                    switch (which) {
                                        case 0:
                                            url = URLCONST.BAI_DU_SEARCH;
                                            break;
                                        case 1:
                                            url = URLCONST.GOOGLE_SEARCH;
                                            break;
                                        case 2:
                                            url = URLCONST.YOU_DAO_SEARCH;
                                            break;
                                    }
                                    url = url.replace("{key}", selectString);
                                    Log.d("SEARCH_URL", url);
                                    try {
                                        Uri uri = Uri.parse(url);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ToastUtils.showError(e.getLocalizedMessage());
                                    }
                                }).setNegativeButton("取消", null)
                                .show();
                        clearSelect();
                        break;
                    case 4:
                        selectString = binding.readPvContent.getSelectStr();
                        ShareUtils.share(ReadActivity.this, selectString);
                        clearSelect();
                        break;
                }
            }
        };
    }

    /**
     * 清除选择
     */
    private void clearSelect() {
        binding.cursorLeft.setVisibility(View.INVISIBLE);
        binding.cursorRight.setVisibility(View.INVISIBLE);
        longPressMenu.hidePopupListWindow();
        binding.readPvContent.clearSelect();
    }

    private TextToSpeech textToSpeech;
    private boolean ttsInitFinish = false;
    private String lastText = "";

    /**
     * 发声
     *
     * @param context
     * @param text
     */
    public void speak(Context context, String text) {
        lastText = text;
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.CHINA);
                    ttsInitFinish = true;
                    speak(context, lastText);
                } else {
                    ToastUtils.showError("TTS初始化失败！");
                }
            });
            return;
        }
        if (!ttsInitFinish) return;
        if ("".equals(text)) return;
        if (textToSpeech.isSpeaking())
            textToSpeech.stop();
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "select_text");
        lastText = "";
    }
}
