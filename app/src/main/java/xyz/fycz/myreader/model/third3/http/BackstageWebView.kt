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

package xyz.fycz.myreader.model.third3.http

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.AndroidRuntimeException
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.*
import org.apache.commons.text.StringEscapeUtils
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.greendao.service.CookieStore
import xyz.fycz.myreader.model.third3.NoStackTraceException
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

/**
 * 后台webView
 */
class BackstageWebView(
    private val url: String? = null,
    private val html: String? = null,
    private val encode: String? = null,
    private val tag: String? = null,
    private val headerMap: Map<String, String>? = null,
    private val sourceRegex: String? = null,
    private val javaScript: String? = null,
) {

    private val mHandler = Handler(Looper.getMainLooper())
    private var callback: Callback? = null
    private var mWebView: WebView? = null

    suspend fun getStrResponse(): StrResponse = suspendCancellableCoroutine { block ->
        block.invokeOnCancellation {
            App.getHandler().post {
                destroy()
            }
        }
        callback = object : Callback() {
            override fun onResult(response: StrResponse) {
                if (!block.isCompleted)
                    block.resume(response)
            }

            override fun onError(error: Throwable) {
                if (!block.isCompleted)
                    block.cancel(error)
            }
        }
        App.getHandler().post {
            try {
                load()
            } catch (error: Throwable) {
                block.cancel(error)
            }
        }
    }

    private fun getEncoding(): String {
        return encode ?: "utf-8"
    }

    @Throws(AndroidRuntimeException::class)
    private fun load() {
        val webView = createWebView()
        mWebView = webView
        try {
            when {
                !html.isNullOrEmpty() -> if (url.isNullOrEmpty()) {
                    webView.loadData(html, "text/html", getEncoding())
                } else {
                    webView.loadDataWithBaseURL(url, html, "text/html", getEncoding(), url)
                }
                else -> if (headerMap == null) {
                    webView.loadUrl(url!!)
                } else {
                    webView.loadUrl(url!!, headerMap)
                }
            }
        } catch (e: Exception) {
            callback?.onError(e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun createWebView(): WebView {
        val webView = WebView(App.getmContext())
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.blockNetworkImage = true
        settings.userAgentString = headerMap?.get(APPCONST.UA_NAME)
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        if (sourceRegex.isNullOrEmpty()) {
            webView.webViewClient = HtmlWebViewClient()
        } else {
            webView.webViewClient = SnifferWebClient()
        }
        return webView
    }

    private fun destroy() {
        mWebView?.destroy()
        mWebView = null
    }

    private fun getJs(): String {
        javaScript?.let {
            if (it.isNotEmpty()) {
                return it
            }
        }
        return JS
    }

    private fun setCookie(url: String) {
        tag?.let {
            val cookie = CookieManager.getInstance().getCookie(url)
            CookieStore.setCookie(it, cookie)
        }
    }

    private inner class HtmlWebViewClient : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            setCookie(url)
            val runnable = EvalJsRunnable(view, url, getJs())
            mHandler.postDelayed(runnable, 1000)
        }

    }

    private inner class EvalJsRunnable(
        webView: WebView,
        private val url: String,
        private val mJavaScript: String
    ) : Runnable {
        var retry = 0
        private val mWebView: WeakReference<WebView> = WeakReference(webView)
        override fun run() {
            mWebView.get()?.evaluateJavascript(mJavaScript) {
                if (it.isNotEmpty() && it != "null") {
                    val content = StringEscapeUtils.unescapeJson(it)
                        .replace("^\"|\"$".toRegex(), "")
                    try {
                        val response = StrResponse(url, content)
                        callback?.onResult(response)
                    } catch (e: Exception) {
                        callback?.onError(e)
                    }
                    mHandler.removeCallbacks(this)
                    destroy()
                    return@evaluateJavascript
                }
                if (retry > 30) {
                    callback?.onError(NoStackTraceException("js执行超时"))
                    mHandler.removeCallbacks(this)
                    destroy()
                    return@evaluateJavascript
                }
                retry++
                mHandler.removeCallbacks(this)
                mHandler.postDelayed(this, 1000)
            }
        }
    }

    private inner class SnifferWebClient : WebViewClient() {

        override fun onLoadResource(view: WebView, resUrl: String) {
            sourceRegex?.let {
                if (resUrl.matches(it.toRegex())) {
                    try {
                        val response = StrResponse(url!!, resUrl)
                        callback?.onResult(response)
                    } catch (e: Exception) {
                        callback?.onError(e)
                    }
                    destroy()
                }
            }
        }

        override fun onPageFinished(webView: WebView, url: String) {
            setCookie(url)
            val js = javaScript
            if (!js.isNullOrEmpty()) {
                val runnable = LoadJsRunnable(webView, javaScript)
                mHandler.postDelayed(runnable, 1000L)
            }
        }

    }

    private class LoadJsRunnable(
        webView: WebView,
        private val mJavaScript: String?
    ) : Runnable {
        private val mWebView: WeakReference<WebView> = WeakReference(webView)
        override fun run() {
            mWebView.get()?.loadUrl("javascript:${mJavaScript ?: ""}")
        }
    }

    companion object {
        const val JS = "document.documentElement.outerHTML"
    }

    abstract class Callback {
        abstract fun onResult(response: StrResponse)
        abstract fun onError(error: Throwable)
    }
}