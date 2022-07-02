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

import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import io.reactivex.Observable
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodReplacement
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.application.SysManager
import xyz.fycz.myreader.base.observer.MyObserver
import xyz.fycz.myreader.common.URLCONST
import xyz.fycz.myreader.ui.dialog.UpdateDialog
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.util.help.StringHelper
import xyz.fycz.myreader.util.utils.NetworkUtils
import xyz.fycz.myreader.util.utils.OkHttpUtils
import xyz.fycz.myreader.util.utils.RxUtils
import xyz.fycz.myreader.webapi.LanZouApi.getFileUrl
import xyz.fycz.myreader.widget.BarPercentView
import java.io.IOException

/**
 * @author fengyue
 * @date 2022/7/1 15:45
 */
@AppFix([243, 244, 245, 246], ["修复检查更新失败的问题", "修复获取更新链接失败的问题"], "2022-07-02")
class App246Fix5 : AppFixHandle {
    override fun onFix(key: String): BooleanArray {
        return handleFix(
            key,
            "checkUpdateUrl" to { fxCheckUpdateUrl() },
            "downloadLanzou" to { fxDownloadLanzou() },
        )
    }

    fun fxCheckUpdateUrl() {
        MapleUtils.findAndHookMethod(
            App::class.java,
            "checkVersionByServer",
            AppCompatActivity::class.java,
            Boolean::class.java,
            object : MethodReplacement() {
                override fun replaceHookedMethod(param: MapleBridge.MethodHookParam) {
                    checkUpdate(param.args[0] as AppCompatActivity, param.args[1] as Boolean)
                }
            }
        )
    }

    fun checkUpdate(activity: AppCompatActivity, isManualCheck: Boolean) {
        App.getApplication().newThread {
            try {
                var content = OkHttpUtils.getUpdateInfo()
                if (StringHelper.isEmpty(content)) {
                    content = getBakUpdateInfo()
                    if (StringHelper.isEmpty(content)) {
                        if (isManualCheck || NetworkUtils.isNetWorkAvailable()) {
                            ToastUtils.showError("检查更新失败！")
                        }
                        return@newThread
                    }
                }
                val contents = content.split(";".toRegex())
                val newestVersion = contents[0].substring(contents[0].indexOf(":") + 1).toInt()
                var isForceUpdate = contents[1].substring(contents[1].indexOf(":") + 1).toBoolean()
                val downloadLink =
                    contents[2].substring(contents[2].indexOf(":") + 1).trim { it <= ' ' }
                val updateContent = contents[3].substring(contents[3].indexOf(":") + 1)

                SharedPreUtils.getInstance().putString(
                    App.getmContext().getString(R.string.lanzousKeyStart),
                    contents[4].substring(contents[4].indexOf(":") + 1)
                )
                val newSplashTime = contents[5].substring(contents[5].indexOf(":") + 1)
                val oldSplashTime = SharedPreUtils.getInstance().getString("splashTime")
                SharedPreUtils.getInstance()
                    .putBoolean("needUdSI", oldSplashTime != newSplashTime)
                SharedPreUtils.getInstance().putString(
                    "splashTime",
                    contents[5].substring(contents[5].indexOf(":") + 1)
                )
                SharedPreUtils.getInstance().putString(
                    "splashImageUrl",
                    contents[6].substring(contents[6].indexOf(":") + 1)
                )
                SharedPreUtils.getInstance().putString(
                    "splashImageMD5",
                    contents[7].substring(contents[7].indexOf(":") + 1)
                )
                val forceUpdateVersion = contents[8].substring(contents[8].indexOf(":") + 1).toInt()
                SharedPreUtils.getInstance().putInt("forceUpdateVersion", forceUpdateVersion)
                val domain = contents[9].substring(contents[9].indexOf(":") + 1)
                SharedPreUtils.getInstance().putString("domain", domain)
                val pluginConfigUrl = contents[10].substring(contents[10].indexOf(":") + 1)
                SharedPreUtils.getInstance().putString("pluginConfigUrl", pluginConfigUrl)
                val versionCode = App.getVersionCode()
                isForceUpdate = isForceUpdate && forceUpdateVersion > versionCode
                if (!StringHelper.isEmpty(downloadLink)) {
                    SharedPreUtils.getInstance()
                        .putString(App.getmContext().getString(R.string.downloadLink), downloadLink)
                } else {
                    SharedPreUtils.getInstance().putString(
                        App.getmContext().getString(R.string.downloadLink),
                        URLCONST.APP_DIR_URL
                    )
                }
                val updateContents = updateContent.split("/".toRegex())
                val s = StringBuilder()
                updateContents.forEach {
                    s.append(it)
                    s.append("<br>")
                }
                Log.i("检查更新，最新版本", newestVersion.toString() + "")
                if (newestVersion > versionCode) {
                    val setting = SysManager.getSetting()
                    if (isManualCheck || setting.newestVersionCode < newestVersion || isForceUpdate) {
                        setting.newestVersionCode = newestVersion
                        SysManager.saveSetting(setting)
                        App.getApplication().updateApp2(
                            activity, downloadLink, newestVersion, s.toString(), isForceUpdate
                        )
                    }
                } else if (isManualCheck) {
                    ToastUtils.showSuccess("已经是最新版本！")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("检查更新失败！", "" + e.localizedMessage)
                if (isManualCheck || NetworkUtils.isNetWorkAvailable()) {
                    ToastUtils.showError("检查更新失败！")
                }
            }
        }
    }

    private fun fxDownloadLanzou() {
        MapleUtils.findAndHookMethod(
            UpdateDialog::class.java,
            "downloadWithLanzous",
            String::class.java,
            object : MethodReplacement() {
                override fun replaceHookedMethod(param: MapleBridge.MethodHookParam) {
                    val updateDialog = param.thisObject as UpdateDialog
                    val binding = MapleUtils.getObjectField(updateDialog, "binding")
                            as ViewBinding
                    val tvProgress = MapleUtils.getObjectField(binding, "tvProgress")
                            as TextView
                    val barPercentView = MapleUtils.getObjectField(binding, "barPercentView")
                            as BarPercentView
                    tvProgress.text = "正在获取下载链接..."
                    barPercentView.setPercentage(0F)
                    val apkUrl = param.args[0] as String
                    getFileUrl(apkUrl)
                        .compose { RxUtils.toSimpleSingle(it) }
                        .subscribe(object : MyObserver<String>() {
                            override fun onNext(directUrl: String) {
                                MapleUtils.callMethod(
                                    updateDialog,
                                    "downloadApkNormal",
                                    arrayOf(String::class.java),
                                    directUrl
                                )
                            }

                            override fun onError(e: Throwable) {
                                MapleUtils.callMethod(
                                    updateDialog,
                                    "error"
                                )
                            }
                        })
                }
            }
        )
    }

    @Throws(IOException::class)
    fun getBakUpdateInfo(): String {
        return OkHttpUtils.getHtml(
            "https://fyreader.coding.net/p/img/d/FYReader-Update/git/raw/master/" +
                    (if (App.isDebug()) "debug" else "release") +
                    "/content.txt"
        )
    }
}