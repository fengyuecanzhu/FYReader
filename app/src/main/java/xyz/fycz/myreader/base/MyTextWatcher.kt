package xyz.fycz.myreader.base

import android.text.TextWatcher

/**
 * @author fengyue
 * @date 2021/12/9 12:32
 */
abstract class MyTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}