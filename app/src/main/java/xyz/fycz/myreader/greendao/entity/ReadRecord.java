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

package xyz.fycz.myreader.greendao.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * @author fengyue
 * @date 2021/6/1 18:38
 */
@Entity
public class ReadRecord {
    @Id
    private String id;
    private String bookImg;
    private String bookName;
    private String bookAuthor;
    private long readTime;
    private long updateTime;


    @Generated(hash = 1191129215)
    public ReadRecord() {
    }

    @Generated(hash = 381392552)
    public ReadRecord(String id, String bookImg, String bookName, String bookAuthor,
            long readTime, long updateTime) {
        this.id = id;
        this.bookImg = bookImg;
        this.bookName = bookName;
        this.bookAuthor = bookAuthor;
        this.readTime = readTime;
        this.updateTime = updateTime;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookName() {
        return this.bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookAuthor() {
        return this.bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getBookImg() {
        return this.bookImg;
    }

    public void setBookImg(String bookImg) {
        this.bookImg = bookImg;
    }

    public long getReadTime() {
        return this.readTime;
    }

    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }
}
