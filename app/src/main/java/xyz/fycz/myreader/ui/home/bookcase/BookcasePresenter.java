package xyz.fycz.myreader.ui.home.bookcase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.backup.BackupAndRestore;
import xyz.fycz.myreader.backup.UserService;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.crawler.ReadCrawler;
import xyz.fycz.myreader.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.custom.DragSortGridView;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.enums.BookcaseStyle;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.ui.about.AboutActivity;
import xyz.fycz.myreader.ui.filesys.FileSystemActivity;
import xyz.fycz.myreader.ui.home.MainActivity;
import xyz.fycz.myreader.ui.search.SearchBookActivity;
import xyz.fycz.myreader.ui.user.LoginActivity;
import xyz.fycz.myreader.util.*;
import xyz.fycz.myreader.util.notification.NotificationClickReceiver;
import xyz.fycz.myreader.util.notification.NotificationUtil;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.webapi.CommonApi;


public class BookcasePresenter implements BasePresenter {

    private final BookcaseFragment mBookcaseFragment;
    private final ArrayList<Book> mBooks = new ArrayList<>();//书目数组
    private BookcaseAdapter mBookcaseAdapter;
    private final BookService mBookService;
    private final ChapterService mChapterService;
    private final MainActivity mMainActivity;
    private PermissionsChecker mPermissionsChecker;
    private boolean isBookcaseStyleChange;
    private Setting mSetting;
    private final List<Book> errorLoadingBooks = new ArrayList<>();
    private int finishLoadBookCount = 0;
    private final BackupAndRestore mBackupAndRestore;
    //    private int notifyId = 11;
    private ExecutorService es = Executors.newFixedThreadPool(1);//更新/下载线程池

    public ExecutorService getEs() {
        return es;
    }

    private NotificationUtil notificationUtil;//通知工具类
    private String downloadingBook;//正在下载的书名
    private String downloadingChapter;//正在下载的章节名
    private boolean isDownloadFinish = true;//单本书是否下载完成
    private static boolean isStopDownload = true;//是否停止下载
    private int curCacheChapterNum;//当前下载的章节数
    private int needCacheChapterNum;//需要下载的章节数
    private int tempCacheChapterNum;//上次下载的章节数
    private int tempCount;//下载超时时间
    private int downloadInterval = 150;//下载间隔
    private Runnable sendDownloadNotification;//发送通知的线程
    private PopupMenu pm;//菜单

    public static final String CANCEL_ACTION = "cancelAction";

    private final String[] backupMenu = {
            MyApplication.getmContext().getResources().getString(R.string.menu_backup_backup),
            MyApplication.getmContext().getResources().getString(R.string.menu_backup_restore),
    };

    private final String[] webSynMenu = {
            MyApplication.getmContext().getString(R.string.menu_backup_webBackup),
            MyApplication.getmContext().getString(R.string.menu_backup_webRestore),
            MyApplication.getmContext().getString(R.string.menu_backup_autoSyn)
    };

    static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //    private ChapterService mChapterService;
    @SuppressLint("HandlerLeak")
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MyApplication.runOnUiThread(() -> mBookcaseAdapter.notifyDataSetChanged());
                    finishLoadBookCount++;
                    mBookcaseFragment.getSrlContent().finishRefresh();
                    break;
                case 2:
                    mBookcaseFragment.getSrlContent().finishRefresh();
                    break;
                case 3:
                    mBookcaseAdapter.notifyDataSetChanged();
                    break;
                case 4:
                    showErrorLoadingBooks();
                    if (MyApplication.isApkInDebug(mMainActivity)) {
                        MyApplication.runOnUiThread(() -> mBookcaseAdapter.notifyDataSetChanged());
                        downloadAll(false);
                    }
                    break;
                case 5:
                    backup();
                    break;
                case 6:
                    restore();
                    break;
                case 7:
                    init();
                    break;
                case 8:
                    sendNotification();
                    break;
                case 9:
                    mBookcaseFragment.getRlDownloadTip().setVisibility(View.GONE);
                    isDownloadFinish = true;
                    break;
                case 10:
                    mBookcaseFragment.getTvDownloadTip().setText("正在初始化缓存任务...");
                    mBookcaseFragment.getPbDownload().setProgress(0);
                    mBookcaseFragment.getRlDownloadTip().setVisibility(View.VISIBLE);
                    break;
                case 11:
                    MyApplication.runOnUiThread(() -> createMenu());
                    break;
                case 12:
                    TextHelper.showText("正在后台缓存书籍，具体进度可查看通知栏！");
                    notificationUtil.requestNotificationPermissionDialog(mMainActivity);
                    break;
            }
        }
    };

    public BookcasePresenter(BookcaseFragment bookcaseFragment) {
        mBookcaseFragment = bookcaseFragment;
        mBookService = BookService.getInstance();
        ;
        mChapterService = ChapterService.getInstance();
        mMainActivity = ((MainActivity) (mBookcaseFragment.getActivity()));
//        mChapterService = new ChapterService();
        mSetting = SysManager.getSetting();
        mBackupAndRestore = new BackupAndRestore();
    }

    @Override
    public void start() {
        if (mSetting.getBookcaseStyle() == null) {
            mSetting.setBookcaseStyle(BookcaseStyle.listMode);
        }
        if (mSetting.isAutoSyn() && UserService.isLogin()) {
            synBookcaseToWeb(true);
        }

        sendDownloadNotification = this::sendNotification;
        notificationUtil = NotificationUtil.getInstance();

        getData();
        //是否启用下拉刷新（默认启用）
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mBookcaseFragment.getSrlContent().setEnableRefresh(false);
        }
        //设置是否启用内容视图拖动效果
        mBookcaseFragment.getSrlContent().setEnableHeaderTranslationContent(false);
        //设置刷新监听器
        mBookcaseFragment.getSrlContent().setOnRefreshListener(refreshlayout -> initNoReadNum());
        //搜索按钮监听器
        mBookcaseFragment.getLlNoDataTips().setOnClickListener(view -> {
            Intent intent = new Intent(mBookcaseFragment.getContext(), SearchBookActivity.class);
            mBookcaseFragment.startActivity(intent);
        });

        //长按事件监听
        mBookcaseFragment.getGvBook().setOnItemLongClickListener((parent, view, position, id) -> false);

        //更多按钮监听器
        mMainActivity.getIvMore().setOnClickListener(v -> mHandler.sendMessage(mHandler.obtainMessage(11)));

        //完成按钮监听器
        mMainActivity.getTvEditFinish().setOnClickListener(v -> editBookcase(false));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //滑动监听器
            mBookcaseFragment.getGvBook().getmScrollView().setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (!mBookcaseAdapter.ismEditState()) {
                    mBookcaseFragment.getSrlContent().setEnableRefresh(scrollY == 0);
                }
            });
        }

    }

    /**
     * 编辑书架
     *
     * @param isEdit
     */
    private void editBookcase(boolean isEdit) {
        if (isEdit) {
            if (mBooks.size() > 0) {
                mBookcaseFragment.getSrlContent().setEnableRefresh(false);
                mBookcaseAdapter.setmEditState(true);
                mBookcaseFragment.getGvBook().setDragModel(DragSortGridView.DRAG_BY_LONG_CLICK);
                mBookcaseAdapter.notifyDataSetChanged();
                mMainActivity.getRlCommonTitle().setVisibility(View.GONE);
                mMainActivity.getRlEditTitle().setVisibility(View.VISIBLE);
                mMainActivity.getIvMore().setVisibility(View.GONE);
//                VibratorUtil.Vibrate(mBookcaseFragment.getActivity(), 100);
            } else {
                TextHelper.showText("当前无任何书籍，无法编辑书架!");
            }
        } else {
            mMainActivity.getRlCommonTitle().setVisibility(View.VISIBLE);
            mMainActivity.getIvMore().setVisibility(View.VISIBLE);
            mMainActivity.getRlEditTitle().setVisibility(View.GONE);
            if (mBookcaseFragment.getGvBook().getmScrollView().getScrollY() == 0
                    && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBookcaseFragment.getSrlContent().setEnableRefresh(true);
            }
            mBookcaseFragment.getGvBook().setDragModel(-1);
            mBookcaseAdapter.setmEditState(false);
            mBookcaseAdapter.notifyDataSetChanged();
        }
    }


    protected void init() {
        initBook();
        if (mBooks.size() == 0) {
            mBookcaseFragment.getGvBook().setVisibility(View.GONE);
            mBookcaseFragment.getLlNoDataTips().setVisibility(View.VISIBLE);
        } else {
            if (mBookcaseAdapter == null || isBookcaseStyleChange) {
                switch (mSetting.getBookcaseStyle()) {
                    case listMode:
                        mBookcaseAdapter = new BookcaseDetailedAdapter(mBookcaseFragment.getContext(), R.layout.gridview_book_detailed_item, mBooks, false, this);
                        mBookcaseFragment.getGvBook().setNumColumns(1);
                        break;
                    case threePalaceMode:
                        mBookcaseAdapter = new BookcaseDragAdapter(mBookcaseFragment.getContext(), R.layout.gridview_book_item, mBooks, false, this);
                        mBookcaseFragment.getGvBook().setNumColumns(3);
                        break;
                }
                mBookcaseFragment.getGvBook().setDragModel(-1);
                mBookcaseFragment.getGvBook().setTouchClashparent(((MainActivity) (mBookcaseFragment.getActivity())).getVpContent());
                mBookcaseFragment.getGvBook().setAdapter(mBookcaseAdapter);
                isBookcaseStyleChange = false;
            } else {
                mBookcaseAdapter.notifyDataSetChanged();
            }
            mBookcaseFragment.getLlNoDataTips().setVisibility(View.GONE);
            mBookcaseFragment.getGvBook().setVisibility(View.VISIBLE);
        }
    }


    public void getData() {
        init();
        initNoReadNum();
    }

    private void initBook() {
        mBooks.clear();
        mBooks.addAll(mBookService.getAllBooks());
        for (int i = 0; i < mBooks.size(); i++) {
            if (mBooks.get(i).getSortCode() != i + 1) {
                mBooks.get(i).setSortCode(i + 1);
                mBookService.updateEntity(mBooks.get(i));
            }
        }
    }

    private void initNoReadNum() {
        errorLoadingBooks.clear();
        finishLoadBookCount = 0;
        for (Book book : mBooks) {
            mBookcaseAdapter.getIsLoading().put(book.getId(), true);
        }
        if (mBooks.size() > 0) {
            mHandler.sendMessage(mHandler.obtainMessage(3));
        }
        for (final Book book : mBooks) {
            if ("本地书籍".equals(book.getType())) {
                mBookcaseAdapter.getIsLoading().put(book.getId(), false);
                mHandler.sendMessage(mHandler.obtainMessage(1));
                continue;
            }
            Thread update = new Thread(() -> {
                final ArrayList<Chapter> mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
                final ReadCrawler mReadCrawler = ReadCrawlerUtil.getReadCrawler(book.getSource());
                CommonApi.getBookChapters(book.getChapterUrl(), mReadCrawler, new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        final ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                        int noReadNum = chapters.size() - book.getChapterTotalNum();
                        book.setNoReadNum(Math.max(noReadNum, 0));
                        book.setNewestChapterTitle(chapters.get(chapters.size() - 1).getTitle());
                        mBookcaseAdapter.getIsLoading().put(book.getId(), false);
                        updateAllOldChapterData(mChapters, chapters, book.getId());
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                        mBookService.updateEntity(book);
                    }

                    @Override
                    public void onError(Exception e) {
                        mBookcaseAdapter.getIsLoading().put(book.getId(), false);
                        errorLoadingBooks.add(book);
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                });
            });
            es.submit(update);
        }
        MyApplication.getApplication().newThread(() -> {
            while (true) {
                if (finishLoadBookCount == mBooks.size()) {
                    mHandler.sendMessage(mHandler.obtainMessage(4));
                    break;
                }
            }
        });
    }

    /**
     * 更新所有章节
     *
     * @param newChapters
     */
    private void updateAllOldChapterData(ArrayList<Chapter> mChapters, ArrayList<Chapter> newChapters, String bookId) {
        int i;
        for (i = 0; i < mChapters.size() && i < newChapters.size(); i++) {
            Chapter oldChapter = mChapters.get(i);
            Chapter newChapter = newChapters.get(i);
            if (!oldChapter.getTitle().equals(newChapter.getTitle())) {
                oldChapter.setTitle(newChapter.getTitle());
                oldChapter.setUrl(newChapter.getUrl());
                oldChapter.setContent(null);
                mChapterService.saveOrUpdateChapter(oldChapter, null);
            }
        }
        if (mChapters.size() < newChapters.size()) {
            int start = mChapters.size();
            for (int j = mChapters.size(); j < newChapters.size(); j++) {
                newChapters.get(j).setId(StringHelper.getStringRandom(25));
                newChapters.get(j).setBookId(bookId);
                mChapters.add(newChapters.get(j));
//                mChapterService.addChapter(newChapters.get(j));
            }
            mChapterService.addChapters(mChapters.subList(start, mChapters.size()));
        } else if (mChapters.size() > newChapters.size()) {
            for (int j = newChapters.size(); j < mChapters.size(); j++) {
                mChapterService.deleteEntity(mChapters.get(j));
                mChapterService.deleteChapterCacheFile(mChapters.get(j));
            }
            mChapters.subList(0, newChapters.size());
        }
    }

    private void setThemeColor(int colorPrimary, int colorPrimaryDark) {
//        mToolbar.setBackgroundResource(colorPrimary);
        mBookcaseFragment.getSrlContent().setPrimaryColorsId(colorPrimary, android.R.color.white);
        if (Build.VERSION.SDK_INT >= 21) {
            mBookcaseFragment.getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(mBookcaseFragment.getContext(), colorPrimaryDark));
        }
    }

    /**
     * 显示更新失败的书籍信息
     */
    private void showErrorLoadingBooks() {
        StringBuilder s = new StringBuilder();
        for (Book book : errorLoadingBooks) {
            s.append(book.getName());
            s.append("、");
        }
        if (errorLoadingBooks.size() > 0) {
            s.deleteCharAt(s.lastIndexOf("、"));
            s.append(" 更新失败");
            TextHelper.showText(s.toString());
        }
    }

    /**
     * 创建菜单栏
     */
    private void createMenu() {
        //如果菜单栏已经创建直接show
        if (pm != null) {
            pm.show();
            return;
        }
        pm = new PopupMenu(mMainActivity, mMainActivity.getIvMore());
        pm.getMenuInflater().inflate(R.menu.menu_book, pm.getMenu());
        setIconEnable(pm.getMenu(), true);
        if (MyApplication.isApkInDebug(mMainActivity)) {
            pm.getMenu().getItem(5).setVisible(true);
        }
        pm.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    editBookcase(true);
                    return true;
                case R.id.action_styleChange:
                    mBookcaseFragment.getGvBook().getmScrollView().setScrollY(0);
                    if (mSetting.getBookcaseStyle().equals(BookcaseStyle.listMode)) {
                        mSetting.setBookcaseStyle(BookcaseStyle.threePalaceMode);
                        TextHelper.showText("已切换为三列网格视图！");
                    } else {
                        mSetting.setBookcaseStyle(BookcaseStyle.listMode);
                        TextHelper.showText("已切换为列表视图！");
                    }
                    isBookcaseStyleChange = true;
                    SysManager.saveSetting(mSetting);
                    init();
                    return true;
                case R.id.action_addLocalBook:
                    /*TextHelper.showText("请选择一个txt格式的书籍文件");
                    Intent addIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    addIntent.setType("text/plain");
                    addIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    mMainActivity.startActivityForResult(addIntent, APPCONST.SELECT_FILE_CODE);*/
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

                        if (mPermissionsChecker == null) {
                            mPermissionsChecker = new PermissionsChecker(mMainActivity);
                        }

                        //获取读取和写入SD卡的权限
                        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
                            //请求权限
                            ActivityCompat.requestPermissions(mMainActivity, PERMISSIONS, APPCONST.PERMISSIONS_REQUEST_STORAGE);
                            return true;
                        }
                    }
                    Intent intent = new Intent(mMainActivity, FileSystemActivity.class);
                    mMainActivity.startActivity(intent);
                    break;
                case R.id.action_syn:
                    if (!UserService.isLogin()) {
                        TextHelper.showText("请先登录！");
                        Intent loginIntent = new Intent(mMainActivity, LoginActivity.class);
                        mMainActivity.startActivity(loginIntent);
                        return true;
                    }
                    if (mSetting.isAutoSyn()) {
                        webSynMenu[2] = MyApplication.getmContext().getString(R.string.menu_backup_autoSyn) + "已开启";
                    } else {
                        webSynMenu[2] = MyApplication.getmContext().getString(R.string.menu_backup_autoSyn) + "已关闭";
                    }
                    new AlertDialog.Builder(mMainActivity)
                            .setTitle(mMainActivity.getString(R.string.menu_bookcase_syn))
                            .setAdapter(new ArrayAdapter<>(mMainActivity,
                                            android.R.layout.simple_list_item_1, webSynMenu),
                                    (dialog, which) -> {
                                        switch (which) {
                                            case 0:
                                                synBookcaseToWeb(false);
                                                break;
                                            case 1:
                                                webRestore();
                                                break;
                                            case 2:
                                                String tip = "";
                                                if (mSetting.isAutoSyn()) {
                                                    mSetting.setAutoSyn(false);
                                                    tip = "每日自动同步已关闭！";
                                                } else {
                                                    mSetting.setAutoSyn(true);
                                                    tip = "每日自动同步已开启！";
                                                }
                                                SysManager.saveSetting(mSetting);
                                                TextHelper.showText(tip);
                                                break;
                                        }
                                    })
                            .setNegativeButton(null, null)
                            .setPositiveButton(null, null)
                            .show();
                    break;
                case R.id.action_download_all:
                    if (!SharedPreUtils.getInstance().getBoolean("isReadDownloadAllTip")) {
                        DialogCreator.createCommonDialog(mMainActivity, "一键缓存",
                                mMainActivity.getString(R.string.all_cathe_tip), true,
                                (dialog, which) -> {
                                    downloadAll(true);
                                    SharedPreUtils.getInstance().putBoolean("isReadDownloadAllTip", true);
                                }, null);
                    }else {
                        downloadAll(true);
                    }

                    return true;
                case R.id.action_backup:
                    AlertDialog bookDialog = new AlertDialog.Builder(mMainActivity)
                            .setTitle(mMainActivity.getResources().getString(R.string.menu_bookcase_backup))
                            .setAdapter(new ArrayAdapter<>(mMainActivity,
                                            android.R.layout.simple_list_item_1, backupMenu),
                                    (dialog, which) -> {
                                        switch (which) {
                                            case 0:
                                                mHandler.sendMessage(mHandler.obtainMessage(5));
                                                break;
                                            case 1:
                                                mHandler.sendMessage(mHandler.obtainMessage(6));
                                                break;
                                        }
                                    })
                            .setNegativeButton(null, null)
                            .setPositiveButton(null, null)
                            .create();
                    bookDialog.show();
                    return true;
                case R.id.action_about:
                    Intent aboutIntent = new Intent(mMainActivity, AboutActivity.class);
                    mMainActivity.startActivity(aboutIntent);
                    return true;
            }
            return false;
        });
        pm.show();
    }

    /**
     * 显示菜单图标
     *
     * @param menu   菜单
     * @param enable 是否显示图标
     */
    private void setIconEnable(Menu menu, boolean enable) {
        try {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)
            m.invoke(menu, enable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 备份
     */
    private void backup() {
        DialogCreator.createCommonDialog(mMainActivity, "确认备份吗?", "新备份会替换原有备份！", true,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (mBackupAndRestore.backup("localBackup")) {
                        DialogCreator.createTipDialog(mMainActivity, "备份成功，备份文件路径：" + APPCONST.BACKUP_FILE_DIR);
                    } else {
                        DialogCreator.createTipDialog(mMainActivity, "未给予储存权限，备份失败！");
                    }
                }, (dialogInterface, i) -> dialogInterface.dismiss());
    }

    /**
     * 恢复
     */
    private void restore() {
        DialogCreator.createCommonDialog(mMainActivity, "确认恢复吗?", "恢复书架会覆盖原有书架！", true,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (mBackupAndRestore.restore("localBackup")) {
                        mHandler.sendMessage(mHandler.obtainMessage(7));
//                            DialogCreator.createTipDialog(mMainActivity,
//                                    "恢复成功！\n注意：本功能属于实验功能，书架恢复后，书籍初次加载时可能加载失败，返回重新加载即可！");
                        mSetting = SysManager.getSetting();
                        TextHelper.showText("书架恢复成功！");
                    } else {
                        DialogCreator.createTipDialog(mMainActivity, "未找到备份文件或未给予储存权限，恢复失败！");
                    }
                }, (dialogInterface, i) -> dialogInterface.dismiss());
    }


    /**
     * 恢复
     */
    private void webRestore() {
        if (!NetworkUtils.isNetWorkAvailable()) {
            TextHelper.showText("无网络连接！");
            return;
        }
        DialogCreator.createCommonDialog(mMainActivity, "确认同步吗?", "将书架从网络同步至本地会覆盖原有书架！", true,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    MyApplication.getApplication().newThread(() -> {
                        if (UserService.webRestore()) {
                            mHandler.sendMessage(mHandler.obtainMessage(7));
//                                    DialogCreator.createTipDialog(mMainActivity,
//                                            "恢复成功！\n注意：本功能属于实验功能，书架恢复后，书籍初次加载时可能加载失败，返回重新加载即可！");、
                            mSetting = SysManager.getSetting();
                            TextHelper.showText("成功将书架从网络同步至本地！");
                        } else {
                            DialogCreator.createTipDialog(mMainActivity, "未找到同步文件，同步失败！");
                        }
                    });
                }, (dialogInterface, i) -> dialogInterface.dismiss());
    }

/**********************************************缓存书籍***************************************************************/
    /**
     * 缓存所有书籍
     */
    private void downloadAll(boolean isDownloadAllChapters) {
        if (!NetworkUtils.isNetWorkAvailable()) {
            TextHelper.showText("无网络连接！");
            return;
        }
        if (isDownloadAllChapters) {
            mHandler.sendEmptyMessage(12);
        }
        MyApplication.getApplication().newThread(() -> {
            ArrayList<Book> needDownloadBooks = new ArrayList<>();
            for (Book book : mBooks) {
                if (!BookSource.pinshu.toString().equals(book.getSource()) && !"本地书籍".equals(book.getType())) {
                    needDownloadBooks.add(book);
                }
            }
            downloadFor:
            for (final Book book : needDownloadBooks) {
                isDownloadFinish = false;
                Thread downloadThread = new Thread(() -> {
                    ArrayList<Chapter> chapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
                    int end;
                    if (isDownloadAllChapters) {
                        end = chapters.size();
                    } else {
                        end = book.getHisttoryChapterNum() + 5;
                    }
                    addDownload(book, chapters,
                            book.getHisttoryChapterNum(), end, true);
                });
                es.submit(downloadThread);
                do {
                    try {
                        Thread.sleep(downloadInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isStopDownload) {
                        break downloadFor;
                    }
                } while (!isDownloadFinish);
            }
            if (isDownloadAllChapters && !isStopDownload) {
                //通知
                Intent mainIntent = new Intent(mMainActivity, MainActivity.class);
                PendingIntent mainPendingIntent = PendingIntent.getActivity(mMainActivity, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = notificationUtil.build(APPCONST.channelIdDownload)
                        .setSmallIcon(R.drawable.ic_download)
                        //通知栏大图标
                        .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getApplication().getResources(), R.mipmap.ic_launcher))
                        .setOngoing(false)
                        //点击通知后自动清除
                        .setAutoCancel(true)
                        .setContentTitle("缓存完成")
                        .setContentText("书籍一键缓存完成！")
                        .setContentIntent(mainPendingIntent)
                        .build();
                notificationUtil.notify(1002, notification);
            }
        });
    }

    /**
     * 添加下载
     *
     * @param book
     * @param mChapters
     * @param begin
     * @param end
     */
    public void addDownload(final Book book, final ArrayList<Chapter> mChapters, int begin, int end, boolean isDownloadAll) {
        if ("本地书籍".equals(book.getType())) {
            TextHelper.showText("《" + book.getName() + "》是本地书籍，不能缓存");
            return;
        }
        if (mChapters.size() == 0) {
            if (!isDownloadAll) {
                TextHelper.showText("《" + book.getName() + "》章节目录为空，缓存失败，请刷新后重试");
            }
            return;
        }
        //取消之前下载
        if (!isDownloadAll) {
            if (!isStopDownload) {
                isStopDownload = true;
                try {
                    Thread.sleep(2 * downloadInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //mHandler.sendMessage(mHandler.obtainMessage(10));
        downloadingBook = book.getName();
        final int finalBegin = Math.max(0, begin);
        final int finalEnd = Math.min(end, mChapters.size());
        needCacheChapterNum = finalEnd - finalBegin;
        curCacheChapterNum = 0;
        tempCacheChapterNum = 0;
        isStopDownload = false;
        ArrayList<Chapter> needDownloadChapters = new ArrayList<>();
        for (int i = finalBegin; i < finalEnd; i++) {
            final Chapter chapter = mChapters.get(i);
            if (StringHelper.isEmpty(chapter.getContent())) {
                needDownloadChapters.add(chapter);
            }
        }
        needCacheChapterNum = needDownloadChapters.size();
        if (!isDownloadAll && needCacheChapterNum > 0) {
            mHandler.sendEmptyMessage(12);
        }
        mHandler.postDelayed(sendDownloadNotification, 2 * downloadInterval);
        for (Chapter chapter : needDownloadChapters) {
            getChapterContent(book, chapter, new ResultCallback() {
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
            if (curCacheChapterNum == needCacheChapterNum) {
                if (!isDownloadAll) {
                    isStopDownload = true;
                }
                mHandler.sendMessage(mHandler.obtainMessage(9));
            }
            if (isStopDownload) {
                break;
            }
        }
        if (!isDownloadAll) {
            if (curCacheChapterNum == needCacheChapterNum) {
                TextHelper.showText("《" + book.getName() + "》" + mMainActivity.getString(R.string.download_already_all_tips));
            }
        }
    }


    /**
     * 获取章节内容
     *
     * @param
     * @param
     */
    private void getChapterContent(Book mBook, final Chapter chapter, final ResultCallback resultCallback) {
        if (StringHelper.isEmpty(chapter.getBookId())) {
            chapter.setBookId(mBook.getId());
        }
        ReadCrawler mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
        CommonApi.getChapterContent(chapter.getUrl(), mReadCrawler, resultCallback);
    }

    /**
     * 发送通知
     */
    private void sendNotification() {
        if (curCacheChapterNum == needCacheChapterNum) {
            mHandler.sendEmptyMessage(9);
            notificationUtil.cancelAll();
            return;
        } else {
            Notification notification = notificationUtil.build(APPCONST.channelIdDownload)
                    .setSmallIcon(R.drawable.ic_download)
                    //通知栏大图标
                    .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getApplication().getResources(), R.mipmap.ic_launcher))
                    .setOngoing(true)
                    //点击通知后自动清除
                    .setAutoCancel(true)
                    .setContentTitle("正在下载：" + downloadingBook +
                            "[" + curCacheChapterNum + "/" + needCacheChapterNum + "]")
                    .setContentText(downloadingChapter == null ? "  " : downloadingChapter)
                    .addAction(R.drawable.ic_stop_black_24dp, "停止",
                            notificationUtil.getChancelPendingIntent(cancelDownloadReceiver.class))
                    .build();
            notificationUtil.notify(1000, notification);
        }
        if (tempCacheChapterNum < curCacheChapterNum) {
            tempCount = 1500 / downloadInterval;
            tempCacheChapterNum = curCacheChapterNum;
        } else if (tempCacheChapterNum == curCacheChapterNum) {
            tempCount--;
            if (tempCount == 0) {
                isDownloadFinish = true;
                notificationUtil.cancel(1000);
                return;
            }
        }
        mHandler.postDelayed(sendDownloadNotification, 2 * downloadInterval);
    }

    public static class cancelDownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //todo 跳转之前要处理的逻辑
            if (CANCEL_ACTION.equals(intent.getAction())) {
                isStopDownload = true;
            }
        }
    }

    /**
     * 添加本地书籍
     *
     * @param path
     */
    public void addLocalBook(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        Book book = new Book();
        book.setName(file.getName().replace(".txt", ""));
        book.setChapterUrl(path);
        book.setType("本地书籍");
        book.setHistoryChapterId("未开始阅读");
        book.setNewestChapterTitle("未拆分章节");
        book.setAuthor("本地书籍");
        book.setSource(BookSource.local.toString());
        book.setDesc("");

        //判断书籍是否已经添加
        Book existsBook = mBookService.findBookByAuthorAndName(book.getName(), book.getAuthor());
        if (book.equals(existsBook)) {
            TextHelper.showText("该书籍已存在，请勿重复添加！");
            return;
        }
        mBookService.addBook(book);
        TextHelper.showText("本地书籍添加成功");
        init();
    }

    /**
     * 同步书架
     */
    private void synBookcaseToWeb(boolean isAutoSyn) {
        if (!NetworkUtils.isNetWorkAvailable()) {
            if (!isAutoSyn) {
                TextHelper.showText("无网络连接！");
            }
            return;
        }
        Date nowTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
        String nowTimeStr = sdf.format(nowTime);
        SharedPreUtils spb = SharedPreUtils.getInstance();
        String synTime = spb.getString("synTime");
        if (!nowTimeStr.equals(synTime) || !isAutoSyn) {
            MyApplication.getApplication().newThread(() -> {
                if (UserService.webBackup()) {
                    spb.putString("synTime", nowTimeStr);
                    if (!isAutoSyn) {
                        DialogCreator.createTipDialog(mMainActivity, "成功将书架同步至网络！");
                    }
                } else {
                    if (!isAutoSyn) {
                        DialogCreator.createTipDialog(mMainActivity, "同步失败，请重试！");
                    }
                }
            });
        }
    }

    /*****************************************用于返回按钮判断*************************************/
    /**
     * 判断是否处于编辑状态
     *
     * @return
     */
    public boolean ismEditState() {
        if (mBookcaseAdapter != null) {
            return mBookcaseAdapter.ismEditState();
        }
        return false;
    }

    /**
     * 取消编辑状态
     */
    public void cancelEdit() {
        editBookcase(false);
    }

    /**
     *
     */
    public void destroy() {
        notificationUtil.cancelAll();
        mHandler.removeCallbacks(sendDownloadNotification);
        for (int i = 0; i < 13; i++) {
            mHandler.removeMessages(i + 1);
        }
    }


    /*class NotificationService extends Service{

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        public void sendNotification(Book book, String chapterTitle){
            //创建 Notification.Builder 对象
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mMainActivity, "channel_download")
                    .setSmallIcon(R.drawable.ic_download)
                    //通知栏大图标
                    .setLargeIcon(BitmapFactory.decodeResource(mMainActivity.getResources(), R.mipmap.ic_launcher))
                    //点击通知后自动清除
                    .setAutoCancel(true)
                    .setContentTitle("正在下载：" + book.getName())
                    .setContentText(chapterTitle);
            builder.addAction(R.drawable.ic_stop_black_24dp, "取消", null);
            //发送通知
            startForeground(1, builder.build());
        }
    }*/


}
