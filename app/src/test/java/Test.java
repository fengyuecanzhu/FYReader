import org.seimicrawler.xpath.JXDocument;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.model.sourceAnalyzer.BaseAnalyzer;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.MD5Utils;
import xyz.fycz.myreader.util.utils.NetworkUtils;

/**
 * @author fengyue
 * @date 2020/11/28 21:57
 */
public class Test {
    @org.junit.Test
    public void test() {
        String s = "tianlai(\"天籁小说\")," +
                "biquge44(\"笔趣阁44\")," +
                "pinshu(\"品书网\")," +
                "biquge(\"笔趣阁\")," +
                "qb5(\"全本小说\")," +
                "miqu(\"米趣小说\")," +
                "jiutao(\"九桃小说\")," +
                "miaobi(\"妙笔阁\")," +
                "dstq(\"丹书铁券\")," +
                "yunzhong(\"云中书库\")," +
                "sonovel(\"搜小说网\")," +
                "quannovel(\"全小说网\")," +
                "qiqi(\"奇奇小说\")," +
                "xs7(\"小说旗\")," +
                "du1du(\"读一读网\")," +
                "paiotian(\"飘天文学\")," +
                "chaoxing(\"超星图书·实体\")," +
                "zuopin(\"作品集·实体\")," +
                "cangshu99(\"99藏书·实体\")," +
                "ben100(\"100本·实体\")";
        String[] ss = s.split(",");
        StringBuilder sb = new StringBuilder();
        for (String s1 : ss) {
            sb.append(s1.substring(0, s1.indexOf("(")));
            sb.append("(App.getApplication().getString(R.string.read_");
            sb.append(s1.substring(0, s1.indexOf("(")));
            sb.append(")),\n");
        }
        System.out.println(sb.toString());
    }


    @org.junit.Test
    public void testUrl() {
        String baseUrl = "https://novel.fycz.xyz/";
        String url = "https://novel.fycz.xyz/29/29787/101469886.html";
        System.out.println(NetworkUtils.getAbsoluteURL(baseUrl, url));
    }

    @org.junit.Test
    public void testXpath() throws UnsupportedEncodingException {
        String str = new String(FileUtils.getBytes(new File("D:\\Java\\Project\\FYReader-master\\app\\src\\test\\resources\\html.html")), "GBK");
        JXDocument jxDoc = JXDocument.create(str);
        System.out.println(jxDoc.selNOne("//*[@id=\"intro\"]/p[1]/text()"));
    }

    @org.junit.Test
    public void testBean() {
        String[] strings = "type;desc;status;wordCount;lastChapter;updateTime;imgUrl".split(";");
        String[] strings1 = new String[strings.length];
        for (int i = 0, stringsLength = strings.length; i < stringsLength; i++) {
            String s = strings[i];
            strings1[i] = s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];
            /*String str =
                    "        for (Book book : books){\n" +
                    "            String "+ s + " = book.get" + strings1[i] + "();\n" +
                    "            if (!StringHelper.isEmpty(bookBean.get" + strings1[i] + "())) break;\n" +
                    "            if (!StringHelper.isEmpty("+ s + ")){\n" +
                    "                bookBean.set" + strings1[i] + "("+ s + ");\n" +
                    "                break;\n" +
                    "            }\n" +
                    "        }";*/
            String str =
                    "            if (StringHelper.isEmpty(book.get" + strings1[i] + "()) && !StringHelper.isEmpty(bean.get" + strings1[i] + "()))\n" +
                            "            book.set" + strings1[i] + "(bean.get" + strings1[i] + "());";
            System.out.println(str);
        }
    }

    @org.junit.Test
    public void testReverse() {
        List<String> list = new ArrayList<>();
        list.add("3");
        list.add("2");
        list.add("1");
        list.add("6");
        list.add("5");
        list.add("4");
        list.add("9");
        list.add("8");
        list.add("7");
        list.add("11");
        list.add("10");
        BaseAnalyzer analyzer = new BaseAnalyzer() {
            @Override
            public List<String> getStringList(String rule, Object obj, boolean first) {
                return null;
            }
        };
        //List<String> newList = analyzer.evalListFunction("%3;", list);
        //for (String s : newList) {
        //    System.out.println(s);
        //}
    }

    @org.junit.Test
    public void md5(){
        File file = new File("D:\\Java\\AndroidSdk\\build-tools\\29.0.3\\dynamic_v1.0.2.dex");
        System.out.println(MD5Utils.INSTANCE.getFileMD5s(file, 32));
    }
}
