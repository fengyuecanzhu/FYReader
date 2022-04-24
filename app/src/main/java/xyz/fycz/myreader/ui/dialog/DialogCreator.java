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

package xyz.fycz.myreader.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.kongzue.dialogx.dialogs.BottomDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.MeUtils;

public class DialogCreator {

    /**
     * 创建一个普通对话框（包含确定、取消按键）
     *
     * @param context
     * @param title
     * @param mesage
     * @param isCancelable     是否允许返回键取消
     * @param positiveListener 确定按键动作
     * @param negativeListener 取消按键动作
     * @return
     */
    public static AlertDialog createCommonDialog(Context context, String title, String mesage, boolean isCancelable,
                                                 DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {

        final AlertDialog.Builder normalDialog = MyAlertDialog.build(context);
//        normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle(title);
        normalDialog.setCancelable(isCancelable);
        normalDialog.setMessage(mesage);
        normalDialog.setPositiveButton("确定", positiveListener);
        normalDialog.setNegativeButton("取消", negativeListener);
        // 显示
        final AlertDialog alertDialog = normalDialog.create();
        App.runOnUiThread(() -> {
            try {
                alertDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return alertDialog;
    }

    /**
     * 创建一个普通对话框（包含key1、key2按键）
     *
     * @param context
     * @param title
     * @param mesage
     * @param key1
     * @param key2
     * @param key1Listener key1按键动作
     * @param key2Listener key2按键动作
     */
    public static void createCommonDialog(Context context, String title, String mesage, boolean isCancelable,
                                          String key1, String key2,
                                          DialogInterface.OnClickListener key1Listener,
                                          DialogInterface.OnClickListener key2Listener) {
        try {

            final AlertDialog.Builder normalDialog = MyAlertDialog.build(context);
//        normalDialog.setIcon(R.drawable.icon_dialog);
            normalDialog.setTitle(title);
            normalDialog.setCancelable(isCancelable);
            if (mesage != null) {
                normalDialog.setMessage(mesage);
            }
            normalDialog.setPositiveButton(key1, key1Listener);
            normalDialog.setNegativeButton(key2, key2Listener);
            // 显示
//        final MyAlertDialog alertDialog = normalDialog.create();
            App.runOnUiThread(() -> {
                try {
//                    final MyAlertDialog alertDialog = normalDialog.create();
                    normalDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return alertDialog;
    }

    /**
     * 单按键对话框
     *
     * @param context
     * @param title
     * @param mesage
     * @param key
     * @param positiveListener
     */
    public static void createCommonDialog(Context context, String title, String mesage, boolean isCancelable,
                                          String key, DialogInterface.OnClickListener positiveListener
    ) {
        try {
            final AlertDialog.Builder normalDialog = MyAlertDialog.build(context);
//        normalDialog.setIcon(R.drawable.icon_dialog);
            normalDialog.setTitle(title);
            normalDialog.setCancelable(isCancelable);
            if (mesage != null) {
                normalDialog.setMessage(mesage);
            }
            normalDialog.setPositiveButton(key, positiveListener);

            // 显示
//        final MyAlertDialog alertDialog = normalDialog.create();
            App.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
//                    final MyAlertDialog alertDialog = normalDialog.create();
                        normalDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return alertDialog;
    }


    /**
     * 三按键对话框
     *
     * @param context
     * @param title
     * @param msg
     * @param btnText1
     * @param btnText2
     * @param btnText3
     * @param positiveListener
     * @param neutralListener
     * @param negativeListener
     * @return
     */
    public static void createThreeButtonDialog(Context context, String title, String msg, boolean isCancelable,
                                               String btnText1, String btnText2, String btnText3,
                                               DialogInterface.OnClickListener neutralListener,
                                               DialogInterface.OnClickListener negativeListener,
                                               DialogInterface.OnClickListener positiveListener) {
        /*  final EditText et = new EditText(context);*/
        try {
            final AlertDialog.Builder dialog = MyAlertDialog.build(context);
            dialog.setTitle(title);
            if (!StringHelper.isEmpty(msg)) {
                dialog.setMessage(msg);
            }
            //  第一个按钮
            dialog.setNeutralButton(btnText1, neutralListener);
            //  中间的按钮
            dialog.setNegativeButton(btnText2, negativeListener);
            //  第三个按钮
            dialog.setPositiveButton(btnText3, positiveListener);

            App.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
//                    final MyAlertDialog alertDialog = normalDialog.create();
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            dialog.setCancelable(isCancelable);

            //  Diglog的显示
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createTipDialog(Context mContext, String message) {
        /*DialogCreator.createCommonDialog(mContext, "提示",
                message, true, "知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });*/
        BottomDialog.show("提示", message).setCancelButton("知道了");
    }

    public static void createTipDialog(Context mContext, String title, String message) {
        /*DialogCreator.createCommonDialog(mContext, title,
                message, true, "知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });*/
        BottomDialog.show(title, message).setCancelButton("知道了");
    }

    /**
     * 从assets文件夹之中读取文件并显示提示框
     *
     * @param mContext
     * @param title
     * @param assetName 需要后缀名
     */
    public static void createAssetTipDialog(Context mContext, String title, String assetName) {
        DialogCreator.createTipDialog(mContext, title, MeUtils.getAssetStr(mContext.getAssets(), assetName));
    }


    public interface OnClickPositiveListener {
        void onClick(Dialog dialog, View view);
    }

    public interface OnClickNegativeListener {
        void onClick(Dialog dialog, View view);
    }

    public interface OnSkipChapterListener {
        void onClick(TextView chapterTitle, TextView chapterUrl, SeekBar sbReadChapterProgress);
    }

    public interface OnMultiDialogListener {
        void onItemClick(DialogInterface dialog, int which, boolean isChecked);

        void onSelectAll(boolean isSelectAll);
    }
}
