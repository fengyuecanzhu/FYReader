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

package xyz.fycz.dynamic.fix

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
@AppFix([243], ["修复软件无法打开的问题(超时时间为5s)", "修复DIY书源重复显示订阅书源的问题"], "2022-03-31")
class App243Fix : AppFixHandle {

    override fun onFix(key: String): BooleanArray {
        var fx1 = false
        var fx2 = false
        try {
            fixGetAllNoLocalSource()
            fx1 = true
            fixResult(key, "getAllNoLocalSource", true)
        } catch (e: Exception) {
            MapleUtils.log(e)
            fixResult(key, "getAllNoLocalSource", false)
        }
        try {
            fixAdTimeout()
            fx2 = true
            fixResult(key, "adTimeout", true)
        } catch (e: Exception) {
            MapleUtils.log(e)
            fixResult(key, "adTimeout", false)
        }
        return booleanArrayOf(fx1, fx2)
    }

    private fun getAllNoLocalSource(): List<BookSource> {
        return DbManager.getDaoSession().bookSourceDao.queryBuilder()
            .where(BookSourceDao.Properties.SourceEName.isNull)
            .where(BookSourceDao.Properties.SourceType.isNotNull)
            .orderAsc(BookSourceDao.Properties.OrderNum)
            .list()
    }


    private fun fixGetAllNoLocalSource() {
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
            if (time > 5) {
                MapleUtils.setIntField(obj, "timeOut", 5)
            }
            val handler = MapleUtils.getObjectField(obj, "handler") as Handler
            val adTimeOutRunnable = MapleUtils.getObjectField(obj, "adTimeOutRunnable") as Runnable
            handler.postDelayed(adTimeOutRunnable, 1000)
        }
    }

    private fun fixAdTimeout() {
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
            object : MethodHook() {
                override fun afterHookedMethod(param: MapleBridge.MethodHookParam) {
                    SharedPreAdUtils.getInstance().putBoolean("adTimeOut", false)
                }
            }
        )
        MapleUtils.findAndHookMethod(
            AdUtils::class.java,
            "backSplashAd",
            object : MethodHook() {
                override fun beforeHookedMethod(param: MapleBridge.MethodHookParam) {
                    if (SharedPreAdUtils.getInstance().getBoolean("adTimeOut")) {
                        param.result = false
                    }
                }
            }
        )
    }
}