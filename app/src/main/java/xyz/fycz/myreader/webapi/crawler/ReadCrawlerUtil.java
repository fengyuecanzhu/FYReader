package xyz.fycz.myreader.webapi.crawler;


import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.FYReadCrawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * @author fengyue
 * @date 2020/5/17 11:45
 */
public class ReadCrawlerUtil {
    private ReadCrawlerUtil() {
    }

    public static ArrayList<ReadCrawler> getReadCrawlers() {
        SharedPreUtils spu = SharedPreUtils.getInstance();
        String searchSource = spu.getString("searchSource", null);
        ArrayList<ReadCrawler> readCrawlers = new ArrayList<>();
        if (searchSource == null) {
            StringBuilder sb = new StringBuilder();
            for (BookSource bookSource : BookSource.values()) {
                if (bookSource.equals(BookSource.fynovel) || bookSource.equals(BookSource.local))
                    continue;
                sb.append(bookSource.toString());
                sb.append(",");
                readCrawlers.add(getReadCrawler(bookSource.toString()));
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            searchSource = sb.toString();
            spu.putString("searchSource", searchSource);
        } else if (!"".equals(searchSource)){
            String[] sources = searchSource.split(",");
            for (String source : sources) {
                readCrawlers.add(getReadCrawler(source));
            }
        }
        return readCrawlers;
    }

    public static HashMap<CharSequence, Boolean> getDisableSources() {
        SharedPreUtils spu = SharedPreUtils.getInstance();
        String searchSource = spu.getString("searchSource", null);
        HashMap<CharSequence, Boolean> mSources = new HashMap<>();
        if (searchSource == null) {
            for (BookSource bookSource : BookSource.values()) {
                if (bookSource.equals(BookSource.fynovel) || bookSource.equals(BookSource.local)) continue;
                mSources.put(bookSource.text, false);
            }
        } else {
            String[] ableSources = searchSource.split(",");
            bookSourceFor:
            for (BookSource bookSource : BookSource.values()) {
                if (bookSource.equals(BookSource.fynovel) || bookSource.equals(BookSource.local)) continue;
                for (String ableSource : ableSources) {
                    if (ableSource.equals(bookSource.toString())) {
                        mSources.put(bookSource.text, false);
                        continue bookSourceFor;
                    }
                }
                mSources.put(bookSource.text, true);
            }
        }
        return mSources;

    }

    public static void resetReaderCrawlers(){
        StringBuilder sb = new StringBuilder();
        for (BookSource bookSource : BookSource.values()) {
            if (bookSource.equals(BookSource.fynovel) || bookSource.equals(BookSource.local))
                continue;
            sb.append(bookSource.toString());
            sb.append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        SharedPreUtils.getInstance().putString("searchSource", sb.toString());
    }

    public static ReadCrawler getReadCrawler(String bookSource) {
        ResourceBundle rb = ResourceBundle.getBundle("crawler");
        try {
            String readCrawlerPath = rb.getString(bookSource);
            Class clz = Class.forName(readCrawlerPath);
            return (ReadCrawler) clz.newInstance();
        } catch (Exception e) {
            return new FYReadCrawler();
        }
    }
}
