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

package xyz.fycz.myreader.webapi.crawler.find;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler3;


public class XS7FindCrawler extends FindCrawler3 {
    public static final String NAME_SPACE = "https://www.xs7.la";
    public static final String CHARSET = "GBK";
    public static final String SEARCH_CHARSET = "GBK";
    public static final String FIND_NAME = "书城[小说旗]";

    @Override
    public String getCharset() {
        return CHARSET;
    }

    @Override
    public String getFindName() {
        return FIND_NAME;
    }

    @Override
    public String getFindUrl() {
        return NAME_SPACE;
    }

    @Override
    public boolean hasImg() {
        return true;
    }

    @Override
    public boolean needSearch() {
        return false;
    }

    @Override
    public List<BookType> getBookTypes(String html) {
        List<BookType> bookTypes = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Element div = doc.getElementsByClass("subnav-hot").first();
        Elements as = div.getElementsByTag("a");
        for (Element a : as) {
            BookType bookType = new BookType();
            bookType.setUrl(a.attr("href"));
            bookType.setTypeName(a.text());
            bookTypes.add(bookType);
        }
        return bookTypes;
    }

    /*
    <div id="alistbox">
		<div class="pic"><a target="_blank" href="https://www.xs7.la/book/1_1201/" title="带着农场混异界最新章节列表"><img src="https://www.xs7.la/files/article/image/1/1201/1201s.jpg"
				 alt="带着农场混异界" title="带着农场混异界" width="115" height="160"></a></div>
		<div class="info">
			<div class="title">
				<h2><a target="_blank" href="https://www.xs7.la/book/1_1201/">带着农场混异界</a></h2>
				<span>作者：明宇</span>
			</div>
			<div class="sys">最新更新：<a href="https://www.xs7.la/book/1_1201/47219709.html" target="_blank" title="第四百四十七章 小山">第四百四十七章
					小山</a></div>
			<div class="intro"> 他横任他横，我自种我田，若要来惹我，过不了明年。
				宅男赵海带着QQ农场穿越到了异界，附身到了一个落迫的小贵族身上，他的封地是一片种不出东西的黑土地，而最主要的是，...</div>
			<div class="yuedu">
				<a target="_blank" href="https://www.xs7.la/book/1_1201/">全文阅读</a>
				<a href="https://www.xs7.la/modules/article/addbookcase.php?bid=1201">加入书架</a>
			</div>
		</div>
	</div>
     */
    @Override
    public List<Book> getFindBooks(String html, BookType bookType) {
        List<Book> books = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Element last = doc.getElementsByClass("last").first();
        bookType.setPageSize(Integer.parseInt(last.text()));
        Element div = doc.getElementById("alist");
        Elements bDivs = div.select("div[id=alistbox]");
        for (Element bDiv : bDivs) {
            Book book = new Book();
            Element title = bDiv.getElementsByClass("title").first();
            Element sys = bDiv.getElementsByClass("sys").first();
            Element intro = bDiv.getElementsByClass("intro").first();
            book.setName(title.getElementsByTag("a").first().text());
            book.setAuthor(title.getElementsByTag("span").first().text().replace("作者：", ""));
            book.setNewestChapterTitle(sys.getElementsByTag("a").first().text());
            book.setImgUrl(bDiv.getElementsByTag("img").attr("src"));
            book.setDesc(intro.text());
            book.setType(bookType.getTypeName());
            book.setChapterUrl(title.getElementsByTag("a").first().attr("href"));
            book.setSource(LocalBookSource.xs7.toString());
            books.add(book);
        }
        return books;
    }

    @Override
    public boolean getTypePage(BookType curType, int page) {
        if (page != 1 && page > curType.getPageSize()) {
            return true;
        }
        curType.setUrl(curType.getUrl().substring(0, curType.getUrl().lastIndexOf("_") + 1) + page + ".html");
        return false;
    }
}
