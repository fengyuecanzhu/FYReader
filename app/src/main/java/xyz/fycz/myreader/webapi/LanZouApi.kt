package xyz.fycz.myreader.webapi

import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import xyz.fycz.myreader.common.URLCONST
import xyz.fycz.myreader.entity.LanZouBean
import xyz.fycz.myreader.util.help.StringHelper
import xyz.fycz.myreader.util.utils.GSON
import xyz.fycz.myreader.util.utils.OkHttpUtils
import xyz.fycz.myreader.util.utils.fromJsonObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * @author fengyue
 * @date 2022/1/22 18:50
 */
object LanZouApi {
    /**
     * 通过api获取蓝奏云可下载直链
     *
     * @param url
     * @param password
     */
    fun getUrl(url: String, password: String = ""): Observable<String> {
        return Observable.create {
            val html = OkHttpUtils.getHtml(url)
            val url2 = if (password.isEmpty()) {
                val url1 = getUrl1(html)
                val key = getKey(OkHttpUtils.getHtml(url1))
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

    private fun getUrl1(html: String): String {
        val doc = Jsoup.parse(html)
        return URLCONST.LAN_ZOUS_URL + doc.getElementsByClass("ifr2").attr("src")
    }

    private fun getKey(html: String): String {
        var lanzousKeyStart = "var pposturl = '"
        val keyName = StringHelper.getSubString(html, "'sign':", ",")
        lanzousKeyStart = if (keyName.endsWith("'")) {
            "'sign':'"
        } else {
            "var $keyName = '"
        }
        return StringHelper.getSubString(html, lanzousKeyStart, "'")
    }


    private fun getUrl2(key: String, referer: String, password: String = ""): String {
        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body = if (password.isEmpty()) {
            "action=downprocess&sign=$key&ves=1"
        } else {
            "action=downprocess&sign=$key&p=$password"
        }
        val requestBody = body.toRequestBody(mediaType)

        val headers = HashMap<String, String>()
        headers["Referer"] = referer

        val html = OkHttpUtils.getHtml(
            URLCONST.LAN_ZOUS_URL + "/ajaxm.php", requestBody,
            "UTF-8", headers
        )
        return getUrl2(html)
    }

    private fun getUrl2(o: String): String {
        /*val info = o.split(",").toTypedArray()
        val zt = info[0].substring(info[0].indexOf(":") + 1)
        if (!"1".endsWith(zt)) {
            return ""
        }
        var dom = info[1].substring(info[1].indexOf(":") + 2, info[1].lastIndexOf("\""))
        var url = info[2].substring(info[2].indexOf(":") + 2, info[2].lastIndexOf("\""))
        dom = dom.replace("\\", "")
        url = url.replace("\\", "")*/
        val lanZouBean = GSON.fromJsonObject<LanZouBean>(o)
        lanZouBean?.run {
            return if (zt == 1) {
                "$dom/file/$url"
            } else {
                inf
            }
        }
        return ""
    }

    /**
     * 获取重定向地址
     *
     * @param path
     */
    private fun getRedirectUrl(path: String): String {
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