package xyz.fycz.myreader.widget.page;

/**
 * Created by newbiechen on 2018/2/5.
 * 作用：翻页动画的模式
 */

public enum PageMode {
    SIMULATION, COVER, SLIDE, NONE, SCROLL, VERTICAL_COVER, AUTO;

    public static PageMode fromString(String string) {
        return valueOf(string);
    }
}
