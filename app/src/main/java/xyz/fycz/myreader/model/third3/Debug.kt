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