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

package xyz.fycz.myreader.webapi

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.JsonParser
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import xyz.fycz.myreader.common.URLCONST
import xyz.fycz.myreader.entity.lanzou.LanZouFile
import xyz.fycz.myreader.entity.lanzou.LanZouParseBean
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.model.third3.Coroutine
import xyz.fycz.myreader.model.third3.http.getProxyClient
import xyz.fycz.myreader.model.third3.http.newCallResponseBody
import xyz.fycz.myreader.model.third3.http.postForm
import xyz.fycz.myreader.model.third3.http.text
import xyz.fycz.myreader.ui.activity.SourceSubscribeActivity
import xyz.fycz.myreader.ui.dialog.DialogCreator
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.util.help.StringHelper
import xyz.fycz.myreader.util.utils.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

/**
 * @author fengyue
 * @date 2022/1/22 18:50
 */
object LanZouApi {
    private val paramCathe = mutableMapOf<String, HashMap<String, Any>>()

    fun checkSubscribeUpdate(context: Context) {
        if (!SharedPreUtils.getInstance().getBoolean("checkSubscribeUpdate", true)) return
        if (DbManager.getDaoSession().subscribeFileDao.count() == 0L) return
        getFoldFiles(URLCONST.SUB_SOURCE_URL, 1, "fm9a")
            .onSuccess {
                it?.let {
                    for (file in it) {
                        val param = file.name_all.removeSuffix(".txt").split("#")
                        val subscribed = DbManager.getDaoSession().subscribeFileDao.load(param[0])
                        subscribed?.let { sub ->
                            if (sub.date < param[2]) {
                                DialogCreator.createThreeButtonDialog(context,
                                    "书源订阅更新", "发现有更新的订阅书源，是否前往更新？",
                                    true, "关闭订阅更新提醒", "取消",
                                    "确定", { _, _ ->
                                        SharedPreUtils.getInstance()
                                            .putBoolean("checkSubscribeUpdate", false)
                                        ToastUtils.showSuccess("自动检查订阅更新已关闭")
                                    }, null, { _, _ ->
                                        context.startActivity(
                                            Intent(
                                                context,
                                                SourceSubscribeActivity::class.java
                                            )
                                        )
                                    }
                                )
                                return@onSuccess
                            }
                        }
                    }
                }
            }
    }

    fun getFoldFiles(
        foldUrl: String,
        page: Int,
        pwd: String? = null
    ): Coroutine<List<LanZouFile>?> {
        return Coroutine.async {
            val params = if (page == 1) {
                getFoldParams(
                    getProxyClient().newCallResponseBody { url(foldUrl) }.text(),
                    page,
                    pwd
                )
            } else {
                paramCathe[foldUrl] ?: getFoldParams(getProxyClient().newCallResponseBody {
                    url(
                        foldUrl
                    )
                }.text(), page, pwd)
            }
            params["pg"] = page
            paramCathe[foldUrl] = params
            val res = getProxyClient().newCallResponseBody {
                url(URLCONST.LAN_ZOU_URL + "/filemoreajax.php")
                postForm(params)
            }.text()
            Log.d("getFoldFiles", params.toString())
            val json = JsonParser.parseString(res).asJsonObject
            val zt = json["zt"].asInt
            val info = json["info"].asString
            if (zt == 1) {
                val file = json["text"].toString()
                GSON.fromJsonArray(file)
            } else {
                throw Exception(info)
            }
        }
    }

    private fun getFoldParams(html: String, page: Int, pwd: String? = null): HashMap<String, Any> {
        val params = HashMap<String, Any>()
        params["lx"] = 2
        params["pg"] = page
        params["fid"] = StringUtils.getSubString(html, "'fid':", ",")
        params["uid"] = StringUtils.getSubString(html, "'uid':'", "',")
        params["rep"] = 0
        val t = StringUtils.getSubString(html, "'t':", ",")
        val k = StringUtils.getSubString(html, "'k':", ",")
        params["t"] = StringUtils.getSubString(html, "var $t = '", "';")
        params["k"] = StringUtils.getSubString(html, "var $k = '", "';")
        params["up"] = 1
        pwd?.let {
            params["ls"] = 1
            params["pwd"] = pwd
        }
        return params
    }

    fun getFileUrl(url: String): Observable<String> {
        url.replace("\\s".toRegex(), "").let {
            val regex = ",|，|密码:".toRegex()
            if (it.contains(regex)) {
                it.split(regex).let { arr ->
                    return getFileUrl(arr[0], arr[1])
                }
            } else {
                return getFileUrl(it, "")
            }
        }
    }

    /**
     * 通过api获取蓝奏云可下载直链
     *
     * @param url
     * @param password
     */
    fun getFileUrl(url: String, password: String = ""): Observable<String> {
        return Observable.create {
            var html = OkHttpUtils.getHtml(url)
            val url2 = if (password.isEmpty()) {
                val url1 = getUrl1(html)
                html = OkHttpUtils.getHtml(url1)
                val data = getDataString(html)
                Log.d("LanZouUtils", "data:$data")
                val key = getKeyValueByKey(html, data, "sign") +
                        "&" + getKeyValueByKey(html, data, "websignkey")
                Log.d("LanZouUtils", "key:$key")
                getUrl2(key, url1)
            } else {
                getUrl2(StringHelper.getSubString(html, "sign=", "&"), url, password)
            }
            if (url2.contains("file")) {
                it.onNext(getRedirectUrl(url2))
            } else {
                it.onError(Throwable(url2))
            }
            it.onComplete()
        }
    }

    fun getDataString(html: String): String {
        val start = html.lastIndexOf("data :") + "data :".length
        val end = html.indexOf("},", start) + 1
        return html.substring(start, end)
    }

    fun getUrl1(html: String): String {
        val doc = Jsoup.parse(html)
        return URLCONST.LAN_ZOU_URL + doc.getElementsByTag("iframe").attr("src")
    }

    fun getKeyValueByKey(html: String, data: String, key: String): String {
        val keyName = StringHelper.getSubString(data, "'$key':", ",")
        return if (keyName.endsWith("'")) {
            key + "=" + keyName.replace("'", "")
        } else {
            val lanzousKeyStart = "var $keyName = '"
            key + "=" + StringHelper.getSubString(html, lanzousKeyStart, "'")
        }
    }

    fun getUrl2(key: String, referer: String, password: String = ""): String {
        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body = if (password.isEmpty()) {
            "action=downprocess&signs=?ctdf&websign=&ves=1&$key"
        } else {
            "action=downprocess&sign=$key&p=$password"
        }
        val requestBody = body.toRequestBody(mediaType)

        val headers = HashMap<String, String>()
        headers["Referer"] = referer

        val html = OkHttpUtils.getHtml(
            URLCONST.LAN_ZOU_URL + "/ajaxm.php", requestBody,
            "UTF-8", headers
        )
        return getUrl2(html)
    }

    fun getUrl2(o: String): String {
        /*val info = o.split(",").toTypedArray()
        val zt = info[0].substring(info[0].indexOf(":") + 1)
        if (!"1".endsWith(zt)) {
            return ""
        }
        var dom = info[1].substring(info[1].indexOf(":") + 2, info[1].lastIndexOf("\""))
        var url = info[2].substring(info[2].indexOf(":") + 2, info[2].lastIndexOf("\""))
        dom = dom.replace("\\", "")
        url = url.replace("\\", "")*/
        val lanZouBean = GSON.fromJsonObject<LanZouParseBean>(o)
        lanZouBean?.run {
            return if (zt == 1) {
                "$dom/file/$url"
            } else {
                "解析失败\n信息：$inf"
            }
        }
        return ""
    }

    /**
     * 获取重定向地址
     *
     * @param path
     */
    fun getRedirectUrl(path: String): String {
        val conn = URL(path)
            .openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.connectTimeout = 5000
        conn.setRequestProperty(
            "User-Agent",
            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)"
        )
        conn.setRequestProperty("Accept-Language", "zh-cn")
        conn.setRequestProperty("Connection", "Keep-Alive")
        conn.setRequestProperty(
            "Accept",
            "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/x-silverlight, */*"
        )
        conn.connect()
        val redirectUrl = conn.getHeaderField("Location")
        conn.disconnect()
        return redirectUrl
    }
}