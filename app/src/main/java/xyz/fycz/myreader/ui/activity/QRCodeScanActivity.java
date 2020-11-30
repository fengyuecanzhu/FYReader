package xyz.fycz.myreader.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import butterknife.BindView;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.util.PermissionsChecker;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.RxUtils;

import static xyz.fycz.myreader.util.UriFileUtil.getPath;

/**
 * @author fengyue
 * @date 2020/11/30 8:31
 */

public class QRCodeScanActivity extends BaseActivity implements QRCodeView.Delegate {

    @BindView(R.id.zxingview)
    ZXingView zxingview;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_flashlight)
    LinearLayout llFlashLight;
    @BindView(R.id.fab_flashlight)
    FloatingActionButton fabFlashlight;
    @BindView(R.id.tv_flashlight)
    TextView tvFlashlight;

    private final int REQUEST_QR_IMAGE = 202;
    private static final String CAMERA = "android.permission.CAMERA";
    private static final int PERMISSIONS_REQUEST_CAMERA = 101;
    private boolean flashlightIsOpen;
    private boolean needScale = true;
    private String picPath;

    @Override
    protected int getContentId() {
        return R.layout.activity_qrcode_capture;
    }


    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("扫一扫");
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData(Bundle savedInstanceState) {
        zxingview.setDelegate(this);
        fabFlashlight.setOnClickListener(view -> {
            if (flashlightIsOpen) {
                flashlightIsOpen = false;
                zxingview.closeFlashlight();
                tvFlashlight.setText(getString(R.string.light_contact));
            } else {
                flashlightIsOpen = true;
                zxingview.openFlashlight();
                tvFlashlight.setText(getString(R.string.close_contact));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startCamera();
    }

    private void startCamera() {
        requestPermission();
    }

    @Override
    protected void onStop() {
        zxingview.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        zxingview.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.d("onScanQRCodeSuccess", needScale + "");
        if (result == null) {
            if (!needScale){
                needScale = true;
                if (StringHelper.isEmpty(picPath)) {
                    return;
                }
                scanFromPath(picPath);
            }else {
                ToastUtils.showError("二维码扫描失败");
            }
        }else {
            Intent intent = new Intent();
            Log.d("result", result);
            intent.putExtra("result", result);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        if (isDark){
            llFlashLight.setVisibility(View.VISIBLE);
        }else {
            if (!flashlightIsOpen) {
                llFlashLight.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }

    private void startScan() {
        zxingview.setVisibility(View.VISIBLE);
        zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    private void requestPermission() {
        //获取读取和写入SD卡的权限
        if (new PermissionsChecker(this).lacksPermissions(CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        } else {
            startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // 如果取消权限，则返回的值为0
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请权限成功
                    startScan();
                } else {
                    //申请权限失败
                    finish();
                    ToastUtils.showWarring("请给予相机权限，否则无法进行扫码！");
                }
                return;
            }
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_qr_code_scan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_choose_from_gallery:
                chooseFromGallery();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_QR_IMAGE) {
            picPath = getPath(this, data.getData());
            if (StringHelper.isEmpty(picPath)) {
                return;
            }
            scanFromPath(picPath);
        }
    }

    private void scanFromPath(String path){
        // 本来就用到 QRCodeView 时可直接调 QRCodeView 的方法，走通用的回调
        Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap == null)
                return;
            if (needScale) {
                int size = 360;
                bitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() - size, bitmap.getHeight() - size, size, size);
            }
            emitter.onSuccess(bitmap);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        zxingview.decodeQRCode(bitmap);
                    }
                });
    }

    private void chooseFromGallery() {
        try {
            if (needScale){
                ToastUtils.showInfo("选择图片仅支持扫描书籍分享图片");
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_QR_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showError(Objects.requireNonNull(e.getLocalizedMessage()));
        }
    }

}
