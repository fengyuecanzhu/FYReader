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
import xyz.fycz.myreader.webapi.crawler.base.BaseReadCrawler;

@Deprecated
public class BiJianReadCrawler extends BaseReadCrawler {
    public static final String NAME_SPACE = "http://www.bjcan.com";
    public static final String NOVEL_SEARCH = "http://www.bjcan.com/home/search/index.html?keyword={key}";
    public static final String CHARSET = "UTF-8";
    public static final String SEARCH_CHARSET = "UTF-8";

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
        Element divContent = doc.getElementsByClass("read-content").first();
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
        Element divList = doc.getElementsByClass("attentions").get(1);
        Elements elementsByTag = divList.getElementsByTag("a");
        for (int i = 0; i < elementsByTag.size(); i++) {
            Element a = elementsByTag.get(i);
            String title = a.text();
            String url = a.attr("href");
            Chapter chapter = new Chapter();
            chapter.setNumber(i);
            chapter.setTitle(title);
            chapter.setUrl(url);
            chapters.add(chapter);
        }
        return chapters;
    }

    /**
     * 从搜索html中得到书列表
     * <li>
     * <a class="pic" href="http://www.bjcan.com/book/74544.html" target="_blank"><img class="lazy" src="http://www.bjcan.com/uploads/novel/20200907/b38cea14e4a3de1e876395e378c1e544.jpeg" alt="大主宰：灵玖"></a>
     * <h5 class="tit"><a href="http://www.bjcan.com/book/74544.html" target="_blank">大主宰：灵玖</a></h5>
     * <p class="info">作者：<span>霞露</span><span>分类：其他</span><i class="serial">连载中</i></p>
     * <p class="intro">简介：她，身负系统，莫名来到了大主宰的时空，成为了聚灵族的最后族人。在她还对周围的情况一片模糊的时候，她遇到了林静这个小恶魔。拜林静所赐，她还遇到了武祖，成为了武柤的小徒弟。参与了灵路，遇到了牧尘和洛璃，她默默的在心中问着自己那一直在划水的系统：&ldquo;你的活来了，说吧，我该干什么？&rdquo;</p>
     * <a class="view" href="http://www.bjcan.com/book/74544.html" target="_blank">小说详情</a>
     * </li>
     */
    public ConMVMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConMVMap<SearchBookBean, Book> books = new ConMVMap<>();
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("book-list");
        Element div = divs.get(0);
        Elements elementsByTag = div.getElementsByTag("li");
        for (int i = 0; i < elementsByTag.size(); i++) {
            Element element = elementsByTag.get(i);
            Elements as = element.getElementsByTag("a");
            Elements ps = element.getElementsByTag("p");
            Book book = new Book();
            book.setImgUrl(element.getElementsByTag("img").attr("src"));
            book.setName(as.get(1).text());
            Elements spans = ps.get(0).getElementsByTag("span");
            book.setAuthor(spans.get(0).text());
            book.setType(spans.get(1).text().replace("分类：", ""));
            book.setChapterUrl(as.get(2).attr("href"));
            book.setDesc(ps.get(1).text());
            book.setNewestChapterTitle("");
            book.setSource(LocalBookSource.bijian.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
        return books;
    }

}
