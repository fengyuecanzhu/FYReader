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
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;


public class MiQuReadCrawler implements ReadCrawler, BookInfoCrawler {
    public static final String NAME_SPACE = "https://www.meegoq.com/";
    public static final String NOVEL_SEARCH = "https://www.meegoq.com/search.htm?keyword={key}";
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
        if (divContent != null) {
            String content = Html.fromHtml(divContent.html()).toString();
            char c = 160;
            String spaec = "" + c;
            content = content.replace(spaec, "  ").replace("applyChapterSetting();", "");
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
        Element divList = doc.getElementsByClass("mulu").first();
        Elements elementsByTag = divList.getElementsByTag("a");
        int i = 0;
        for (int j = 9; j < elementsByTag.size(); j++) {
            Element a = elementsByTag.get(j);
            String title = a.text();
            String url = "http:" + a.attr("href");
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
     * @return
     */
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("lastest");
        Element div = divs.get(0);
        Elements elementsByTag = div.getElementsByTag("li");
        for (int i = 1; i < elementsByTag.size() - 1; i++) {
            Element element = elementsByTag.get(i);
            Book book = new Book();
            Element info = element.getElementsByClass("n2").first();
            book.setName(info.text());
            book.setInfoUrl("http:" + info.getElementsByTag("a").attr("href"));
            book.setChapterUrl("http:" + info.getElementsByTag("a").attr("href").replace("info", "book"));
            book.setAuthor(element.getElementsByClass("a2").first().text());
            book.setType(element.getElementsByClass("nt").first().text());
            book.setNewestChapterTitle(element.getElementsByClass("c2").first().text());
            book.setSource(BookSource.miqu.toString());
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
        Element img = doc.getElementsByClass("cover").first();
        book.setImgUrl(img.getElementsByTag("img").get(0).attr("src"));

        String desc = doc.select("meta[property=og:description]").attr("content");
        book.setDesc(desc);

        return book;
    }

}
