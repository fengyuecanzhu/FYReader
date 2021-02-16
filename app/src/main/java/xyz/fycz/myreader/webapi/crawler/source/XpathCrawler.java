package xyz.fycz.myreader.webapi.crawler.source;

import org.seimicrawler.xpath.JXDocument;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.SearchRule;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.model.source.XpathAnalyzer;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BaseSourceCrawler;

/**
 * @author fengyue
 * @date 2021/2/14 17:52
 */
public class XpathCrawler extends BaseSourceCrawler {
    private final XpathAnalyzer analyzer;

    public XpathCrawler(BookSource source) {
        super(source, new XpathAnalyzer());
        this.analyzer = (XpathAnalyzer) super.analyzer;
    }

    @Override
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        JXDocument jxDoc = JXDocument.create(html);
        SearchRule searchRule = source.getSearchRule();
        if (StringHelper.isEmpty(searchRule.getList())) {
            getBooksNoList(jxDoc, searchRule, books);
        } else {
            getBooks(jxDoc, searchRule, books);
        }
        return books;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        JXDocument jxDoc = JXDocument.create(html);
        getChapters(jxDoc, chapters);
        return chapters;
    }


    @Override
    public String getContentFormHtml(String html) {
        JXDocument jxDoc = JXDocument.create(html);
        return getContent(jxDoc);
    }

    @Override
    public Book getBookInfo(String html, Book book) {
        JXDocument jxDoc = JXDocument.create(html);
        return getBookInfo(jxDoc, book);
    }


    protected List getList(String str, Object obj) {
        return analyzer.getJXNodeList(str, obj);
    }
}
