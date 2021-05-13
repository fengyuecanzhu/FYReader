package xyz.fycz.myreader.webapi.crawler.base;

import java.util.Map;

/**
 * @author fengyue
 * @date 2021/5/13 22:29
 */
public abstract class BaseLocalCrawler implements ReadCrawler {
    @Override
    public Map<String, String> getHeaders() {
        return null;
    }
}
