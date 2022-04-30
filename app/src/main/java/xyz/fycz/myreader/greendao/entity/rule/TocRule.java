/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.greendao.entity.rule;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.converter.PropertyConverter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

import static xyz.fycz.myreader.util.utils.StringUtils.stringEquals;

/**
 * @author fengyue
 * @date 2021/2/8 17:59
 */
public class TocRule implements Parcelable {
    private String chapterList;
    private String chapterBaseUrl;
    private String chapterName;
    private String chapterUrl;
    private String tocUrlNext;
    private String isVip;
    private String isPay;
    private String updateTime;

    protected TocRule(Parcel in) {
        chapterList = in.readString();
        chapterBaseUrl = in.readString();
        chapterName = in.readString();
        chapterUrl = in.readString();
        tocUrlNext = in.readString();
        isVip = in.readString();
        isPay = in.readString();
        updateTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chapterList);
        dest.writeString(chapterBaseUrl);
        dest.writeString(chapterName);
        dest.writeString(chapterUrl);
        dest.writeString(tocUrlNext);
        dest.writeString(isVip);
        dest.writeString(isPay);
        dest.writeString(updateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TocRule> CREATOR = new Creator<TocRule>() {
        @Override
        public TocRule createFromParcel(Parcel in) {
            return new TocRule(in);
        }

        @Override
        public TocRule[] newArray(int size) {
            return new TocRule[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) o = new TocRule();
        if (getClass() != o.getClass()) return false;
        TocRule tocRule = (TocRule) o;
        return stringEquals(chapterList, tocRule.chapterList) &&
                stringEquals(chapterBaseUrl, tocRule.chapterBaseUrl) &&
                stringEquals(chapterName, tocRule.chapterName) &&
                stringEquals(chapterUrl, tocRule.chapterUrl) &&
                stringEquals(tocUrlNext, tocRule.tocUrlNext) &&
                stringEquals(isVip, tocRule.isVip) &&
                stringEquals(isPay, tocRule.isPay) &&
                stringEquals(updateTime, tocRule.updateTime);
    }

    public String getChapterList() {
        return chapterList;
    }

    public void setChapterList(String chapterList) {
        this.chapterList = chapterList;
    }

    public String getChapterBaseUrl() {
        return chapterBaseUrl;
    }

    public void setChapterBaseUrl(String chapterBaseUrl) {
        this.chapterBaseUrl = chapterBaseUrl;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }

    public String getTocUrlNext() {
        return tocUrlNext;
    }

    public void setTocUrlNext(String tocUrlNext) {
        this.tocUrlNext = tocUrlNext;
    }

    public String getIsVip() {
        return isVip;
    }

    public void setIsVip(String isVip) {
        this.isVip = isVip;
    }

    public String getIsPay() {
        return isPay;
    }

    public void setIsPay(String isPay) {
        this.isPay = isPay;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public TocRule() {
    }

}
