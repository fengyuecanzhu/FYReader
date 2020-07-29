package xyz.fycz.myreader.widget.page;

import java.util.List;

/**
 * Created by newbiechen on 17-7-1.
 */

public class TxtPage {
    int position;
    String title;
    int titleLines; //当前 lines 中为 title 的行数。
    List<String> lines;

    public int getTitleLines() {
        return titleLines;
    }

    public int size() {
        return lines.size();
    }

    public String getLine(int i) {
        return lines.get(i);
    }
}
