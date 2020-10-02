package xyz.fycz.myreader.enums;

/**
 * 小说源
 */

public enum BookSource {

    tianlai("天籁小说"),
    fynovel("风月小说"),
    biquge44("笔趣阁44"),
    pinshu("品书网"),
    biquge("笔趣阁"),
    qb5("全本小说"),
    miqu("米趣小说"),
    jiutao("九桃小说"),
    yunzhong("云中书库"),
    sonovel("搜小说网"),
    quannovel("全小说网"),
    qiqi("奇奇小说"),
    chaoxing("超星图书·实体"),
    zuopin("作品集·实体"),
    cangshu99("99藏书·实体"),
    ben100("100本·实体"),
    local("本地书籍");
    public String text;

    BookSource(String text) {
        this.text = text;
    }

    public static String getFromName(String name){
        for (BookSource bookSource : BookSource.values()){
            if (bookSource.text.equals(name)){
                return bookSource.toString();
            }
        }
        return null;
    }

    public static BookSource get(int var0) {
        return values()[var0];
    }

    public static BookSource fromString(String string) {
        return valueOf(string);
    }

}
