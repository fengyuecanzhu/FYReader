@file:Suppress("unused")

package xyz.fycz.myreader.greendao.service

import android.text.TextUtils
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.entity.CookieBean
import xyz.fycz.myreader.greendao.service.api.CookieManager
import xyz.fycz.myreader.util.utils.NetworkUtils

object CookieStore : CookieManager {

    override fun setCookie(url: String, cookie: String?) {
        val cookieBean = CookieBean(NetworkUtils.getSubDomain(url), cookie ?: "")
        DbManager.getDaoSession().cookieBeanDao.insertOrReplace(cookieBean)
    }

    override fun replaceCookie(url: String, cookie: String) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(cookie)) {
            return
        }
        val oldCookie = getCookie(url)
        if (TextUtils.isEmpty(oldCookie)) {
            setCookie(url, cookie)
        } else {
            val cookieMap = cookieToMap(oldCookie)
            cookieMap.putAll(cookieToMap(cookie))
            val newCookie = mapToCookie(cookieMap)
            setCookie(url, newCookie)
        }
    }

    override fun getCookie(url: String): String {
        val cookieBean = DbManager.getDaoSession().cookieBeanDao.load(NetworkUtils.getSubDomain(url))
        return cookieBean?.cookie ?: ""
    }

    override fun removeCookie(url: String) {
        DbManager.getDaoSession().cookieBeanDao.deleteByKey(NetworkUtils.getSubDomain(url))
    }

    override fun cookieToMap(cookie: String): MutableMap<String, String> {
        val cookieMap = mutableMapOf<String, String>()
        if (cookie.isBlank()) {
            return cookieMap
        }
        val pairArray = cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (pair in pairArray) {
            val pairs = pair.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (pairs.size == 1) {
                continue
            }
            val key = pairs[0].trim { it <= ' ' }
            val value = pairs[1]
            if (value.isNotBlank() || value.trim { it <= ' ' } == "null") {
                cookieMap[key] = value.trim { it <= ' ' }
            }
        }
        return cookieMap
    }

    override fun mapToCookie(cookieMap: Map<String, String>?): String? {
        if (cookieMap == null || cookieMap.isEmpty()) {
            return null
        }
        val builder = StringBuilder()
        for (key in cookieMap.keys) {
            val value = cookieMap[key]
            if (value?.isNotBlank() == true) {
                builder.append(key)
                    .append("=")
                    .append(value)
                    .append(";")
            }
        }
        return builder.deleteCharAt(builder.lastIndexOf(";")).toString()
    }
}