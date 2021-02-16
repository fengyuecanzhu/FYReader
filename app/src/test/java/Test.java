import org.seimicrawler.xpath.JXDocument;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

import xyz.fycz.myreader.model.source.MatcherAnalyzer;
import xyz.fycz.myreader.util.utils.FileUtils;
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
    public void testRegex() throws UnsupportedEncodingException {
        String str = new String(FileUtils.getBytes(new File("D:\\Java\\Project\\FYReader-master\\app\\src\\test\\resources\\html.html")), "GBK");
        MatcherAnalyzer analyzer = new MatcherAnalyzer();
        String ruleList = "<div id=\"content\"><text><div class=\"footer\">";
        String list = analyzer.getInnerText(ruleList, str);
        String rule = "(*)<td class=\"odd\"><a href=\".*\"><text></a></td>";

        List<String> listStr = analyzer.matcherInnerText(rule, list);
        for (String s : listStr) {
            System.out.println(s);
        }
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
}
