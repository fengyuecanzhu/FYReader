package xyz.fycz.myreader.webapi;


import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.crawler.BiQuGeReadCrawler;

/**
 * Created by zhao on 2017/7/24.
 */

public class BookStoreApi extends BaseApi{


    /**
     * 获取书城小说分类列表
     * @param url
     * @param callback
     */
    public static void getBookTypeList(String url, final ResultCallback callback){

        getCommonReturnHtmlStringApi(url, null, "GBK", new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(BiQuGeReadCrawler.getBookTypeList((String) o),0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);

            }
        });
    }


    /**
     * 获取某一分类小说排行榜列表
     * @param url
     * @param callback
     */
    public static void getBookRankList(String url, final ResultCallback callback){

        getCommonReturnHtmlStringApi(url, null, "GBK", new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(BiQuGeReadCrawler.getBookRankList((String) o),0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);

            }
        });
    }


}
