package xyz.fycz.myreader.util;

import android.os.Build;
import android.widget.Toast;
import androidx.annotation.NonNull;
import es.dmoral.toasty.Toasty;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;

/**
 * Toast工具类：对Toasty的二次封装
 * https://github.com/GrenderG/Toasty
 */
public class ToastUtils {

    static {
        Toasty.Config.getInstance().setTextSize(14).apply();
    }

    public static void show(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> {
            if (showOld(msg)) return;
            Toasty.custom(MyApplication.getmContext(), msg,
                    MyApplication.getmContext().getDrawable(R.drawable.ic_smile_face),
                    MyApplication.getmContext().getColor(R.color.toast_default),
                    MyApplication.getmContext().getColor(R.color.white),
                    Toasty.LENGTH_SHORT, true, true).show();
        });
    }

    //红色
    public static void showError(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> {
            if (showOld(msg)) return;
            Toasty.custom(MyApplication.getmContext(), msg,
                    MyApplication.getmContext().getDrawable(R.drawable.ic_error),
                    MyApplication.getmContext().getColor(R.color.toast_red),
                    MyApplication.getmContext().getColor(R.color.white),
                    Toasty.LENGTH_SHORT, true, true).show();
        });
    }

    //绿色
    public static void showSuccess(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> {
            if (showOld(msg)) return;
            Toasty.custom(MyApplication.getmContext(), msg,
                    MyApplication.getmContext().getDrawable(R.drawable.ic_success),
                    MyApplication.getmContext().getColor(R.color.toast_green),
                    MyApplication.getmContext().getColor(R.color.white),
                    Toasty.LENGTH_SHORT, true, true).show();
        });
    }

    //蓝色
    public static void showInfo(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> {
            if (showOld(msg)) return;
            Toasty.custom(MyApplication.getmContext(), msg,
                    MyApplication.getmContext().getDrawable(R.drawable.ic_smile_face),
                    MyApplication.getmContext().getColor(R.color.toast_blue),
                    MyApplication.getmContext().getColor(R.color.white),
                    Toasty.LENGTH_SHORT, true, true).show();
        });
    }

    //黄色
    public static void showWarring(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> {
            if (showOld(msg)) return;
            Toasty.warning(MyApplication.getmContext(), msg, Toasty.LENGTH_SHORT, true).show();
        });
    }

    public static void showExit(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> {
            if (showOld(msg)) return;
            Toasty.custom(MyApplication.getmContext(), msg,
                    MyApplication.getmContext().getDrawable(R.drawable.ic_cry_face),
                    MyApplication.getmContext().getColor(R.color.toast_blue),
                    MyApplication.getmContext().getColor(R.color.white),
                    Toasty.LENGTH_SHORT, true, true).show();
        });
    }

    private static boolean showOld(@NonNull String msg){
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Toast.makeText(MyApplication.getmContext(), msg, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}
