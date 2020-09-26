package xyz.fycz.myreader.application;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatDelegate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.creator.APPDownloadTip;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.entity.UpdateInfo;
import xyz.fycz.myreader.ui.activity.MainActivity;
import xyz.fycz.myreader.ui.fragment.BookcaseFragment;
import xyz.fycz.myreader.util.*;
import xyz.fycz.myreader.util.utils.NetworkUtils;


public class MyApplication extends Application {

    private static Handler handler = new Handler();
    private static MyApplication application;
    private ExecutorService mFixedThreadPool;


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        HttpUtil.trustAllHosts();//信任所有证书
//        handleSSLHandshake();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        mFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());//初始化线程池

        BaseActivity.setCloseAntiHijacking(true);
        initNightTheme();
    }

    public void initNightTheme() {
        if (isNightFS()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else {
            if (isNightTheme()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    protected boolean isNightTheme() {
        return !SysManager.getSetting().isDayStyle();
    }

    public boolean isNightFS() {
        return SharedPreUtils.getInstance().getBoolean("isNightFS", false);
    }

    /**
     * 设置夜间模式
     * @param isNightMode
     */
    public void setNightTheme(boolean isNightMode) {
        SharedPreUtils.getInstance().putBoolean("isNightFS", false);
        Setting setting = SysManager.getSetting();
        setting.setDayStyle(!isNightMode);
        SysManager.saveSetting(setting);
        MyApplication.getApplication().initNightTheme();
    }

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
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
        NotificationChannel channel = new NotificationChannel(APPCONST.channelIdDownload, "下载通知", NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);//是否在桌面icon右上角展示小红点
        channel.setLightColor(Color.RED);//小红点颜色
        channel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
        notificationManager.createNotificationChannel(channel);
    }


    /**
     * 主线程执行
     *
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public static MyApplication getApplication() {
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
     * 检查更新
     */
    public static void checkVersion(AppCompatActivity activity) {
        UpdateInfo updateInfo = (UpdateInfo) CacheHelper.readObject(APPCONST.FILE_NAME_UPDATE_INFO);
        int versionCode = getVersionCode();
        if (updateInfo != null) {
            if (updateInfo.getNewestVersionCode() > versionCode) {
                //updateApp(activity, updateInfo.getDownLoadUrl(), versionCode);
            }
        }
    }


    /**
     * 检查更新
     */
    /*public static void checkVersionByServer(final Activity activity, final boolean isManualCheck) {
        MyApplication.getApplication().newThread(new Runnable() {
            @Override
            public void run() {
                Document doc = null;
                try {
                    doc = Jsoup.connect("https://novel.fycz.xyz/app/update.html").get();
                    int newestVersion = 0;
                    String updateContent = "";
                    String downloadLink = null;
                    boolean isForceUpdate = false;
                    StringBuilder s = new StringBuilder();
                    assert doc != null;
                    Elements nodes = doc.getElementsByClass("secd-rank-list");
                    newestVersion = Integer.valueOf(nodes.get(0).getElementsByTag("a").get(1).text());
                    downloadLink = nodes.get(0).getElementsByTag("a").get(1).attr("href");
                    updateContent = nodes.get(0).getElementsByTag("a").get(2).text();
                    isForceUpdate = Boolean.parseBoolean(nodes.get(0).getElementsByTag("a").get(3).text());
                    String[] updateContents = updateContent.split("/");
                    for (String string : updateContents) {
                        s.append(string);
                        s.append("\n");
                    }
                    int versionCode = getVersionCode();
                    if (newestVersion > versionCode) {
                        MyApplication m = new MyApplication();
                        Setting setting = SysManager.getSetting();
                        if (isManualCheck || setting.getNewestVersionCode() < newestVersion || isForceUpdate) {
                            setting.setNewestVersionCode(newestVersion);
                            SysManager.saveSetting(setting);
                            int i = setting.getNewestVersionCode();
                            m.updateApp(activity, downloadLink, newestVersion, s.toString(), isForceUpdate);
                        }
                    } else if (isManualCheck) {
                        TextHelper.showText("已经是最新版本！");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    TextHelper.showText("无网络连接！");
                }
            }
        });
    }*/
    public static void checkVersionByServer(final AppCompatActivity activity, final boolean isManualCheck,
                                            final BookcaseFragment mBookcaseFragment) {
        MyApplication.getApplication().newThread(() -> {
            Document doc = null;
            try {
                String url = "https://shimo.im/docs/cqkgjPRRydYYhQKt/read";
                if (isApkInDebug(getmContext())) {
                    url = "https://shimo.im/docs/zfzpda7MUGskOC9v/read";
                }
                doc = Jsoup.connect(url).get();
                String content = doc.getElementsByClass("ql-editor").text();
                if (StringHelper.isEmpty(content)) {
                    if (isManualCheck || NetworkUtils.isNetWorkAvailable()) {
                        ToastUtils.showError("检查更新失败！");
                    }
                    return;
                }
                String[] contents = content.split(";");
                int newestVersion = 0;
                String updateContent = "";
                String downloadLink = null;
                boolean isForceUpdate = false;
                StringBuilder s = new StringBuilder();
                newestVersion = Integer.parseInt(contents[0].substring(contents[0].indexOf(":") + 1));
                isForceUpdate = Boolean.parseBoolean(contents[1].substring(contents[1].indexOf(":") + 1));
                downloadLink = contents[2].substring(contents[2].indexOf(":") + 1).trim();
                updateContent = contents[3].substring(contents[3].indexOf(":") + 1);
                SharedPreUtils spu = SharedPreUtils.getInstance();
                spu.putString("lanzousKeyStart", contents[4].substring(contents[4].indexOf(":") + 1));
                if (!StringHelper.isEmpty(downloadLink)) {
                    spu.putString("downloadLink", downloadLink);
                } else {
                    spu.putString("downloadLink", URLCONST.APP_DIR_UR);
                }
                String[] updateContents = updateContent.split("/");
                for (String string : updateContents) {
                    s.append(string);
                    s.append("\n");
                }
                int versionCode = getVersionCode();
                Log.i("检查更新，最新版本", newestVersion + "");
                if (newestVersion > versionCode) {
                    MyApplication m = new MyApplication();
                    Setting setting = SysManager.getSetting();
                    if (isManualCheck || setting.getNewestVersionCode() < newestVersion || isForceUpdate) {
                        setting.setNewestVersionCode(newestVersion);
                        SysManager.saveSetting(setting);
                        m.updateApp(activity, downloadLink, newestVersion, s.toString(), isForceUpdate,
                                mBookcaseFragment);
                    }
                } else if (isManualCheck) {
                    ToastUtils.showSuccess("已经是最新版本！");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("检查更新失败！", e.getLocalizedMessage());
                if (isManualCheck || NetworkUtils.isNetWorkAvailable()) {
                    ToastUtils.showError("检查更新失败！");
                }
            }
        });
    }

    /**
     * App自动升级
     *
     * @param activity
     * @param versionCode
     */
    public void updateApp(final AppCompatActivity activity, final String url, final int versionCode, String message,
                          final boolean isForceUpdate, final BookcaseFragment mBookcaseFragment) {
        //String version = (versionCode / 100 % 10) + "." + (versionCode / 10 % 10) + "." + (versionCode % 10);
        String cancelTitle;
        if (isForceUpdate) {
            cancelTitle = "退出";
        } else {
            cancelTitle = "忽略此版本";
        }
        if (mBookcaseFragment == null) {
            DialogCreator.createCommonDialog(activity, "发现新版本：", message, true, "取消", "立即更新", null,
                    (dialog, which) -> goDownload(activity, url));
            return;
        }

        DialogCreator.createThreeButtonDialog(activity, "发现新版本：", message, !isForceUpdate, cancelTitle, "直接下载",
                "浏览器下载", (dialog, which) -> {
                    if (isForceUpdate) {
                        activity.finish();
                    }
                }, (dialog, which) -> {
                    if (activity instanceof MainActivity){
                        MainActivity mainActivity = (MainActivity) activity;
                        mainActivity.getViewPagerMain().setCurrentItem(0);
                    }
                    if (url == null || "".equals(url)) {
                        ToastUtils.showError("获取链接失败，请前往浏览器下载！");
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(URLCONST.APP_DIR_UR));
                        activity.startActivity(intent);
                        if (isForceUpdate) {
                            activity.finish();
                        }
                    } else {
                        APPDownloadTip downloadTip = new APPDownloadTip(url, mBookcaseFragment, activity, isForceUpdate);
                        downloadTip.downloadApp();
                    }
                }, (dialog, which) -> {
                    goDownload(activity, url);
                    if (isForceUpdate) {
                        activity.finish();
                    }
                });
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
}
