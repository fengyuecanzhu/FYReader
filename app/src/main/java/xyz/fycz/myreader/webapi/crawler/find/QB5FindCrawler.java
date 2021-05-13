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
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;


public class QB5FindCrawler extends FindCrawler {
    public static final String FIND_URL = "https://www.qb50.com";
    public static final String FIND_NAME = "书城[全本小说]";
    private static final String CHARSET = "GBK";

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
        return FIND_URL;
    }

    @Override
    public boolean hasImg() {
        return false;
    }

    @Override
    public boolean needSearch() {
        return false;
    }

    /**
     * 获取书城小说分类列表
     *
     * @param html
     * @return
     */
    public List<BookType> getBookTypes(String html) {
        List<BookType> bookTypes = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("nav_cont");
        if (divs.size() > 0) {
            Elements uls = divs.get(0).getElementsByTag("ul");
            if (uls.size() > 0) {
                for (Element li : uls.get(0).children()) {
                    Element a = li.child(0);
                    BookType bookType = new BookType();
                    bookType.setTypeName(a.attr("title"));
                    bookType.setUrl(a.attr("href"));
                    if (bookType.getTypeName().contains("首页") || bookType.getTypeName().contains("热门小说"))
                        continue;
                    if (!StringHelper.isEmpty(bookType.getTypeName())) {
                        bookTypes.add(bookType);
                    }
                }
            }

        }
        return bookTypes;
    }

    /**
     * 获取某一分类小说排行榜列表
     *
     * @param html
     * @return
     */
    public List<Book> getBookRankList(String html) {
        List<Book> books = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("r");
        if (divs.size() > 0) {
            Elements uls = divs.get(0).getElementsByTag("ul");
            if (uls.size() > 0) {
                for (Element li : uls.get(0).children()) {
                    Book book = new Book();
                    Element scanS1 = li.getElementsByClass("s1").get(0);
                    Element scanS2 = li.getElementsByClass("s2").get(0);
                    Element scanS5 = li.getElementsByClass("s5").get(0);
                    book.setType(scanS1.html().replace("[", "").replace("]", ""));
                    Element a = scanS2.getElementsByTag("a").get(0);
                    book.setName(a.attr("title"));
                    book.setChapterUrl(a.attr("href"));
                    book.setAuthor(scanS5.html());
                    book.setSource(LocalBookSource.biquge.toString());
                    books.add(book);
                }
            }
        }

        return books;
    }

    public List<Book> getFindBooks(String html, BookType bookType) {
        List<Book> books = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        try {
            int pageSize = Integer.parseInt(doc.getElementsByClass("last").first().text());
            bookType.setPageSize(pageSize);
        } catch (Exception ignored) {
        }
        String type = doc.select("meta[name=keywords]").attr("content").replace(",全本小说网", "");
        Element div = doc.getElementById("tlist");
        Elements uls = div.getElementsByTag("ul");
        if (uls.size() > 0) {
            for (Element li : uls.get(0).children()) {
                Book book = new Book();
                Element aName = li.getElementsByClass("name").get(0);
                Element divZz = li.getElementsByClass("zz").get(0);
                Element divAuthor = li.getElementsByClass("author").get(0);
                Element divSj = li.getElementsByClass("sj").get(0);
                book.setType(type);
                book.setName(aName.attr("title"));
                book.setChapterUrl(aName.attr("href"));
                book.setNewestChapterTitle(divZz.text());
                book.setAuthor(divAuthor.text());
                book.setUpdateDate(divSj.text());
                book.setSource(LocalBookSource.qb5.toString());
                books.add(book);
            }
        }

        return books;

    }

    public boolean getTypePage(BookType curType, int page) {
        if (curType.getPageSize() <= 0) {
            curType.setPageSize(10);
        }
        if (page > curType.getPageSize()) {
            return true;
        }
        if (!curType.getTypeName().equals("完本小说")) {
            curType.setUrl(curType.getUrl().substring(0, curType.getUrl().lastIndexOf("_") + 1) + page + "/");
        } else {
            curType.setUrl(curType.getUrl().substring(0, curType.getUrl().lastIndexOf("/") + 1) + page);
        }
        return false;
    }

}
