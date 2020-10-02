package xyz.fycz.myreader.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.activity.MainActivity;
import xyz.fycz.myreader.ui.fragment.BookcaseFragment;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.webapi.CommonApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author fengyue
 * @date 2020/5/20 20:50
 */

public class APPDownloadTip {

    private String url;
    private BookcaseFragment mBookcaseFragment;
    private MainActivity activity;
    private boolean isForceUpdate;

    public APPDownloadTip(String url, BookcaseFragment mBookcaseFragment, AppCompatActivity activity, boolean isForceUpdate) {
        this.url = url;
        this.mBookcaseFragment = mBookcaseFragment;
        this.activity = (MainActivity) activity;
        this.isForceUpdate = isForceUpdate;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!MyApplication.isDestroy(activity)) {
                switch (msg.what) {
                    case 1:
                        mBookcaseFragment.getTvDownloadTip().setText("获取下载链接失败，请前往浏览器下载！");
                        mBookcaseFragment.getRlDownloadTip().setVisibility(View.GONE);
                        break;
                    case 2:
                        mBookcaseFragment.getTvDownloadTip().setText("连接中...");
                        break;
                    case 3:
                        updateDownloadPro((double) msg.obj);
                        break;
                    case 4:
                        mBookcaseFragment.getRlDownloadTip().setVisibility(View.GONE);
                        break;
                }
            }
        }
    };

    public void downloadApp() {
        mBookcaseFragment.getTvStopDownload().setVisibility(View.GONE);
        mBookcaseFragment.getRlDownloadTip().setVisibility(View.VISIBLE);
        mBookcaseFragment.getPbDownload().setProgress(0);
        mBookcaseFragment.getTvDownloadTip().setText("正在获取下载链接...");
        CommonApi.getUrl(url, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                final String downloadUrl = (String) o;
                if (downloadUrl == null) {
                    error();
                    return;
                }
                MyApplication.getApplication().newThread(() -> {
                    HttpURLConnection con = null;
                    InputStream is = null;
                    FileOutputStream fos = null;
                    File appFile = null;
                    try {
                        URL webUrl = new URL(downloadUrl);
                        mHandler.sendMessage(mHandler.obtainMessage(2));
                        con = (HttpURLConnection) webUrl.openConnection();
                        is = con.getInputStream();
                        String filePath = APPCONST.UPDATE_APK_FILE_DIR + "FYReader.apk.temp";
                        appFile = FileUtils.getFile(filePath);
                        fos = new FileOutputStream(appFile);
                        byte[] tem = new byte[1024];
                        long alreadyLen = 0;
                        long fileLength = con.getContentLength();
                        int len;
                        double progress;
                        while ((len = is.read(tem)) != -1) {
                            fos.write(tem, 0, len);
                            alreadyLen += len;
                            progress = alreadyLen * 1.0f * 100f / fileLength;
                            mHandler.sendMessage(mHandler.obtainMessage(3, progress));
                        }
                        fos.flush();
                        if (fileLength == appFile.length()) {
                            String newPath = filePath.replace(".temp", "");
                            final File newFile = new File(newPath);
                            if (appFile.renameTo(newFile)) {
                                mHandler.sendMessage(mHandler.obtainMessage(4));
                                DialogCreator.createCommonDialog(activity, "提示", "风月读书下载完成，安装包路径：" + newPath,
                                        !isForceUpdate, "取消", "立即安装", (dialog, which) -> {
                                            if (isForceUpdate) {
                                                activity.finish();
                                            }
                                        }, (dialog, which) -> activity.installProcess(newFile, isForceUpdate));
                                activity.installProcess(newFile, isForceUpdate);
                            } else {
                                appFile.delete();
                                error();
                            }
                        } else {
                            appFile.delete();
                            error();
                        }
                    } catch (IOException e) {
                        if (appFile != null) {
                            appFile.delete();
                        }
                        error();
                        e.printStackTrace();
                    } finally {
                        if (con != null) {
                            con.disconnect();
                        }
                        IOUtils.close(is, fos);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                error();
            }
        });
    }


    private void error() {
        mHandler.sendMessage(mHandler.obtainMessage(1));
        ToastUtils.showError("获取下载链接失败，请前往浏览器下载！");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        activity.startActivity(intent);
        if (isForceUpdate) {
            activity.finish();
        }
    }

    @SuppressLint({"SetTextI18n"})
    private void updateDownloadPro(double progress) {
        mBookcaseFragment.getPbDownload().setProgress((int) progress);
        //保留两位小数
        //mBookcaseFragment.getTvDownloadTip().setText("正在下载风月读书最新版本...[" + String.format("%.2f", progress) + "%]");
        mBookcaseFragment.getTvDownloadTip().setText("正在下载风月读书最新版本...[" + (int) progress + "%]");
    }

}
