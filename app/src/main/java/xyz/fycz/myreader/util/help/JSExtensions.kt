package xyz.fycz.myreader.util.help

import android.util.Base64
import android.util.Log
import androidx.annotation.Keep
import org.jsoup.Connection
import org.jsoup.Jsoup
import xyz.fycz.myreader.greendao.service.CookieStore
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeUrl
import xyz.fycz.myreader.util.ZipUtils
import xyz.fycz.myreader.util.utils.*
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author fengyue
 * @date 2021/5/15 19:26
 */
@Keep
@Suppress("unused")
interface JSExtensions {
    /**
     * js实现跨域访问,不能删
     */
    fun ajax(urlStr: String?): String? {
        return try {
            val analyzeUrl = AnalyzeUrl(urlStr)
            OkHttpUtils.getStrResponse(analyzeUrl).blockingFirst().body()
        } catch (e: Exception) {
            e.localizedMessage
        }
    }


    /**
     * js实现压缩文件解压
     */
    fun unzipFile(zipPath: String): String {
        if (zipPath.isEmpty()) return ""
        val unzipPath = FileUtils.getCachePath() + File.separator + FileUtils.getNameExcludeExtension(zipPath)
        FileUtils.deleteFile(unzipPath)
        val zipFile = FileUtils.getFile(zipPath)
        val unzipFolder = FileUtils.getFolder(unzipPath)
        ZipUtils.unzipFile(zipFile, unzipFolder)
        FileUtils.deleteFile(zipPath)
        return unzipPath
    }

    /**
     * js实现文件夹内所有文件读取
     */
    fun getTxtInFolder(unzipPath: String): String {
        if (unzipPath.isEmpty()) return ""
        val unzipFolder = FileUtils.getFolder(unzipPath)
        val contents = StringBuilder()
        unzipFolder.listFiles().let {
            if (it != null) {
                for (f in it) {
                    val charsetName = FileUtils.getFileCharset(f)
                    contents.append(String(f.readBytes(), charset(charsetName)))
                        .append("\n")
                }
                contents.deleteCharAt(contents.length - 1)
            }
        }
        FileUtils.deleteFile(unzipPath)
        return contents.toString()
    }

    /**
     * js实现重定向拦截,不能删
     */
    @Throws(IOException::class)
    operator fun get(urlStr: String?, headers: Map<String?, String?>?): Connection.Response? {
        return Jsoup.connect(urlStr)
            .sslSocketFactory(SSLSocketClient.createSSLSocketFactory())
            .ignoreContentType(true)
            .followRedirects(false)
            .headers(headers)
            .method(Connection.Method.GET)
            .execute()
    }

    /**
     * js实现重定向拦截,不能删
     */
    @Throws(IOException::class)
    fun post(
        urlStr: String?,
        body: String?,
        headers: Map<String?, String?>?
    ): Connection.Response? {
        return Jsoup.connect(urlStr)
            .sslSocketFactory(SSLSocketClient.createSSLSocketFactory())
            .ignoreContentType(true)
            .followRedirects(false)
            .requestBody(body)
            .headers(headers)
            .method(Connection.Method.POST)
            .execute()
    }

    /**
     *js实现读取cookie
     */
    fun getCookie(tag: String, key: String? = null): String {
        val cookie = CookieStore.getCookie(tag)
        val cookieMap = CookieStore.cookieToMap(cookie)
        return if (key != null) {
            cookieMap[key] ?: ""
        } else {
            cookie
        }
    }

    /**
     * js实现解码,不能删
     */
    fun base64Decode(str: String): String {
        return EncoderUtils.base64Decode(str, Base64.NO_WRAP)
    }

    fun base64Decode(str: String, flags: Int): String {
        return EncoderUtils.base64Decode(str, flags)
    }

    fun base64DecodeToByteArray(str: String?): ByteArray? {
        if (str.isNullOrBlank()) {
            return null
        }
        return Base64.decode(str, Base64.DEFAULT)
    }

    fun base64DecodeToByteArray(str: String?, flags: Int): ByteArray? {
        if (str.isNullOrBlank()) {
            return null
        }
        return Base64.decode(str, flags)
    }

    fun base64Encode(str: String): String? {
        return EncoderUtils.base64Encode(str, Base64.NO_WRAP)
    }

    fun base64Encode(str: String, flags: Int): String? {
        return EncoderUtils.base64Encode(str, flags)
    }

    fun md5Encode(str: String): String {
        return MD5Utils.md5Encode(str)
    }

    fun md5Encode16(str: String): String {
        return MD5Utils.md5Encode16(str)
    }

    /**
     * 时间格式化
     */
    fun timeFormat(time: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm")
        return sdf.format(Date(time))
    }

    /**
     * utf8编码转gbk编码
     */
    fun utf8ToGbk(str: String): String {
        val utf8 = String(str.toByteArray(charset("UTF-8")))
        val unicode = String(utf8.toByteArray(), charset("UTF-8"))
        return String(unicode.toByteArray(charset("GBK")))
    }

    fun encodeURI(str: String): String {
        return try {
            URLEncoder.encode(str, "UTF-8")
        } catch (e: Exception) {
            ""
        }
    }

    fun encodeURI(str: String, enc: String): String {
        return try {
            URLEncoder.encode(str, enc)
        } catch (e: Exception) {
            ""
        }
    }

    fun htmlFormat(str: String): String {
        return StringUtils.formatHtml(str)
    }

    /**
     * 读取本地文件
     */
    fun readFile(path: String): ByteArray {
        return File(path).readBytes()
    }

    fun readTxtFile(path: String): String {
        val f = File(path)
        val charsetName = FileUtils.getFileCharset(f)
        return String(f.readBytes(), charset(charsetName))
    }

    fun readTxtFile(path: String, charsetName: String): String {
        return String(File(path).readBytes(), charset(charsetName))
    }

    /**
     * 输出调试日志
     */
    fun log(msg: String): String {
        Log.d("JS", msg)
        return msg
    }

    /**
     * AES 解码为 ByteArray
     * @param str 传入的AES加密的数据
     * @param key AES 解密的key
     * @param transformation AES加密的方式
     * @param iv ECB模式的偏移向量
     */
    fun aesDecodeToByteArray(
        str: String,
        key: String,
        transformation: String,
        iv: String = ""
    ): ByteArray? {

        return EncoderUtils.decryptAES(
            data = str.encodeToByteArray(),
            key = key.encodeToByteArray(),
            transformation,
            iv.encodeToByteArray()
        )
    }

    /**
     * AES 解码为 String
     * @param str 传入的AES加密的数据
     * @param key AES 解密的key
     * @param transformation AES加密的方式
     * @param iv ECB模式的偏移向量
     */

    fun aesDecodeToString(
        str: String,
        key: String,
        transformation: String,
        iv: String = ""
    ): String? {
        return aesDecodeToByteArray(str, key, transformation, iv)?.let { String(it) }
    }

    /**
     * 已经base64的AES 解码为 ByteArray
     * @param str 传入的AES Base64加密的数据
     * @param key AES 解密的key
     * @param transformation AES加密的方式
     * @param iv ECB模式的偏移向量
     */

    fun aesBase64DecodeToByteArray(
        str: String,
        key: String,
        transformation: String,
        iv: String = ""
    ): ByteArray? {
        return EncoderUtils.decryptBase64AES(
            data = str.encodeToByteArray(),
            key = key.encodeToByteArray(),
            transformation,
            iv.encodeToByteArray()
        )
    }

    /**
     * 已经base64的AES 解码为 String
     * @param str 传入的AES Base64加密的数据
     * @param key AES 解密的key
     * @param transformation AES加密的方式
     * @param iv ECB模式的偏移向量
     */

    fun aesBase64DecodeToString(
        str: String,
        key: String,
        transformation: String,
        iv: String = ""
    ): String? {
        return aesBase64DecodeToByteArray(str, key, transformation, iv)?.let { String(it) }
    }

    /**
     * 加密aes为ByteArray
     * @param data 传入的原始数据
     * @param key AES加密的key
     * @param transformation AES加密的方式
     * @param iv ECB模式的偏移向量
     */
    fun aesEncodeToByteArray(
        data: String, key: String, transformation: String,
        iv: String = ""
    ): ByteArray? {
        return EncoderUtils.encryptAES(
            data.encodeToByteArray(),
            key = key.encodeToByteArray(),
            transformation,
            iv.encodeToByteArray()
        )
    }

    /**
     * 加密aes为String
     * @param data 传入的原始数据
     * @param key AES加密的key
     * @param transformation AES加密的方式
     * @param iv ECB模式的偏移向量
     */
    fun aesEncodeToString(
        data: String, key: String, transformation: String,
        iv: String = ""
    ): String? {
        return aesEncodeToByteArray(data, key, transformation, iv)?.let { String(it) }
    }

    /**
     * 加密aes后Base64化的ByteArray
     * @param data 传入的原始数据
     * @param key AES加密的key
     * @param transformation AES加密的方式
     * @param iv ECB模式的偏移向量
     */
    fun aesEncodeToBase64ByteArray(
        data: String, key: String, transformation: String,
        iv: String = ""
    ): ByteArray? {
        return EncoderUtils.encryptAES2Base64(
            data.encodeToByteArray(),
            key = key.encodeToByteArray(),
            transformation,
            iv.encodeToByteArray()
        )
    }

    /**
     * 加密aes后Base64化的String
     * @param data 传入的原始数据
     * @param key AES加密的key
     * @param transformation AES加密的方式
     * @param iv ECB模式的偏移向量
     */
    fun aesEncodeToBase64String(
        data: String, key: String, transformation: String,
        iv: String = ""
    ): String? {
        return aesEncodeToBase64ByteArray(data, key, transformation, iv)?.let { String(it) }
    }
}