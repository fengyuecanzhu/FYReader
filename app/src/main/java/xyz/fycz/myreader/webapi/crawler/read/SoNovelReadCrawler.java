package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;
import java.util.List;


public class SoNovelReadCrawler implements ReadCrawler, BookInfoCrawler {
    public static final String NAME_SPACE = "https://www.soxs.cc";
    public static final String NOVEL_SEARCH = "https://www.soxs.cc/search.html,searchtype=all&searchkey={key}&action=search&submit= 搜  索 ";
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
        return true;
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
        Element divContent = doc.getElementsByClass("content").first();
        if (divContent != null) {
            StringBuilder sb = new StringBuilder();
            for (TextNode textNode : divContent.textNodes()){
                sb.append(textNode.text());
                sb.append("\n");
            }
            String content = sb.toString();
            char c = 160;
            String spaec = "" + c;
            content = content.replace(spaec, "  ").replaceAll("\\s*您可以.*最.*章节.*", "");
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
        Element divList = doc.getElementsByClass("novel_list").get(1);
        Elements elementsByTag = divList.getElementsByTag("dd");
        int i = 0;
        for (Element dd : elementsByTag) {
            Element a = dd.getElementsByTag("a").first();
            String title = a.text();
            String url = NAME_SPACE + a.attr("href");
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
        try {
            Document doc = Jsoup.parse(html);
            Elements divs = doc.getElementsByClass("novelslist2");
            Element div = divs.get(0);
            Elements elementsByTag = div.getElementsByTag("li");
            for (int i = 1; i < elementsByTag.size(); i++) {
                Element element = elementsByTag.get(i);
                Book book = new Book();
                Element info = element.getElementsByClass("s2").first();
                book.setName(info.text());
                book.setChapterUrl(NAME_SPACE + info.getElementsByTag("a").attr("href"));
                book.setAuthor(element.getElementsByClass("s4").first().text());
                book.setType(element.getElementsByTag("span").first().text().replace("[", "").replace("]", ""));
                book.setNewestChapterTitle(element.getElementsByClass("s3").first().text());
                book.setSource(BookSource.sonovel.toString());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        Element img = doc.getElementsByClass("book_cover").first();
        book.setImgUrl(img.getElementsByTag("img").get(0).attr("src"));
        List<TextNode> descNodes = doc.getElementById("intro").textNodes();
        if (descNodes != null && descNodes.size() > 0) {
            String desc = descNodes.get(0).text();
            book.setDesc(desc);
        }

        return book;
    }

}
