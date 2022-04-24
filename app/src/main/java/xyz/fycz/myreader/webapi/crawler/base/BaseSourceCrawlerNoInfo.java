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

package xyz.fycz.myreader.webapi.crawler.base;

import java.util.ArrayList;
import java.util.Map;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;

/**
 * @author fengyue
 * @date 2021/2/14 18:28
 */
public class BaseSourceCrawlerNoInfo extends BaseReadCrawler {
    protected final BaseSourceCrawler crawler;

    public BaseSourceCrawlerNoInfo(BaseSourceCrawler crawler) {
        this.crawler = crawler;
    }

    @Override
    public String getSearchLink() {
        return crawler.getSearchLink();
    }

    @Override
    public String getCharset() {
        return crawler.getCharset();
    }

    @Override
    public String getSearchCharset() {
        return crawler.getSearchCharset();
    }

    @Override
    public String getNameSpace() {
        return crawler.getNameSpace();
    }

    @Override
    public Boolean isPost() {
        return crawler.isPost();
    }

    @Override
    public ConMVMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        return crawler.getBooksFromSearchHtml(html);
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        return crawler.getChaptersFromHtml(html);
    }

    @Override
    public String getContentFormHtml(String html) {
        return crawler.getContentFormHtml(html);
    }

    @Override
    public Map<String, String> getHeaders() {
        return crawler.getHeaders();
    }
}
