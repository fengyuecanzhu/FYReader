package xyz.fycz.dynamic

import android.os.Handler
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook
import me.fycz.maple.MethodReplacement
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.greendao.gen.BookSourceDao
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager
import xyz.fycz.myreader.ui.activity.SplashActivity
import xyz.fycz.myreader.util.SharedPreAdUtils
import xyz.fycz.myreader.util.utils.AdUtils

/**
 * @author fengyue
 * @date 2022/3/31 9:21
 */
object App243Fix {
    private fun getAllNoLocalSource(): List<BookSource> {
        return DbManager.getDaoSession().bookSourceDao.queryBuilder()
            .where(BookSourceDao.Properties.SourceEName.isNull)
            .where(BookSourceDao.Properties.SourceType.isNotNull)
            .orderAsc(BookSourceDao.Properties.OrderNum)
            .list()
    }


    fun fixGetAllNoLocalSource() {
        MapleUtils.findAndHookMethod(
            BookSourceManager::class.java,
            "getAllNoLocalSource",
            object : MethodReplacement() {
                override fun replaceHookedMethod(param: MapleBridge.MethodHookParam): Any {
                    return getAllNoLocalSource()
                }
            }
        )
    }

    private fun adTimeOut(param: MapleBridge.MethodHookParam) {
        val time = param.args[0] as Int
        val obj = param.thisObject
        if (time == 0) {
            MapleUtils.setStaticIntField(SplashActivity::class.java, "WAIT_INTERVAL", 0)
            SharedPreAdUtils.getInstance()
                .putLong("splashAdTime", System.currentTimeMillis())
            MapleUtils.callMethod(obj, "startNormal")
            SharedPreAdUtils.getInstance().putBoolean("adTimeOut", true)
        } else {
            if (time > 5){
                MapleUtils.setIntField(obj, "timeOut", 5)
            }
            val handler = MapleUtils.getObjectField(obj, "handler") as Handler
            val adTimeOutRunnable = MapleUtils.getObjectField(obj, "adTimeOutRunnable") as Runnable
            handler.postDelayed(adTimeOutRunnable, 1000)
        }
    }

    fun fixAdTimeout() {
        MapleUtils.findAndHookMethod(
            SplashActivity::class.java,
            "adTimeout",
            Int::class.javaPrimitiveType,
            object : MethodReplacement() {
                override fun replaceHookedMethod(param: MapleBridge.MethodHookParam) {
                    adTimeOut(param)
                }
            }
        )
        MapleUtils.findAndHookMethod(
            SplashActivity::class.java,
            "countTodayAd",
            object : MethodHook(){
                override fun afterHookedMethod(param: MapleBridge.MethodHookParam) {
                    SharedPreAdUtils.getInstance().putBoolean("adTimeOut", false)
                }
            }
        )
        MapleUtils.findAndHookMethod(
            AdUtils::class.java,
            "backSplashAd",
            object : MethodHook(){
                override fun beforeHookedMethod(param: MapleBridge.MethodHookParam) {
                    if (SharedPreAdUtils.getInstance().getBoolean("adTimeOut")){
                        param.result = false
                    }
                }
            }
        )
    }
}