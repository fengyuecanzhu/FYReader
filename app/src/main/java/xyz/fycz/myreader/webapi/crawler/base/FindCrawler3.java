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

package xyz.fycz.myreader.webapi.crawler.base;

import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.greendao.entity.Book;

import java.io.Serializable;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/9/14 18:36
 */
public abstract class FindCrawler3 implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract String getCharset();
    public abstract String getFindName();
    public abstract String getFindUrl();
    public abstract boolean hasImg();
    public abstract boolean needSearch();
    //动态获取
    public List<BookType> getBookTypes(String html) {
        return null;
    }
    //静态获取
    public List<BookType> getBookTypes(){
        return null;
    }
    public abstract List<Book> getFindBooks(String html, BookType bookType);
    public abstract boolean getTypePage(BookType curType, int page);
}
