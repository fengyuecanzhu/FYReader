/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.entity.ad;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

/**
 * @author fengyue
 * @date 2022/2/17 18:56
 */
public class AdConfig {
    //是否云控
    private boolean isCloud;
    //是否有广告（总开关）
    private boolean hasAd;
    //是否有广告（用户）
    private boolean userHasAd;
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
    //获取订阅书源是否需要看广告
    private boolean subSource;
    //详情页广告配置
    private AdBean detail;
    //搜索页广告配置
    private AdBean search;
    //阅读页广告配置
    private AdBean read;
    //发现页广告配置
    private AdBean find;

    public AdConfig() {
    }

    public AdConfig(boolean isCloud, boolean hasAd, boolean userHasAd, int expireTime, int backAdTime, int intervalAdTime, int removeAdTime, int maxRemove, int totalRemove, boolean subSource) {
        this.isCloud = isCloud;
        this.hasAd = hasAd;
        this.userHasAd = userHasAd;
        this.expireTime = expireTime;
        this.backAdTime = backAdTime;
        this.intervalAdTime = intervalAdTime;
        this.removeAdTime = removeAdTime;
        this.maxRemove = maxRemove;
        this.totalRemove = totalRemove;
        this.subSource = subSource;
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

    public AdBean getRead() {
        if (read == null) {
            read = new AdBean(0, 60);
        }
        return read;
    }

    public void setRead(AdBean read) {
        this.read = read;
    }

    public AdBean getFind() {
        if (find == null) {
            find = new AdBean(0, 60);
        }
        return find;
    }

    public void setFind(AdBean find) {
        this.find = find;
    }

    public boolean isSubSource() {
        return subSource;
    }

    public void setSubSource(boolean subSource) {
        this.subSource = subSource;
    }

    public boolean isUserHasAd() {
        return userHasAd;
    }

    public void setUserHasAd(boolean userHasAd) {
        this.userHasAd = userHasAd;
    }

    @NonNull
    @Override
    public String toString() {
        return GsonExtensionsKt.getGSON().toJson(this);
    }
}
