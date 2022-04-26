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

package xyz.fycz.myreader.greendao.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class CookieBean implements Parcelable {

    @Id
    private String url;
    private String cookie;

    private CookieBean(Parcel in) {
        url = in.readString();
        cookie = in.readString();
    }

    @Generated(hash = 517179762)
    public CookieBean(String url, String cookie) {
        this.url = url;
        this.cookie = cookie;
    }

    @Generated(hash = 769081142)
    public CookieBean() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(cookie);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCookie() {
        return cookie == null ? "" : cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public static final Creator<CookieBean> CREATOR = new Creator<CookieBean>() {
        @Override
        public CookieBean createFromParcel(Parcel in) {
            return new CookieBean(in);
        }

        @Override
        public CookieBean[] newArray(int size) {
            return new CookieBean[size];
        }
    };
}
