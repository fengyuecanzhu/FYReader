package xyz.fycz.myreader.webapi.crawler.read;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;


public class YunZhongReadCrawler implements ReadCrawler, BookInfoCrawler {
    public static final String NAME_SPACE = "http://www.yunxs.com";
    public static final String NOVEL_SEARCH = "http://www.yunxs.com/plus/search.php?q={key}";
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
        Element divContent = doc.getElementsByClass("box_box").first();
        StringBuilder sb = new StringBuilder();
        for (TextNode textNode : divContent.textNodes()) {
            sb.append(textNode.text());
            sb.append("\n");
        }
        String content = sb.toString();
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
        try {
            Document doc = Jsoup.parse(html);
            String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
            Element divList = doc.getElementsByClass("list_box").first();
            Elements elementsByTag = divList.getElementsByTag("a");
            int i = 0;
            for (Element a : elementsByTag) {
                String title = a.text();
                String url = readUrl + a.attr("href");
                Chapter chapter = new Chapter();
                chapter.setNumber(i++);
                chapter.setTitle(title);
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
//        try {
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("ul_b_list");
        Element div = divs.get(0);
        Elements elementsByTag = div.getElementsByTag("li");
        for (Element element : elementsByTag) {
            Book book = new Book();
            Element info = element.getElementsByTag("h2").first();
            book.setName(info.getElementsByTag("a").first().text());
            book.setType(info.getElementsByTag("span").first().text());
            book.setAuthor(element.getElementsByClass("state").first().text().replaceAll("作者.|类型.*", ""));
            book.setImgUrl(NAME_SPACE + element.getElementsByClass("pic").first().getElementsByTag("img").first().attr("src"));
            book.setChapterUrl(element.getElementsByTag("a").first().attr("href"));
            book.setNewestChapterTitle("");
            book.setSource(LocalBookSource.yunzhong.toString());
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

        try {
            String newestChapter = doc.select("meta[property=og:novel:latest_chapter_name]").attr("content");
            book.setNewestChapterTitle(newestChapter);
            String desc = doc.getElementsByClass("words").first().getElementsByTag("p").get(2).text().replace("简介：", "");
            book.setDesc(desc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return book;
    }

}
