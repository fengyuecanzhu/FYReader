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

package xyz.fycz.myreader.webapi.crawler.source;

import java.util.List;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BaseReadCrawler;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;

/**
 * @author fengyue
 * @date 2021/5/14 10:55
 */
public class ThirdCrawler extends BaseReadCrawler implements BookInfoCrawler {
    private final BookSource source;

    public ThirdCrawler(BookSource source) {
        this.source = source;
    }

    public BookSource getSource() {
        return source;
    }

    @Override
    public String getSearchLink() {
        return source.getSearchRule().getSearchUrl();
    }

    @Override
    public String getCharset() {
        return null;
    }

    @Override
    public Book getBookInfo(String html, Book book) {
        return null;
    }

    @Override
    public String getSearchCharset() {
        return null;
    }

    @Override
    public String getNameSpace() {
        return source.getSourceUrl();
    }

    @Override
    public Boolean isPost() {
        return null;
    }

    public ConMVMap<SearchBookBean, Book> getBooks(List<Book> books) {
        ConMVMap<SearchBookBean, Book> newBooks = new ConMVMap<>();
        for (Book book : books){
            if (book == null || StringHelper.isEmpty(book.getName())) continue;
            book.setSource(source.getSourceUrl());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            newBooks.add(sbb, book);
        }
        return newBooks;
    }
}
