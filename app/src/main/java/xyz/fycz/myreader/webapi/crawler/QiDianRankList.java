package xyz.fycz.myreader.webapi.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.greendao.entity.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/5/27 11:17
 */
public class QiDianRankList {

    public static final String[] TYPE_NAME = {
            "月票榜", "畅销榜", "阅读榜", "推荐榜", "收藏榜"
    };

    /**
     * 获取书城小说分类列表
     *
     * @param html
     * @return
     */
    public static List<BookType> getBookTypeList(String html) {
        List<BookType> bookTypes = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("more");
        for (int i = 0; i < 5; i++) {
            BookType bookType = new BookType();
            bookType.setTypeName(TYPE_NAME[i]);
            bookType.setUrl("https:" + divs.get(i).attr("href"));
            bookTypes.add(bookType);
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
        Elements lis = doc.getElementsByTag("li");
        if (lis.size() > 0) {
            Elements bookLis = lis.select("data-rid");
            if (bookLis.size() > 0) {
                for (Element li : bookLis) {
                    Book book = new Book();
                    String imgSrc = li.getElementsByClass("book-img-box").get(0)
                            .getElementsByTag("img").attr("src");
                    book.setImgUrl("https:" + imgSrc);
                    Element bookInfo = li.getElementsByClass("book-mid-info").get(0);
                    Elements tagA = bookInfo.getElementsByTag("a");
                    book.setName(tagA.get(0).text());
                    book.setAuthor(tagA.get(1).text());
                    book.setType(tagA.get(2).text());
                    book.setNewestChapterTitle(tagA.get(3).text());
                    String desc = bookInfo.getElementsByClass("intro").get(0).text();
                    book.setDesc(desc);
                    books.add(book);
                }
            }
        }
        return books;
    }

}
