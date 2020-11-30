package xyz.fycz.myreader.webapi.crawler.base;

import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.greendao.entity.Book;

import java.io.Serializable;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/9/14 18:36
 */
public abstract class FindCrawler implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract String getCharset();
    public abstract String getFindName();
    public abstract String getFindUrl();
    public abstract boolean hasImg();
    public abstract boolean needSearch();
    //动态获取
    public List<BookType> getBookTypes(String html) {
        return null;
    }
    //静态获取
    public List<BookType> getBookTypes(){
        return null;
    }
    public abstract List<Book> getFindBooks(String html, BookType bookType);
    public abstract boolean getTypePage(BookType curType, int page);
}
