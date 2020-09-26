package xyz.fycz.myreader.entity.bookstore;

/**
 * @author fengyue
 * @date 2020/9/18 20:46
 */
public class QDBook {
    protected String bid;
    protected String bName;
    protected String bAuth;
    protected String img;
    protected String cat;
    protected int catId;
    protected String cnt;
    protected String desc;

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getbName() {
        return bName;
    }

    public void setbName(String bName) {
        this.bName = bName;
    }

    public String getbAuth() {
        return bAuth;
    }

    public void setbAuth(String bAuth) {
        this.bAuth = bAuth;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    public String getCnt() {
        return cnt;
    }

    public void setCnt(String cnt) {
        this.cnt = cnt;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
