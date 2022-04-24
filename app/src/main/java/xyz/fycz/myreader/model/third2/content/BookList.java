/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.model.third2.content;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.FindRule;
import xyz.fycz.myreader.greendao.entity.rule.InfoRule;
import xyz.fycz.myreader.greendao.entity.rule.SearchRule;
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeByRegex;
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeRule;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

import static android.text.TextUtils.isEmpty;

public class BookList {
    private String tag;
    private String sourceName;
    private BookSource bookSource;
    private boolean isFind;
    //规则
    private String ruleList;
    private String ruleName;
    private String ruleAuthor;
    private String ruleKind;
    private String ruleIntroduce;
    private String ruleLastChapter;
    private String ruleCoverUrl;
    private String ruleNoteUrl;

    public BookList(String tag, String sourceName, BookSource bookSource, boolean isFind) {
        this.tag = tag;
        this.sourceName = sourceName;
        this.bookSource = bookSource;
        this.isFind = isFind;
    }

    public Observable<List<Book>> analyzeSearchBook(final StrResponse strResponse) {
        return Observable.create(e -> {
            String baseUrl;
            baseUrl = NetworkUtils.getUrl(strResponse.getResponse());
            String body = strResponse.body();
            if (TextUtils.isEmpty(body)) {
                e.onError(new Throwable(String.format("访问网站失败%s", baseUrl)));
                return;
            } else {
                Log.d(tag, "┌成功获取搜索结果");
                Log.d(tag, "└" + baseUrl);
            }
            List<Book> books = new ArrayList<>();
            AnalyzeRule analyzer = new AnalyzeRule(null);
            analyzer.setContent(body, baseUrl);
            //如果符合详情页url规则
            if (!isEmpty(bookSource.getInfoRule().getUrlPattern())
                    && baseUrl.matches(bookSource.getInfoRule().getUrlPattern())) {
                Log.d(tag, ">搜索结果为详情页");
                Book item = getItem(analyzer, baseUrl);
                if (item != null) {
                    item.putCathe("BookInfoHtml", body);
                    books.add(item);
                }
            } else {
                initRule();
                List<Object> collections;
                boolean reverse = false;
                boolean allInOne = false;
                if (ruleList.startsWith("-")) {
                    reverse = true;
                    ruleList = ruleList.substring(1);
                }
                // 仅使用java正则表达式提取书籍列表
                if (ruleList.startsWith(":")) {
                    ruleList = ruleList.substring(1);
                    Log.d(tag, "┌解析搜索列表");
                    getBooksOfRegex(body, ruleList.split("&&"), 0, analyzer, books);
                } else {
                    if (ruleList.startsWith("+")) {
                        allInOne = true;
                        ruleList = ruleList.substring(1);
                    }
                    //获取列表
                    Log.d(tag, "┌解析搜索列表");
                    collections = analyzer.getElements(ruleList);
                    if (collections.size() == 0 && isEmpty(bookSource.getInfoRule().getUrlPattern())) {
                        Log.d(tag, "└搜索列表为空,当做详情页处理");
                        Book item = getItem(analyzer, baseUrl);
                        if (item != null) {
                            item.putCathe("BookInfoHtml", body);
                            books.add(item);
                        }
                    } else {
                        Log.d(tag, "└找到 " + collections.size() + " 个匹配的结果");
                        if (allInOne) {
                            for (int i = 0; i < collections.size(); i++) {
                                Object object = collections.get(i);
                                Book item = getItemAllInOne(analyzer, object, baseUrl, i == 0);
                                if (item != null) {
                                    //如果网址相同则缓存
                                    if (baseUrl.equals(item.getInfoUrl())) {
                                        item.putCathe("BookInfoHtml", body);
                                    }
                                    books.add(item);
                                }
                            }
                        } else {
                            for (int i = 0; i < collections.size(); i++) {
                                Object object = collections.get(i);
                                analyzer.setContent(object, baseUrl);
                                Book item = getItemInList(analyzer, baseUrl, i == 0);
                                if (item != null) {
                                    //如果网址相同则缓存
                                    if (baseUrl.equals(item.getInfoUrl())) {
                                        item.putCathe("BookInfoHtml", body);
                                    }
                                    books.add(item);
                                }
                            }
                        }
                    }
                }
                if (books.size() > 1 && reverse) {
                    Collections.reverse(books);
                }
            }
            if (books.isEmpty()) {
                e.onError(new Throwable("未获取到书名"));
                return;
            }
            Log.d(tag, "-书籍列表解析结束");
            e.onNext(books);
            e.onComplete();
        });
    }

    private void initRule() {
        if (isFind && !TextUtils.isEmpty(bookSource.getFindRule().getList())) {
            FindRule findRule = bookSource.getFindRule();
            ruleList = findRule.getList();
            ruleName = findRule.getName();
            ruleAuthor = findRule.getAuthor();
            ruleKind = findRule.getType();
            ruleIntroduce = findRule.getDesc();
            ruleCoverUrl = findRule.getImgUrl();
            ruleLastChapter = findRule.getLastChapter();
            ruleNoteUrl = findRule.getInfoUrl();
        } else {
            SearchRule searchRule = bookSource.getSearchRule();
            ruleList = searchRule.getList();
            ruleName = searchRule.getName();
            ruleAuthor = searchRule.getAuthor();
            ruleKind = searchRule.getType();
            ruleIntroduce = searchRule.getDesc();
            ruleCoverUrl = searchRule.getImgUrl();
            ruleLastChapter = searchRule.getLastChapter();
            ruleNoteUrl = searchRule.getInfoUrl();
        }
    }

    /**
     * 详情页
     */
    private Book getItem(AnalyzeRule analyzer, String baseUrl) throws Exception {
        InfoRule infoRule = bookSource.getInfoRule();
        Book item = new Book();
        analyzer.setBook(item);
        item.setTag(tag);
        item.setSource(bookSource.getSourceUrl());
        item.setInfoUrl(baseUrl);
        // 获取详情页预处理规则
        String ruleInfoInit = infoRule.getInit();
        if (!isEmpty(ruleInfoInit)) {
            // 仅使用java正则表达式提取书籍详情
            if (ruleInfoInit.startsWith(":")) {
                ruleInfoInit = ruleInfoInit.substring(1);
                Log.d(tag, "┌详情信息预处理");
                AnalyzeByRegex.getInfoOfRegex(String.valueOf(analyzer.getContent()), ruleInfoInit.split("&&"), 0, item, analyzer, bookSource, tag);
                if (isEmpty(item.getName())) return null;
                return item;
            } else {
                Object object = analyzer.getElement(ruleInfoInit);
                if (object != null) {
                    analyzer.setContent(object);
                }
            }
        }
        Log.d(tag, ">书籍网址:" + baseUrl);
        Log.d(tag, "┌获取书名");
        String bookName = StringUtils.formatHtml(analyzer.getString(infoRule.getName()));
        Log.d(tag, "└" + bookName);
        if (!TextUtils.isEmpty(bookName)) {
            item.setName(bookName);
            Log.d(tag, "┌获取作者");
            item.setAuthor(StringUtils.formatHtml(analyzer.getString(infoRule.getAuthor())));
            Log.d(tag, "└" + item.getAuthor());
            Log.d(tag, "┌获取分类");
            item.setType(analyzer.getString(infoRule.getType()));
            Log.d(tag, "└" + item.getType());
            Log.d(tag, "┌获取最新章节");
            item.setNewestChapterTitle(analyzer.getString(infoRule.getLastChapter()));
            Log.d(tag, "└" + item.getNewestChapterTitle());
            Log.d(tag, "┌获取简介");
            item.setDesc(analyzer.getString(infoRule.getDesc()));
            Log.d(tag, "└" + item.getDesc());
            Log.d(tag, "┌获取封面");
            item.setImgUrl(analyzer.getString(infoRule.getImgUrl(), true));
            Log.d(tag, "└" + item.getImgUrl());
            return item;
        }
        return null;
    }

    private Book getItemAllInOne(AnalyzeRule analyzer, Object object, String baseUrl, boolean printLog) {
        Book item = new Book();
        analyzer.setBook(item);
        NativeObject nativeObject = (NativeObject) object;
        if (printLog) Log.d(tag, "┌获取书名");
        String bookName = StringUtils.formatHtml(String.valueOf(nativeObject.get(ruleName)));
        if (printLog) Log.d(tag, "└" + bookName);
        if (!isEmpty(bookName)) {
            item.setTag(tag);
            item.setSource(bookSource.getSourceUrl());
            item.setName(bookName);
            if (printLog) Log.d(tag, "┌获取作者");
            item.setAuthor(StringUtils.formatHtml(String.valueOf(nativeObject.get(ruleAuthor))));
            if (printLog) Log.d(tag, "└" + item.getAuthor());
            if (printLog) Log.d(tag, "┌获取分类");
            item.setType(String.valueOf(nativeObject.get(ruleKind)));
            if (printLog) Log.d(tag, "└" + item.getType());
            if (printLog) Log.d(tag, "┌获取最新章节");
            item.setNewestChapterTitle(String.valueOf(nativeObject.get(ruleLastChapter)));
            if (printLog) Log.d(tag, "└" + item.getNewestChapterTitle());
            if (printLog) Log.d(tag, "┌获取简介");
            item.setDesc(String.valueOf(nativeObject.get(ruleIntroduce)));
            if (printLog) Log.d(tag, "└" + item.getDesc());
            if (printLog) Log.d(tag, "┌获取封面");
            if (!isEmpty(ruleCoverUrl))
                item.setImgUrl(NetworkUtils.getAbsoluteURL(baseUrl, String.valueOf(nativeObject.get(ruleCoverUrl))));
            if (printLog) Log.d(tag, "└" + item.getImgUrl());
            if (printLog) Log.d(tag, "┌获取书籍网址");
            String resultUrl = String.valueOf(nativeObject.get(ruleNoteUrl));
            if (isEmpty(resultUrl)) resultUrl = baseUrl;
            item.setInfoUrl(resultUrl);
            if (printLog) Log.d(tag, "└" + item.getInfoUrl());
            return item;
        }
        return null;
    }

    private Book getItemInList(AnalyzeRule analyzer, String baseUrl, boolean printLog) throws
            Exception {
        Book item = new Book();
        analyzer.setBook(item);
        if (printLog) Log.d(tag, "┌获取书名");
        String bookName = StringUtils.formatHtml(analyzer.getString(ruleName));
        if (printLog) Log.d(tag, "└" + bookName);
        if (!TextUtils.isEmpty(bookName)) {
            item.setTag(tag);
            item.setSource(bookSource.getSourceUrl());
            item.setName(bookName);
            if (printLog) Log.d(tag, "┌获取作者");
            item.setAuthor(StringUtils.formatHtml(analyzer.getString(ruleAuthor)));
            if (printLog) Log.d(tag, "└" + item.getAuthor());
            if (printLog) Log.d(tag, "┌获取分类");
            item.setType(analyzer.getString(ruleKind));
            if (printLog) Log.d(tag, "└" + item.getType());
            if (printLog) Log.d(tag, "┌获取最新章节");
            item.setNewestChapterTitle(analyzer.getString(ruleLastChapter));
            if (printLog) Log.d(tag, "└" + item.getNewestChapterTitle());
            if (printLog) Log.d(tag, "┌获取简介");
            item.setDesc(analyzer.getString(ruleIntroduce));
            if (printLog) Log.d(tag, "└" + item.getDesc());
            if (printLog) Log.d(tag, "┌获取封面");
            item.setImgUrl(analyzer.getString(ruleCoverUrl, true));
            if (printLog) Log.d(tag, "└" + item.getImgUrl());
            Log.d(tag, "┌获取书籍网址");
            String resultUrl = analyzer.getString(ruleNoteUrl, true);
            if (isEmpty(resultUrl)) resultUrl = baseUrl;
            item.setInfoUrl(resultUrl);
            if (printLog) Log.d(tag, "└" + item.getInfoUrl());
            return item;
        }
        return null;
    }

    // 纯java模式正则表达式获取书籍列表
    private void getBooksOfRegex(String res, String[] regs,
                                 int index, AnalyzeRule analyzer, final List<Book> books) throws Exception {
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        String baseUrl = analyzer.getBaseUrl();
        // 判断规则是否有效,当搜索列表规则无效时当作详情页处理
        if (!resM.find()) {
            books.add(getItem(analyzer, baseUrl));
            return;
        }
        // 判断索引的规则是最后一个规则
        if (index + 1 == regs.length) {
            // 获取规则列表
            HashMap<String, String> ruleMap = new HashMap<>();
            ruleMap.put("ruleName", ruleName);
            ruleMap.put("ruleAuthor", ruleAuthor);
            ruleMap.put("ruleKind", ruleKind);
            ruleMap.put("ruleLastChapter", ruleLastChapter);
            ruleMap.put("ruleIntroduce", ruleIntroduce);
            ruleMap.put("ruleCoverUrl", ruleCoverUrl);
            ruleMap.put("ruleNoteUrl", ruleNoteUrl);
            // 分离规则参数
            List<String> ruleName = new ArrayList<>();
            List<List<String>> ruleParams = new ArrayList<>();  // 创建规则参数容器
            List<List<Integer>> ruleTypes = new ArrayList<>();  // 创建规则类型容器
            List<Boolean> hasVarParams = new ArrayList<>();     // 创建put&get标志容器
            for (String key : ruleMap.keySet()) {
                String val = ruleMap.get(key);
                ruleName.add(key);
                hasVarParams.add(!TextUtils.isEmpty(val) && (val.contains("@put") || val.contains("@get")));
                List<String> ruleParam = new ArrayList<>();
                List<Integer> ruleType = new ArrayList<>();
                AnalyzeByRegex.splitRegexRule(val, ruleParam, ruleType);
                ruleParams.add(ruleParam);
                ruleTypes.add(ruleType);
            }
            // 提取书籍列表
            do {
                // 新建书籍容器
                Book item = new Book();
                item.setTag(tag);
                item.setSource(bookSource.getSourceUrl());
                analyzer.setBook(item);
                // 提取规则内容
                HashMap<String, String> ruleVal = new HashMap<>();
                StringBuilder infoVal = new StringBuilder();
                for (int i = ruleParams.size(); i-- > 0; ) {
                    List<String> ruleParam = ruleParams.get(i);
                    List<Integer> ruleType = ruleTypes.get(i);
                    infoVal.setLength(0);
                    for (int j = ruleParam.size(); j-- > 0; ) {
                        int regType = ruleType.get(j);
                        if (regType > 0) {
                            infoVal.insert(0, resM.group(regType));
                        } else if (regType < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            infoVal.insert(0, resM.group(ruleParam.get(j)));
                        } else {
                            infoVal.insert(0, ruleParam.get(j));
                        }
                    }
                    ruleVal.put(ruleName.get(i), hasVarParams.get(i) ? AnalyzeByRegex.checkKeys(infoVal.toString(), analyzer) : infoVal.toString());
                }
                // 保存当前节点的书籍信息
                item.setName(StringUtils.formatHtml(ruleVal.get("ruleName")));
                item.setAuthor(StringUtils.formatHtml(ruleVal.get("ruleAuthor")));
                item.setType(ruleVal.get("ruleKind"));
                item.setNewestChapterTitle(ruleVal.get("ruleLastChapter"));
                item.setDesc(ruleVal.get("ruleIntroduce"));
                item.setImgUrl(ruleVal.get("ruleCoverUrl"));
                item.setInfoUrl(NetworkUtils.getAbsoluteURL(baseUrl, ruleVal.get("ruleNoteUrl")));
                books.add(item);
                // 判断搜索结果是否为详情页
                if (books.size() == 1 && (isEmpty(ruleVal.get("ruleNoteUrl")) || ruleVal.get("ruleNoteUrl").equals(baseUrl))) {
                    books.get(0).setInfoUrl(baseUrl);
                    books.get(0).putCathe("BookInfoHtml", res);
                    ;
                    return;
                }
            } while (resM.find());
            // 输出调试信息
            Log.d(tag, "└找到 " + books.size() + " 个匹配的结果");
            Log.d(tag, "┌获取书名");
            Log.d(tag, "└" + books.get(0).getName());
            Log.d(tag, "┌获取作者");
            Log.d(tag, "└" + books.get(0).getAuthor());
            Log.d(tag, "┌获取分类");
            Log.d(tag, "└" + books.get(0).getType());
            Log.d(tag, "┌获取最新章节");
            Log.d(tag, "└" + books.get(0).getNewestChapterTitle());
            Log.d(tag, "┌获取简介");
            Log.d(tag, "└" + books.get(0).getDesc());
            Log.d(tag, "┌获取封面");
            Log.d(tag, "└" + books.get(0).getImgUrl());
            Log.d(tag, "┌获取书籍");
            Log.d(tag, "└" + books.get(0).getInfoUrl());
        } else {
            StringBuilder result = new StringBuilder();
            do {
                result.append(resM.group());
            } while (resM.find());
            getBooksOfRegex(result.toString(), regs, ++index, analyzer, books);
        }
    }
}