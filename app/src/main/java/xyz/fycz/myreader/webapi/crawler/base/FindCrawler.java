package xyz.fycz.myreader.webapi.crawler.base;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import xyz.fycz.myreader.entity.FindKind;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.greendao.entity.Book;

/**
 * @author fengyue
 * @date 2021/7/21 22:07
 */
public interface FindCrawler {
    String getName();
    String getTag();
    List<String> getGroups();
    Map<String, List<FindKind>> getKindsMap();
    List<FindKind> getKindsByKey(String key);
    Observable<Boolean> initData();
    boolean needSearch();
    Observable<List<Book>> getFindBooks(StrResponse strResponse, FindKind kind);
}
