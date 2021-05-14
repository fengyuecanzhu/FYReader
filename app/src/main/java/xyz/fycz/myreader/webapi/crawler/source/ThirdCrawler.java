package xyz.fycz.myreader.webapi.crawler.source;

import java.util.List;
import java.util.Map;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.BaseReadCrawler;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;

/**
 * @author fengyue
 * @date 2021/5/14 10:55
 */
public class ThirdCrawler extends BaseReadCrawler implements BookInfoCrawler {
    private BookSource source;

    public ThirdCrawler(BookSource source) {
        this.source = source;
    }

    public BookSource getSource() {
        return source;
    }

    @Override
    public String getSearchLink() {
        return source.getSearchRule().getSearchUrl();
    }

    @Override
    public String getCharset() {
        return null;
    }

    @Override
    public Book getBookInfo(String html, Book book) {
        return null;
    }

    @Override
    public String getSearchCharset() {
        return null;
    }

    @Override
    public String getNameSpace() {
        return source.getSourceUrl();
    }

    @Override
    public Boolean isPost() {
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooks(List<Book> books) {
        ConcurrentMultiValueMap<SearchBookBean, Book> newBooks = new ConcurrentMultiValueMap<>();
        for (Book book : books){
            book.setSource(source.getSourceUrl());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            newBooks.add(sbb, book);
        }
        return newBooks;
    }
}
