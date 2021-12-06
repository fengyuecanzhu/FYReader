package xyz.fycz.myreader.util.help;


import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Keep;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.fycz.myreader.greendao.service.CookieStore;
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeUrl;
import xyz.fycz.myreader.util.ZipUtils;
import xyz.fycz.myreader.util.utils.EncoderUtils;
import xyz.fycz.myreader.util.utils.EncodingDetect;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.MD5Utils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

@Keep
@SuppressWarnings({"unused"})

public interface JsExtensions {
    String TAG = JsExtensions.class.getSimpleName();

    /**
     * js实现跨域访问,不能删
     */
    default String ajax(String urlStr) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(urlStr);
            return OkHttpUtils.getStrResponse(analyzeUrl).blockingFirst().body();
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    /**
     * js实现压缩文件解压
     */
    default String unzipFile(String zipPath) throws IOException {
        if (zipPath.isEmpty()) return "";
        String unzipPath = FileUtils.getCachePath() + File.separator + FileUtils.getNameExcludeExtension(zipPath);
        FileUtils.deleteFile(unzipPath);
        File zipFile = FileUtils.getFile(zipPath);
        File unzipFolder = FileUtils.getFolder(unzipPath);
        ZipUtils.unzipFile(zipFile, unzipFolder);
        FileUtils.deleteFile(zipPath);
        return unzipPath;
    }

    /**
     * js实现文件夹内所有文件读取
     */
    default String getTxtInFolder(String unzipPath) throws UnsupportedEncodingException {
        if (unzipPath.isEmpty()) return "";
        File unzipFolder = FileUtils.getFolder(unzipPath);
        StringBuilder contents = new StringBuilder();
        File[] files = unzipFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                String charsetName = FileUtils.getFileCharset(file);
                contents.append(new String(FileUtils.getBytes(file), Charset.forName(charsetName)))
                        .append("\n");
            }
            if (contents.length() > 0)
                contents.deleteCharAt(contents.length() - 1);
        }
        FileUtils.deleteFile(unzipPath);
        return contents.toString();
    }


    /**
     * js实现重定向拦截,不能删
     */
    default Connection.Response get(String urlStr, Map<String, String> headers) throws IOException {
        return Jsoup.connect(urlStr)
                .sslSocketFactory(SSLSocketClient.createSSLSocketFactory())
                .ignoreContentType(true)
                .followRedirects(false)
                .headers(headers)
                .method(Connection.Method.GET)
                .execute();
    }

    /**
     * js实现重定向拦截,不能删
     */
    default Connection.Response post(String urlStr, String body, Map<String, String> headers) throws IOException {
        return Jsoup.connect(urlStr)
                .sslSocketFactory(SSLSocketClient.createSSLSocketFactory())
                .ignoreContentType(true)
                .followRedirects(false)
                .requestBody(body)
                .headers(headers)
                .method(Connection.Method.POST)
                .execute();
    }

    /**
     * js实现读取cookie
     */
    default String getCookie(String tag, String key) {
        String cookie = CookieStore.INSTANCE.getCookie(tag);
        Map<String, String> cookieMap = CookieStore.INSTANCE.cookieToMap(cookie);
        if (key != null) {
            return cookieMap.get(key) == null ? "" : cookieMap.get(key);
        } else {
            return cookie;
        }
    }

    default String getCookie(String tag) {
        return getCookie(tag, null);
    }

    /**
     * js实现解码,不能删
     */
    default String base64Decoder(String base64) {
        return EncoderUtils.INSTANCE.base64Decode(base64, Base64.NO_WRAP);
    }

    default String base64Decoder(String base64, int flags) {
        return EncoderUtils.INSTANCE.base64Decode(base64, flags);
    }

    default byte[] base64DecodeToByteArray(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return Base64.decode(str, Base64.DEFAULT);
    }

    default byte[] base64DecodeToByteArray(String str, int flags) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return Base64.decode(str, flags);
    }

    default String base64Encode(String base64) {
        return EncoderUtils.INSTANCE.base64Encode(base64, Base64.NO_WRAP);
    }

    default String base64Encode(String base64, int flags) {
        return EncoderUtils.INSTANCE.base64Encode(base64, flags);
    }

    default String md5Encode(String str) {
        return MD5Utils.INSTANCE.md5Encode(str);
    }

    default String md5Encode16(String str) {
        return MD5Utils.INSTANCE.md5Encode16(str);
    }

    /**
     * 章节数转数字
     */
    default String toNumChapter(String s) {
        if (s == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("(第)(.+?)(章)");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group(1) + StringUtils.stringToInt(matcher.group(2)) + matcher.group(3);
        }
        return s;
    }

    /**
     * 时间格式化
     */
    default String timeFormat(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return sdf.format(new Date(time));
    }

    /**
     * utf8编码转gbk编码
     */
    default String utf8ToGbk(String str) {
        String utf8 = new String(str.getBytes(StandardCharsets.UTF_8));
        String unicode = new String(utf8.getBytes(), StandardCharsets.UTF_8);
        return new String(unicode.getBytes(Charset.forName("GBK")));
    }

    default String encodeURI(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    default String encodeURI(String str, String enc) {
        try {
            return URLEncoder.encode(str, enc);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 读取本地文件
     */
    default byte[] readFile(String path) {
        return FileUtils.getBytes(new File(path));
    }

    default String readTxtFile(String path) {
        File f = new File(path);
        String charsetName = FileUtils.getFileCharset(f);
        return new String(FileUtils.getBytes(f), Charset.forName(charsetName));
    }

    default String readTxtFile(String path, String charsetName) {
        return new String(FileUtils.getBytes(new File(path)), Charset.forName(charsetName));
    }

    /**
     * 输出调试日志
     */
    default String log(String msg) {
        Log.d(TAG, msg);
        return msg;
    }


    /**
     * AES 解码为 ByteArray
     *
     * @param str            传入的AES加密的数据
     * @param key            AES 解密的key
     * @param transformation AES加密的方式
     * @param iv             ECB模式的偏移向量
     */
    default byte[] aesDecodeToByteArray(String str, String key, String transformation, String iv) {

        return EncoderUtils.INSTANCE.decryptAES(
                str.getBytes(StandardCharsets.UTF_8),
                key.getBytes(StandardCharsets.UTF_8),
                transformation,
                iv.getBytes(StandardCharsets.UTF_8)
        );
    }

    default byte[] aesDecodeToByteArray(String str, String key, String transformation) {
        return aesDecodeToByteArray(str, key, transformation, "");
    }

    /**
     * AES 解码为 String
     *
     * @param str            传入的AES加密的数据
     * @param key            AES 解密的key
     * @param transformation AES加密的方式
     * @param iv             ECB模式的偏移向量
     */

    default String aesDecodeToString(String str, String key, String transformation, String iv) {
        byte[] bytes = aesDecodeToByteArray(str, key, transformation, iv);
        if (bytes == null) {
            return "";
        }
        return new String(bytes);
    }

    default String aesDecodeToString(String str, String key, String transformation) {
        return aesDecodeToString(str, key, transformation, "");
    }

    /**
     * 已经base64的AES 解码为 ByteArray
     *
     * @param str            传入的AES Base64加密的数据
     * @param key            AES 解密的key
     * @param transformation AES加密的方式
     * @param iv             ECB模式的偏移向量
     */

    default byte[] aesBase64DecodeToByteArray(String str, String key, String transformation, String iv) {
        return EncoderUtils.INSTANCE.decryptBase64AES(
                str.getBytes(StandardCharsets.UTF_8),
                key.getBytes(StandardCharsets.UTF_8),
                transformation,
                iv.getBytes(StandardCharsets.UTF_8)
        );
    }

    default byte[] aesBase64DecodeToByteArray(String str, String key, String transformation) {
        return aesBase64DecodeToByteArray(str, key, transformation, "");
    }


    /**
     * 已经base64的AES 解码为 String
     *
     * @param str            传入的AES Base64加密的数据
     * @param key            AES 解密的key
     * @param transformation AES加密的方式
     * @param iv             ECB模式的偏移向量
     */

    default String aesBase64DecodeToString(String str, String key, String transformation, String iv) {
        byte[] bytes = aesBase64DecodeToByteArray(str, key, transformation, iv);
        if (bytes == null) {
            return "";
        }
        return new String(bytes);
    }

    default String aesBase64DecodeToString(String str, String key, String transformation) {
        return aesBase64DecodeToString(str, key, transformation, "");
    }

    /**
     * 加密aes为ByteArray
     *
     * @param data           传入的原始数据
     * @param key            AES加密的key
     * @param transformation AES加密的方式
     * @param iv             ECB模式的偏移向量
     */
    default byte[] aesEncodeToByteArray(String data, String key, String transformation, String iv) {
        return EncoderUtils.INSTANCE.encryptAES(
                data.getBytes(StandardCharsets.UTF_8),
                key.getBytes(StandardCharsets.UTF_8),
                transformation,
                iv.getBytes(StandardCharsets.UTF_8)
        );
    }

    default byte[] aesEncodeToByteArray(String data, String key, String transformation) {
        return aesEncodeToByteArray(data, key, transformation, "");
    }

    /**
     * 加密aes为String
     *
     * @param data           传入的原始数据
     * @param key            AES加密的key
     * @param transformation AES加密的方式
     * @param iv             ECB模式的偏移向量
     */
    default String aesEncodeToString(String data, String key, String transformation, String iv) {
        byte[] bytes = aesEncodeToByteArray(data, key, transformation, iv);
        if (bytes == null) {
            return "";
        }
        return new String(bytes);
    }

    default String aesEncodeToString(String str, String key, String transformation) {
        return aesEncodeToString(str, key, transformation, "");
    }

    /**
     * 加密aes后Base64化的ByteArray
     *
     * @param data           传入的原始数据
     * @param key            AES加密的key
     * @param transformation AES加密的方式
     * @param iv             ECB模式的偏移向量
     */
    default byte[] aesEncodeToBase64ByteArray(String data, String key, String transformation, String iv) {
        return EncoderUtils.INSTANCE.encryptAES2Base64(
                data.getBytes(StandardCharsets.UTF_8),
                key.getBytes(StandardCharsets.UTF_8),
                transformation,
                iv.getBytes(StandardCharsets.UTF_8)
        );
    }

    default byte[] aesEncodeToBase64ByteArray(String data, String key, String transformation) {
        return aesEncodeToBase64ByteArray(data, key, transformation, "");
    }

    /**
     * 加密aes后Base64化的String
     *
     * @param data           传入的原始数据
     * @param key            AES加密的key
     * @param transformation AES加密的方式
     * @param iv             ECB模式的偏移向量
     */
    default String aesEncodeToBase64String(String data, String key, String transformation, String iv) {
        byte[] bytes = aesEncodeToBase64ByteArray(data, key, transformation, iv);
        if (bytes == null) {
            return "";
        }
        return new String(bytes);
    }

    default String aesEncodeToBase64String(String str, String key, String transformation) {
        return aesEncodeToBase64String(str, key, transformation, "");
    }
}
