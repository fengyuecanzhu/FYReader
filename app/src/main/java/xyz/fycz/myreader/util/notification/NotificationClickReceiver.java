package xyz.fycz.myreader.util.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import xyz.fycz.myreader.application.MyApplication;

/**
 * @author fengyue
 * @date 2020/8/14 22:04
 */
public class NotificationClickReceiver extends BroadcastReceiver {

    public static final String CANCEL_ACTION = "cancelAction";

    @Override
    public void onReceive(Context context, Intent intent) {
        //todo 跳转之前要处理的逻辑
        if (CANCEL_ACTION.equals(intent.getAction())){
            MyApplication.getApplication().shutdownThreadPool();
        }
    }
}
