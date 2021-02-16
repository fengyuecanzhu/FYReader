package xyz.fycz.myreader.util;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import es.dmoral.toasty.Toasty;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;

/**
 * Toast工具类：对Toasty的二次封装
 * https://github.com/GrenderG/Toasty
 */
public class ToastUtils {

    static {
        Toasty.Config.getInstance().setTextSize(14).apply();
    }

    public static void show(@NonNull String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getmContext(), msg,
                ContextCompat.getDrawable(App.getmContext(), R.drawable.ic_smile_face),
                App.getmContext().getResources().getColor(R.color.toast_default),
                App.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //红色
    public static void showError(@NonNull String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getmContext(), msg,
                ContextCompat.getDrawable(App.getmContext(), R.drawable.ic_error),
                App.getmContext().getResources().getColor(R.color.toast_red),
                App.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //绿色
    public static void showSuccess(@NonNull String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getmContext(), msg,
                ContextCompat.getDrawable(App.getmContext(), R.drawable.ic_success),
                App.getmContext().getResources().getColor(R.color.toast_green),
                App.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //蓝色
    public static void showInfo(@NonNull String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getmContext(), msg,
                ContextCompat.getDrawable(App.getmContext(), R.drawable.ic_smile_face),
                App.getmContext().getResources().getColor(R.color.toast_blue),
                App.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

    //黄色
    public static void showWarring(@NonNull String msg) {
        App.runOnUiThread(() -> Toasty.warning(App.getmContext(), msg, Toasty.LENGTH_SHORT, true).show());
    }

    public static void showExit(@NonNull String msg) {
        App.runOnUiThread(() -> Toasty.custom(App.getmContext(), msg,
                ContextCompat.getDrawable(App.getmContext(), R.drawable.ic_cry_face),
                App.getmContext().getResources().getColor(R.color.toast_blue),
                App.getmContext().getResources().getColor(R.color.white),
                Toasty.LENGTH_SHORT, true, true).show());
    }

}
