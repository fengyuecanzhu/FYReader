package xyz.fycz.myreader.util;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

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
        MyApplication.runOnUiThread(() -> Toasty.custom(MyApplication.getmContext(), msg,
                ContextCompat.getDrawable(MyApplication.getmContext(), R.drawable.ic_smile_face),
                MyApplication.getmContext().getResources().getColor(R.color.toast_default),
                MyApplication.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //红色
    public static void showError(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> Toasty.custom(MyApplication.getmContext(), msg,
                ContextCompat.getDrawable(MyApplication.getmContext(), R.drawable.ic_error),
                MyApplication.getmContext().getResources().getColor(R.color.toast_red),
                MyApplication.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //绿色
    public static void showSuccess(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> Toasty.custom(MyApplication.getmContext(), msg,
                ContextCompat.getDrawable(MyApplication.getmContext(), R.drawable.ic_success),
                MyApplication.getmContext().getResources().getColor(R.color.toast_green),
                MyApplication.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //蓝色
    public static void showInfo(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> Toasty.custom(MyApplication.getmContext(), msg,
                ContextCompat.getDrawable(MyApplication.getmContext(), R.drawable.ic_smile_face),
                MyApplication.getmContext().getResources().getColor(R.color.toast_blue),
                MyApplication.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //黄色
    public static void showWarring(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> Toasty.warning(MyApplication.getmContext(), msg, Toasty.LENGTH_SHORT, true).show());
    }

    public static void showExit(@NonNull String msg) {
        MyApplication.runOnUiThread(() -> Toasty.custom(MyApplication.getmContext(), msg,
                ContextCompat.getDrawable(MyApplication.getmContext(), R.drawable.ic_cry_face),
                MyApplication.getmContext().getResources().getColor(R.color.toast_blue),
                MyApplication.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

}
