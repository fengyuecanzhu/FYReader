package xyz.fycz.myreader.webapi.crawler;

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
    public abstract boolean getTypePage(BookType curType, int page);
    public abstract boolean hasImg();
    public abstract List<BookType> getBookTypeList(String html);
    public abstract List<Book> getRankBookList(String html);
}
