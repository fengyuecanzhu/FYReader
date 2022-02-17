package xyz.fycz.myreader.application;


import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.style.MaterialStyle;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.internal.functions.Functions;
import io.reactivex.plugins.RxJavaPlugins;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.dialog.UpdateDialog;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.help.SSLSocketClient;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.AdUtils;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;


public class App extends Application {

    public static final String TAG = App.class.getSimpleName();
    private static final Handler handler = new Handler();
    private static App application;
    private ExecutorService mFixedThreadPool;
    private static boolean debug;
    public static boolean isBackground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        debug = isApkInDebug(this);
        CrashHandler.register(this);
        firstInit();
        SSLSocketClient.trustAllHosts();//信任所有证书
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            webviewSetPath(this);
        }
        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15_000) // set connection timeout.
                        .readTimeout(15_000) // set read timeout.
                ))
                .commit();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        mFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());//初始化线程池
        initNightTheme();
//        LLog.init(APPCONST.LOG_DIR);
        initDialogX();
        AdUtils.initAd();
    }


    private void firstInit() {
        SharedPreUtils sru = SharedPreUtils.getInstance();
        if (!sru.getBoolean("firstInit")) {
            BookSourceManager.initDefaultSources();
            sru.putBoolean("firstInit", true);
        }
    }

    private void initDialogX() {
        DialogX.init(this);
        DialogX.DEBUGMODE = debug;
        DialogX.globalStyle = MaterialStyle.style();
    }

    public void initNightTheme() {
        if (isNightFS()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            DialogX.globalTheme = DialogX.THEME.AUTO;
        } else {
            if (isNightTheme()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                DialogX.globalTheme = DialogX.THEME.DARK;
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                DialogX.globalTheme = DialogX.THEME.LIGHT;
            }
        }
    }

    public boolean isNightTheme() {
        return !SysManager.getSetting().isDayStyle();
    }

    public boolean isNightFS() {
        return SharedPreUtils.getInstance().getBoolean(getString(R.string.isNightFS), false);
    }

    /**
     * 设置夜间模式
     *
     * @param isNightMode
     */
    public void setNightTheme(boolean isNightMode) {
        SharedPreUtils.getInstance().putBoolean(getmContext().getString(R.string.isNightFS), false);
        Setting setting = SysManager.getSetting();
        setting.setDayStyle(!isNightMode);
        SysManager.saveSetting(setting);
        App.getApplication().initNightTheme();
    }


    public static Context getmContext() {
        return application;
    }


    public void newThread(Runnable runnable) {
        try {
            mFixedThreadPool.execute(runnable);
        } catch (Exception e) {
            //e.printStackTrace();
            mFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());//初始化线程池
            mFixedThreadPool.execute(runnable);
        }
    }

    public void shutdownThreadPool() {
        mFixedThreadPool.shutdownNow();
    }


    @TargetApi(26)
    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel downloadChannel = new NotificationChannel(APPCONST.channelIdDownload, "下载通知", NotificationManager.IMPORTANCE_LOW);
        downloadChannel.enableLights(true);//是否在桌面icon右上角展示小红点
        downloadChannel.setLightColor(Color.RED);//小红点颜色
        downloadChannel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
        notificationManager.createNotificationChannel(downloadChannel);

        NotificationChannel readChannel = new NotificationChannel(APPCONST.channelIdRead, "朗读通知", NotificationManager.IMPORTANCE_LOW);
        readChannel.enableLights(true);//是否在桌面icon右上角展示小红点
        readChannel.setLightColor(Color.RED);//小红点颜色
        readChannel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
        notificationManager.createNotificationChannel(readChannel);
    }


    /**
     * 主线程执行
     *
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public static Handler getHandler() {
        return handler;
    }

    public static App getApplication() {
        return application;
    }


    private boolean isFolderExist(String dir) {
        File folder = Environment.getExternalStoragePublicDirectory(dir);
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }

    /**
     * 获取app版本号
     *
     * @return
     */
    public static int getVersionCode() {
        try {
            PackageManager manager = application.getPackageManager();
            PackageInfo info = manager.getPackageInfo(application.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取app版本号(String)
     *
     * @return
     */
    public static String getStrVersionName() {
        try {
            PackageManager manager = application.getPackageManager();
            PackageInfo info = manager.getPackageInfo(application.getPackageName(), 0);

            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.0";
        }
    }

    /**
     * 获取apk包的信息：版本号，名称，图标等
     *
     * @param absPath apk包的绝对路径
     */
    public static int apkInfo(String absPath) {
        PackageManager pm = application.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            int versionCode = pkgInfo.versionCode;
            Log.i(TAG, String.format("PkgInfo: %s", versionCode));
            return versionCode;
        }
        return 0;
    }


    /**
     * 检查更新
     */
    public static void checkVersionByServer(final AppCompatActivity activity, final boolean isManualCheck) {
        App.getApplication().newThread(() -> {
            try {
                String url = "https://shimo.im/docs/cqkgjPRRydYYhQKt/read";
                if (debug) {
                    url = "https://shimo.im/docs/zfzpda7MUGskOC9v/read";
                }
                String html = OkHttpUtils.getHtml(url);
                Document doc = Jsoup.parse(html);
                String content = doc.getElementsByClass("ql-editor").text();
                if (StringHelper.isEmpty(content)) {
                    content = OkHttpUtils.getUpdateInfo();
                    if (StringHelper.isEmpty(content)) {
                        if (isManualCheck || NetworkUtils.isNetWorkAvailable()) {
                            ToastUtils.showError("检查更新失败！");
                        }
                        return;
                    }
                }
                String[] contents = content.split(";");
                int newestVersion = 0;
                String updateContent = "";
                String downloadLink = null;
                boolean isForceUpdate = false;
                int forceUpdateVersion;
                StringBuilder s = new StringBuilder();
                newestVersion = Integer.parseInt(contents[0].substring(contents[0].indexOf(":") + 1));
                isForceUpdate = Boolean.parseBoolean(contents[1].substring(contents[1].indexOf(":") + 1));
                downloadLink = contents[2].substring(contents[2].indexOf(":") + 1).trim();
                updateContent = contents[3].substring(contents[3].indexOf(":") + 1);
                SharedPreUtils spu = SharedPreUtils.getInstance();
                spu.putString(getmContext().getString(R.string.lanzousKeyStart), contents[4].substring(contents[4].indexOf(":") + 1));

                String newSplashTime = contents[5].substring(contents[5].indexOf(":") + 1);
                String oldSplashTime = spu.getString("splashTime");
                spu.putBoolean("needUdSI", !oldSplashTime.equals(newSplashTime));
                spu.putString("splashTime", contents[5].substring(contents[5].indexOf(":") + 1));
                spu.putString("splashImageUrl", contents[6].substring(contents[6].indexOf(":") + 1));
                spu.putString("splashImageMD5", contents[7].substring(contents[7].indexOf(":") + 1));

                forceUpdateVersion = Integer.parseInt(contents[8].substring(contents[8].indexOf(":") + 1));
                spu.putInt("forceUpdateVersion", forceUpdateVersion);

                String domain = contents[9].substring(contents[9].indexOf(":") + 1);
                spu.putString("domain", domain);

                int versionCode = getVersionCode();

                isForceUpdate = isForceUpdate && forceUpdateVersion > versionCode;
                if (!StringHelper.isEmpty(downloadLink)) {
                    spu.putString(getmContext().getString(R.string.downloadLink), downloadLink);
                } else {
                    spu.putString(getmContext().getString(R.string.downloadLink), URLCONST.APP_DIR_UR);
                }
                String[] updateContents = updateContent.split("/");
                for (String string : updateContents) {
                    s.append(string);
                    s.append("<br>");
                }
                Log.i("检查更新，最新版本", newestVersion + "");
                if (newestVersion > versionCode) {
                    Setting setting = SysManager.getSetting();
                    if (isManualCheck || setting.getNewestVersionCode() < newestVersion || isForceUpdate) {
                        setting.setNewestVersionCode(newestVersion);
                        SysManager.saveSetting(setting);
                        getApplication().updateApp2(activity, downloadLink, newestVersion, s.toString(), isForceUpdate
                        );
                    }
                } else if (isManualCheck) {
                    ToastUtils.showSuccess("已经是最新版本！");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("检查更新失败！", "" + e.getLocalizedMessage());
                if (isManualCheck || NetworkUtils.isNetWorkAvailable()) {
                    ToastUtils.showError("检查更新失败！");
                }
            }
        });
    }


    public void updateApp2(final AppCompatActivity activity, final String url, final int versionCode, String message,
                           final boolean isForceUpdate) {
        //String version = (versionCode / 100 % 10) + "." + (versionCode / 10 % 10) + "." + (versionCode % 10);
        int hun = versionCode / 100;
        int ten = versionCode / 10 % 10;
        int one = versionCode % 10;
        String versionName = "v" + hun + "." + ten + "." + one;
        UpdateDialog updateDialog = new UpdateDialog.Builder()
                .setVersion(versionName)
                .setContent(message)
                .setCancelable(!isForceUpdate)
                .setDownloadUrl(url)
                .setContentHtml(true)
                .setDebug(App.isDebug())
                .build();

        updateDialog.showUpdateDialog(activity);
    }

    private void goDownload(Activity activity, String url) {
        String downloadLink = url;
        if (url == null || "".equals(url)) {
            downloadLink = URLCONST.APP_DIR_UR;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(downloadLink));
        activity.startActivity(intent);
    }

    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断Activity是否Destroy
     *
     * @param mActivity
     * @return
     */
    public static boolean isDestroy(Activity mActivity) {
        return mActivity == null || mActivity.isFinishing() || mActivity.isDestroyed();
    }


    /****************
     *
     * 发起添加群流程。群号：风月读书交流群(1085028304) 的 key 为： 8PIOnHFuH6A38hgxvD_Rp2Bu-Ke1ToBn
     * 调用 joinQQGroup(8PIOnHFuH6A38hgxvD_Rp2Bu-Ke1ToBn) 即可发起手Q客户端申请加群 风月读书交流群(1085028304)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     ******************/
    public static boolean joinQQGroup(Context context, String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    @RequiresApi(api = 28)
    public void webviewSetPath(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName(context);

            if (!getApplicationContext().getPackageName().equals(processName)) {//判断不等于默认进程名称
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }

    public String getProcessName(Context context) {
        if (context == null) return null;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }

    public static boolean isDebug() {
        return debug;
    }

    public ExecutorService getmFixedThreadPool() {
        return mFixedThreadPool;
    }

}
