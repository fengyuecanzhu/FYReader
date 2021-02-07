package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;


public class LuoQiuReadCrawler implements ReadCrawler, BookInfoCrawler {
    public static final String NAME_SPACE = "https://www.lqbook.com";
    public static final String NOVEL_SEARCH = "https://www.lqbook.com/modules/article/search.php?searchkey={key}&submit=%CB%D1%CB%F7";
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
        content = content.replace(spaec, "  ")
                .replaceAll("^.*最新章节！", "");
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
        Element divList = doc.selectFirst(".zjlist");
        String lastTile = null;
        int i = 0;
        Elements elementsByTag = divList.getElementsByTag("a");
        for (int j = 0; j < elementsByTag.size(); j++) {
            Element a = elementsByTag.get(j);
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
     * @return <tr>
     * <td class="odd" align="center"><a href="https://www.lqbook.com/book_91153/">文娱大主宰</a></td>
     * <td class="even" align="center"><a href="https://www.lqbook.com/book_91153/52814257.html" target="_blank" title=" 很抱歉，是时候结束了。">很抱歉，是时候结束了。</a></td>
     * <td class="odd" align="center">羽林都督</td>
     * <td class="even" align="center">480K</td>
     * <td class="odd" align="center">20-10-02</td>
     * <td class="even" align="center">连载</td>
     * </tr>
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
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        } else {
            Element div = doc.getElementById("main");
            Elements elements = div.getElementsByTag("tr");
            for (int i = 1; i < elements.size(); i++) {
                Element element = elements.get(i);
                Book book = new Book();
                Elements info = element.getElementsByTag("td");
                book.setName(info.get(0).text());
                book.setChapterUrl(info.get(0).selectFirst("a").attr("href"));
                book.setAuthor(info.get(2).text());
                book.setNewestChapterTitle(info.get(1).text());
                book.setSource(BookSource.luoqiu.toString());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
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
        book.setSource(BookSource.luoqiu.toString());

        String name = doc.select("meta[property=og:title]").attr("content");
        book.setName(name);
        String url = doc.select("meta[property=og:novel:read_url]").attr("content");
        book.setChapterUrl(url);
        String author = doc.select("meta[property=og:novel:author]").attr("content");
        book.setAuthor(author);
        String newestChapter = doc.select("meta[property=og:novel:latest_chapter_name]").attr("content");
        book.setNewestChapterTitle(newestChapter);

        Element img = doc.getElementById("picbox");
        book.setImgUrl(img.getElementsByTag("img").get(0).attr("src"));
        Element desc = doc.getElementById("intro");
        book.setDesc(Html.fromHtml(desc.html()).toString());
        //类型
        String type = doc.select("meta[property=og:novel:category]").attr("content");
        book.setType(type);
        return book;
    }

}
