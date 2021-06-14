package xyz.fycz.myreader.widget.page;

import java.util.List;

public class TxtPage {
    int position;
    String title;
    int titleLines; //当前 lines 中为 title 的行数。
    List<String> lines;
    List<TxtLine> txtLists;

    public TxtPage(int position) {
        this.position = position;
    }

    public TxtPage() {
    }

    public int getTitleLines() {
        return titleLines;
    }

    public int size() {
        return lines.size();
    }

    public String getLine(int i) {
        return lines.get(i);
    }

    public String getContent() {
        StringBuilder s = new StringBuilder();
        if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                s.append(lines.get(i));
            }
        }
        return s.toString();
    }

}
