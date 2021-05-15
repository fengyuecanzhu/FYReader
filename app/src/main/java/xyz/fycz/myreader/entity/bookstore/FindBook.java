package xyz.fycz.myreader.entity.bookstore;

/**
 * @author fengyue
 * @date 2021/5/15 15:46
 */
public class FindBook {
    private String name;
    private String author;
    private String imgUrl;
    private String source;
    private String center;
    private String bottomLeft;
    private String bottomRight;

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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getBottomLeft() {
        return bottomLeft;
    }

    public void setBottomLeft(String bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public String getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(String bottomRight) {
        this.bottomRight = bottomRight;
    }
}
