package xyz.fycz.myreader.enums;

import java.io.Serializable;

/**
 * 小说源
 * Created by zhao on 2020/04/13.
 */

public enum FindType implements Serializable {

    qidian("排行榜[起点中文网]", "https://www.qidian.com/rank"),
    qidianns("排行榜[起点女生网]", "https://www.qidian.com/mm/rank"),
    qb5("书城[全本小说]", "https://www.qb5.tw"),
    biquge("书城[笔趣阁]", "https://www.52bqg.com");
    private String text;
    private String url;
    private static final long serialVersionUID = 1L;

    FindType(String text, String url) {
        this.text = text;
        this.url = url;
    }

    public static FindType get(int var0) {
        return values()[var0];
    }

    public static FindType fromString(String string) {
        return valueOf(string);
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }
}
