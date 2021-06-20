package xyz.fycz.myreader.util.download;

import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import com.hjq.permissions.OnPermissionCallback;

import java.util.List;

import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.util.utils.StoragePermissionUtils;

import static android.content.Context.DOWNLOAD_SERVICE;

public class DownloadUtil {

    private static final String TAG = DownloadUtil.class.getSimpleName();

    public static void downloadBySystem(Context context, String url, String contentDisposition, String mimeType) {
        StoragePermissionUtils.request(context, (permissions, all) -> {
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
            Log.d(TAG, String.format("fileName:%s", fileName));
            DialogCreator.createCommonDialog(context, "添加下载",
                    "确定要下载文件[" + fileName + "]吗？", true, (dialog, which) -> {
                        startDownload(context, url, contentDisposition, mimeType);
                    }, null);
        });
    }

    private static void startDownload(Context context, String url, String contentDisposition, String mimeType) {
        registerReceiver(context);
        // 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 设置通知栏的标题，如果不设置，默认使用文件名
//        request.setTitle("This is title");
        // 设置通知栏的描述
//        request.setDescription("This is description");
        // 允许在计费流量下下载
        request.setAllowedOverMetered(true);
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许下载的网路类型
        //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // 设置下载文件保存的路径和文件名
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//        另外可选一下方法，自定义下载路径
//        request.setDestinationUri()
//        request.setDestinationInExternalFilesDir()
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        // 添加一个下载任务
        long downloadId = downloadManager.enqueue(request);
        Log.d(TAG, String.format("downloadId:%s", downloadId));
    }

    public static void registerReceiver(Context context) {
        // 使用
        DownloadCompleteReceiver receiver = new DownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(receiver, intentFilter);
    }
}
