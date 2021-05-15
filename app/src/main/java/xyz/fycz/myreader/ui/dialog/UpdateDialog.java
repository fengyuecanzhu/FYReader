package xyz.fycz.myreader.ui.dialog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.databinding.FragmentUpdateBinding;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.webapi.LanZousApi;
import xyz.fycz.myreader.webapi.ResultCallback;


public class UpdateDialog extends Fragment {

    private String version, content, appUrl, path;
    private boolean isContentHtml = false;
    private boolean cancelable;
    //    private OnConfirmListener listener;
    private FragmentActivity mActivity;
    private final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0x100;
    private BaseDownloadTask baseDownloadTask;
    private boolean debug = false;
    private FragmentUpdateBinding binding;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (FragmentActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUpdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FileDownloader.setup(mActivity);
        initViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (baseDownloadTask != null && (!baseDownloadTask.isRunning() || baseDownloadTask.pause()))
            baseDownloadTask = null;
    }

    private void initViews() {
        binding.tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.ivClose.setVisibility(cancelable ? View.VISIBLE : View.GONE);
        binding.btUpdate.setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onConfirm(v);
//            } else {
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
            } else {
                downloadApk(appUrl);
            }
//            }
        });
        binding.btBrowser.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(appUrl));
            mActivity.startActivity(intent);
        });
        binding.rlProgress.setOnClickListener(v -> {
            if (binding.barPercentView.getProgress() == 1) {
                install(new File(path), mActivity);
            }
        });
        binding.ivClose.setOnClickListener(v -> {
            if (baseDownloadTask == null) {
                dismissUpdateDialog();
                return;
            }
            if (!baseDownloadTask.isRunning() || baseDownloadTask.pause()) {
                dismissUpdateDialog();
            }
        });
        binding.tvVersion.setText(TextUtils.isEmpty(version) ? "" : version);
        if (isContentHtml) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvContent.setText(Html.fromHtml(TextUtils.isEmpty(content) ? "" : content, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvContent.setText(Html.fromHtml(TextUtils.isEmpty(content) ? "" : content));
            }
        } else {
            binding.tvContent.setText(content);
        }
    }

    public static class Builder {

        private UpdateDialog updateDialog;

        public Builder() {
            this.updateDialog = new UpdateDialog();
        }

        public Builder setVersion(String version) {
            updateDialog.version = version;
            return this;
        }

        public Builder setContent(String content) {
            updateDialog.content = content;
            return this;
        }

        public Builder setContentHtml(boolean isContentHtml) {
            updateDialog.isContentHtml = isContentHtml;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            updateDialog.cancelable = cancelable;
            return this;
        }

        public Builder setDebug(boolean debug) {
            updateDialog.debug = debug;
            return this;
        }

        public Builder setDownloadUrl(String url) {
            updateDialog.appUrl = url;
            return this;
        }

//        public Builder setOnConfirmListener(OnConfirmListener listener) {
//            updateDialog.listener = listener;
//            return this;
//        }

        public UpdateDialog build() {
            return updateDialog;
        }

    }

//    public void setPercent(int progressPercent) {
//        barPercentView.setPercentage(progressPercent);
//        progressTv.setText(progressPercent + "%");
//    }

//    public interface OnConfirmListener {
//        void onConfirm(View view);
//    }

    public void showUpdateDialog(FragmentActivity activity) {
        if (this.isAdded() && this.isVisible()) return;
        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(android.R.id.content, this);
        fragmentTransaction.setCustomAnimations(R.anim.push_bottom_in, R.anim.push_bottom_out);
        fragmentTransaction.show(this);
        if (activity.isFinishing() || activity.isDestroyed()) {
            fragmentTransaction.commitAllowingStateLoss();
        } else {
            fragmentTransaction.commit();
        }
    }

    public void dismissUpdateDialog() {
        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
        if (this.isAdded() && this.isVisible()) {
            fragmentTransaction.setCustomAnimations(R.anim.push_bottom_in, R.anim.push_bottom_out);
            fragmentTransaction.hide(this);
            fragmentTransaction.commit();
            new Handler().postDelayed(this::destroyUpdateDialog, 500);
        }
    }

    private void destroyUpdateDialog() {
        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(this);
        fragmentTransaction.remove(this);
        fragmentTransaction.commit();
    }

    private void downloadApk(String apkUrl) {
        if (TextUtils.isEmpty(apkUrl)) throw new NullPointerException("url can not be empty");
        binding.rlProgress.setVisibility(View.VISIBLE);
        binding.btUpdate.setVisibility(View.GONE);
        binding.btBrowser.setVisibility(View.GONE);
        if (apkUrl.endsWith(".apk")) {
            downloadApkNormal(apkUrl);
        } else if (apkUrl.contains("fycz.lanzou")) {
            downloadWithLanzous(apkUrl);
        } else {
            downloadApkWithNoFileName(apkUrl);
        }
    }

    private void downloadApkNormal(String apkUrl) {
        String decodeUrl;
        try {
            decodeUrl = URLDecoder.decode(apkUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            decodeUrl = apkUrl;
        }
        if (debug) Log.e("downloadApk", "originUrl------->" + apkUrl);
        if (debug) Log.e("downloadApk", "decodeUrl------->" + decodeUrl);
//        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + decodeUrl.substring(decodeUrl.lastIndexOf("/") + 1);
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/风月读书" + version + ".apk";
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists() && dir.mkdir()) {
            startDownloadingApk(decodeUrl);
        } else {
            startDownloadingApk(decodeUrl);
        }
    }

    private void startDownloadingApk(String decodeUrl) {
        baseDownloadTask = FileDownloader.getImpl()
                .create(decodeUrl)
                .setPath(path, new File(path).isDirectory())
                .setCallbackProgressMinInterval(100)
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        if (debug) Log.e("downloadApk", "pending-------");
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        float percent = 1f * soFarBytes / totalBytes * 100;
                        if (debug) Log.e("downloadApk", "progress-------" + percent);
                        if (percent >= 3) {
                            binding.barPercentView.setPercentage(percent);
                            binding.tvProgress.setText((int) percent + "%");
                        }
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        if (debug) Log.e("downloadApk", "paused-------");
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        if (debug) Log.e("downloadApk", "completed-------");
                        binding.barPercentView.setPercentage(100);
                        binding.tvProgress.setText("100%");
                        install(new File(path), mActivity);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        if (debug) Log.e("downloadApk", "error-------" + e.toString());
                        UpdateDialog.this.error();
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        if (debug) Log.e("downloadApk", "warn-------");
                    }
                });
        baseDownloadTask.setAutoRetryTimes(3);
        baseDownloadTask.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被用户同意，可以去放肆了。
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                } else {
                    downloadApk(appUrl);
                }
            } else {
                // 权限被用户拒绝了，洗洗睡吧。
            }
        }
    }

    /**
     * 安装
     */
    private void install(File filePath, Context context) {
        if (filePath.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //判断是否是Android N 以及更高的版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.d("UpdateDialog", "install: " + filePath);
                Uri contentUri = FileProvider.getUriForFile(context, mActivity.getPackageName() + ".fileprovider", filePath);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(filePath), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } else {
            downloadApk(appUrl);
        }
    }

    private void downloadApkWithNoFileName(String appUrl) {
        App.getApplication().newThread(() -> {
            try {
                URL url = new URL(appUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    URL url1 = urlConnection.getURL();
                    downloadApkNormal(url1.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void downloadWithLanzous(String apkUrl) {
        binding.tvProgress.setText("正在获取下载链接...");
        binding.barPercentView.setPercentage(0);
        LanZousApi.getUrl(apkUrl, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                String downloadUrl = (String) o;
                if (downloadUrl == null) {
                    App.runOnUiThread(() -> error());
                    return;
                }
                App.runOnUiThread(() -> downloadApkNormal(downloadUrl));
            }

            @Override
            public void onError(Exception e) {
                App.runOnUiThread(() -> error());
            }
        });
    }

    private void error() {
        ToastUtils.showError("下载链接获取失败，请前往浏览器下载！");
        binding.btBrowser.performClick();
        binding.rlProgress.setVisibility(View.GONE);
        binding.btUpdate.setVisibility(View.VISIBLE);
        binding.btBrowser.setVisibility(View.VISIBLE);
    }
}
