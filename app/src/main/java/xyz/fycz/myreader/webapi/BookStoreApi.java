package xyz.fycz.myreader.webapi;


import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.webapi.crawler.BiQuGeReadCrawler;
import xyz.fycz.myreader.webapi.crawler.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.QB5ReadCrawler;

/**
 * Created by zhao on 2017/7/24.
 */

public class BookStoreApi extends BaseApi{


    /**
     * 获取书城小说分类列表
     * @param findCrawler
     * @param callback
     */
    public static void getBookTypeList(FindCrawler findCrawler, final ResultCallback callback){

        getCommonReturnHtmlStringApi(findCrawler.getFindUrl(), null, findCrawler.getCharset(), new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(findCrawler.getBookTypeList((String) o),0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);

            }
        });
    }


    /**
     * 获取某一分类小说排行榜列表
     * @param findCrawler
     * @param callback
     */
    public static void getBookRankList(String url, FindCrawler findCrawler, final ResultCallback callback){

        getCommonReturnHtmlStringApi(url, null, findCrawler.getCharset(), new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(findCrawler.getRankBookList((String) o),0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);

            }
        });
    }


}
