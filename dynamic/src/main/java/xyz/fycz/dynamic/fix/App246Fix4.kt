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

package xyz.fycz.dynamic.fix

import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodReplacement
import xyz.fycz.dynamic.utils.LanZouUtils
import xyz.fycz.myreader.base.observer.MyObserver
import xyz.fycz.myreader.util.utils.RxUtils
import xyz.fycz.myreader.webapi.LanZouApi
import xyz.fycz.myreader.webapi.LanZousApi
import xyz.fycz.myreader.webapi.ResultCallback
import java.lang.Exception

/**
 * @author fengyue
 * @date 2022/6/30 20:40
 */
@AppFix([243, 244, 245, 246], ["修复书源订阅失败的问题", "修复字体下载失败的问题"], "2022-06-30")
class App246Fix4: AppFixHandle {
    override fun onFix(key: String): BooleanArray {
        return handleFix(
            key,
            "lanZouApi" to { fxLanZouApi() },
            "fontLanZouApi" to { fxFontLanZouApi() },
        )
    }

    private fun fxLanZouApi() {
        MapleUtils.findAndHookMethod(
            LanZouApi::class.java,
            "getFileUrl",
            String::class.java,
            String::class.java,
            object : MethodReplacement(){
                override fun replaceHookedMethod(param: MapleBridge.MethodHookParam): Any {
                    return LanZouUtils.getFileUrl(param.args[0] as String, param.args[1] as String)
                }
            }
        )
    }

    private fun fxFontLanZouApi(){
        MapleUtils.findAndHookMethod(
            LanZousApi::class.java,
            "getUrl",
            String::class.java,
            ResultCallback::class.java,
            object : MethodReplacement(){
                override fun replaceHookedMethod(param: MapleBridge.MethodHookParam) {
                    val callback = param.args[1] as ResultCallback
                    LanZouUtils.getFileUrl(param.args[0] as String)
                        .compose { RxUtils.toSimpleSingle(it) }
                        .subscribe(object : MyObserver<String>(){
                            override fun onNext(t: String) {
                                callback.onFinish(t, 1)
                            }

                            override fun onError(e: Throwable) {
                                callback.onError(e as Exception)
                            }
                        })
                }
            }
        )
    }
}