/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.webapi.crawler;


import android.text.TextUtils;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.BaseSourceCrawler;
import xyz.fycz.myreader.webapi.crawler.base.BaseSourceCrawlerNoInfo;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.FYReadCrawler;
import xyz.fycz.myreader.webapi.crawler.source.JsonPathCrawler;
import xyz.fycz.myreader.webapi.crawler.source.MatcherCrawler;
import xyz.fycz.myreader.webapi.crawler.source.Third3Crawler;
import xyz.fycz.myreader.webapi.crawler.source.ThirdCrawler;
import xyz.fycz.myreader.webapi.crawler.source.XpathCrawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

import static xyz.fycz.myreader.common.APPCONST.JSON_PATH;
import static xyz.fycz.myreader.common.APPCONST.MATCHER;
import static xyz.fycz.myreader.common.APPCONST.THIRD_3_SOURCE;
import static xyz.fycz.myreader.common.APPCONST.THIRD_SOURCE;
import static xyz.fycz.myreader.common.APPCONST.XPATH;

/**
 * @author fengyue
 * @date 2020/5/17 11:45
 */
public class ReadCrawlerUtil {
    private ReadCrawlerUtil() {
    }

    public static ArrayList<ReadCrawler> getReadCrawlers() {
        String searchSource = SharedPreUtils.getInstance().getString(App.getmContext().getString(R.string.searchSource), null);
        ArrayList<ReadCrawler> readCrawlers = new ArrayList<>();
        if (searchSource == null) {
            StringBuilder sb = new StringBuilder();
            for (LocalBookSource bookSource : LocalBookSource.values()) {
                if (bookSource.equals(LocalBookSource.fynovel) || bookSource.equals(LocalBookSource.local))
                    continue;
                sb.append(bookSource);
                sb.append(",");
                readCrawlers.add(getReadCrawler(bookSource.toString()));
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            searchSource = sb.toString();
            SharedPreUtils.getInstance().putString(App.getmContext().getString(R.string.searchSource), searchSource);
        } else if (!"".equals(searchSource)) {
            String[] sources = searchSource.split(",");
            for (String source : sources) {
                readCrawlers.add(getReadCrawler(source));
            }
        }
        return readCrawlers;
    }

    public static List<String> getAllSources() {
        List<String> sources = new ArrayList<>();
        for (LocalBookSource bookSource : LocalBookSource.values()) {
            if (bookSource.equals(LocalBookSource.fynovel))
                continue;
            sources.add(bookSource.text);
        }
        return sources;
    }

    public static HashMap<CharSequence, Boolean> getDisableSources() {
        String searchSource = SharedPreUtils.getInstance().getString(App.getmContext().getString(R.string.searchSource), null);
        HashMap<CharSequence, Boolean> mSources = new LinkedHashMap<>();
        if (searchSource == null) {
            for (LocalBookSource bookSource : LocalBookSource.values()) {
                if (bookSource.equals(LocalBookSource.fynovel) || bookSource.equals(LocalBookSource.local))
                    continue;
                mSources.put(bookSource.text, false);
            }
        } else {
            String[] ableSources = searchSource.split(",");
            bookSourceFor:
            for (LocalBookSource bookSource : LocalBookSource.values()) {
                if (bookSource.equals(LocalBookSource.fynovel) || bookSource.equals(LocalBookSource.local))
                    continue;
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

    public static void resetReadCrawlers() {
        StringBuilder sb = new StringBuilder();
        for (LocalBookSource bookSource : LocalBookSource.values()) {
            if (bookSource.equals(LocalBookSource.fynovel) || bookSource.equals(LocalBookSource.local))
                continue;
            sb.append(bookSource.toString());
            sb.append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        SharedPreUtils.getInstance().putString(App.getmContext().getString(R.string.searchSource), sb.toString());
    }

    public synchronized static void addReadCrawler(LocalBookSource... bookSources) {
        String searchSource = SharedPreUtils.getInstance().getString(App.getmContext().getString(R.string.searchSource));
        if ("".equals(searchSource)) {
            resetReadCrawlers();
            return;
        }
        StringBuilder sb = new StringBuilder(searchSource);
        for (LocalBookSource bookSource : bookSources) {
            sb.append(",");
            sb.append(bookSource.toString());
        }
        SharedPreUtils.getInstance().putString(App.getmContext().getString(R.string.searchSource), sb.toString());
    }

    public synchronized static void removeReadCrawler(String... bookSources) {
        String searchSource = SharedPreUtils.getInstance().getString(App.getmContext().getString(R.string.searchSource), null);
        if (searchSource == null) {
            return;
        }
        String[] ableSources = searchSource.split(",");
        for (int i = 0; i < ableSources.length; i++) {
            String ableSource = ableSources[i];
            for (String bookSource : bookSources) {
                if (ableSource.equals(bookSource)) {
                    ableSources[i] = "";
                    break;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String ableSource : ableSources) {
            if ("".equals(ableSource)) continue;
            sb.append(ableSource);
            sb.append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        SharedPreUtils.getInstance().putString(App.getmContext().getString(R.string.searchSource), sb.toString());
    }

    public static ReadCrawler getReadCrawler(String bookSource) {
        /*try {
            if (NetworkUtils.isUrl(bookSource)){
                BookSource source = BookSourceManager.getBookSourceByUrl(bookSource);
                if (source.getSearchRule().isRelatedWithInfo()){
                    return new MatcherCrawler(source);
                }else {
                    return new MatcherCrawlerNoInfo(source);
                }
            }
            ResourceBundle rb = ResourceBundle.getBundle("crawler");
            String readCrawlerPath = rb.getString(bookSource);
            Class clz = Class.forName(readCrawlerPath);
            return (ReadCrawler) clz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return new FYReadCrawler();
        }*/
        return getReadCrawler(BookSourceManager.getBookSourceByStr(bookSource));
    }

    public static ReadCrawler getReadCrawler(BookSource source) {
        return getReadCrawler(source, false);
    }

    public static ReadCrawler getReadCrawler(BookSource source, boolean isInfo) {
        try {
            if (!StringHelper.isEmpty(source.getSourceType())) {
                BaseSourceCrawler crawler;
                switch (source.getSourceType()) {
                    case MATCHER:
                    default:
                        crawler = new MatcherCrawler(source);
                        break;
                    case XPATH:
                        crawler = new XpathCrawler(source);
                        break;
                    case JSON_PATH:
                        crawler = new JsonPathCrawler(source);
                        break;
                    case THIRD_SOURCE:
                        return new ThirdCrawler(source);
                    case THIRD_3_SOURCE:
                        return new Third3Crawler(source);
                }
                if (source.getSearchRule().isRelatedWithInfo() || isInfo) {
                    return crawler;
                } else {
                    return new BaseSourceCrawlerNoInfo(crawler);
                }
            } else {
                if (THIRD_3_SOURCE.equals(source.getSourceType())) {
                    return new Third3Crawler(source);
                } else if (THIRD_SOURCE.equals(source.getSourceType())) {
                    return new ThirdCrawler(source);
                }
                Class clz = Class.forName(source.getSourceUrl());
                return (ReadCrawler) clz.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new FYReadCrawler();
        }
    }

    public static List<ReadCrawler> getEnableReadCrawlers() {
        return getEnableReadCrawlers("");
    }

    public static List<ReadCrawler> getEnableReadCrawlers(String group) {
        List<ReadCrawler> crawlers = new ArrayList<>();
        List<BookSource> sources = TextUtils.isEmpty(group) ?
                BookSourceManager.getEnabledBookSource() :
                BookSourceManager.getEnableSourceByGroup(group);
        for (BookSource source : sources) {
            crawlers.add(getReadCrawler(source));
        }
        return crawlers;
    }


    public static String getReadCrawlerClz(String bookSource) {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("crawler");
            return rb.getString(bookSource);
        } catch (Exception e) {
            return "";
        }
    }
}
