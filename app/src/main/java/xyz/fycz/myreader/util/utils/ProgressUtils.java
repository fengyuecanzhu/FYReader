package xyz.fycz.myreader.util.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ProgressUtils {

    private static ProgressDialog dialog = null;

    public static void show(Context context) {
        show(context, null);
    }

    public static void show(Context context, String msg) {
        dialog = new ProgressDialog(context);
        dialog.setMessage(msg == null ? "正在加载" : msg);
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void show(Context context, String msg, String btnName, DialogInterface.OnClickListener listener) {
        dialog = new ProgressDialog(context);
        dialog.setMessage(msg == null ? "正在加载" : msg);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, btnName, listener);
        dialog.show();
    }

    public static void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static boolean isShowing() {
        return dialog.isShowing();
    }
}
