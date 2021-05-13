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
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.BaseLocalCrawler;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Deprecated
public class Ben100ReadCrawler extends BaseLocalCrawler implements BookInfoCrawler {
    public static final String NAME_SPACE = "https://www.100ben.net";
    public static final String NOVEL_SEARCH = "https://www.100ben.net/plus/search.php?keyword={key}";
    public static final String CHARSET = "UTF-8";
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
        try {
            Element divList = doc.getElementById("dir");
            int i = 0;
            Elements elementsByTag = divList.getElementsByTag("dd");
            for (int j = 0; j < elementsByTag.size(); j++) {
                Element dd = elementsByTag.get(j);
                Elements as = dd.getElementsByTag("a");
                Element a = as.get(0);
                String title = a.text();
                Chapter chapter = new Chapter();
                chapter.setNumber(i++);
                chapter.setTitle(title);
                String url = a.attr("href");
                chapter.setUrl(url);
                chapters.add(chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
//        try {
        Elements divs = doc.getElementsByClass("recommand");
        Element div = divs.get(0);
        Elements elementsByTag = div.getElementsByTag("li");
        for (Element element : elementsByTag) {
            Book book = new Book();
            String name = element.getElementsByClass("titles").first().getElementsByTag("a").first().text();
            book.setName(name);
            String author = element.getElementsByClass("author").first().text().replace("作者：", "");
            book.setAuthor(author);
            String imgUrl = element.getElementsByTag("img").first().attr("src");
            book.setImgUrl(imgUrl);
            String chapterUrl = element.getElementsByClass("titles").first().getElementsByTag("a").first().attr("href");
            book.setChapterUrl(chapterUrl);
            String desc = element.getElementsByClass("intro").first().text();
            book.setDesc(desc);
            book.setNewestChapterTitle("");
            book.setIsCloseUpdate(true);
            book.setSource(LocalBookSource.ben100.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
