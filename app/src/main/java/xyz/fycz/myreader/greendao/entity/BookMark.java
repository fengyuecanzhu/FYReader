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

import org.greenrobot.greendao.annotation.*;

/**
 * @author fengyue
 * @date 2020/7/21 16:02
 * 书签
 */
@Entity
public class BookMark {
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    private String id;//书签id

    @NotNull
    private String bookId;//书签所属书的ID

    private int number;//书签序号

    private String title;//书签标题

    @NotNull
    private int bookMarkChapterNum;//书签章节

    private int bookMarkReadPosition;//书签章节的位置
    
    @Generated(hash = 1704575762)
    public BookMark() {
    }

    @Generated(hash = 126149112)
    public BookMark(String id, @NotNull String bookId, int number, String title, int bookMarkChapterNum,
            int bookMarkReadPosition) {
        this.id = id;
        this.bookId = bookId;
        this.number = number;
        this.title = title;
        this.bookMarkChapterNum = bookMarkChapterNum;
        this.bookMarkReadPosition = bookMarkReadPosition;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getBookMarkChapterNum() {
        return bookMarkChapterNum;
    }

    public void setBookMarkChapterNum(int bookMarkChapterNum) {
        this.bookMarkChapterNum = bookMarkChapterNum;
    }

    public int getBookMarkReadPosition() {
        return bookMarkReadPosition;
    }

    public void setBookMarkReadPosition(int bookMarkReadPosition) {
        this.bookMarkReadPosition = bookMarkReadPosition;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
