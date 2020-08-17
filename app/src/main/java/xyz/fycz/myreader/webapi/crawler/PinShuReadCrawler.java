package xyz.fycz.myreader.webapi.crawler;

import android.text.Html;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;

import java.util.ArrayList;

/**
 * @author fengyue
 * @date 2020/5/19 19:50
 */
public class PinShuReadCrawler implements ReadCrawler, BookInfoCrawler {
    public static final String NAME_SPACE = "https://www.vodtw.com";
    public static final String NOVEL_SEARCH = "https://www.vodtw.com/Book/Search.aspx";
    public static final String SEARCH_KEY = "SearchKey";
    public static final String CHARSET = "gbk";

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
    public String getSearchKey() {
        return SEARCH_KEY;
    }

    /**
     * 从html中获取章节正文
     *
     * @param html
     * @return
     */
    public String getContentFormHtml(String html) {
        Document doc = Jsoup.parse(html);
        Element divContent = doc.getElementById("BookText");
        if (divContent != null) {
            String content = Html.fromHtml(divContent.html()).toString();
            char c = 160;
            String spaec = "" + c;
            content = content.replace(spaec, "  ").replace("品书网", "")
            .replace("手机阅读", "");
            return StringHelper.IgnoreCaseReplace(content, "www.vodtw.com", "");
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
        String readUrl = doc.select("meta[property=og:novel:read_url]")
                .attr("content").replace("index.html", "");
        Element divList = doc.getElementsByClass("insert_list").get(0);
        String lastTile = null;
        int i = 0;
        Elements elementsByTag = divList.getElementsByTag("a");
        for (Element a : elementsByTag) {
            String title = a.text();
            if (!StringHelper.isEmpty(lastTile) && title.equals(lastTile)) {
                continue;
            }
            Chapter chapter = new Chapter();
            chapter.setNumber(i++);
            chapter.setTitle(title);
            String url = readUrl + a.attr("href");
            chapter.setUrl(url.replace(".la", ".com"));
            chapters.add(chapter);
            lastTile = title;
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
        Element div = doc.getElementById("Content");
        Elements elementsSelected = div.select("[id=CListTitle]");
        for (Element element : elementsSelected) {
            Book book = new Book();
            Elements info = element.getElementsByTag("a");
            book.setName(info.get(0).text());
            book.setChapterUrl(NAME_SPACE + info.get(0).attr("href"));
            book.setAuthor(info.get(1).text());
            book.setType(info.get(3).text());
            book.setNewestChapterTitle(info.get(4).text());
            book.setSource(BookSource.pinshu.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
        return books;
    }

    /**
     * 获取书籍详细信息
     *
     * @param book
     */
    public Book getBookInfo(String html, Book book) {
        Document doc = Jsoup.parse(html);
        Element img = doc.getElementsByClass("bookpic").get(0);
        book.setImgUrl(img.getElementsByTag("img").get(0).attr("src"));
        Element desc = doc.getElementsByClass("bookintro").get(0);
        book.setDesc(desc.text());
        return book;
    }

}
