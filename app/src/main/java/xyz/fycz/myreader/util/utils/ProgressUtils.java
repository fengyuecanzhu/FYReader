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
