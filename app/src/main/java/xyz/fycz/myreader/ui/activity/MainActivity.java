package xyz.fycz.myreader.ui.activity;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.custom.CircleImageView;
import xyz.fycz.myreader.ui.presenter.MainPresenter;
import xyz.fycz.myreader.util.TextHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

import java.io.File;

import static xyz.fycz.myreader.util.UriFileUtil.getPath;


public class MainActivity extends BaseActivity {


    @BindView(R.id.civ_avatar)
    CircleImageView civAvatar;
    @BindView(R.id.tl_tab_menu)
    TabLayout tlTabMenu;
    @BindView(R.id.iv_search)
    ImageView ivSearch;
    @BindView(R.id.iv_more)
    ImageView ivMore;
    @BindView(R.id.rl_common_title)
    RelativeLayout rlCommonTitle;
    @BindView(R.id.tv_edit_finish)
    TextView tvEditFinish;
    @BindView(R.id.rl_edit_titile)
    RelativeLayout rlEditTitle;
    @BindView(R.id.vp_content)
    ViewPager vpContent;
    private MainPresenter mMainPrensenter;


    private File appFile;
    private boolean isForceUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setStatusBar(R.color.sys_line);
        mMainPrensenter = new MainPresenter(this);
        mMainPrensenter.start();
    }

    @Override
    public void onBackPressed() {
        if (mMainPrensenter.ismEditState()){
            mMainPrensenter.cancelEdit();
            return;
        }
        if (System.currentTimeMillis() - APPCONST.exitTime > APPCONST.exitConfirmTime) {
            TextHelper.showText("再按一次退出");
            APPCONST.exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        MyApplication.checkVersionByServer(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode == RESULT_OK || resultCode == RESULT_CANCELED) && requestCode == APPCONST.APP_INSTALL_CODE) {
            installProcess(appFile, isForceUpdate);//再次执行安装流程，包含权限判等
        }else if (resultCode == RESULT_OK && requestCode == APPCONST.SELECT_FILE_CODE){
            String path;
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                path = uri.getPath();
            }else {
                path = getPath(this, uri);
            }
            mMainPrensenter.addLocalBook(path);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case APPCONST.PERMISSIONS_REQUEST_STORAGE: {
                // 如果取消权限，则返回的值为0
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //跳转到 FileSystemActivity
                    Intent intent = new Intent(this, FileSystemActivity.class);
                    startActivity(intent);

                } else {
                    TextHelper.showText("用户拒绝开启读写权限");
                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        MyApplication.getApplication().shutdownThreadPool();
        super.onDestroy();
    }

    //安装应用的流程
    public void installProcess(File file, boolean isForceUpdate) {
        if (appFile == null || !this.isForceUpdate) {
            appFile = file;
            this.isForceUpdate = isForceUpdate;
        }
        boolean haveInstallPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //先获取是否有安装未知来源应用的权限
            haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {//没有权限
                DialogCreator.createCommonDialog(this, "安装应用",
                        "安装应用需要打开未知来源权限，请去设置中开启权限", true,
                        "确定", (dialog, which) -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startInstallPermissionSettingActivity();
                            }
                        });
                return;
            }
        }
        //有权限，开始安装应用程序
        installApk(file, isForceUpdate);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        Uri packageURI = Uri.parse("package:" + getPackageName());
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, APPCONST.APP_INSTALL_CODE);
    }

    /**
     * 安装应用
     * @param file
     * @param isForceUpdate
     */
    public void installApk(File file, boolean isForceUpdate) {
        String authority = getApplicationContext().getPackageName() + ".fileprovider";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri apkUri = FileProvider.getUriForFile(this, authority, file);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        startActivity(intent);
        if (isForceUpdate){
            finish();
        }
    }

    public CircleImageView getCivAvatar() {
        return civAvatar;
    }

    public TabLayout getTlTabMenu() {
        return tlTabMenu;
    }

    public ImageView getIvSearch() {
        return ivSearch;
    }

    public ViewPager getVpContent() {
        return vpContent;
    }

    public RelativeLayout getRlCommonTitle() {
        return rlCommonTitle;
    }

    public TextView getTvEditFinish() {
        return tvEditFinish;
    }

    public RelativeLayout getRlEditTitle() {
        return rlEditTitle;
    }

    public ImageView getIvMore() {
        return ivMore;
    }
}
