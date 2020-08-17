package xyz.fycz.myreader.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.view.Window;
import android.view.WindowManager;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.ui.activity.MainActivity;
import xyz.fycz.myreader.util.PermissionsChecker;
import xyz.fycz.myreader.util.TextHelper;

public class SplashActivity extends AppCompatActivity {
    /*************Constant**********/
    private static final int WAIT_INTERVAL = 1000;
    private static final int PERMISSIONS_REQUEST_STORAGE = 1;

    static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private PermissionsChecker mPermissionsChecker;
    private Thread myThread = new Thread() {//创建子线程
        @Override
        public void run() {
            try {
                sleep(WAIT_INTERVAL);//使程序休眠
                Intent it = new Intent(getApplicationContext(), MainActivity.class);//启动MainActivity
                startActivity(it);
                finish();//关闭当前活动
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            mPermissionsChecker = new PermissionsChecker(this);
            requestPermission();
        }else {
            myThread.start();
        }


    }
    // 全屏显示
    private void setFullScreen() {
        // 如果该类是 extends Activity ，下面这句代码起作用
        // 去除ActionBar(因使用的是NoActionBar的主题，故此句有无皆可)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 如果该类是 extends AppCompatActivity， 下面这句代码起作用
        if (getSupportActionBar() != null){ getSupportActionBar().hide(); }
        // 去除状态栏，如 电量、Wifi信号等
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
                    TextHelper.showText("请给予储存权限，否则程序无法正常运行！");
                }
                return;
            }
        }
    }
}
