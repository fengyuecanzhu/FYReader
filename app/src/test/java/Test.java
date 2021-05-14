import org.seimicrawler.xpath.JXDocument;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xyz.fycz.myreader.ai.MatrixUtil;
import xyz.fycz.myreader.model.sourceAnalyzer.BaseAnalyzer;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.GsonUtils;
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
    public void testMatrix() {
        double[][] m = MatrixUtil.ones(4,5);

        System.out.println(Arrays.deepToString(m));
    }

    @org.junit.Test
    public void testDownload() {
        String json = "[{\"icon\":\"apk\",\"t\":0,\"id\":\"i6b1horcnde\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.4-\\u53bb\\u66f4\\u65b0\\u3001\\u5e7f\\u544a\\u7248.apk\",\"size\":\"10.2 M\",\"time\":\"5 \\u5929\\u524d\",\"duan\":\"iorcnd\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i0Jokohm6gf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.4.apk\",\"size\":\"14.9 M\",\"time\":\"12 \\u5929\\u524d\",\"duan\":\"iohm6g\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ivyijmwfdqd\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.3.apk\",\"size\":\"10.0 M\",\"time\":\"2021-03-13\",\"duan\":\"imwfdq\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iMpIkmcv9dc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.2.apk\",\"size\":\"10.0 M\",\"time\":\"2021-03-02\",\"duan\":\"imcv9d\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ijJsnmcfwja\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.1.apk\",\"size\":\"10.0 M\",\"time\":\"2021-03-01\",\"duan\":\"imcfwj\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ixI19lqnscf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.0-beta.apk\",\"size\":\"10.0 M\",\"time\":\"2021-02-16\",\"duan\":\"ilqnsc\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iEy9kldwrpi\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.8.2.apk\",\"size\":\"9.6 M\",\"time\":\"2021-02-06\",\"duan\":\"ildwrp\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i5vu3l6pcuj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.8.1.apk\",\"size\":\"9.6 M\",\"time\":\"2021-02-01\",\"duan\":\"il6pcu\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i4pMekkx9zc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.8.0.apk\",\"size\":\"9.7 M\",\"time\":\"2021-01-19\",\"duan\":\"ikkx9z\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iu2FYkkvjyj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.7.9.apk\",\"size\":\"9.7 M\",\"time\":\"2021-01-19\",\"duan\":\"ikkvjy\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"icrU3kkqrud\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.7.9.apk\",\"size\":\"9.7 M\",\"time\":\"2021-01-19\",\"duan\":\"ikkqru\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ieSvwk7vucf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.7.8.apk\",\"size\":\"9.6 M\",\"time\":\"2021-01-09\",\"duan\":\"ik7vuc\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iGTvFk7ugmf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.7.8.apk\",\"size\":\"9.6 M\",\"time\":\"2021-01-09\",\"duan\":\"ik7ugm\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iwIrojatw9e\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.121218.apk\",\"size\":\"9.5 M\",\"time\":\"2020-12-12\",\"duan\":\"ijatw9\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i9W2Gj3mera\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.120612.apk\",\"size\":\"9.8 M\",\"time\":\"2020-12-06\",\"duan\":\"ij3mer\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iNFGZj3m4xg\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.120612.apk\",\"size\":\"9.8 M\",\"time\":\"2020-12-06\",\"duan\":\"ij3m4x\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iqvwqj3kr5e\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.120612.apk\",\"size\":\"9.8 M\",\"time\":\"2020-12-06\",\"duan\":\"ij3kr5\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ib8ZJivckmd\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112822.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-28\",\"duan\":\"iivckm\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iPzSWipk1wb\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112410.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-24\",\"duan\":\"iipk1w\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"itfplio73ob\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112309.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-23\",\"duan\":\"iio73o\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ikhkGinehwj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112216.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-22\",\"duan\":\"iinehw\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iB8Xsin8tgj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112213.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-22\",\"duan\":\"iin8tg\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iJ9qwin5qeb\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112212.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-22\",\"duan\":\"iin5qe\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iHgbpin5b3a\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112212.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-22\",\"duan\":\"iin5b3\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iowiDiecicj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111420.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-14\",\"duan\":\"iiecic\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iGu0Mibqpni\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111217.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-12\",\"duan\":\"iibqpn\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i3wzXib1eaj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111209.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-12\",\"duan\":\"iib1ea\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iVjH6ib15hc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111208.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-12\",\"duan\":\"iib15h\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iEAaUiamvub\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111121.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-11\",\"duan\":\"iiamvu\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"inaPWi6fy6j\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.110811.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-08\",\"duan\":\"ii6fy6\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iGMHPi0c7pc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.110311.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-03\",\"duan\":\"ii0c7p\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iDjPghnbxod\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.102217.apk\",\"size\":\"8.2 M\",\"time\":\"2020-10-22\",\"duan\":\"ihnbxo\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iTmLPh4t2wj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.100316.apk\",\"size\":\"7.9 M\",\"time\":\"2020-10-03\",\"duan\":\"ih4t2w\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"icVyfh4lnsd\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.100313.apk\",\"size\":\"7.9 M\",\"time\":\"2020-10-03\",\"duan\":\"ih4lns\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iBofFh42pxg\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.100220.apk\",\"size\":\"7.2 M\",\"time\":\"2020-10-02\",\"duan\":\"ih42px\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iG2tPh3akej\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.100123.apk\",\"size\":\"7.2 M\",\"time\":\"2020-10-01\",\"duan\":\"ih3ake\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iMIWSgz5eaf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.092718.apk\",\"size\":\"7.2 M\",\"time\":\"2020-09-27\",\"duan\":\"igz5ea\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"itWVmgxz3ob\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.092617.apk\",\"size\":\"7.2 M\",\"time\":\"2020-09-26\",\"duan\":\"igxz3o\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iYYzdgrtkda\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.091922.apk\",\"size\":\"7.2 M\",\"time\":\"2020-09-19\",\"duan\":\"igrtkd\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ivi6Sgrrj2b\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.091921.apk\",\"size\":\"7.2 M\",\"time\":\"2020-09-19\",\"duan\":\"igrrj2\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iviXVgfksuj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.090722.apk\",\"size\":\"7.0 M\",\"time\":\"2020-09-07\",\"duan\":\"igfksu\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i1HYEg0q8je\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.082422.apk\",\"size\":\"6.9 M\",\"time\":\"2020-08-24\",\"duan\":\"ig0q8j\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i2m6vfnvvfa\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.081523.apk\",\"size\":\"6.8 M\",\"time\":\"2020-08-15\",\"duan\":\"ifnvvf\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iUQ0Zfka1bc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.081221.apk\",\"size\":\"6.8 M\",\"time\":\"2020-08-12\",\"duan\":\"ifka1b\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"igzNbfdbc2d\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.080709.apk\",\"size\":\"6.7 M\",\"time\":\"2020-08-07\",\"duan\":\"ifdbc2\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iHXgWf67zqj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.073120.apk\",\"size\":\"6.7 M\",\"time\":\"2020-07-31\",\"duan\":\"if67zq\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iXaHUf5uyyd\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.073114.apk\",\"size\":\"6.7 M\",\"time\":\"2020-07-31\",\"duan\":\"if5uyy\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"il7HPezefjc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.072519.apk\",\"size\":\"6.7 M\",\"time\":\"2020-07-25\",\"duan\":\"iezefj\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iUmNAevkyhg\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.072221.apk\",\"size\":\"6.2 M\",\"time\":\"2020-07-22\",\"duan\":\"ievkyh\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iviG9evcn5a\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.072217.apk\",\"size\":\"6.2 M\",\"time\":\"2020-07-22\",\"duan\":\"ievcn5\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ivPOBerhale\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.072010.apk\",\"size\":\"6.2 M\",\"time\":\"2020-07-20\",\"duan\":\"ierhal\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iJYy0ep96ba\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.071809.apk\",\"size\":\"6.2 M\",\"time\":\"2020-07-18\",\"duan\":\"iep96b\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i66syem8nre\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.071509.apk\",\"size\":\"6.2 M\",\"time\":\"2020-07-15\",\"duan\":\"iem8nr\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i0YwKeel0sh\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.070809.apk\",\"size\":\"6.8 M\",\"time\":\"2020-07-08\",\"duan\":\"ieel0s\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i1IOje8vhsd\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.070219.apk\",\"size\":\"6.8 M\",\"time\":\"2020-07-02\",\"duan\":\"ie8vhs\",\"p_ico\":0}]";
        List<File1> files =  GsonUtils.parseJArray(json, File1.class);
        String basePath = "D:/风月读书";
        for (File1 file : files){

        }
    }


}
