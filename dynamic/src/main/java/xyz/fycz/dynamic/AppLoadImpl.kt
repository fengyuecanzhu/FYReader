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

package xyz.fycz.dynamic

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.kongzue.dialogx.dialogs.BottomDialog
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook
import xyz.fycz.dynamic.fix.*
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.ui.activity.MainActivity

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
        App244Fix::class.java,
        App244Fix2::class.java,
        App245Fix::class.java,
    )

    override fun onLoad(appParam: AppParam) {
        val sb = StringBuilder()
        fixList.forEach {
            val annotation = it.getAnnotation(AppFix::class.java)!!
            for (version in annotation.versions) {
                if (App.getVersionCode() == version) {
                    val fix = it.newInstance()
                    val fixResult = fix.onFix(annotation.date)
                    if (!spu.getBoolean(annotation.date, false)) {
                        if (sb.isNotEmpty()) sb.append("\n")
                        sb.append("${annotation.date}\n")
                        fixResult.forEachIndexed { i, b ->
                            sb.append("${i + 1}、${annotation.fixLog[i]}：${if (b) "成功" else "失败"}\n")
                        }
                        spu.edit().run {
                            putBoolean(annotation.date, true)
                            apply()
                        }
                    }
                    break
                }
            }
        }
        if (sb.isNotEmpty()) {
            if (sb.endsWith("\n")) sb.substring(0, sb.length - 1)
            val key = "fix244-2"
            val hasRead = spu.getBoolean(key, false)
            if (!hasRead) {
                announce("插件更新", "更新内容：\n$sb")
                spu.edit().run {
                    putBoolean(key, true)
                    apply()
                }
            }
        }
    }

    private fun announce(title: String, msg: String) {
        try {
            MapleUtils.findAndHookMethod(
                MainActivity::class.java,
                "onCreate",
                Bundle::class.java,
                object : MethodHook() {
                    override fun afterHookedMethod(param: MapleBridge.MethodHookParam) {
                        App.getHandler().postDelayed({
                            BottomDialog.show(title, msg).cancelButton = "知道了"
                        }, 1000)
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MapleUtils.log(e)
        }
    }
}