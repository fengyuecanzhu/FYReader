package xyz.fycz.myreader.util.notification;

import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.creator.MyAlertDialog;
import xyz.fycz.myreader.greendao.service.BookMarkService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static xyz.fycz.myreader.util.notification.NotificationClickReceiver.CANCEL_ACTION;

/**
 * @author fengyue
 * @date 2020/8/14 22:07
 */
public class NotificationUtil {
    private static volatile NotificationUtil sInstance;
    private NotificationManager notificationManager;

    public static NotificationUtil getInstance() {
        if (sInstance == null){
            synchronized (NotificationUtil.class){
                if (sInstance == null){
                    sInstance = new NotificationUtil();
                }
            }
        }
        return sInstance;
    }

    public NotificationUtil() {
        notificationManager = (NotificationManager) MyApplication.getmContext().getSystemService(Context.NOTIFICATION_SERVICE);

    }

    public NotificationCompat.Builder createBuilder(Context context, String channelId){
        return new NotificationCompat.Builder(context, channelId);
    }

    @TargetApi(26)
    public void createNotificationChannel(String channelId, String channelName) {
        NotificationManager notificationManager = (NotificationManager) MyApplication.getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);//是否在桌面icon右上角展示小红点
        channel.setLightColor(Color.RED);//小红点颜色
        channel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
        notificationManager.createNotificationChannel(channel);
    }

    public NotificationCompat.Builder build(String channelId){
        return new NotificationCompat.Builder(MyApplication.getmContext(), channelId);
    }

    public void sendDownloadNotification(String title, String text, PendingIntent pendingIntent){
        NotificationCompat.Builder builder = build(APPCONST.channelIdDownload)
                .setSmallIcon(R.drawable.ic_download)
                //通知栏大图标
                .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getApplication().getResources(), R.mipmap.ic_launcher))
                .setOngoing(true)
                //点击通知后自动清除
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(text);
        if (pendingIntent == null) {
            pendingIntent = getChancelPendingIntent(NotificationClickReceiver.class);
        }
        builder.addAction(R.drawable.ic_stop_black_24dp, "停止", pendingIntent);
        notificationManager.notify(1000, builder.build());
    }

    public PendingIntent getChancelPendingIntent(Class<?> clz) {
        Intent intent = new Intent(MyApplication.getmContext(), clz);
        intent.setAction(CANCEL_ACTION);
        return PendingIntent.getBroadcast(MyApplication.getmContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void cancel(int id){
        notificationManager.cancel(id);
    }
    public void cancelAll(){
        notificationManager.cancelAll();
    }
    public void notify(int id, Notification notification){
        notificationManager.notify(id, notification);
    }

    /**
     * 跳到通知栏设置界面
     * @param context
     */
    public void requestNotificationPermission(Context context){
        if (!isNotificationEnabled(context)) {
            try {
                // 根据isOpened结果，判断是否需要提醒用户跳转AppInfo页面，去打开App通知权限
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);

                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);

                // 小米6 -MIUI9.6-8.0.0系统，是个特例，通知设置界面只能控制"允许使用通知圆点"——然而这个玩意并没有卵用，我想对雷布斯说：I'm not ok!!!
                if ("MI 6".equals(Build.MODEL)) {
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    intent.setAction("com.android.settings/.SubSettings");
                }
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
                Intent intent = new Intent();
                //下面这种方案是直接跳转到当前应用的设置界面。
                //https://blog.csdn.net/ysy950803/article/details/71910806
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            }
        }

    }

    public void requestNotificationPermissionDialog(Context context){
        if (!isNotificationEnabled(context)) {
            MyAlertDialog.build(context)
                    .setTitle("开启通知")
                    .setMessage("检测到未开启通知权限，无法在通知栏查看缓存进度，是否前往开启？")
                    .setCancelable(true)
                    .setPositiveButton("确定", (dialog, which) -> requestNotificationPermission(context))
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    /**
     * 获取通知权限
     * @param context
     */
    public boolean isNotificationEnabled(Context context) {
        boolean isOpened = false;
        try {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            isOpened = manager.areNotificationsEnabled();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOpened;
    }
}
