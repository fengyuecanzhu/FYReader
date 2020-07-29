package xyz.fycz.myreader.crawler;

import xyz.fycz.myreader.greendao.entity.Book;

/**
 * @author fengyue
 * @date 2020/5/19 19:50
 */
public interface BookInfoCrawler {
    String getCharset();
    Book getBookInfo(String html, Book book);
}
