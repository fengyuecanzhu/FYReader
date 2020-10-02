package xyz.fycz.myreader.webapi.crawler.base;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;

import java.util.ArrayList;

/**
 * @author fengyue
 * @date 2020/4/28 16:18
 */

public interface ReadCrawler {
    String getSearchLink();
    String getCharset();
    String getSearchCharset();
    String getNameSpace();
    Boolean isPost();
    String getContentFormHtml(String html);
    ArrayList<Chapter> getChaptersFromHtml(String html);
    ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html);
}
