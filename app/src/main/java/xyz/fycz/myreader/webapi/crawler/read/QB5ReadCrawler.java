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
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;

import java.util.ArrayList;


public class QB5ReadCrawler extends BaseReadCrawler implements BookInfoCrawler {
    private static final String NAME_SPACE = "https://www.qb50.com";
    private static final String NOVEL_SEARCH = "https://www.qb50.com/modules/article/search.php?searchkey={key}&submit=%CB%D1%CB%F7";
    private static final String CHARSET = "GBK";
    public static final String SEARCH_CHARSET = "GBK";

    @Override
    public String getSearchLink() {
        return NOVEL_SEARCH;
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
    public String getCharset() {
        return CHARSET;
    }

    @Override
    public String getSearchCharset() {
        return SEARCH_CHARSET;
    }

    @Override
    public String getContentFormHtml(String html) {
        Document doc = Jsoup.parse(html);
        Element divBook = doc.getElementsByClass("nav-style").get(0);
        String bookName = divBook.getElementsByTag("a").get(1).attr("title");
        Element divContent = doc.getElementById("content");
        String content = Html.fromHtml(divContent.html()).toString();
        char c = 160;
        String spaec = "" + c;
        content = content.replace(spaec, "  ");
        content = content.replaceAll("全本小说.*最新章节！", "");
        return content;
    }


    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
        int num = 0;
        Element zjbox = doc.getElementsByClass("zjbox").get(0);
        Elements as = zjbox.getElementsByTag("a");
        for (int i = 12; i < as.size(); i++) {
            Element a = as.get(i);
            Chapter chapter = new Chapter();
            chapter.setNumber(num++);
            chapter.setTitle(a.text());
            chapter.setUrl(readUrl + a.attr("href"));
            chapters.add(chapter);
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
        final ConMVMap<SearchBookBean, Book> books = new ConMVMap<>();
        Document doc = Jsoup.parse(html);
        String urlType = doc.select("meta[property=og:type]").attr("content");
        if ("novel".equals(urlType)) {
            String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
            Book book = new Book();
            book.setChapterUrl(readUrl);
            getBookInfo(html, book);
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        } else {
            Elements divs = doc.getElementsByClass("grid");
            Element div = divs.get(0);
            Elements elementsByTag = div.getElementsByTag("tr");
            for (int i = 1; i < elementsByTag.size(); i++) {
                Element element = elementsByTag.get(i);
                Book book = new Book();
                Elements info = element.getElementsByTag("td");
                book.setName(info.get(0).text());
                book.setChapterUrl(info.get(0).getElementsByTag("a").attr("href"));
                book.setNewestChapterTitle(info.get(1).text());
                book.setAuthor(info.get(2).text());
                book.setSource(LocalBookSource.qb5.toString());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        }
        return books;
    }

    /**
     * 获取小说详细信息
     *
     * @param html
     * @return
     */
    public Book getBookInfo(String html, Book book) {
        //小说源
        book.setSource(LocalBookSource.qb5.toString());
        Document doc = Jsoup.parse(html);
        //书名
        String name = doc.select("meta[property=og:title]").attr("content");
        book.setName(name);
        //作者
        String author = doc.select("meta[property=og:novel:author]").attr("content");
        book.setAuthor(author);
        //最新章节
        String newestChapter = doc.select("meta[property=og:novel:latest_chapter_name]").attr("content");
        book.setNewestChapterTitle(newestChapter);
        //更新时间
        String updateTime = doc.select("meta[property=og:novel:update_time]").attr("content");
        book.setUpdateDate(updateTime);
        //图片url
        Element divImg = doc.getElementsByClass("img_in").get(0);
        Element img = divImg.getElementsByTag("img").get(0);
        book.setImgUrl(img.attr("src"));
        //简介
        Element divIntro = doc.getElementById("intro");
        book.setDesc(divIntro.text());
        //类型
        String type = doc.select("meta[property=og:novel:category]").attr("content");
        book.setType(type);
        return book;
    }

}
