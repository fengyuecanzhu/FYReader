package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.Book;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 风月小说网html解析工具
 */

public class FYReadCrawler implements ReadCrawler {
    public static final String NAME_SPACE = "https://novel.fycz.xyz";
    public static final String NOVEL_SEARCH  = "https://novel.fycz.xyz/search.html?keyword={key}";
    public static final String CHARSET = "utf-8";
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
        Pattern pattern = Pattern.compile("<div class=\"read-content j_readContent\">[\\s\\S]*?</div>");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String content = Html.fromHtml(matcher.group(0)).toString();
            char c = 160;
            String spaec = "" + c;
            content = content.replace(spaec, "  ");
            return content;
        } else {
            return "";
        }
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
        Elements nodes = doc.getElementsByClass("cate-list");
        Element node = nodes.get(0);
        Elements divs = node.getElementsByTag("a");
        for (int i = 0; i < divs.size(); i++) {
            Chapter chapter = new Chapter();
            Element div = divs.get(i);
            chapter.setNumber(i);
            chapter.setUrl(div.attr("href").replace("https://novel.fycz.xyz",""));
            chapter.setTitle(div.getElementsByTag("span").get(0).text());
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
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
        Elements nodes = doc.getElementsByClass("secd-rank-list");
        for (Element div : nodes) {
            Book book = new Book();
            book.setName(div.getElementsByTag("a").get(1).text());
            book.setAuthor(div.getElementsByTag("a").get(2).text());
            book.setType(div.getElementsByTag("a").get(3).text());
            book.setNewestChapterTitle(div.getElementsByTag("a").get(4).text());
            book.setNewestChapterUrl(div.getElementsByTag("a").get(4).attr("href"));
            Element img = div.getElementsByTag("img").get(0);
            book.setImgUrl(img.attr("data-original"));
            Element chapterUrl = div.getElementsByTag("a").get(1);
            book.setChapterUrl(chapterUrl.attr("href"));
            book.setDesc(div.getElementsByTag("p").get(1).text());
            book.setUpdateDate(div.getElementsByTag("span").get(1).text().replace("|", ""));
            book.setSource(LocalBookSource.fynovel.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
        return books;
    }

}
