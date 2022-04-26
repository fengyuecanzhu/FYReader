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

import java.util.Objects;

import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

import static xyz.fycz.myreader.util.utils.StringUtils.stringEquals;

/**
 * @author fengyue
 * @date 2021/2/8 18:01
 */
public class ContentRule implements Parcelable {
    private String content;
    private String contentBaseUrl;
    private String contentUrlNext;
    private String webJs;
    private String sourceRegex;
    private String replaceRegex;

    protected ContentRule(Parcel in) {
        content = in.readString();
        contentBaseUrl = in.readString();
        contentUrlNext = in.readString();
        webJs = in.readString();
        sourceRegex = in.readString();
        replaceRegex = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeString(contentBaseUrl);
        dest.writeString(contentUrlNext);
        dest.writeString(webJs);
        dest.writeString(sourceRegex);
        dest.writeString(replaceRegex);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ContentRule> CREATOR = new Creator<ContentRule>() {
        @Override
        public ContentRule createFromParcel(Parcel in) {
            return new ContentRule(in);
        }

        @Override
        public ContentRule[] newArray(int size) {
            return new ContentRule[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) o = new ContentRule();
        if (getClass() != o.getClass()) return false;
        ContentRule that = (ContentRule) o;
        return  stringEquals(content, that.content) &&
                stringEquals(contentBaseUrl, that.contentBaseUrl) &&
                stringEquals(contentUrlNext, that.contentUrlNext) &&
                stringEquals(webJs, that.webJs) &&
                stringEquals(sourceRegex, that.sourceRegex) &&
                stringEquals(replaceRegex, that.replaceRegex);
    }


    public ContentRule() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentBaseUrl() {
        return contentBaseUrl;
    }

    public void setContentBaseUrl(String contentBaseUrl) {
        this.contentBaseUrl = contentBaseUrl;
    }

    public String getContentUrlNext() {
        return contentUrlNext;
    }

    public void setContentUrlNext(String contentUrlNext) {
        this.contentUrlNext = contentUrlNext;
    }

    public String getWebJs() {
        return webJs;
    }

    public void setWebJs(String webJs) {
        this.webJs = webJs;
    }

    public String getSourceRegex() {
        return sourceRegex;
    }

    public void setSourceRegex(String sourceRegex) {
        this.sourceRegex = sourceRegex;
    }

    public String getReplaceRegex() {
        return replaceRegex;
    }

    public void setReplaceRegex(String replaceRegex) {
        this.replaceRegex = replaceRegex;
    }
}
