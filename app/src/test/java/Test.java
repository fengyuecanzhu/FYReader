/**
 * @author fengyue
 * @date 2020/11/28 21:57
 */
public class Test {
    @org.junit.Test
    public void test(){
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
        for (String s1 : ss){
            sb.append(s1.substring(0, s1.indexOf("(")));
            sb.append("(MyApplication.getApplication().getString(R.string.read_");
            sb.append(s1.substring(0, s1.indexOf("(")));
            sb.append(")),\n");
        }
        System.out.println(sb.toString());
    }
}
