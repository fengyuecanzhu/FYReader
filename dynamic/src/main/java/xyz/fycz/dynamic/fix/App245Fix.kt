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
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.model.user.UserService
import xyz.fycz.myreader.util.AppInfoUtils
import xyz.fycz.myreader.util.utils.EncoderUtils

/**
 * @author fengyue
 * @date 2022/5/23 18:20
 */
@AppFix([243, 244, 245], ["修改用户服务验证机制(beta)"], "2022-05-23")
class App245Fix : AppFixHandle {

    override fun onFix(key: String): BooleanArray {
        val result = try {
            fixMakeAuth()
            true
        } catch (e: Exception) {
            MapleUtils.log(e)
            false
        }
        fixResult(key, "makeAuth", result)
        return booleanArrayOf(result)
    }

    private fun fixMakeAuth() {
        MapleUtils.findAndHookMethod(
            UserService::class.java,
            "makeAuth",
            object : MethodReplacement() {
                override fun replaceHookedMethod(param: MapleBridge.MethodHookParam): Any {
                    return makeAuth()
                }
            }
        )
    }

    fun makeAuth(): String {
        var auth = "signal=" + AppInfoUtils.getSingInfo(
            App.getmContext(),
            App.getApplication().packageName,
            AppInfoUtils.SHA1
        ) + "&appVersion=" + App.getVersionCode()
        auth = try {
            EncoderUtils.encryptAES2Base64(
                auth.toByteArray(), DO_FILTER_KEY,
                "AES/ECB/PKCS5Padding"
            )?.let { String(it) }.toString()
        } catch (e: Exception) {
            ""
        }
        return "&auth=$auth" +
                "&deviceId=" + UserService.getUUID() +
                "&isDebug=" + App.isDebug()
    }

    val DO_FILTER_KEY = "79qdunN8534y44T3".toByteArray()

}