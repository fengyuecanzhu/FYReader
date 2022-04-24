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
        setKeyWord(textView, str, keyWord, Color.RED);
    }

    public static void setKeyWord(TextView textView, String str, String keyWord, int colorId){
        int start = str.indexOf(keyWord);
        if (start == -1){
            textView.setText(str);
        }else {
            SpannableString spannableString = new SpannableString(str);
            spannableString.setSpan(new ForegroundColorSpan(colorId),
                    start, start + keyWord.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textView.setText(spannableString);
        }
    }
}
