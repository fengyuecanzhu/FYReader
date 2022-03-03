package xyz.fycz.myreader.entity.ad;

import androidx.annotation.NonNull;

import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

/**
 * @author fengyue
 * @date 2022/2/17 18:56
 */
public class AdConfig {
    //是否云控
    private boolean isCloud;
    //是否有广告
    private boolean hasAd;
    //配置过期时间
    private int expireTime;
    //应用处于后台一段时间展示开屏广告(单位：分钟)
    private int backAdTime;
    //应用回到前台展示广告间隔(单位：分钟)
    private int intervalAdTime;
    //单次激励视频可去广告时间(单位：小时)，为0时表示关闭去广告
    private int removeAdTime;
    //每日最大去除广告次数
    private int maxRemove;
    //累计最高去除时间(单位：小时)
    private int totalRemove;
    //详情页广告配置
    private AdBean detail;
    //搜索页广告配置
    private AdBean search;

    public AdConfig() {
    }

    public AdConfig(boolean hasAd, int expireTime, int backAdTime, int intervalAdTime, int removeAdTime, int maxRemove, int totalRemove) {
        this.hasAd = hasAd;
        this.expireTime = expireTime;
        this.backAdTime = backAdTime;
        this.intervalAdTime = intervalAdTime;
        this.removeAdTime = removeAdTime;
        this.maxRemove = maxRemove;
        this.totalRemove = totalRemove;
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

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public int getRemoveAdTime() {
        return removeAdTime;
    }

    public void setRemoveAdTime(int removeAdTime) {
        this.removeAdTime = removeAdTime;
    }

    public int getMaxRemove() {
        return maxRemove;
    }

    public void setMaxRemove(int maxRemove) {
        this.maxRemove = maxRemove;
    }

    public int getTotalRemove() {
        return totalRemove;
    }

    public void setTotalRemove(int totalRemove) {
        this.totalRemove = totalRemove;
    }

    public AdBean getDetail() {
        if (detail == null) {
            detail = new AdBean(2, 60);
        }
        return detail;
    }

    public void setDetail(AdBean detail) {
        this.detail = detail;
    }

    public AdBean getSearch() {
        if (search == null) {
            search = new AdBean(0, 60);
        }
        return search;
    }

    public void setSearch(AdBean search) {
        this.search = search;
    }

    @NonNull
    @Override
    public String toString() {
        return GsonExtensionsKt.getGSON().toJson(this);
    }
}
