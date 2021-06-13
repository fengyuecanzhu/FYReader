package xyz.fycz.myreader.entity;

/**
 * @author fengyue
 * @date 2021/6/12 20:55
 */
public class Quotation {
    private String hitokoto;
    private String from;

    public String getHitokoto() {
        return hitokoto;
    }

    public void setHitokoto(String hitokoto) {
        this.hitokoto = hitokoto;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return hitokoto + "   --- " + from;
    }
}
