package xyz.fycz.myreader.enums;

/**
 * @author fengyue
 * @date 2020/4/23 11:02
 */
public enum BookcaseStyle {
    listMode,//列表模式
    threePalaceMode;//三列宫格模式

    BookcaseStyle() {
    }
    public static BookcaseStyle get(int var0) {
        return values()[var0];
    }

    public static BookcaseStyle fromString(String string) {
        return BookcaseStyle.valueOf(string);
    }
}
