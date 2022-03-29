package xyz.fycz.dynamic

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook
import me.fycz.maple.MethodReplacement

/**
 * @author fengyue
 * @date 2022/3/29 11:59
 */
class AppLoadImpl : IAppLoader {
    override fun onLoad(appParam: AppParam) {
        /*try {
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

        try {
            MapleUtils.findAndHookMethod(
                "xyz.fycz.myreader.ui.activity.MainActivity",
                appParam.classLoader,
                "onCreate",
                Bundle::class.java,
                object : MethodHook() {
                    override fun afterHookedMethod(param: MapleBridge.MethodHookParam) {
                        AlertDialog.Builder(param.thisObject as Context)
                            .setTitle("风月读书插件")
                            .setMessage("此消息由风月读书插件提供")
                            .create().show()
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MapleUtils.log(e)
        }*/
    }
}