package xyz.fycz.myreader.entity;

/**
 * @author fengyue
 * @date 2021/7/21 20:44
 */
public class FindKind {
    private String tag;
    private String name;
    private String url;
    private int maxPage;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }
}

