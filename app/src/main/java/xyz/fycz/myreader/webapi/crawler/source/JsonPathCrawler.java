package xyz.fycz.myreader.webapi.crawler.source;

import com.jayway.jsonpath.ReadContext;


import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.SearchRule;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.model.sourceAnalyzer.JsonPathAnalyzer;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BaseSourceCrawler;

/**
 * @author fengyue
 * @date 2021/2/14 17:52
 */
public class JsonPathCrawler extends BaseSourceCrawler {

    private final JsonPathAnalyzer analyzer;

    public JsonPathCrawler(BookSource source) {
        super(source, new JsonPathAnalyzer());
        this.analyzer = (JsonPathAnalyzer) super.analyzer;
    }

    @Override
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String json) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        ReadContext rc = analyzer.getReadContext(json);
        SearchRule searchRule = source.getSearchRule();
        if (StringHelper.isEmpty(searchRule.getList())) {
            getBooksNoList(rc, searchRule, books);
        } else {
            getBooks(json, searchRule, books);
        }
        return books;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String json) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        ReadContext rc = analyzer.getReadContext(json);
        getChapters(rc, chapters);
        return chapters;
    }

    @Override
    public String getContentFormHtml(String json) {
        ReadContext rc = analyzer.getReadContext(json);
        return getContent(rc);
    }

    @Override
    public Book getBookInfo(String json, Book book) {
        ReadContext rc = analyzer.getReadContext(json);
        return getBookInfo(rc, book);
    }

    protected Object getListObj(Object obj) {
        return analyzer.getReadContext(obj);
    }

    @Override
    protected List getList(String str, Object obj) {
        return analyzer.getReadContextList(str, analyzer.getReadContext(obj));
    }
}
