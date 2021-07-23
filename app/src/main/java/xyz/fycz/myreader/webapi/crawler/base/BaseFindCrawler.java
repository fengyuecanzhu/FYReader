package xyz.fycz.myreader.webapi.crawler.base;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import xyz.fycz.myreader.entity.FindKind;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.greendao.entity.Book;

/**
 * @author fengyue
 * @date 2021/7/21 20:26
 */
public abstract class BaseFindCrawler implements FindCrawler {
    protected Map<String, List<FindKind>> kindsMap = new LinkedHashMap<>();

    @Override
    public List<String> getGroups() {
        return new ArrayList<>(kindsMap.keySet());
    }

    @Override
    public Map<String, List<FindKind>> getKindsMap() {
        return kindsMap;
    }

    @Override
    public List<FindKind> getKindsByKey(String key) {
        return kindsMap.get(key);
    }

    @Override
    public abstract Observable<Boolean> initData();

    @Override
    public boolean needSearch() {
        return false;
    }

}
