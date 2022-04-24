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

package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BaseReadCrawler;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;


public class ZW37ReadCrawler extends BaseReadCrawler implements BookInfoCrawler {
    private static final String NAME_SPACE = "https://www.777zw.net";
    private static final String NOVEL_SEARCH = "https://www.777zw.net/modules/article/search.php?searchtype=articlename&searchkey={key}";
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
        Element divContent = doc.getElementById("content");
        String content = Html.fromHtml(divContent.html()).toString();
        char c = 160;
        String spaec = "" + c;
        content = content.replace(spaec, "  ");
        return content;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
        Element divList = doc.getElementById("list");
        String lastTile = null;
        int i = 0;
        Elements elementsByTag = divList.getElementsByTag("a");
        for (int j = 0; j < elementsByTag.size(); j++) {
            Element a = elementsByTag.get(j);
            String title = a.text();
            if (!StringHelper.isEmpty(lastTile) && title.equals(lastTile)) {
                continue;
            }
            Chapter chapter = new Chapter();
            chapter.setNumber(i++);
            chapter.setTitle(title);
            String url = readUrl + a.attr("href");
            chapter.setUrl(url);
            chapters.add(chapter);
            lastTile = title;
        }
        return chapters;
    }

    /**
     * 从搜索html中得到书列表
     *
     * @param html
     * @return <tr>
     * <td class="odd"><a href="https://www.37zww.net/1/1812/">斗罗大陆IV终极斗罗</a></td>
     * <td class="even"><a href="https://www.37zww.net/1/1812/index.html" target="_blank"> 第一千五百八十一章 突破，真神级！</a></td>
     * <td class="odd">唐家三少</td>
     * <td class="even">8427K</td>
     * <td class="odd" align="center">21-02-06</td>
     * <td class="even" align="center">连载</td>
     * </tr>
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
            Element div = doc.getElementById("main");
            Elements elements = div.getElementsByTag("tr");
            for (int i = 1; i < elements.size(); i++) {
                Element element = elements.get(i);
                Book book = new Book();
                Elements info = element.getElementsByTag("td");
                book.setName(info.get(0).text());
                book.setChapterUrl(info.get(0).selectFirst("a").attr("href"));
                book.setAuthor(info.get(2).text());
                book.setNewestChapterTitle(info.get(1).text());
                book.setSource(LocalBookSource.zw37.toString());
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
        Document doc = Jsoup.parse(html);
        book.setSource(LocalBookSource.zw37.toString());

        String name = doc.select("meta[property=og:title]").attr("content");
        book.setName(name);
        String url = doc.select("meta[property=og:novel:read_url]").attr("content");
        book.setChapterUrl(url);
        String author = doc.select("meta[property=og:novel:author]").attr("content");
        book.setAuthor(author);
        String newestChapter = doc.select("meta[property=og:novel:latest_chapter_name]").attr("content");
        book.setNewestChapterTitle(newestChapter);

        String img = doc.select("meta[property=og:image]").attr("content");
        book.setImgUrl(img);

        String desc = doc.select("meta[property=og:description]").attr("content");
        book.setDesc(desc);
        //类型
        String type = doc.select("meta[property=og:novel:category]").attr("content");
        book.setType(type);
        return book;

    }


}
