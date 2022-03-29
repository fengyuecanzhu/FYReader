package xyz.fycz.dynamic

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
    }
}