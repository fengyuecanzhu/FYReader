package xyz.fycz.myreader.entity;

import java.util.Objects;

/**
 * @author fengyue
 * @date 2020/5/19 9:19
 */
public class SearchBookBean {
    private String name;//书名
    private String author;//作者
    private int sourceCount;//书源数量

    public SearchBookBean() {
    }

    public SearchBookBean(String name, String author) {
        this.name = name;
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public void setSourceCount(int sourceCount) {
        this.sourceCount = sourceCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchBookBean that = (SearchBookBean) o;
        if (author == null){
            return name.equals(that.name);
        }
        if (name == null) return false;
        return name.equals(that.name) &&
                author.equals(that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, author);
    }
}
