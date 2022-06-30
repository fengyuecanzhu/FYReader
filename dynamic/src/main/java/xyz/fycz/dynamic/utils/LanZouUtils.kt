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

package xyz.fycz.dynamic.utils

import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import xyz.fycz.myreader.common.URLCONST
import xyz.fycz.myreader.entity.lanzou.LanZouParseBean
import xyz.fycz.myreader.util.help.StringHelper
import xyz.fycz.myreader.util.utils.GSON
import xyz.fycz.myreader.util.utils.OkHttpUtils
import xyz.fycz.myreader.util.utils.StringUtils
import xyz.fycz.myreader.util.utils.fromJsonObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author fengyue
 * @date 2022/1/22 18:50
 */
object LanZouUtils {

    /**
     * 通过api获取蓝奏云可下载直链
     *
     * @param url
     * @param password
     */
    fun getFileUrl(url: String, password: String = ""): Observable<String> {
        return Observable.create {
            val html = OkHttpUtils.getHtml(url)
            val url2 = if (password.isEmpty()) {
                val url1 = getUrl1(html)
                val data = StringUtils.getSubString(OkHttpUtils.getHtml(url1), "},", "},")
                val key = getKeyValueByKey(data, "sign") + "&" + getKeyValueByKey(data, "websignkey")
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

    fun getUrl1(html: String): String {
        val doc = Jsoup.parse(html)
        return URLCONST.LAN_ZOU_URL + doc.getElementsByTag("iframe").attr("src")
    }

    fun getKeyValueByKey(html: String, key: String): String {
        val keyName = StringHelper.getSubString(html, "'$key':", ",")
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

    private fun getUrl2(o: String): String {
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