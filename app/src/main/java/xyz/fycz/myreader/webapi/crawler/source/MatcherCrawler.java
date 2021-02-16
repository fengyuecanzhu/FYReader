package xyz.fycz.myreader.webapi.crawler.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.model.source.MatcherAnalyzer;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.webapi.crawler.base.BaseSourceCrawler;

/**
 * @author fengyue
 * @date 2021/2/7 17:55
 */
public class MatcherCrawler extends BaseSourceCrawler {

    private final MatcherAnalyzer analyzer;

    public MatcherCrawler(BookSource source) {
        super(source, new MatcherAnalyzer());
        this.analyzer = (MatcherAnalyzer) super.analyzer;
    }

    @Override
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        String list = analyzer.getString(source.getSearchRule().getList(), html);
        if (StringHelper.isEmpty(list)) list = html;
        List<String> names = analyzer.getStringList(source.getSearchRule().getName(), list);
        //未搜索到书籍，按详情页处理
        if (names.size() == 0) {
            Book book = new Book();
            getBookInfo(html, book);
            if(!StringHelper.isEmpty(book.getName())){
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        } else {
            List<String> authors = analyzer.getStringList(source.getSearchRule().getAuthor(), list);
            List<String> types = analyzer.getStringList(source.getSearchRule().getType(), list);
            List<String> descs = analyzer.getStringList(source.getSearchRule().getDesc(), list);
            List<String> wordCounts = analyzer.getStringList(source.getSearchRule().getWordCount(), list);
            List<String> statuss = analyzer.getStringList(source.getSearchRule().getStatus(), list);
            List<String> lastChapters = analyzer.getStringList(source.getSearchRule().getLastChapter(), list);
            List<String> updateTimes = analyzer.getStringList(source.getSearchRule().getUpdateTime(), list);
            List<String> imgs = analyzer.getStringList(source.getSearchRule().getImgUrl(), list);
            List<String> chapterUrls = analyzer.getStringList(source.getSearchRule().getTocUrl(), list);
            List<String> infoUrls = analyzer.getStringList(source.getSearchRule().getInfoUrl(), list);
            String baseUrl = getNameSpace();
            for (int i = 0; i < names.size(); i++) {
                Book book = new Book();
                book.setName(names.get(i));
                book.setSource(source.getSourceUrl());
                if (authors.size() > i) book.setAuthor(authors.get(i));
                if (types.size() > i) book.setType(types.get(i));
                if (descs.size() > i) book.setDesc(descs.get(i));
                if (wordCounts.size() > i) book.setWordCount(wordCounts.get(i));
                if (statuss.size() > i) book.setStatus(statuss.get(i));
                if (lastChapters.size() > i) book.setNewestChapterTitle(lastChapters.get(i));
                if (updateTimes.size() > i) book.setUpdateDate(updateTimes.get(i));
                if (imgs.size() > i)
                    book.setImgUrl(NetworkUtils.getAbsoluteURL(baseUrl, imgs.get(i)));
                if (chapterUrls.size() > i)
                    book.setChapterUrl(NetworkUtils.getAbsoluteURL(baseUrl, chapterUrls.get(i)));
                if (infoUrls.size() > i)
                    book.setInfoUrl(NetworkUtils.getAbsoluteURL(baseUrl, infoUrls.get(i)));
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        }
        return books;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        String list = analyzer.getString(source.getTocRule().getChapterList(), html);
        String baseUrl = NetworkUtils.getAbsoluteURL(getNameSpace(), analyzer.getString(source.getTocRule().getChapterBaseUrl(), html));
        if (StringHelper.isEmpty(baseUrl)) baseUrl = getNameSpace();
        if (StringHelper.isEmpty(list)) list = html;
        ArrayList<Chapter> chapters = analyzer.matchChapters(source.getTocRule().getChapterName(), list, baseUrl);
        getNextPageChapters(html, source.getTocRule(), baseUrl, chapters);
        return chapters;
    }

    @Override
    public String getContentFormHtml(String html) {
        String content = analyzer.getString(source.getContentRule().getContent(), html);
        return getNextPageContent(html, content);
    }

    @Override
    public Book getBookInfo(String html, Book book) {
        return getBookInfo((Object) html, book);
    }
}
