package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;


public class XingXingReadCrawler implements ReadCrawler {
    public static final String NAME_SPACE = "https://www.hs918.com";
    public static final String NOVEL_SEARCH = "https://www.hs918.com/search.php?key={key}";
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
        Element divContent = doc.getElementById("txt");
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
        String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
        Element divList = doc.getElementById("newlist");
        Elements elementsByTag = divList.getElementsByTag("a");
        int i = 0;
        for (int j = 0; j < elementsByTag.size(); j++) {
            Element a = elementsByTag.get(j);
            String title = a.text();
            String url = a.attr("href");
            Chapter chapter = new Chapter();
            chapter.setNumber(i++);
            chapter.setTitle(title);
            chapter.setUrl(readUrl + url);
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
    /*
      <li>
            <div class="sCboxBookParL left"><a href="/xiaoshuo/21351.html"><img data-original="https://www.hs918.com/files/article/image/20/20240/20240s.jpg"  /></a></div>
            <div class="sCboxBookParR left">
                <div class="top clearfix">
                    <h1><a href="/xiaoshuo/21351.html"><font style="font-weight:bold;color:#f00">大主宰</font>之剑仙</a></h1>
                    <span class="s2">遗弃的梦 </span>
                    <span class="s4">95万字</span>
                    <span class="s6">474人气值</span>
                </div>
                <div class="tips clearfix"><a href="/fenlei/3_0_0_0_0_1.html" title="浪漫青春" class="tipsa">浪漫青春</a></div>
                <div class="c"><strong>内容介绍：</strong>    一位少年意外穿越大主宰，结识了牧尘…………（新人作家，写得不好还请多多包涵）
                ...</div>
                <div class="bottom clearfix">
                    <span class="redTps">最近更新</span>
                    <a href="/xiaoshuo/21351/43873.html">第57章：陷阱</a>

                    <span class="time2">更新时间：2020-12-05 01:45</span>
                </div>
            </div>
            <div class="sCboxBookParS right">
                <a href="/xiaoshuo/21351/" class="b1">开始阅读</a>
                <a href="javascript:;" onclick="BookCaseAdd('21351');" class="b2 apdBshelf">加入书架</a>
            </div>
        </li>
     */
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
//        try {
        Element div = doc.getElementsByClass("leftBox").first();
        Elements lis = div.getElementsByTag("li");
        for (Element li : lis) {
            Elements as = li.getElementsByTag("a");
            Book book = new Book();
            book.setName(as.get(1).text());
            book.setAuthor(li.getElementsByTag("span").first().text());
            book.setType(as.get(2).text());
            book.setNewestChapterTitle(as.get(3).text());
            book.setDesc(li.getElementsByClass("c").first().text().replace("内容介绍：", ""));
            book.setUpdateDate(li.getElementsByClass("time2").first().text().replace("更新时间：", ""));
            book.setImgUrl(li.getElementsByTag("img").attr("data-original"));
            book.setChapterUrl(NAME_SPACE + as.get(1).attr("href").replace(".html", "/"));
            book.setSource(BookSource.xingxing.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return books;
    }


}
