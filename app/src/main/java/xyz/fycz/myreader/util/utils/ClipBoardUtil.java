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