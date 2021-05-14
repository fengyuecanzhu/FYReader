package xyz.fycz.myreader.webapi.crawler.base;

import java.util.ArrayList;
import java.util.Map;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;

/**
 * 因新版书源使用StrResponse，为了兼容旧版本，书源全部继承自此类
 * @author fengyue
 * @date 2021/5/13 22:29
 */
public abstract class BaseReadCrawler implements ReadCrawler {
    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    @Override
    public ConMVMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        return null;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        return null;
    }

    @Override
    public String getContentFormHtml(String html) {
        return null;
    }
}
