package xyz.fycz.dynamic

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook

/**
 * @author fengyue
 * @date 2022/3/29 11:59
 */
class AppLoadImpl : IAppLoader {
    val spuName = "FYReader_plugin"

    override fun onLoad(appParam: AppParam) {
        announce(appParam, "风月读书插件测试",
            "2022-03-23\n当你看到这条消息时，表示插件成功加载", "2022-03-30")
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
                        val spu = context.getSharedPreferences(spuName, Context.MODE_PRIVATE)
                        val hasRead = spu.getBoolean(key, false)
                        if (!hasRead) {
                            AlertDialog.Builder(context)
                                .setTitle(title)
                                .setMessage(msg)
                                .setPositiveButton("我知道了") { _, _ ->
                                    spu.edit().run {
                                        putBoolean(key, true)
                                        apply()
                                    }
                                }.create().show()
                            MapleUtils.callStaticMethod(
                                MapleUtils.findClass(
                                    "xyz.fycz.myreader.util.utils.AdUtils",
                                    appParam.classLoader
                                ),"adRecord",
                                arrayOf(String::class.java, String::class.java),
                                "plugin", "success"
                            )
                        }
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MapleUtils.log(e)
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