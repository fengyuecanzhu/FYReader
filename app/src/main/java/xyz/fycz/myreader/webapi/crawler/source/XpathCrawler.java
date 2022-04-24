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

import org.seimicrawler.xpath.JXDocument;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.SearchRule;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.model.sourceAnalyzer.XpathAnalyzer;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BaseSourceCrawler;

/**
 * @author fengyue
 * @date 2021/2/14 17:52
 */
public class XpathCrawler extends BaseSourceCrawler {
    private final XpathAnalyzer analyzer;

    public XpathCrawler(BookSource source) {
        super(source, new XpathAnalyzer());
        this.analyzer = (XpathAnalyzer) super.analyzer;
    }

    @Override
    public ConMVMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConMVMap<SearchBookBean, Book> books = new ConMVMap<>();
        JXDocument jxDoc = JXDocument.create(html);
        SearchRule searchRule = source.getSearchRule();
        if (StringHelper.isEmpty(searchRule.getList())) {
            getBooksNoList(jxDoc, searchRule, books);
        } else {
            getBooks(jxDoc, searchRule, books);
        }
        return books;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        JXDocument jxDoc = JXDocument.create(html);
        getChapters(jxDoc, chapters);
        return chapters;
    }


    @Override
    public String getContentFormHtml(String html) {
        JXDocument jxDoc = JXDocument.create(html);
        return getContent(jxDoc);
    }

    @Override
    public Book getBookInfo(String html, Book book) {
        JXDocument jxDoc = JXDocument.create(html);
        return getBookInfo(jxDoc, book);
    }


    protected List getList(String str, Object obj) {
        return analyzer.getJXNodeList(str, obj);
    }
}
