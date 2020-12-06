package xyz.fycz.myreader.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.gyf.immersionbar.ImmersionBar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.util.DateHelper;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.PermissionsChecker;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.ImageLoader;
import xyz.fycz.myreader.util.utils.MD5Utils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.SystemBarUtils;

public class SplashActivity extends BaseActivity {
    /*************Constant**********/
    private static int WAIT_INTERVAL = 1000;
    private static final int PERMISSIONS_REQUEST_STORAGE = 1;

    static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @BindView(R.id.iv_splash)
    AppCompatImageView ivSplash;

    private PermissionsChecker mPermissionsChecker;
    private Thread myThread = new Thread() {//创建子线程
        @Override
        public void run() {
            try {
                sleep(WAIT_INTERVAL);//使程序休眠
                Intent it = new Intent(SplashActivity.this, MainActivity.class);//启动MainActivity
                startActivity(it);
                finish();//关闭当前活动
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected int getContentId() {
        return R.layout.activity_splash;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        ImmersionBar.with(this)
                .fullScreen(true)
                .init();
        SystemBarUtils.hideStableStatusBar(this);
        loadImage();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            mPermissionsChecker = new PermissionsChecker(this);
            requestPermission();
        }else {
            myThread.start();
        }
    }

    private void loadImage() {
        File imgFile = getFileStreamPath(APPCONST.FILE_NAME_SPLASH_IMAGE);
        SharedPreUtils preUtils = SharedPreUtils.getInstance();
        String splashImageMD5 = preUtils.getString("splashImageMD5");
        if (!imgFile.exists() || preUtils.getBoolean("needUdSI") ||
                !splashImageMD5.equals(MD5Utils.getFileMD5s(imgFile, 16))){
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
        if (startTime == 0){
            startTime = DateHelper.strDateToLong(splashLoadDate + " 00:00:00");
        }
        if (endTime == 0){
            endTime = startTime + 24 * 60 * 60 * 1000;
        }
        if (curTime >= startTime && curTime <= endTime){
            WAIT_INTERVAL = 1500;
            ImageLoader.INSTANCE
                    .load(this, imgFile)
                    .error(R.drawable.start)
                    .signature(new ObjectKey(splashLoadDate))
                    .into(ivSplash);
        }
    }

    private void downLoadImage() {
        MyApplication.getApplication().newThread(() -> {
            String url = SharedPreUtils.getInstance().getString("splashImageUrl", "");
            if (!url.equals("")) {
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    is = OkHttpUtils.getInputStream(url);
                    fos = openFileOutput(APPCONST.FILE_NAME_SPLASH_IMAGE, MODE_PRIVATE);
                    byte[] bytes = new byte[1024];
                    int len;
                    while ((len = is.read(bytes)) != -1){
                        fos.write(bytes, 0, len);
                    }
                    fos.flush();
                    Log.d("SplashActivity", "downLoadImage success!");
                } catch (IOException e) {
                    File data = getFileStreamPath(APPCONST.FILE_NAME_SPLASH_IMAGE);
                    if (data != null && data.exists()){
                        data.delete();
                    }
                    e.printStackTrace();
                }finally {
                    IOUtils.close(is, fos);
                }
            }
        });
    }

    private void requestPermission(){
        //获取读取和写入SD卡的权限
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_STORAGE);
        } else {
            myThread.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_STORAGE: {
                // 如果取消权限，则返回的值为0
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请权限成功
                    myThread.start();
                } else {
                    //申请权限失败
                    finish();
                    ToastUtils.showWarring("请给予储存权限，否则程序无法正常运行！");
                }
                return;
            }
        }
    }
}
