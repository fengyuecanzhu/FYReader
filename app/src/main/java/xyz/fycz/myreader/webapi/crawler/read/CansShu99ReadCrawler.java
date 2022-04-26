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

package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.webapi.crawler.base.BaseReadCrawler;

import java.util.ArrayList;


public class CansShu99ReadCrawler extends BaseReadCrawler {
    public static final String NAME_SPACE = "http://www.99csw.com";
    public static final String NOVEL_SEARCH = "http://www.99csw.com/book/search.php?type=all&keyword={key}";
    public static final String CHARSET = "UTF-8";
    public static final String SEARCH_CHARSET = "utf-8";

    @Override
    public String getSearchLink() {
        return NOVEL_SEARCH;
    }

    @Override
    public String getCharset() {
        return CHARSET;
    }

    @Override
    public String getNameSpace() {
        return NAME_SPACE;
    }

    @Override
    public Boolean isPost() {
        return false;
    }

    @Override
    public String getSearchCharset() {
        return SEARCH_CHARSET;
    }

    /**
     * 从html中获取章节正文
     *
     * @param html
     * @return
     */
    public String getContentFormHtml(String html) {
        Document doc = Jsoup.parse(html);
        Element divContent = doc.getElementById("content");
        String content = Html.fromHtml(divContent.html()).toString();
        char c = 160;
        String spaec = "" + c;
        content = content.replace(spaec, "  ");
        return content;
    }

    /**
     * 从html中获取章节列表
     *
     * @param html
     * @return
     */
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        try {
            Element divList = doc.getElementById("dir");
            int i = 0;
            Elements elementsByTag = divList.getElementsByTag("dd");
            for (int j = 0; j < elementsByTag.size(); j++) {
                Element dd = elementsByTag.get(j);
                Elements as = dd.getElementsByTag("a");
                Element a = as.get(0);
                String title = a.text();
                Chapter chapter = new Chapter();
                chapter.setNumber(i++);
                chapter.setTitle(title);
                String url = a.attr("href");
                chapter.setUrl(url);
                chapters.add(chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chapters;
    }

    /**
     * 从搜索html中得到书列表
     *
     * @param html
     * @return
     */
    public ConMVMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConMVMap<SearchBookBean, Book> books = new ConMVMap<>();
        Document doc = Jsoup.parse(html);
//        try {
        Elements divs = doc.getElementsByClass("list_box");
        Element div = divs.get(0);
        Elements elementsByTag = div.getElementsByTag("li");
        for (Element element : elementsByTag) {
            Book book = new Book();
            String name = element.getElementsByTag("h2").first().getElementsByTag("a").first().text();
            book.setName(name);
            String author = element.getElementsByTag("h4").first().getElementsByTag("a").first().text();
            book.setAuthor(author);
            String type = element.getElementsByTag("h4").get(1).getElementsByTag("a").text();
            book.setType(type);
            String desc = element.getElementsByClass("intro").first().text();
            book.setDesc(desc);
            String imgUrl = element.getElementsByTag("img").first().attr("src");
            book.setImgUrl("http:" + imgUrl);
            String chapterUrl = element.getElementsByTag("h2").first().getElementsByTag("a").first().attr("href");
            book.setChapterUrl(chapterUrl);
            book.setNewestChapterTitle("");
            book.setSource(LocalBookSource.cangshu99.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return books;
    }


}
