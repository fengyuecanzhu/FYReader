package xyz.fycz.myreader.webapi.crawler.find;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler3;

/**
 * @author fengyue
 * @date 2020/11/28 22:43
 * 已失效
 */
@Deprecated
public class XS7Rank extends FindCrawler3 {
    private FindCrawler3 xs7 = new XS7FindCrawler();
    public static final String CHARSET = "GBK";
    public static final String FIND_NAME = "排行榜[小说旗]";
    public static final String FIND_URL = "https://www.xs7.la/top/lastupdate/1.html";
    @Override
    public String getCharset() {
        return xs7.getCharset();
    }

    @Override
    public String getFindName() {
        return FIND_NAME;
    }

    @Override
    public String getFindUrl() {
        return FIND_URL;
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
        Element div = doc.getElementsByClass("toplist").first();
        Elements as = div.getElementsByTag("a");
        for (Element a : as){
            BookType bookType = new BookType();
            bookType.setUrl(a.attr("href"));
            bookType.setTypeName(a.text());
            bookTypes.add(bookType);
        }
        return bookTypes;
    }

    @Override
    public List<Book> getFindBooks(String html, BookType bookType) {
        return xs7.getFindBooks(html, bookType);
    }

    @Override
    public boolean getTypePage(BookType curType, int page) {
        if (page != 1 && page > curType.getPageSize()){
            return true;
        }
        curType.setUrl(curType.getUrl().substring(0, curType.getUrl().lastIndexOf("/") + 1) + page + ".html");
        return false;
    }
}
