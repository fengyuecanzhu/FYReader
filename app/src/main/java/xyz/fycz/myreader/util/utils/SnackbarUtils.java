package xyz.fycz.myreader.util.utils;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;


/**
 * Created by zhouas666 on 2017/12/28.
 * Snackbar工具类
 */
public class SnackbarUtils {


    public static void show(@NonNull View view, @NonNull String msg) {
        show(view, msg, true, null, null);
    }

    /**
     * 展示snackBar
     *
     * @param view                view
     * @param msg                 消息
     * @param isDismiss           是否自动消失
     * @param action              事件名
     * @param iSnackBarClickEvent 事件处理接口
     */
    public static void show(@NonNull View view, @NonNull String msg, boolean isDismiss, String action, final ISnackBarClickEvent iSnackBarClickEvent) {
        //snackBar默认显示时间为LENGTH_LONG
        int duringTime = Snackbar.LENGTH_LONG;
        if (!isDismiss) {
            duringTime = Snackbar.LENGTH_INDEFINITE;
        }
        Snackbar snackbar;
        snackbar = Snackbar.make(view, msg, duringTime);
        if (action != null)
            snackbar.setAction(action, view1 -> {
                //以接口方式发送出去，便于使用者处理自己的业务逻辑
                iSnackBarClickEvent.clickEvent();
            });
        //设置snackBar和titleBar颜色一致
        snackbar.getView().setBackgroundColor(App.getmContext().getResources().getColor(R.color.textPrimary));
        //设置action文字的颜色
        snackbar.setActionTextColor(App.getmContext().getResources().getColor(R.color.md_white_1000));
        //设置snackBar图标 这里是获取到snackBar的textView 然后给textView增加左边图标的方式来实现的
        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        /*Drawable drawable = getResources().getDrawable(R.mipmap.ic_notification);//图片自己选择
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(drawable, null, null, null);*/
        //增加文字和图标的距离
        textView.setCompoundDrawablePadding(20);
        //展示snackBar
        snackbar.show();
    }

    /**
     * snackBar的action事件
     */
    public interface ISnackBarClickEvent {
        void clickEvent();
    }

}
