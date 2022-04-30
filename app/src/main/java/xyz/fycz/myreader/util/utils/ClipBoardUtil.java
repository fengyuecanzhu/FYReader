/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.util.utils;

/**
 * @author fengyue
 * @date 2021/1/19 14:30
 */

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import xyz.fycz.myreader.util.ToastUtils;

/**
 * 剪切板读写工具
 */
public class ClipBoardUtil {
    /**
     * 获取剪切板内容
     *
     * @return
     */
    public static String paste(Context context) {
        try {
            ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (manager != null) {
                if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
                    CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
                    String addedTextString = String.valueOf(addedText);
                    if (!TextUtils.isEmpty(addedTextString)) {
                        return addedTextString;
                    }
                }
            }
        } catch (Exception e) {
            ToastUtils.showError("" + e.getLocalizedMessage());
        }
        return "";
    }

    /**
     * 写入剪切板
     * @param text
     * @return
     */
    public static boolean write(Context context, String text){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
            return true;
        }
        return false;
    }

    /**
     * 清空剪切板
     */
    public static void clear(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setPrimaryClip(ClipData.newPlainText("", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}