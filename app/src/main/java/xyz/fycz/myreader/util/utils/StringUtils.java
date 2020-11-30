package xyz.fycz.myreader.util.utils;


import android.text.Editable;
import android.text.TextUtils;

import androidx.annotation.StringRes;
import xyz.fycz.myreader.application.MyApplication;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


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
    public static String dateConvert(long time, String pattern){
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    //将日期转换成昨天、今天、明天
    public static String dateConvert(String source, String pattern){
        DateFormat format = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = format.parse(source);
            long curTime = calendar.getTimeInMillis();
            calendar.setTime(date);
            //将MISC 转换成 sec
            long difSec = Math.abs((curTime - date.getTime())/1000);
            long difMin =  difSec/60;
            long difHour = difMin/60;
            long difDate = difHour/60;
            int oldHour = calendar.get(Calendar.HOUR);
            //如果没有时间
            if (oldHour == 0){
                //比日期:昨天今天和明天
                if (difDate == 0){
                    return "今天";
                }
                else if (difDate < DAY_OF_YESTERDAY){
                    return "昨天";
                }
                else {
                    DateFormat convertFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String value = convertFormat.format(date);
                    return value;
                }
            }

            if (difSec < TIME_UNIT){
                return difSec+"秒前";
            }
            else if (difMin < TIME_UNIT){
                return difMin+"分钟前";
            }
            else if (difHour < HOUR_OF_DAY){
                return difHour+"小时前";
            }
            else if (difDate < DAY_OF_YESTERDAY){
                return "昨天";
            }
            else {
                DateFormat convertFormat = new SimpleDateFormat("yyyy-MM-dd");
                String value = convertFormat.format(date);
                return value;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String toFirstCapital(String str){
        return str.substring(0,1).toUpperCase()+str.substring(1);
    }

    public static String getString(@StringRes int id){
        return MyApplication.getmContext().getResources().getString(id);
    }

    public static String getString(@StringRes int id, Object... formatArgs){
        return MyApplication.getmContext().getResources().getString(id,formatArgs);
    }

    /**
     * 将文本中的半角字符，转换成全角字符
     * @param input
     * @return
     */
    public static String halfToFull(String input)
    {
        char[] c = input.toCharArray();
        for (int i = 0; i< c.length; i++)
        {
            if (c[i] == 32) //半角空格
            {
                c[i] = (char) 12288;
                continue;
            }
            //根据实际情况，过滤不需要转换的符号
            //if (c[i] == 46) //半角点号，不转换
            // continue;

            if (c[i]> 32 && c[i]< 127)    //其他符号都转换为全角
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }

    //功能：字符串全角转换为半角
    public static String fullToHalf(String input)
    {
        char[] c = input.toCharArray();
        for (int i = 0; i< c.length; i++)
        {
            if (c[i] == 12288) //全角空格
            {
                c[i] = (char) 32;
                continue;
            }

            if (c[i]> 65280&& c[i]< 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }


    public static void isNotChinese(Editable s){
        //禁止输入中文
        if (s.length() > 0) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c >= 0x4e00 && c <= 0X9fff) { // 根据字节码判断
                    // 如果是中文，则清除输入的字符，否则保留
                    s.delete(i,i+1);
                }
            }
        }
    }

    /**
     * 计算两个字符串的相似度
     * @param str1
     * @param str2
     * @return
     */
    public static float levenshtein(String str1, String str2) {
        //计算两个字符串的长度。
        int len1 = str1.length();
        int len2 = str2.length();
        //建立上面说的数组，比字符长度大一个空间
        int[][]dif = new int[len1 + 1][ len2 + 1];
        //赋初值，步骤B。
        for (int a = 0; a <= len1; a++) {
            dif[a][0] =a;
        }
        for (int a = 0; a <= len2; a++) {
            dif[0][a] =a;
        }
        //计算两个字符是否一样，计算左上的值
        char [] ch1 = str1.toCharArray();
        char [] ch2 = str2.toCharArray();
        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (ch1[i - 1] == ch2[j - 1]) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                //取三个值中最小的
                int temp1 = dif[i - 1][j - 1]+temp;
                int temp2 = dif[i][j - 1]+1;
                int temp3 = dif[i - 1][j]+1;
                int arr [] =  new int[]{temp1,temp2, temp3};

                dif[i][j] =min(arr);
            }
        }
        //计算相似度
        float similarity = 1 - (float) dif[len1][len2] /Math.max(str1.length(), str2.length());
        return similarity;
    }

    //得到最小值
    private static int min(int[]arr) {
        int min = arr[0];
        for( int i :arr){
            if (min > i) {
                min = i;
            }
        }
        return min;
    }

    /**
     * 去除所有空格
     * @param str
     * @return
     */
    public static String deleteWhitespace(String str){
        return str.replaceAll("\\s*", "");
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

    public static boolean isContainEachOther(String s1, String s2){
        if (s1 == null || s2 == null) return true;
        s1 = s1.trim();
        s2 = s2.trim();
        return s1.contains(s2) || s2.contains(s1);
    }

    public static boolean isEqual(String s1, String s2){
        if (s1 == null || s2 == null) return true;
        s1 = s1.trim();
        s2 = s2.trim();
        return s1.equals(s2);
    }

}
