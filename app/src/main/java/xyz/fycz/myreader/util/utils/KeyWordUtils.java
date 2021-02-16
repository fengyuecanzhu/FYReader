package xyz.fycz.myreader.util.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

/**
 * @author fengyue
 * @date 2021/2/16 10:46
 */
public class KeyWordUtils {

    public static void setKeyWord(TextView textView, String str, String keyWord){
        int start = str.indexOf(keyWord);
        if (start == -1){
            textView.setText(str);
        }else {
            SpannableString spannableString = new SpannableString(str);
            spannableString.setSpan(new ForegroundColorSpan(Color.RED),
                    start, start + keyWord.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textView.setText(spannableString);
        }
    }

}
