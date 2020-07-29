package xyz.fycz.myreader.enums;

/**
 * 小说源
 * Created by zhao on 2020/04/13.
 */

public enum BookSource {

    tianlai("天籁小说"),
    fynovel("风月小说"),
    biquge44("笔趣阁44"),
    pinshu("品书网"),
    biquge("笔趣阁"),
    local("本地书籍");
    public String text;

    BookSource(String text) {
        this.text = text;
    }

    public static BookSource get(int var0) {
        return values()[var0];
    }

    public static BookSource fromString(String string) {
        return valueOf(string);
    }

}
