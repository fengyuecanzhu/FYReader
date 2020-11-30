package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;
import java.util.List;


public class QB5ReadCrawler extends FindCrawler implements ReadCrawler, BookInfoCrawler {
    private static final String NAME_SPACE = "https://www.qb5.tw";
    private static final String NOVEL_SEARCH = "https://www.qb5.tw/modules/article/search.php?searchkey={key}&submit=%CB%D1%CB%F7";
    public static final String FIND_URL = "https://www.qb5.tw";
    public static final String FIND_NAME = "书城[全本小说]";
    private static final String CHARSET = "GBK";
    public static final String SEARCH_CHARSET = "GBK";

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
    public String getFindName() {
        return FIND_NAME;
    }

    @Override
    public String getFindUrl() {
        return FIND_URL;
    }

    @Override
    public String getSearchCharset() {
        return SEARCH_CHARSET;
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
                    if (bookType.getTypeName().contains("首页") || bookType.getTypeName().contains("热门小说")) continue;
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
                    book.setSource(BookSource.biquge.toString());
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
        }catch (Exception ignored){}
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
                book.setSource(BookSource.qb5.toString());
                books.add(book);
            }
        }

        return books;

    }

    @Override
    public String getContentFormHtml(String html) {
        Document doc = Jsoup.parse(html);
        Element divBook = doc.getElementsByClass("nav-style").get(0);
        String bookName = divBook.getElementsByTag("a").get(1).attr("title");
        Element divContent = doc.getElementById("content");
        if (divContent != null) {
            String content = Html.fromHtml(divContent.html()).toString();
            char c = 160;
            String spaec = "" + c;
            content = content.replace(spaec, "  ");
            content = content.replaceAll("全本小说.*最新章节！", "");
            return content;
        } else {
            return "";
        }
    }


    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
        int num = 0;
        Element zjbox = doc.getElementsByClass("zjbox").get(0);
        Elements as = zjbox.getElementsByTag("a");
        for (int i = 12; i < as.size(); i++) {
            Element a = as.get(i);
            Chapter chapter = new Chapter();
            chapter.setNumber(num++);
            chapter.setTitle(a.text());
            chapter.setUrl(readUrl + a.attr("href"));
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
        final ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
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
            Elements divs = doc.getElementsByClass("grid");
            Element div = divs.get(0);
            Elements elementsByTag = div.getElementsByTag("tr");
            for (int i = 1; i < elementsByTag.size(); i++) {
                Element element = elementsByTag.get(i);
                Book book = new Book();
                Elements info = element.getElementsByTag("td");
                book.setName(info.get(0).text());
                book.setChapterUrl(info.get(0).getElementsByTag("a").attr("href"));
                book.setNewestChapterTitle(info.get(1).text());
                book.setAuthor(info.get(2).text());
                book.setSource(BookSource.qb5.toString());
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
        book.setSource(BookSource.qb5.toString());
        Document doc = Jsoup.parse(html);
        //书名
        String name = doc.select("meta[property=og:title]").attr("content");
        book.setName(name);
        //作者
        String author = doc.select("meta[property=og:novel:author]").attr("content");
        book.setAuthor(author);
        //最新章节
        String newestChapter = doc.select("meta[property=og:novel:latest_chapter_name]").attr("content");
        book.setNewestChapterTitle(newestChapter);
        //更新时间
        String updateTime = doc.select("meta[property=og:novel:update_time]").attr("content");
        book.setUpdateDate(updateTime);
        //图片url
        Element divImg = doc.getElementsByClass("img_in").get(0);
        Element img = divImg.getElementsByTag("img").get(0);
        book.setImgUrl(img.attr("src"));
        //简介
        Element divIntro = doc.getElementById("intro");
        book.setDesc(divIntro.text());
        //类型
        String type = doc.select("meta[property=og:novel:category]").attr("content");
        book.setType(type);
        return book;
    }


    public boolean getTypePage(BookType curType, int page){
        if (curType.getPageSize() <= 0){
            curType.setPageSize(10);
        }
        if (page > curType.getPageSize()){
            return true;
        }
        if (!curType.getTypeName().equals("完本小说")) {
            curType.setUrl(curType.getUrl().substring(0, curType.getUrl().lastIndexOf("_") + 1) + page + "/");
        }else {
            curType.setUrl(curType.getUrl().substring(0, curType.getUrl().lastIndexOf("/") + 1) + page);
        }
        return false;
    }

}
