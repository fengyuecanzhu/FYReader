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

package xyz.fycz.dynamic

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook
import xyz.fycz.dynamic.fix.App243Fix
import xyz.fycz.dynamic.fix.App244Fix
import xyz.fycz.dynamic.fix.AppFix
import xyz.fycz.dynamic.fix.AppFixHandle
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.ui.activity.MainActivity
import xyz.fycz.myreader.util.utils.AdUtils

/**
 * @author fengyue
 * @date 2022/3/29 11:59
 */
class AppLoadImpl : IAppLoader {

    companion object {
        private const val spuName = "FYReader_plugin"
        val spu: SharedPreferences =
            App.getmContext().getSharedPreferences(spuName, Context.MODE_PRIVATE)
    }

    private val fixList = listOf(
        App243Fix::class.java,
        App244Fix::class.java
    )

    override fun onLoad(appParam: AppParam) {
        val sb = StringBuilder()
        var index = 1
        fixList.forEach {
            val annotation = it.getAnnotation(AppFix::class.java)!!
            annotation.version.forEach { version ->
                if (App.getVersionCode() == version) {
                    val fix = it.newInstance()
                    val fixResult = fix.onFix(annotation.date)
                    if (!spu.getBoolean(annotation.date, false)) {
                        fixResult.forEachIndexed { i, b ->
                            sb.append("${index++}、${annotation.fixLog[i]}：${if (b) "成功" else "失败"}\n")
                        }
                        spu.edit().run {
                            putBoolean(annotation.date, true)
                            apply()
                        }
                    }
                }
            }
        }
        if (sb.lastIndexOf("\n") > 0) sb.substring(0, sb.length - 1)
        announce("插件更新", "2022-04-25更新内容：\n$sb", "fix244")
    }

    private fun announce(title: String, msg: String, key: String) {
        try {
            MapleUtils.findAndHookMethod(
                MainActivity::class.java,
                "onCreate",
                Bundle::class.java,
                object : MethodHook() {
                    override fun afterHookedMethod(param: MapleBridge.MethodHookParam) {
                        val context = param.thisObject as Context
                        val hasRead = spu.getBoolean(key, false)
                        if (!hasRead) {
                            AlertDialog.Builder(context)
                                .setTitle(title)
                                .setMessage(msg)
                                .setPositiveButton("我知道了", null)
                                .create().show()
                            spu.edit().run {
                                putBoolean(key, true)
                                apply()
                            }
                            AdUtils.adRecord("plugin", "fxRecord")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MapleUtils.log(e)
        }
    }
}