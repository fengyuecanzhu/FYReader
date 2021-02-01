package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;

/**
 * @author fengyue
 * @date 2020/5/19 19:50
 */
public class PinShuReadCrawler2 implements ReadCrawler, BookInfoCrawler {
    public static final String NAME_SPACE = "https://www.vodtw.la";
    public static final String NOVEL_SEARCH = "https://www.vodtw.la/search.html";
    public static final String SEARCH_KEY = "q";
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
     * @return
     */
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
        Element div = doc.getElementsByClass("booklist").first();
        Elements divs = div.getElementsByClass("clearfix");
        for (Element element : divs) {
            Book book = new Book();
            Elements info = element.getElementsByTag("a");
            book.setName(info.get(1).text());
            book.setChapterUrl(NAME_SPACE + info.get(0).attr("href"));
            book.setSource("pinshu");
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
        Element img = doc.getElementsByClass("bookimg").get(0);
        book.setImgUrl(NAME_SPACE + img.getElementsByTag("img").get(0).attr("src"));

        String author = doc.select("meta[property=og:novel:author]").attr("content");
        String type = doc.select("meta[property=og:novel:category]").attr("content");
        String desc = doc.select("meta[property=og:description]").attr("content");
        String newestChapterTitle = doc.select("meta[property=og:novel:latest_chapter_name]").attr("content");

        book.setAuthor(author);
        book.setType(type);
        book.setDesc(desc);
        book.setNewestChapterTitle(newestChapterTitle);

        return book;
    }

}
