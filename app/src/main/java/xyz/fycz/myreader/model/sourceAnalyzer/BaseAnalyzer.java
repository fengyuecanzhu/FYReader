package xyz.fycz.myreader.model.sourceAnalyzer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.StringUtils;

/**
 * @author fengyue
 * @date 2021/2/14 18:51
 */
public abstract class BaseAnalyzer {

    public abstract List<String> getStringList(String rule, Object obj, boolean first);

    public List<String> getStringList(String rule, Object obj) {
        return getStringList(rule, obj, false);
    }

    public String getString(String rule, Object obj) {
        if (StringHelper.isEmpty(rule)) return "";
        List<String> list = getStringList(rule, obj, true);
        if (list.size() == 0) return "";
        return list.get(0);
    }

    /**
     * 执行函数
     * 1、@r/@replace(oldStr, newStr)
     * 2、@a/@append(str1+str2+str3)
     * 3、@e/equal(str1,str2) 如：@e(<text>, novel)
     * 4、@c/contains(str) 必须包含
     * 5、@nc/notContains(str) 必须不包含
     *
     * @param funs
     * @param content
     * @return
     */
    protected String evalFunction(String funs, String content) {
        boolean hasSemicolon = funs.contains("\\;");
        if (hasSemicolon) funs = funs.replace("\\;", "\\；");
        if (funs.endsWith(";")) funs = funs.substring(0, funs.length() - 1);
        String[] funArr = funs.split(";");
        for (String fun : funArr) {
            if (hasSemicolon) fun = fun.replace("\\；", ";");
            if (fun.contains("@e")) {
                content = String.valueOf(evalEqual(fun, content));
            } else if (fun.contains("@r")) {
                content = evalReplace(fun, content);
            } else if (fun.contains("@a")) {
                content = evalAppend(fun, content);
            } else if (fun.contains("@c")) {
                content = evalContains(fun, content);
            } else if (fun.contains("@nc")) {
                content = evalNotContains(fun, content);
            }
        }
        return content;
    }

    /**
     * 执行替换函数
     *
     * @param rule
     * @param content
     * @return
     */
    protected String evalReplace(String rule, String content) {
        rule = rule.trim();
        if (!rule.matches("@(r|replace)\\(.+,.*\\)")) {
            Log.d("evalReplace", "格式错误");
            return content;
        }
        rule = rule.replace("(*)", ".*");
        rule = rule.replace("\\,", "\\，");
        String oldStr = null;
        String newStr = null;
        try {
            oldStr = StringUtils.getSubString(rule, "(", ",")
                    .replace("\\，", ",");
            newStr = StringUtils.getSubString(rule, ",", ")")
                    .replace("\\，", ",");
            Log.d("evalReplace", "执行成功");
        } catch (Exception e) {
            e.printStackTrace();
            return content;
        }
        return content.replaceAll(oldStr, newStr);
    }

    /**
     * 执行添加函数
     *
     * @param rule
     * @param content
     * @return
     */
    protected String evalAppend(String rule, String content) {
        rule = rule.trim();
        if (!rule.matches("@(a|append)\\(.+\\)")) {
            Log.d("evalAppend", "格式错误");
            return content;
        }
        StringBuilder reVal = null;
        try {
            rule = StringUtils.getMSubString(rule, "(", ")");
            rule = rule.replace("\\+", "\\plus");
            String[] strs = rule.split("\\+");
            reVal = new StringBuilder();
            for (String str : strs) {
                str = str.replace("\\plus", "+");
                if (str.matches("<(html|text)>")) {
                    reVal.append(content);
                } else {
                    reVal.append(str);
                }
            }
            Log.d("evalAppend", "执行成功");
        } catch (Exception e) {
            e.printStackTrace();
            return content;
        }
        return reVal.toString();
    }


    /**
     * 执行判断是否相等
     *
     * @param rule
     * @return
     */
    protected boolean evalEqual(String rule, String content) {
        rule = rule.trim();
        if (!rule.matches("@(e|equal)\\(.*,.*\\)")) {
            Log.d("evalEqual", "格式错误");
            return false;
        }
        String lStr = null;
        String rStr = null;
        try {
            lStr = StringUtils.getSubString(rule, "(", ",");
            rStr = StringUtils.getSubString(rule, ",", ")");
            if (lStr.matches("<(html|text)>")) {
                lStr = content;
            }
            if (rStr.matches("<(html|text)>")) {
                rStr = content;
            }
            Log.d("evalEqual", "执行成功");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return lStr != null && lStr.equals(rStr);
    }

    /**
     * 执行是否包含函数
     *
     * @param rule
     * @param content
     * @return
     */
    protected String evalContains(String rule, String content) {
        rule = rule.trim();
        if (!rule.matches("@(c|contains)\\(.+\\)")) {
            Log.d("evalContains", "格式错误");
            return content;
        }
        try {
            rule = StringUtils.getMSubString(rule, "(", ")");
            if (!content.contains(rule)) {
                content = "";
            }
            Log.d("evalContains", "执行成功");
        } catch (Exception e) {
            e.printStackTrace();
            return content;
        }
        return content;
    }

    /**
     * 执行是否不包含函数
     *
     * @param rule
     * @param content
     * @return
     */
    protected String evalNotContains(String rule, String content) {
        rule = rule.trim();
        if (!rule.matches("@(nc|notContains)\\(.+\\)")) {
            Log.d("evalNotContains", "格式错误");
            return content;
        }
        try {
            rule = StringUtils.getMSubString(rule, "(", ")");
            if (content.contains(rule)) {
                content = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return content;
        }
        Log.d("evalNotContains", "执行成功");
        return content;
    }

    /**
     * 执行跳过函数
     *
     * @param rule
     * @param content
     * @return
     */
    private String evalSkip(String rule, String content, int index) {
        rule = rule.trim();
        if (!rule.matches("@(s|skip)\\([0-9]+\\)") && !rule.matches("!([0-9]+)")) {
            Log.d("evalSkip", "格式错误");
            return content;
        }
        if (rule.matches("@(s|skip)\\([0-9]+\\)")) {
            rule = StringUtils.getMSubString(rule, "(", ")");
        } else if (rule.matches("!([0-9]+)")) {
            rule = rule.substring(1);
        }
        int skip = 0;
        try {
            skip = Integer.parseInt(rule);
            Log.d("evalSkip", "执行成功");
            if (index < skip) return "";
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 执行列表(特殊)函数
     * 1、!n：跳过列表前n个
     * 2、%n：以n为分组长度进行分组，并对各组进行反转
     * @param funs
     * @param list
     * @return
     */
    protected List evalListFunction(String funs, List list) {
        if (funs.endsWith(";")) funs = funs.substring(0, funs.length() - 1);
        String[] funArr = funs.split(";");
        for (String fun : funArr) {
            if (fun.contains("!")) {
                list = evalListSkip(fun, list);
            } else if (fun.contains("%")) {
                list = evalListGroupReverse(fun, list);
            }
        }
        return new ArrayList(list);
    }

    /**
     * 执行列表跳过函数
     * @param rule
     * @param list
     * @return
     */
    protected List evalListSkip(String rule, List list) {
        int skip = 0;
        if (rule.matches("!([0-9]+)")) {
            rule = rule.substring(1);
        }
        try {
            skip = Integer.parseInt(rule);
            Log.d("evalListSkip", "执行成功");
        } catch (Exception ignored) {
        }
        if (skip > list.size()) skip = 0;
        return list.subList(skip, list.size());
    }

    /**
     * 执行列表分组反转函数
     * @param rule
     * @param list
     * @return
     */
    protected List evalListGroupReverse(String rule, List list){
        int groupSize = 0;
        if (rule.matches("%([0-9]+)")) {
            rule = rule.substring(1);
        }
        try {
            groupSize = Integer.parseInt(rule);
            Log.d("evalListGroupReverse", "执行成功");
        } catch (Exception ignored) {
        }
        if (groupSize < 2) return list;
        List reverseList = new ArrayList();
        int groupNum = list.size() / groupSize;
        if (list.size() % groupSize != 0) groupNum++;
        for (int i = 0; i < groupNum; i++) {
            int start = i * groupSize;
            int end = (i + 1) * groupSize;
            if (end > list.size()) end = list.size();
            List subList = list.subList(start, end);
            Collections.reverse(subList);
            reverseList.addAll(subList);
        }
        return reverseList;
    }

    public static String getCharset(String charset) {
        if (StringHelper.isEmpty(charset)) {
            charset = "UTF-8";
        }
        return charset;
    }
}
