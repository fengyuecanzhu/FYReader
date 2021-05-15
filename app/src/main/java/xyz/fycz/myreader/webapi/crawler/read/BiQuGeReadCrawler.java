package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BaseReadCrawler;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BiQuGeReadCrawler extends BaseReadCrawler implements BookInfoCrawler {
    private static final String NAME_SPACE = "https://www.52bqg.net";
    private static final String NOVEL_SEARCH = "https://www.52bqg.net/modules/article/search.php?searchkey={key}";
    private static final String CHARSET = "GBK";
    public static final String SEARCH_CHARSET = "GBK";

    private ReadCrawler rc = new TianLaiReadCrawler();

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

    /**
     * 获取书城小说分类列表
     *
     * @param html
     * @return
     */
    public static List<BookType> getBookTypeList(String html) {
        List<BookType> bookTypes = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("nav");
        if (divs.size() > 0) {
            Elements uls = divs.get(0).getElementsByTag("ul");
            if (uls.size() > 0) {
                for (Element li : uls.get(0).children()) {
                    Element a = li.child(0);
                    BookType bookType = new BookType();
                    bookType.setTypeName(a.attr("title"));
                    bookType.setUrl(a.attr("href"));
                    if (!bookType.getTypeName().contains("小说") || bookType.getTypeName().contains("排行")) continue;
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
    public static List<Book> getBookRankList(String html) {
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

    public static List<Book> getLatestBookList(String html) {
        List<Book> books = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Element div = doc.getElementById("newscontent");
        Elements uls = div.getElementsByTag("ul");
        if (uls.size() > 0) {
            for (Element li : uls.get(0).children()) {
                Book book = new Book();
                Element scanS1 = li.getElementsByClass("s1").get(0);
                Element scanS2 = li.getElementsByClass("s2").get(0);
                Element scanS3 = li.getElementsByClass("s3").get(0);
                Element scanS4 = li.getElementsByClass("s4").get(0);
                Element scanS5 = li.getElementsByClass("s5").get(0);
                book.setType(scanS1.text().replace("[", "").replace("]", ""));
                Element a = scanS2.getElementsByTag("a").get(0);
                book.setName(a.attr("title"));
                book.setChapterUrl(a.attr("href"));
                book.setNewestChapterTitle(scanS3.text());
                book.setAuthor(scanS4.text());
                book.setUpdateDate(scanS5.text());
                book.setSource(LocalBookSource.biquge.toString());
                books.add(book);
            }
        }

        return books;

    }

    @Override
    public String getContentFormHtml(String html) {
        return rc.getContentFormHtml(html);
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        return rc.getChaptersFromHtml(html);
    }

    /**
     * 从搜索html中得到书列表
     *
     * @param html
     * @return
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
            Elements divs = doc.getElementsByClass("novelslistss");
            Element div = divs.get(0);
            Elements elementsByTag = div.getElementsByTag("li");
            for (Element element : elementsByTag) {
                Book book = new Book();
                Elements info = element.getElementsByTag("span");
                book.setName(info.get(1).text());
                book.setChapterUrl(info.get(1).getElementsByTag("a").attr("href"));
                book.setAuthor(info.get(3).text());
                book.setNewestChapterTitle(info.get(2).text());
                book.setSource(LocalBookSource.biquge.toString());
                book.setType(info.get(0).text());
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
        //小说源
        book.setSource(LocalBookSource.biquge.toString());
        Document doc = Jsoup.parse(html);
        //图片url
        Element divImg = doc.getElementById("fmimg");
        Element img = divImg.getElementsByTag("img").get(0);
        book.setImgUrl(img.attr("src"));
        Element divInfo = doc.getElementById("info");

        //书名
        Element h1 = divInfo.getElementsByTag("h1").get(0);
        book.setName(h1.html());

        Elements ps = divInfo.getElementsByTag("p");

        //作者
        Element p0 = ps.get(0);
        Element a = p0.getElementsByTag("a").get(0);
        book.setAuthor(a.html());

        //更新时间
        Element p2 = ps.get(2);

        Pattern pattern = Pattern.compile("更新时间：(.*)&nbsp;");
        Matcher matcher = pattern.matcher(p2.html());
        if (matcher.find()) {
            book.setUpdateDate(matcher.group(1));
        }

        //最新章节
        Element p3 = ps.get(3);
        a = p3.getElementsByTag("a").get(0);
        book.setNewestChapterTitle(a.attr("title"));

        //类型
        String type = doc.select("meta[property=og:novel:category]").attr("content");
        book.setType(type);

        //简介
        Element divIntro = doc.getElementById("intro");
        book.setDesc(Html.fromHtml(divIntro.html()).toString());
        return book;

    }


}
