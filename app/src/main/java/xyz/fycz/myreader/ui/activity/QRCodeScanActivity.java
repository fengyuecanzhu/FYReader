package xyz.fycz.myreader.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.databinding.ActivityQrcodeCaptureBinding;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.RxUtils;

import static xyz.fycz.myreader.util.UriFileUtil.getPath;

/**
 * @author fengyue
 * @date 2020/11/30 8:31
 */

public class QRCodeScanActivity extends BaseActivity implements QRCodeView.Delegate {


    private ActivityQrcodeCaptureBinding binding;

    private final int REQUEST_QR_IMAGE = 202;
    private boolean flashlightIsOpen;
    private boolean needScale = true;
    private String picPath;

    @Override
    protected void bindView() {
        binding = ActivityQrcodeCaptureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        binding.zxingview.setDelegate(this);
        binding.fabFlashlight.setOnClickListener(view -> {
            if (flashlightIsOpen) {
                flashlightIsOpen = false;
                binding.zxingview.closeFlashlight();
                binding.tvFlashlight.setText(getString(R.string.light_contact));
            } else {
                flashlightIsOpen = true;
                binding.zxingview.openFlashlight();
                binding.tvFlashlight.setText(getString(R.string.close_contact));
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
        binding.zxingview.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        binding.zxingview.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.d("onScanQRCodeSuccess", needScale + "");
        if (result == null) {
            if (!needScale) {
                needScale = true;
                if (StringHelper.isEmpty(picPath)) {
                    return;
                }
                scanFromPath(picPath);
            } else {
                ToastUtils.showError("二维码扫描失败");
            }
        } else {
            Intent intent = new Intent();
            Log.d("result", result);
            intent.putExtra("result", result);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        if (isDark) {
            binding.llFlashlight.setVisibility(View.VISIBLE);
        } else {
            if (!flashlightIsOpen) {
                binding.llFlashlight.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }

    private void startScan() {
        binding.zxingview.setVisibility(View.VISIBLE);
        binding.zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    private void requestPermission() {
        //获取相机权限
        XXPermissions.with(this)
                .permission(Permission.CAMERA)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        //申请权限成功
                        startScan();
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        //申请权限失败
                        finish();
                        ToastUtils.showWarring("请给予相机权限，否则无法进行扫码！");
                    }
                });
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

        binding.zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_QR_IMAGE) {
            picPath = getPath(this, data.getData());
            if (StringHelper.isEmpty(picPath)) {
                return;
            }
            scanFromPath(picPath);
        }
    }

    private void scanFromPath(String path) {
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
                        binding.zxingview.decodeQRCode(bitmap);
                    }
                });
    }

    private void chooseFromGallery() {
        try {
            if (needScale) {
                ToastUtils.showInfo("选择图片仅支持扫描书籍分享图片");
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_QR_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showError(e.getLocalizedMessage() + "");
        }
    }

}
