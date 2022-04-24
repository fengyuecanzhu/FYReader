package xyz.fycz.dynamic

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.ui.activity.MainActivity
import xyz.fycz.myreader.util.utils.AdUtils

/**
 * @author fengyue
 * @date 2022/3/29 11:59
 */
class AppLoadImpl : IAppLoader {
    private val spuName = "FYReader_plugin"
    private val spu = App.getmContext().getSharedPreferences(spuName, Context.MODE_PRIVATE)

    override fun onLoad(appParam: AppParam) {
        if (App.getVersionCode() == 243) {
            val key = "2022-03-31"
            var fx1 = false
            var fx2 = false
            try {
                App243Fix.fixGetAllNoLocalSource()
                fx1 = true
                fixResult(key, "getAllNoLocalSource", true)
            } catch (e: Exception) {
                MapleUtils.log(e)
                fixResult(key, "getAllNoLocalSource", false)
            }
            try {
                App243Fix.fixAdTimeout()
                fx2 = true
                fixResult(key, "adTimeout", true)
            } catch (e: Exception) {
                MapleUtils.log(e)
                fixResult(key, "adTimeout", false)
            }
            val msg = "$key\n更新内容:\n1、修复软件无法打开的问题(超时时间为5s)：$fx1\n" +
                    "2、修复DIY书源重复显示订阅书源的问题：$fx2"
            announce("插件更新", msg, key)
        }
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

    private fun fixResult(key: String, name: String, success: Boolean) {
        val res = if (success) "Success" else "Failed"
        if (!spu.getBoolean("$key-$name-$res", false)) {
            AdUtils.adRecord(name, "fx$res")
            spu.edit().run {
                putBoolean("$key-$name-$res", true)
                apply()
            }
        }
    }
}