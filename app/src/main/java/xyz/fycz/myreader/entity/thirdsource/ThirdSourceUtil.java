package xyz.fycz.myreader.entity.thirdsource;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.ContentRule;
import xyz.fycz.myreader.greendao.entity.rule.FindRule;
import xyz.fycz.myreader.greendao.entity.rule.InfoRule;
import xyz.fycz.myreader.greendao.entity.rule.SearchRule;
import xyz.fycz.myreader.greendao.entity.rule.TocRule;

/**
 * @author fengyue
 * @date 2021/5/14 9:24
 */
public class ThirdSourceUtil {
    public static BookSource source2ToSource(BookSourceBean bean) {
        BookSource bookSource = new BookSource();
        bookSource.setSourceUrl(bean.getBookSourceUrl());
        bookSource.setSourceName(bean.getBookSourceName());
        bookSource.setSourceGroup(bean.getBookSourceGroup());
        bookSource.setSourceType(APPCONST.THIRD_SOURCE);
        bookSource.setLastUpdateTime(bean.getLastUpdateTime());
        bookSource.setOrderNum(bean.getSerialNumber());
        bookSource.setWeight(bean.getWeight());
        bookSource.setEnable(bean.getEnable());

        SearchRule searchRule = new SearchRule();
        searchRule.setSearchUrl(bean.getRuleSearchUrl());
        searchRule.setList(bean.getRuleSearchList());
        searchRule.setName(bean.getRuleSearchName());
        searchRule.setAuthor(bean.getRuleSearchAuthor());
        searchRule.setType(bean.getRuleSearchKind());
        searchRule.setDesc(bean.getRuleSearchIntroduce());
        searchRule.setLastChapter(bean.getRuleSearchLastChapter());
        searchRule.setImgUrl(bean.getRuleSearchCoverUrl());
        searchRule.setTocUrl(bean.getRuleSearchNoteUrl());
        searchRule.setRelatedWithInfo(true);
        bookSource.setSearchRule(searchRule);

        InfoRule infoRule = new InfoRule();
        infoRule.setName(bean.getRuleBookName());
        infoRule.setAuthor(bean.getRuleBookAuthor());
        infoRule.setType(bean.getRuleBookKind());
        infoRule.setDesc(bean.getRuleIntroduce());
        infoRule.setLastChapter(bean.getRuleBookLastChapter());
        infoRule.setImgUrl(bean.getRuleCoverUrl());
        infoRule.setTocUrl(bean.getRuleChapterUrl());
        bookSource.setInfoRule(infoRule);

        TocRule tocRule = new TocRule();
        tocRule.setChapterList(bean.getRuleChapterList());
        tocRule.setChapterName(bean.getRuleChapterName());
        tocRule.setChapterUrl(bean.getRuleContentUrl());
        tocRule.setTocUrlNext(bean.getRuleChapterUrlNext());
        bookSource.setTocRule(tocRule);

        ContentRule contentRule = new ContentRule();
        contentRule.setContent(bean.getRuleBookContent());
        contentRule.setContentUrlNext(bean.getRuleContentUrlNext());
        bookSource.setContentRule(contentRule);

        FindRule findRule = new FindRule();
        findRule.setUrl(bean.getRuleFindUrl());
        findRule.setBookList(bean.getRuleFindList());
        findRule.setName(bean.getRuleFindName());
        findRule.setAuthor(bean.getRuleFindAuthor());
        findRule.setType(bean.getRuleFindKind());
        findRule.setDesc(bean.getRuleFindIntroduce());
        findRule.setLastChapter(bean.getRuleFindLastChapter());
        findRule.setImgUrl(bean.getRuleFindCoverUrl());
        findRule.setTocUrl(bean.getRuleFindNoteUrl());
        bookSource.setFindRule(findRule);
        return bookSource;
    }

    public static BookSource source3ToSource(BookSource3Bean bean) {
        BookSource bookSource = source2ToSource(bean.toBookSourceBean());
        bookSource.setSourceComment(bean.getBookSourceComment());
        return bookSource;
    }

    public static List<BookSource> source2sToSources(List<BookSourceBean> beans){
        List<BookSource> sources = new ArrayList<>();
        for (BookSourceBean bean : beans){
            sources.add(source2ToSource(bean));
        }
        return sources;
    }

    public static List<BookSource> source3sToSources(List<BookSource3Bean> beans){
        List<BookSource> sources = new ArrayList<>();
        for (BookSource3Bean bean : beans){
            sources.add(source3ToSource(bean));
        }
        return sources;
    }
}