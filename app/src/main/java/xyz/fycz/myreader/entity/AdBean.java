package xyz.fycz.myreader.entity;

/**
 * @author fengyue
 * @date 2022/2/17 18:56
 */
public class AdBean {
    //是否云控
    private boolean isCloud;
    //是否有广告
    private boolean hasAd;
    //应用处于后台一段时间展示开屏广告(单位：分钟)
    private int backAdTime;
    //应用回到前台展示广告间隔(单位：分钟)
    private int intervalAdTime;
    //是否在详情页展示信息流广告
    private boolean showFlowAd;

    public AdBean() {
    }

    public AdBean(boolean hasAd, int backAdTime, int intervalAdTime) {
        this.hasAd = hasAd;
        this.backAdTime = backAdTime;
        this.intervalAdTime = intervalAdTime;
    }

    public AdBean(boolean hasAd, int backAdTime, int intervalAdTime, boolean showFlowAd) {
        this.hasAd = hasAd;
        this.backAdTime = backAdTime;
        this.intervalAdTime = intervalAdTime;
        this.showFlowAd = showFlowAd;
    }

    public boolean isCloud() {
        return isCloud;
    }

    public void setCloud(boolean cloud) {
        isCloud = cloud;
    }

    public boolean isHasAd() {
        return hasAd;
    }

    public void setHasAd(boolean hasAd) {
        this.hasAd = hasAd;
    }

    public int getBackAdTime() {
        return backAdTime;
    }

    public void setBackAdTime(int backAdTime) {
        this.backAdTime = backAdTime;
    }

    public int getIntervalAdTime() {
        return intervalAdTime;
    }

    public void setIntervalAdTime(int intervalAdTime) {
        this.intervalAdTime = intervalAdTime;
    }

    public boolean isShowFlowAd() {
        return showFlowAd;
    }

    public void setShowFlowAd(boolean showFlowAd) {
        this.showFlowAd = showFlowAd;
    }
}
