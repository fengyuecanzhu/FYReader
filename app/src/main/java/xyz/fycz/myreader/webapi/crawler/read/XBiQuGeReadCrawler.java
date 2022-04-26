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

import java.util.ArrayList;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.webapi.crawler.base.BaseReadCrawler;

@Deprecated
public class XBiQuGeReadCrawler extends BaseReadCrawler {
    public static final String NAME_SPACE = "https://www.xquge.com";
    public static final String NOVEL_SEARCH = "https://www.xquge.com/search?keyword={key}&sign=";
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
        Element divContent = doc.getElementById("content");
        String content = Html.fromHtml(divContent.html()).toString();
        char c = 160;
        String spaec = "" + c;
        content = content.replace(spaec, "  ").replace("applyChapterSetting();", "");
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
        Element divList = doc.getElementsByClass("catelog_list").last();
        Elements elementsByTag = divList.getElementsByTag("a");
        int i = 0;
        for (int j = 0; j < elementsByTag.size(); j++) {
            Element a = elementsByTag.get(j);
            String title = a.text();
            String url = a.attr("href");
            Chapter chapter = new Chapter();
            chapter.setNumber(i++);
            chapter.setTitle(title);
            chapter.setUrl(url);
            chapters.add(chapter);
        }
        return chapters;
    }

    /**
     * 从搜索html中得到书列表
     *
     * @param html
     * @return <li>
     *     <div class="rank_items">
     *         <div class="items_l"><a href="https://www.xquge.com/book/5661.html" class="book_img"><img
     *                 src="//static.xquge.com/Public/upload/book/201912/23/22/5715770830115e0060832a553.jpg?v=2020060502"
     *                 alt="绝世元尊"></a></div>
     *         <div class="items_center">
     *             <div class="rank_bkname"><a href="https://www.xquge.com/book/5661.html">异界大主宰</a></div>
     *             <div class="rank_bkinfo"><span class="author">范范的萧</span><span>玄幻奇幻</span><span>连载</span>
     *             </div>
     *             <div class="rank_bkbrief">
     *                 “怎么回事？”    冷尧一脸茫然、这是哪儿、他看着这个陌生的的环境，一时不知所错……    啊！！！    冷尧看着自己有一双粗大而又充满爆发力的大手，每个细胞都充满爆发力。    这具身体不是自己的，难道我穿越了？    ……    ……    ……                              </div>
     *             <div class="rank_bkother">
     *                 <div class="rank_bktime">2020-08-28 04:24</div>
     *                 <div class="rank_newpage"><a href="https://www.xquge.com/book/5661/98087932.html">更新章节：新书《苍山牧云记》以发布</a>
     *                 </div>
     *             </div>
     *         </div>
     *         <div class="items_rig">
     *             <a href="https://www.xquge.com/book/5661.html" class="bk_brief_btn">书籍详情</a></div>
     *     </div>
     * </li>
     */
    public ConMVMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConMVMap<SearchBookBean, Book> books = new ConMVMap<>();
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("rank_ullist");
        Element div = divs.get(0);
        Elements elementsByTag = div.getElementsByTag("li");
        for (int i = 0; i < elementsByTag.size(); i++) {
            Element element = elementsByTag.get(i);
            Book book = new Book();
            Elements as = element.getElementsByTag("a");
            book.setName(as.get(1).text());
            book.setChapterUrl(as.get(1).attr("href"));
            book.setNewestChapterTitle(as.get(2).text().replace("更新章节：", ""));
            String img = as.first().selectFirst("img").attr("src");
            if (!img.contains("http")) img = "https:" + img;
            book.setImgUrl(img);
            Elements spans = element.selectFirst(".rank_bkinfo").select("span");
            book.setAuthor(spans.first().text());
            book.setType(spans.get(1).text());
            book.setDesc(element.selectFirst(".rank_bkbrief").text());
            book.setSource(LocalBookSource.xbiquge.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
        return books;
    }

}
