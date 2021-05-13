package xyz.fycz.myreader.enums;

public enum Font {

    默认字体( "默认字体"),
    本地字体( "默认字体"),
    方正楷体( "https://fycz.lanzousi.com/ilLFMe6kefe"),
    方正行楷( "https://fycz.lanzousi.com/imFvne6keji"),
    经典宋体("https://fycz.lanzousi.com/idhI5e6keqf"),
    方正硬笔行书("https://fycz.lanzousi.com/ilVh6ep9xja"),
    包图小白体("https://fycz.lanzoui.com/i5qgAicrirc"),
    仓耳非白W02("https://fycz.lanzoui.com/iHwRnicriuf"),
    仓耳舒圆体W02("https://fycz.lanzoui.com/i3GVPicrj3e"),
    仓耳与墨W02("https://fycz.lanzoui.com/ivhv9icrj7i"),
    方正仿宋简体("https://fycz.lanzoui.com/iEcCHicrjef"),
    方正黑体简体("https://fycz.lanzoui.com/iw8kKicrjij"),
    方正书宋简体("https://fycz.lanzoui.com/i5976icrjmd"),
    品如手写体("https://fycz.lanzoui.com/iZccuicrjyf"),
    千图小兔体("https://fycz.lanzoui.com/iOONMicrkda"),
    手书体("https://fycz.lanzoui.com/iqbmdicrkvi"),
    演示春风楷("https://fycz.lanzoui.com/ioRJSicrldg"),
    演示秋鸿楷("https://fycz.lanzoui.com/i8qnzicrlsb"),
    演示夏行楷("https://fycz.lanzoui.com/iyYUTicrm6f"),
    演示悠然小楷("https://fycz.lanzoui.com/ikKq7icrmrg"),
    杨任东竹石体("https://fycz.lanzoui.com/iiWdVicrnbg"),
    站酷仓耳渔阳体("https://fycz.lanzoui.com/if5weicrnje"),
    迷你隶书( "https://fycz.lanzousi.com/ihaXVe6kekj"),
    方正黄草("https://fycz.lanzousi.com/iQg67e6keed");

    public String downloadPath;

    Font(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public static Font get(int var0) {
        return values()[var0];
    }

    public static Font fromString(String string) {
        return Font.valueOf(string);
    }
}
