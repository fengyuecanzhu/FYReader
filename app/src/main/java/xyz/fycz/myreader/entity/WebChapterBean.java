package xyz.fycz.myreader.entity;

import java.util.LinkedHashSet;
import java.util.List;

import xyz.fycz.myreader.greendao.entity.Chapter;

public class WebChapterBean {
    private String url;

    private List<Chapter> data;

    private LinkedHashSet<String> nextUrlList;

    public WebChapterBean(String url) {
        this.url = url;
    }

    public WebChapterBean(List<Chapter> data, LinkedHashSet<String> nextUrlList) {
        this.data = data;
        this.nextUrlList = nextUrlList;
    }

    public List<Chapter> getData() {
        return data;
    }

    public void setData(List<Chapter> data) {
        this.data = data;
    }

    public LinkedHashSet<String> getNextUrlList() {
        return nextUrlList;
    }

    public String getUrl() {
        return url;
    }

    public boolean noData() {
        return data == null;
    }
}
