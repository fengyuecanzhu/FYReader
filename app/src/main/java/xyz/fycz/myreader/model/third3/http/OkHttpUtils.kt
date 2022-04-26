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

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.util.help.UTF8BOMFighter
import xyz.fycz.myreader.util.utils.EncodingDetect
import xyz.fycz.myreader.util.utils.GSON
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun OkHttpClient.newCallResponse(
    retry: Int = 0,
    builder: Request.Builder.() -> Unit
): Response {
    return withContext(IO) {
        val requestBuilder = Request.Builder()
        requestBuilder.header(APPCONST.UA_NAME, APPCONST.DEFAULT_USER_AGENT)
        requestBuilder.apply(builder)
        var response: Response? = null
        for (i in 0..retry) {
            response = this@newCallResponse.newCall(requestBuilder.build()).await()
            if (response.isSuccessful) {
                return@withContext response
            }
        }
        return@withContext response!!
    }
}

suspend fun OkHttpClient.newCallResponseBody(
    retry: Int = 0,
    builder: Request.Builder.() -> Unit
): ResponseBody {
    return withContext(IO) {
        val requestBuilder = Request.Builder()
        requestBuilder.header(APPCONST.UA_NAME, APPCONST.DEFAULT_USER_AGENT)
        requestBuilder.apply(builder)
        var response: Response? = null
        for (i in 0..retry) {
            response = this@newCallResponseBody.newCall(requestBuilder.build()).await()
            if (response.isSuccessful) {
                return@withContext response.body!!
            }
        }
        return@withContext response!!.body ?: throw IOException(response.message)
    }
}

suspend fun OkHttpClient.newCallStrResponse(
    retry: Int = 0,
    builder: Request.Builder.() -> Unit
): StrResponse {
    return withContext(IO) {
        val requestBuilder = Request.Builder()
        requestBuilder.header(APPCONST.UA_NAME, APPCONST.DEFAULT_USER_AGENT)
        requestBuilder.apply(builder)
        var response: Response? = null
        for (i in 0..retry) {
            response = this@newCallStrResponse.newCall(requestBuilder.build()).await()
            if (response.isSuccessful) {
                return@withContext StrResponse(response, response.body!!.text())
            }
        }
        return@withContext StrResponse(response!!, response.body?.text() ?: response.message)
    }
}

suspend fun Call.await(): Response = suspendCancellableCoroutine { block ->

    block.invokeOnCancellation {
        cancel()
    }

    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            block.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            block.resume(response)
        }
    })

}

fun ResponseBody.text(encode: String? = null): String {
    val responseBytes = UTF8BOMFighter.removeUTF8BOM(bytes())
    var charsetName: String? = encode

    charsetName?.let {
        return String(responseBytes, Charset.forName(charsetName))
    }

    //根据http头判断
    contentType()?.charset()?.let {
        return String(responseBytes, it)
    }

    //根据内容判断
    charsetName = EncodingDetect.getEncodeInHtml(responseBytes)
    return String(responseBytes, Charset.forName(charsetName))
}

fun Request.Builder.addHeaders(headers: Map<String, String>) {
    headers.forEach {
        if (it.key == APPCONST.UA_NAME) {
            //防止userAgent重复
            removeHeader(APPCONST.UA_NAME)
        }
        addHeader(it.key, it.value)
    }
}

fun Request.Builder.get(url: String, queryMap: Map<String, String>, encoded: Boolean = false) {
    val httpBuilder = url.toHttpUrl().newBuilder()
    queryMap.forEach {
        if (encoded) {
            httpBuilder.addEncodedQueryParameter(it.key, it.value)
        } else {
            httpBuilder.addQueryParameter(it.key, it.value)
        }
    }
    url(httpBuilder.build())
}

fun Request.Builder.postForm(form: Map<String, Any>, encoded: Boolean = false) {
    val formBody = FormBody.Builder()
    form.forEach {
        if (encoded) {
            formBody.addEncoded(it.key, it.value.toString())
        } else {
            formBody.add(it.key, it.value.toString())
        }
    }
    post(formBody.build())
}

fun Request.Builder.postMultipart(type: String?, form: Map<String, Any>) {
    val multipartBody = MultipartBody.Builder()
    type?.let {
        multipartBody.setType(type.toMediaType())
    }
    form.forEach {
        when (val value = it.value) {
            is Map<*, *> -> {
                val fileName = value["fileName"] as String
                val file = value["file"]
                val mediaType = (value["contentType"] as? String)?.toMediaType()
                val requestBody = when (file) {
                    is File -> {
                        file.asRequestBody(mediaType)
                    }
                    is ByteArray -> {
                        file.toRequestBody(mediaType)
                    }
                    is String -> {
                        file.toRequestBody(mediaType)
                    }
                    else -> {
                        GSON.toJson(file).toRequestBody(mediaType)
                    }
                }
                multipartBody.addFormDataPart(it.key, fileName, requestBody)
            }
            else -> multipartBody.addFormDataPart(it.key, it.value.toString())
        }
    }
    post(multipartBody.build())
}

fun Request.Builder.postJson(json: String?) {
    json?.let {
        val requestBody = json.toRequestBody("application/json; charset=UTF-8".toMediaType())
        post(requestBody)
    }
}