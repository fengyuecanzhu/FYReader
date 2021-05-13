package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BaseLocalCrawler;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;


public class BiQuGe44ReadCrawler extends BaseLocalCrawler implements BookInfoCrawler {
//    public static final String NAME_SPACE = "http://www.wqge.net";
    public static final String NAME_SPACE = "https://www.wqge.cc";
    public static final String NOVEL_SEARCH = "https://www.wqge.cc/modules/article/search.php?searchkey={key}";
    public static final String CHARSET = "GBK";
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
        Document doc = Jsoup.parse(html);
        Element divContent = doc.getElementById("content");
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
        Element divList = doc.getElementById("list");
        String lastTile = null;
        int i = 0;
        Elements elementsByTag = divList.getElementsByTag("dd");
        for (int j = 9; j < elementsByTag.size(); j++) {
            Element dd = elementsByTag.get(j);
            Elements as = dd.getElementsByTag("a");
            if (as.size() > 0) {
                Element a = as.get(0);
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
        Elements divs = doc.getElementsByTag("table");
        Element div = divs.get(0);
        Elements elementsByTag = div.getElementsByTag("tr");
        for (int i = 1; i < elementsByTag.size(); i++) {
            Element element = elementsByTag.get(i);
            Book book = new Book();
            Elements info = element.getElementsByTag("td");
            book.setName(info.get(0).text());
            book.setChapterUrl(info.get(0).getElementsByTag("a").attr("href"));
            book.setAuthor(info.get(2).text());
            book.setNewestChapterTitle(info.get(1).text());
            book.setSource(LocalBookSource.biquge44.toString());
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
        Element img = doc.getElementById("fmimg");
        book.setImgUrl(img.getElementsByTag("img").get(0).attr("src"));
        Element desc = doc.getElementById("intro");
        book.setDesc(desc.getElementsByTag("p").get(0).text());
        Element type = doc.getElementsByClass("con_top").get(0);
        book.setType(type.getElementsByTag("a").get(2).text());
        return book;
    }

}
