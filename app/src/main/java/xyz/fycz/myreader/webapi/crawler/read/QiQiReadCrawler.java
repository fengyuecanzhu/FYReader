package xyz.fycz.myreader.webapi.crawler.read;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;

/**
 * 已失效
 */
@Deprecated
public class QiQiReadCrawler implements ReadCrawler {
    public static final String NAME_SPACE = "https://www.qq717.com";
    public static final String NOVEL_SEARCH = "https://www.qq717.com/search.php?keyword={key}";
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
        StringBuilder sb = new StringBuilder();
        for (TextNode textNode : divContent.textNodes()) {
            sb.append(textNode.text());
            sb.append("\n");
        }
        String content = sb.toString();
        char c = 160;
        String spaec = "" + c;
        content = content.replace(spaec, "  ").replaceAll("奇奇小说全网.*com|更新最.*com\\\\/|哽噺.*com|www.*com\"", "");
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
        Element divList = doc.getElementById("list");
        Elements elementsByClass = divList.getElementsByTag("dd");
        int i = 0;
        for (Element div : elementsByClass) {
            Element a = div.getElementsByTag("a").first();
            String title = a.text();
            String url = a.attr("href");
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
            Elements divs = doc.getElementsByClass("result-list");
            Element div = divs.get(0);
            Elements elementsByTag = div.getElementsByClass("result-item");
            for (Element element : elementsByTag) {
                Book book = new Book();
                Element info = element.getElementsByTag("h3").first();
                book.setName(info.getElementsByTag("a").first().text());
                book.setChapterUrl(info.getElementsByTag("a").first().attr("href"));
                book.setAuthor(element.getElementsByTag("span").get(1).text());
                book.setType(element.getElementsByTag("span").get(3).text());
                book.setNewestChapterTitle(element.getElementsByTag("p").get(4).getElementsByTag("a").first().text());
                book.setImgUrl(element.getElementsByTag("img").first().attr("src"));
                book.setDesc(element.getElementsByClass("result-game-item-desc").text());
                book.setSource("qiqi");
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }


}
