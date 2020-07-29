package xyz.fycz.myreader.enums;

import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.common.APPCONST;

/**
 * Created by zhao on 2016/11/3.
 */

public enum Font {

    默认字体("默认字体", "默认字体"),
    方正楷体("fangzhengkaiti.ttf", "https://fycz.lanzous.com/ilLFMe6kefe"),
    方正行楷("fangzhengxingkai.ttf", "https://fycz.lanzous.com/imFvne6keji"),
    经典宋体("songti.ttf", "https://fycz.lanzous.com/idhI5e6keqf"),
    迷你隶书("mini_lishu.ttf", "https://fycz.lanzous.com/ihaXVe6kekj"),
    方正黄草("fangzhenghuangcao.ttf", "https://fycz.lanzous.com/iQg67e6keed"),
    方正硬笔行书("fangzheng_yingbi_xingshu.ttf", "https://fycz.lanzous.com/ilVh6ep9xja"),
    本地字体("本地字体", "默认字体");

    public String fileName;

    public String downloadPath;

    Font(String fileName, String downloadPath) {
        this.fileName = fileName;
        this.downloadPath = downloadPath;
    }

    public static Font get(int var0) {
        return values()[var0];
    }

    public static Font fromString(String string) {
        return Font.valueOf(string);
    }
}
