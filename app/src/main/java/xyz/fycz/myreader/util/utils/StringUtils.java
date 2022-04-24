/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.util.utils;


import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.StringRes;

import xyz.fycz.myreader.application.App;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static android.text.TextUtils.isEmpty;


/**
 * Created by newbiechen on 17-4-22.
 * 对文字操作的工具类
 */

public class StringUtils {
    private static final String TAG = "StringUtils";
    private static final int HOUR_OF_DAY = 24;
    private static final int DAY_OF_YESTERDAY = 2;
    private static final int TIME_UNIT = 60;
    private final static HashMap<Character, Integer> ChnMap = getChnMap();

    //将时间转换成日期
    public static String dateConvert(long time, String pattern) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    //将日期转换成昨天、今天、明天
    public static String dateConvert(String source, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = format.parse(source);
            long curTime = calendar.getTimeInMillis();
            calendar.setTime(date);
            //将MISC 转换成 sec
            long difSec = Math.abs((curTime - date.getTime()) / 1000);
            long difMin = difSec / 60;
            long difHour = difMin / 60;
            long difDate = difHour / 60;
            int oldHour = calendar.get(Calendar.HOUR);
            //如果没有时间
            if (oldHour == 0) {
                //比日期:昨天今天和明天
                if (difDate == 0) {
                    return "今天";
                } else if (difDate < DAY_OF_YESTERDAY) {
                    return "昨天";
                } else {
                    DateFormat convertFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String value = convertFormat.format(date);
                    return value;
                }
            }

            if (difSec < TIME_UNIT) {
                return difSec + "秒前";
            } else if (difMin < TIME_UNIT) {
                return difMin + "分钟前";
            } else if (difHour < HOUR_OF_DAY) {
                return difHour + "小时前";
            } else if (difDate < DAY_OF_YESTERDAY) {
                return "昨天";
            } else {
                DateFormat convertFormat = new SimpleDateFormat("yyyy-MM-dd");
                String value = convertFormat.format(date);
                return value;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String toFirstCapital(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String getString(@StringRes int id) {
        return App.getmContext().getResources().getString(id);
    }

    public static String getString(@StringRes int id, Object... formatArgs) {
        return App.getmContext().getResources().getString(id, formatArgs);
    }

    /**
     * 将文本中的半角字符，转换成全角字符
     *
     * @param input
     * @return
     */
    public static String halfToFull(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) //半角空格
            {
                c[i] = (char) 12288;
                continue;
            }
            //根据实际情况，过滤不需要转换的符号
            //if (c[i] == 46) //半角点号，不转换
            // continue;

            if (c[i] > 32 && c[i] < 127)    //其他符号都转换为全角
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }

    //功能：字符串全角转换为半角
    public static String fullToHalf(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) //全角空格
            {
                c[i] = (char) 32;
                continue;
            }

            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }


    public static void isNotChinese(Editable s) {
        //禁止输入中文
        if (s.length() > 0) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c >= 0x4e00 && c <= 0X9fff) { // 根据字节码判断
                    // 如果是中文，则清除输入的字符，否则保留
                    s.delete(i, i + 1);
                }
            }
        }
    }

    /**
     * 计算两个字符串的相似度
     *
     * @param str1
     * @param str2
     * @return
     */
    public static float levenshtein(String str1, String str2) {
        //计算两个字符串的长度。
        int len1 = str1.length();
        int len2 = str2.length();
        //建立上面说的数组，比字符长度大一个空间
        int[][] dif = new int[len1 + 1][len2 + 1];
        //赋初值，步骤B。
        for (int a = 0; a <= len1; a++) {
            dif[a][0] = a;
        }
        for (int a = 0; a <= len2; a++) {
            dif[0][a] = a;
        }
        //计算两个字符是否一样，计算左上的值
        char[] ch1 = str1.toCharArray();
        char[] ch2 = str2.toCharArray();
        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (ch1[i - 1] == ch2[j - 1]) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                //取三个值中最小的
                int temp1 = dif[i - 1][j - 1] + temp;
                int temp2 = dif[i][j - 1] + 1;
                int temp3 = dif[i - 1][j] + 1;
                int arr[] = new int[]{temp1, temp2, temp3};

                dif[i][j] = min(arr);
            }
        }
        //计算相似度
        float similarity = 1 - (float) dif[len1][len2] / Math.max(str1.length(), str2.length());
        return similarity;
    }

    //得到最小值
    private static int min(int[] arr) {
        int min = arr[0];
        for (int i : arr) {
            if (min > i) {
                min = i;
            }
        }
        return min;
    }

    /**
     * 去除所有空格
     *
     * @param str
     * @return
     */
    public static String deleteWhitespace(String str) {
        return str.replaceAll("\\s*", "");
    }

    public static Boolean stringEquals(String str1, String str2) {
        return Objects.equals(str1, str2) || (isEmpty(str1) && isEmpty(str2));
    }

    private static HashMap<Character, Integer> getChnMap() {
        HashMap<Character, Integer> map = new HashMap<>();
        String cnStr = "零一二三四五六七八九十";
        char[] c = cnStr.toCharArray();
        for (int i = 0; i <= 10; i++) {
            map.put(c[i], i);
        }
        cnStr = "〇壹贰叁肆伍陆柒捌玖拾";
        c = cnStr.toCharArray();
        for (int i = 0; i <= 10; i++) {
            map.put(c[i], i);
        }
        map.put('两', 2);
        map.put('百', 100);
        map.put('佰', 100);
        map.put('千', 1000);
        map.put('仟', 1000);
        map.put('万', 10000);
        map.put('亿', 100000000);
        return map;
    }


    @SuppressWarnings("ConstantConditions")
    public static int chineseNumToInt(String chNum) {
        int result = 0;
        int tmp = 0;
        int billion = 0;
        char[] cn = chNum.toCharArray();

        // "一零二五" 形式
        if (cn.length > 1 && chNum.matches("^[〇零一二三四五六七八九壹贰叁肆伍陆柒捌玖]$")) {
            for (int i = 0; i < cn.length; i++) {
                cn[i] = (char) (48 + ChnMap.get(cn[i]));
            }
            return Integer.parseInt(new String(cn));
        }

        // "一千零二十五", "一千二" 形式
        try {
            for (int i = 0; i < cn.length; i++) {
                int tmpNum = ChnMap.get(cn[i]);
                if (tmpNum == 100000000) {
                    result += tmp;
                    result *= tmpNum;
                    billion = billion * 100000000 + result;
                    result = 0;
                    tmp = 0;
                } else if (tmpNum == 10000) {
                    result += tmp;
                    result *= tmpNum;
                    tmp = 0;
                } else if (tmpNum >= 10) {
                    if (tmp == 0)
                        tmp = 1;
                    result += tmpNum * tmp;
                    tmp = 0;
                } else {
                    if (i >= 2 && i == cn.length - 1 && ChnMap.get(cn[i - 1]) > 10)
                        tmp = tmpNum * ChnMap.get(cn[i - 1]) / 10;
                    else
                        tmp = tmp * 10 + tmpNum;
                }
            }
            result += tmp + billion;
            return result;
        } catch (Exception e) {
            return -1;
        }
    }

    public static int stringToInt(String str) {
        if (str != null) {
            String num = fullToHalf(str).replaceAll("\\s", "");
            try {
                return Integer.parseInt(num);
            } catch (Exception e) {
                return chineseNumToInt(num);
            }
        }
        return -1;
    }

    public static String repeat(String str, int n) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    public static boolean isCompressJsonType(String str) {
        if (!TextUtils.isEmpty(str)) {
            if (str.replaceAll("(\\s|\n)*", "").matches("^\\{.*[^}]$")) {
                return true;
            }
        }
        return false;
    }

    public static String unCompressJson(String str) {
        if (TextUtils.isEmpty(str))
            return "";
        // 如果是未压缩的json
        if (str.replaceAll("(\\s|\n)*", "").matches("^\\{.*\\}$"))
            return str;
//        if (str.replaceAll("(\\s|\n)*","").matches("^\\{.*[^}]$"))
        String string = null;
        str = str.trim();
        try {
            if (str.charAt(0) == '{')
                string = unzipString(str.substring(1));
            else
                string = unzipString(str);
            if (string.charAt(string.length() - 1) == '}')
                return "{" + string;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static boolean isJsonObject(String text) {
        boolean result = false;
        if (!TextUtils.isEmpty(text)) {
            text = text.trim();
            if (text.startsWith("{") && text.endsWith("}")) {
                result = true;
            }
        }
        return result;
    }

    public static boolean isJsonType(String str) {
        boolean result = false;
        if (!TextUtils.isEmpty(str)) {
            str = str.trim();
            if (str.startsWith("{") && str.endsWith("}")) {
                result = true;
            } else if (str.startsWith("[") && str.endsWith("]")) {
                result = true;
            }
        }
        return result;
    }

    public static boolean isJsonArray(String str) {
        boolean result = false;
        if (!TextUtils.isEmpty(str)) {
            str = str.trim();
            if (str.startsWith("[") && str.endsWith("]")) {
                result = true;
            }
        }
        return result;
    }

    public static boolean isContainEachOther(String s1, String s2) {
        if (s1 == null || s2 == null) return true;
        s1 = s1.trim();
        s2 = s2.trim();
        return s1.contains(s2) || s2.contains(s1);
    }

    public static boolean isEqual(String s1, String s2) {
        if (s1 == null || s2 == null) return true;
        s1 = s1.trim();
        s2 = s2.trim();
        return s1.equals(s2);
    }

    /**
     * 取两个文本之间的文本值
     *
     * @param text  源文本 比如：欲取全文本为 12345
     * @param left  文本前面
     * @param right 后面文本
     * @return 返回 String
     */
    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }

    public static String getMSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.lastIndexOf(right);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }

    public static boolean startWithIgnoreCase(String src, String obj) {
        if (src == null || obj == null) return false;
        if (obj.length() > src.length()) return false;
        return src.substring(0, obj.length()).equalsIgnoreCase(obj);
    }

    public static boolean endWithIgnoreCase(String src, String obj) {
        if (src == null || obj == null) return false;
        if (obj.length() > src.length()) return false;
        return src.substring(src.length() - obj.length()).equalsIgnoreCase(obj);
    }

    public static String fromHtml(String content) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            content = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            content = Html.fromHtml(content).toString();
        }
        return content;
    }

    public static String formatHtml(String html) {
        if (TextUtils.isEmpty(html)) return "";
        return html.replaceAll("(?i)<(br[\\s/]*|/?p[^>]*|/?div[^>]*)>", "\n")// 替换特定标签为换行符
                //.replaceAll("<(script[^>]*>)?[^>]*>|&nbsp;", "")// 删除script标签对和空格转义符
                .replaceAll("</?[a-zA-Z][^>]*>", "")// 删除标签对
                .replaceAll("\\s*\\n+\\s*", "\n　　")// 移除空行,并增加段前缩进2个汉字
                .replaceAll("^[\\n\\s]+", "　　")//移除开头空行,并增加段前缩进2个汉字
                .replaceAll("[\\n\\s]+$", "");//移除尾部空行
    }

    public static String formatHtml2Intor(String html) {
        if (TextUtils.isEmpty(html)) return "";
        return "　　"
                + html.replaceAll("(?i)<(br[\\s/]*|/?p[^>]*|/?div[^>]*)>", "\n")// 替换特定标签为换行符
                .replaceAll("</?[a-zA-Z][^>]*>", "")// 删除标签对
                .replaceAll("\\s*\\n+\\s*", "\n　　")// 移除空行,并增加段前缩进2个汉字
                .trim();
    }

    /**
     * 压缩
     */
    public static String zipString(String unzipString) {
        /*
         *     https://www.yiibai.com/javazip/javazip_deflater.html#article-start
         *     0 ~ 9 压缩等级 低到高
         *     public static final int BEST_COMPRESSION = 9;            最佳压缩的压缩级别。
         *     public static final int BEST_SPEED = 1;                  压缩级别最快的压缩。
         *     public static final int DEFAULT_COMPRESSION = -1;        默认压缩级别。
         *     public static final int DEFAULT_STRATEGY = 0;            默认压缩策略。
         *     public static final int DEFLATED = 8;                    压缩算法的压缩方法(目前唯一支持的压缩方法)。
         *     public static final int FILTERED = 1;                    压缩策略最适用于大部分数值较小且数据分布随机分布的数据。
         *     public static final int FULL_FLUSH = 3;                  压缩刷新模式，用于清除所有待处理的输出并重置拆卸器。
         *     public static final int HUFFMAN_ONLY = 2;                仅用于霍夫曼编码的压缩策略。
         *     public static final int NO_COMPRESSION = 0;              不压缩的压缩级别。
         *     public static final int NO_FLUSH = 0;                    用于实现最佳压缩结果的压缩刷新模式。
         *     public static final int SYNC_FLUSH = 2;                  用于清除所有未决输出的压缩刷新模式; 可能会降低某些压缩算法的压缩率。
         */

        try {
            //使用指定的压缩级别创建一个新的压缩器。
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            //设置压缩输入数据。
            deflater.setInput(unzipString.getBytes(StandardCharsets.UTF_8));
            //当被调用时，表示压缩应该以输入缓冲区的当前内容结束。
            deflater.finish();

            final byte[] bytes = new byte[512];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);

            while (!deflater.finished()) {
                //压缩输入数据并用压缩数据填充指定的缓冲区。
                int length = deflater.deflate(bytes);
                outputStream.write(bytes, 0, length);
            }
            //关闭压缩器并丢弃任何未处理的输入。
            deflater.end();
            String zipString = new String(Base64.encode(outputStream.toByteArray(), Base64.DEFAULT), StandardCharsets.UTF_8);

            Log.d("zipString()压缩比", "char:" + zipString.length() + "/" + unzipString.length() + "=" + zipString.length() / (float) (unzipString.length()) +
                    "\tbyte:" + zipString.getBytes("utf-8").length + "/" + unzipString.getBytes("utf-8").length
                    + "=" + zipString.getBytes("UTF-8").length / (float) unzipString.getBytes("utf-8").length);
            return zipString.trim();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
        //处理回车符
//        return zipString.replaceAll("[\r\n]", "");
    }

    /**
     * 解压缩
     */
    public static String unzipString(String zipString) {
        byte[] decode //= Base64.decodeBase64(zipString);
                = Base64.decode(zipString, Base64.DEFAULT);
        //创建一个新的解压缩器  https://www.yiibai.com/javazip/javazip_inflater.html

        Inflater inflater = new Inflater();
        //设置解压缩的输入数据。
        inflater.setInput(decode);
        final byte[] bytes = new byte[512];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);
        try {
            //finished() 如果已到达压缩数据流的末尾，则返回true。
            while (!inflater.finished()) {
                //将字节解压缩到指定的缓冲区中。
                int length = inflater.inflate(bytes);
                outputStream.write(bytes, 0, length);
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
            return null;
        } finally {
            //关闭解压缩器并丢弃任何未处理的输入。
            inflater.end();
        }

        try {
            return outputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String base64Decode(String str) {
        byte[] bytes = Base64.decode(str, Base64.DEFAULT);
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new String(bytes);
        }
    }

    public static String escape(String src) {
        int i;
        char j;
        StringBuilder tmp = new StringBuilder();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j)
                    || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static String getBaseUrl(String url) {
        if (url == null || !url.startsWith("http")) return null;
        int index = url.indexOf("/", 9);
        if (index == -1) {
            return url;
        }
        return url.substring(0, index);
    }

    // 移除字符串首尾空字符的高效方法(利用ASCII值判断,包括全角空格)
    public static String trim(String s) {
        if (isEmpty(s)) return "";
        int start = 0, len = s.length();
        int end = len - 1;
        while ((start < end) && ((s.charAt(start) <= 0x20) || (s.charAt(start) == '　'))) {
            ++start;
        }
        while ((start < end) && ((s.charAt(end) <= 0x20) || (s.charAt(end) == '　'))) {
            --end;
        }
        if (end < len) ++end;
        return ((start > 0) || (end < len)) ? s.substring(start, end) : s;
    }

    public static String byteToHexString(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int hex = 0xff & b;
            if (hex < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(hex));
        }
        return sb.toString();
    }

    public static byte[] hexStringToByte(String hexString) {
        String hexStr = hexString.replace(" ", "");
        int len = hexStr.length();
        byte[] bytes = new byte[len / 2];
        int i = 0;
        while (i < len) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) +
                    Character.digit(hexString.charAt(i+1), 16));
            i += 2;
        }
        return bytes;
    }

    /**
     * 对字符串处理:将指定位置到指定位置的字符以星号代替
     *
     * @param content 传入的字符串
     * @param begin 开始位置
     * @param end 结束位置
     * @return
     */
    public static String getStarString(String content, int begin, int end) {
        if (begin >= content.length() || begin < 0) {
            return content;
        }
        if (end >= content.length() || end < 0) {
            return content;
        }
        if (begin >= end) {
            return content;
        }
        StringBuilder starStr = new StringBuilder();
        for (int i = begin; i < end; i++) {
            starStr.append("*");
        }
        return content.substring(0, begin) + starStr + content.substring(end, content.length());
    }

    /**
     * 对字符加星号处理：除前面几位和后面几位外，其他的字符以星号代替
     *
     * @param content 传入的字符串
     * @param frontNum 保留前面字符的位数
     * @param endNum 保留后面字符的位数
     * @return 带星号的字符串
     */
    public static String getStarString2(String content, int frontNum, int endNum) {
        if (frontNum >= content.length() || frontNum < 0) {
            return content;
        }
        if (endNum >= content.length() || endNum < 0) {
            return content;
        }
        if (frontNum + endNum >= content.length()) {
            return content;
        }
        StringBuilder starStr = new StringBuilder();
        for (int i = 0; i < (content.length() - frontNum - endNum); i++) {
            starStr.append("*");
        }
        return content.substring(0, frontNum) + starStr
                + content.substring(content.length() - endNum, content.length());
    }

}
