package xyz.fycz.myreader.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.weaction.ddsdk.ad.DdSdkSplashAd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivitySplashBinding;
import xyz.fycz.myreader.greendao.service.BookGroupService;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.help.DateHelper;
import xyz.fycz.myreader.util.utils.AdUtils;
import xyz.fycz.myreader.util.utils.ImageLoader;
import xyz.fycz.myreader.util.utils.MD5Utils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.SystemBarUtils;

public class SplashActivity extends BaseActivity {
    /*************Constant**********/
    public static final String TAG = SplashActivity.class.getSimpleName();
    public static int WAIT_INTERVAL = 0;

    private ActivitySplashBinding binding;
    private SharedPreUtils spu;
    private int todayAdCount;
    private int adTimes;
    private boolean hasStart = false;
    private boolean startToAd = false;
    private static final String INTENT_TO_AD = "startToAd";


    //创建子线程
    private Runnable start = () -> {
        if (!hasStart && !App.isDestroy(SplashActivity.this)) {
            hasStart = true;
            try {
                Thread.sleep(WAIT_INTERVAL);//使程序休眠
                Intent it = new Intent(SplashActivity.this, MainActivity.class);//启动MainActivity
                it.putExtra("startFromSplash", true);
                startActivity(it);
                finish();//关闭当前活动
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Thread countTime = new Thread() {
        @Override
        public void run() {
            App.runOnUiThread(() -> binding.tvSkip.setVisibility(View.VISIBLE));
            for (int i = 0; i < 5; i++) {
                int time = 5 - i;
                App.runOnUiThread(() -> binding.tvSkip.setText(getString(R.string.skip_ad, time)));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            WAIT_INTERVAL = 0;
            startNormal();
        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(INTENT_TO_AD, true);
        context.startActivity(intent);
    }

    @Override
    protected void bindView() {
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0 && !startToAd) {
            finish();
            return;
        }
        ImmersionBar.with(this)
                .fullScreen(true)
                .init();
        SystemBarUtils.hideStableStatusBar(this);
        //loadImage();
        boolean agreePrivacyPolicy = SharedPreUtils.getInstance().getBoolean("agreePrivacyPolicy");
        if (agreePrivacyPolicy) {
            requestPermission();
        } else {
            MyAlertDialog.showPrivacyDialog(this, (dialog, which) -> {
                SharedPreUtils.getInstance().putBoolean("agreePrivacyPolicy", true);
                requestPermission();
            }, (dialog, which) -> android.os.Process.killProcess(android.os.Process.myPid()));
        }
    }

    @Override
    protected boolean initSwipeBackEnable() {
        return false;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        startToAd = getIntent().getBooleanExtra(INTENT_TO_AD, false);
        spu = SharedPreUtils.getInstance();
        String splashAdCount = spu.getString("splashAdCount");
        adTimes = spu.getInt("curAdTimes", 3);
        String[] splashAdCounts = splashAdCount.split(":");
        String today = DateHelper.getYearMonthDay1();
        if (today.equals(splashAdCounts[0])) {
            todayAdCount = Integer.parseInt(splashAdCounts[1]);
        } else {
            todayAdCount = 0;
        }
    }

    @Override
    protected void initClick() {
        binding.tvSkip.setOnClickListener(v -> {
            WAIT_INTERVAL = 0;
            startNormal();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void start() {
        //startNoAd();
        /*if (adTimes >= 0 && todayAdCount >= adTimes) {
            startNoAd();
        } else {*/
            /*App.getHandler().postDelayed(() -> {
                binding.tvSkip.setVisibility(View.VISIBLE);
            }, 2000);*/
        AdUtils.checkHasAd()
                .subscribe(new MySingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(@NonNull Boolean aBoolean) {
                        if (aBoolean) {
                            //AdUtils.initAd();
                            startWithAd();
                            binding.ivSplash.setVisibility(View.GONE);
                            binding.llAd.setVisibility(View.VISIBLE);
                        } else {
                            startNoAd();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        startNoAd();
                    }
                });
        //}
    }

    private void startNoAd() {
        Animation inAni = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in);
        binding.ivSplash.setVisibility(View.VISIBLE);
        binding.ivSplash.startAnimation(inAni);
        binding.llAd.setVisibility(View.GONE);
        WAIT_INTERVAL = 1500;
        loadImage();
        startNormal();
    }

    private void startNormal() {
        if (!App.isDestroy(this)) {
            if (startToAd) {
                finish();
                return;
            }
            if (BookGroupService.getInstance().curGroupIsPrivate()) {
                App.runOnUiThread(() -> {
                    MyAlertDialog.showPrivateVerifyDia(SplashActivity.this, needGoTo -> {
                        if (!hasStart)
                            (new Thread(start)).start();
                    }, () -> {
                        SharedPreUtils.getInstance().putString(SplashActivity.this.getString(R.string.curBookGroupId), "");
                        SharedPreUtils.getInstance().putString(SplashActivity.this.getString(R.string.curBookGroupName), "");
                        if (!hasStart)
                            (new Thread(start)).start();
                    });
                });
            } else {
                if (!hasStart)
                    (new Thread(start)).start();
            }
        }
    }

    private void startWithAd() {
        try {
            new DdSdkSplashAd().show(binding.flAd, this, new DdSdkSplashAd.CountdownCallback() {
                // 展示成功
                @Override
                public void show() {
                    SharedPreUtils.getInstance(true).putLong("splashAdTime", System.currentTimeMillis());
                    Log.d(TAG, "广告展示成功");
                    AdUtils.adRecord("splash", "adShow");
                    countTodayAd();
                    //countTime.start();
                }

                // 广告被点击
                @Override
                public void click() {
                    Log.d(TAG, "广告被点击");
                    AdUtils.adRecord("splash", "adClick");
                }

                // 展示出错时可读取 msg 中的错误信息
                @Override
                public void error(String msg) {
                    WAIT_INTERVAL = 1500;
                    startNormal();
                    Log.e(TAG, msg);
                    //ToastUtils.showError(msg);
                }

                // 倒计时结束或用户主动点击跳过按钮后调用
                @Override
                public void finishCountdown() {
                    Log.d(TAG, "倒计时结束或用户主动点击跳过按钮");
                    WAIT_INTERVAL = 0;
                    AdUtils.adRecord("splash", "adFinishCount");
                    startNormal();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            WAIT_INTERVAL = 1500;
            startNormal();
        }
    }


    private void loadImage() {
        File imgFile = getFileStreamPath(APPCONST.FILE_NAME_SPLASH_IMAGE);
        SharedPreUtils preUtils = SharedPreUtils.getInstance();
        String splashImageMD5 = preUtils.getString("splashImageMD5");
        if (!imgFile.exists() || preUtils.getBoolean("needUdSI") ||
                !splashImageMD5.equals(MD5Utils.INSTANCE.getFileMD5s(imgFile, 16))) {
            if ("".equals(splashImageMD5)) return;
            downLoadImage();
            return;
        }
        String splashLoadDate = preUtils.getString("splashTime");
        if (splashLoadDate.equals("")) {
            return;
        }
        long startTime = 0;
        long endTime = 0;
        long curTime = DateHelper.getLongDate();
        if (splashLoadDate.contains("~")) {
            String[] splashLoadDates = splashLoadDate.split("~");
            startTime = DateHelper.strDateToLong(splashLoadDates[0] + " 00:00:00");
            endTime = DateHelper.strDateToLong(splashLoadDates[1] + " 00:00:00");
        }
        if (startTime == 0) {
            startTime = DateHelper.strDateToLong(splashLoadDate + " 00:00:00");
        }
        if (endTime == 0) {
            endTime = startTime + 24 * 60 * 60 * 1000;
        }
        if (curTime >= startTime && curTime <= endTime) {
            WAIT_INTERVAL = 1500;
            RequestOptions options = new RequestOptions()
                    .error(R.drawable.start)
                    .signature(new ObjectKey(splashLoadDate));
            ImageLoader.INSTANCE
                    .load(this, imgFile)
                    /*.error(R.drawable.start)
                    .signature(new ObjectKey(splashLoadDate))*/
                    .apply(options)
                    .into(binding.ivSplash);
        }
    }

    private void downLoadImage() {
        App.getApplication().newThread(() -> {
            String url = SharedPreUtils.getInstance().getString("splashImageUrl", "");
            if (!url.equals("")) {
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    is = OkHttpUtils.getInputStream(url);
                    fos = openFileOutput(APPCONST.FILE_NAME_SPLASH_IMAGE, MODE_PRIVATE);
                    byte[] bytes = new byte[1024];
                    int len;
                    while ((len = is.read(bytes)) != -1) {
                        fos.write(bytes, 0, len);
                    }
                    fos.flush();
                    Log.d("SplashActivity", "downLoadImage success!");
                } catch (Exception e) {
                    File data = getFileStreamPath(APPCONST.FILE_NAME_SPLASH_IMAGE);
                    if (data != null && data.exists()) {
                        data.delete();
                    }
                    e.printStackTrace();
                } finally {
                    IOUtils.close(is, fos);
                }
            }
        });
    }

    private void requestPermission() {
        //获取读取和写入SD卡的权限
        XXPermissions.with(this)
                /*.permission(new String[]{Permission.READ_PHONE_STATE,
                        Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.ACCESS_COARSE_LOCATION})*/
                .permission(APPCONST.STORAGE_PERMISSIONS)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        start();
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (permissions.contains(Permission.WRITE_EXTERNAL_STORAGE))
                            ToastUtils.showWarring("储存权限被拒绝，部分功能可能无法正常运行！");
                        start();
                    }
                });
    }


    private void countTodayAd() {
        String today = DateHelper.getYearMonthDay1();
        todayAdCount++;
        spu.putString("splashAdCount", today + ":" + todayAdCount);
    }
}
