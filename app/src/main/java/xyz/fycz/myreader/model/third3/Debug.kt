package xyz.fycz.myreader.model.third3

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author fengyue
 * @date 2022/1/21 10:42
 */
object Debug {
    var callback: Callback? = null
    @SuppressLint("ConstantLocale")
    private val debugTimeFormat = SimpleDateFormat("[hh:mm:ss.SSS]", Locale.getDefault())

    fun log(msg: String) {
        val time = debugTimeFormat.format(System.currentTimeMillis())
        callback?.print("$time $msg")
    }

    fun log(tag: String, msg: String) {
        val time = debugTimeFormat.format(System.currentTimeMillis())
        callback?.print("$time $tag：$msg")
    }

    interface Callback {
        fun print(msg: String)
    }
}