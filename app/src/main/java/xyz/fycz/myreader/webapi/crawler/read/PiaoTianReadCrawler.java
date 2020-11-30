package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

public class PiaoTianReadCrawler implements ReadCrawler, BookInfoCrawler {
    public static final String NAME_SPACE = "https://www.piaotian.org";
    public static final String NOVEL_SEARCH = "https://www.piaotian.org/modules/article/search.php?searchkey={key}&submit=%CB%D1%CB%F7";
    public static final String CHARSET = "GBK";
    public static final String SEARCH_CHARSET = "GBK";
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
     * @param html
     * @return
     */
    public String getContentFormHtml(String html) {
        Document doc = Jsoup.parse(html);
        Element divContent = doc.getElementById("htmlContent");
        if (divContent != null) {
            String content = Html.fromHtml(divContent.html()).toString();
            char c = 160;
            String spaec = "" + c;
            content = content.replace(spaec, "  ").replaceAll("飘天文学.*最新章节！", "");
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
        String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
        Element ul = doc.getElementsByClass("chapterlist").get(1);
        Elements elementsByTag = ul.getElementsByTag("a");
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
     * @param html
     * @return
     */
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
        String urlType = doc.select("meta[property=og:type]").attr("content");
        if ("novel".equals(urlType)) {
            String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
            Book book = new Book();
            book.setChapterUrl(readUrl);
            getBookInfo(html, book);
            book.setSource(BookSource.paiotian.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        } else {
            Elements divs = doc.getElementsByClass("grid");
            Element div = divs.get(0);
            Elements trs = div.getElementsByTag("tr");
            for (int i = 1; i < trs.size(); i++) {
                Element tr = trs.get(i);
                Book book = new Book();
                Elements info = tr.getElementsByTag("td");
                book.setName(info.get(0).text());
                book.setChapterUrl(info.get(0).getElementsByTag("a").attr("href"));
                book.setAuthor(info.get(2).text());
                book.setNewestChapterTitle(info.get(1).text());
                book.setUpdateDate(info.get(4).text());
                book.setDesc("");
                book.setSource(BookSource.paiotian.toString());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        }
        return books;
    }

    /**
     * 获取书籍详细信息
     * @param book
     */
    public Book getBookInfo(String html, Book book){
        Document doc = Jsoup.parse(html);
        Element name = doc.selectFirst("meta[property=og:novel:book_name]");
        book.setName(name.attr("content"));
        Element author = doc.selectFirst("meta[property=og:novel:author]");
        book.setAuthor(author.attr("content"));
        Element newestChapter = doc.selectFirst("meta[property=og:novel:latest_chapter_name]");
        book.setNewestChapterTitle(newestChapter.attr("content"));
        Element img = doc.selectFirst("meta[property=og:image]");
        book.setImgUrl(img.attr("content"));
        Element desc = doc.getElementsByClass("bookinfo_intro").first();
        book.setDesc(desc.text());
        Element type = doc.selectFirst("meta[property=og:novel:category]");
        book.setType(type.attr("content"));
        return book;
    }

}
