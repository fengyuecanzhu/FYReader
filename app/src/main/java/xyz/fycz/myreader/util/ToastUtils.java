/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

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
