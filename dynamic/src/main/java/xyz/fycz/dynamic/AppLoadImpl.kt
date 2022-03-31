package xyz.fycz.dynamic

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook
import xyz.fycz.myreader.application.App
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
            try {
                App243Fix.fixGetAllNoLocalSource()
                fixResult(key, "getAllNoLocalSource", true)
            } catch (e: Exception) {
                MapleUtils.log(e)
                fixResult(key, "getAllNoLocalSource", false)
            }
            try {
                App243Fix.fixAdTimeout()
                fixResult(key, "adTimeout", true)
            } catch (e: Exception) {
                MapleUtils.log(e)
                fixResult(key, "adTimeout", false)
            }
        }
    }

    private fun announce(appParam: AppParam, title: String, msg: String, key: String) {
        try {
            MapleUtils.findAndHookMethod(
                "xyz.fycz.myreader.ui.activity.MainActivity",
                appParam.classLoader,
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
                                .setPositiveButton("我知道了") { _, _ ->

                                }.create().show()
                            spu.edit().run {
                                putBoolean(key, true)
                                apply()
                            }
                            AdUtils.adRecord("plugin", "adSuccess")
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

    /*fun noAd(appParam: AppParam) {
        try {
            MapleUtils.findAndHookMethod(
                "xyz.fycz.myreader.util.utils.AdUtils",
                appParam.classLoader,
                "checkHasAd",
                Boolean::class.java,
                Boolean::class.java,
                object : MethodReplacement() {
                    override fun replaceHookedMethod(param: MapleBridge.MethodHookParam): Any? {
                        val just = MapleUtils.findMethodExact(
                            "io.reactivex.Single",
                            appParam.classLoader,
                            "just",
                            Any::class.java
                        )
                        return just.invoke(null, false)
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MapleUtils.log(e)
        }
    }*/
}