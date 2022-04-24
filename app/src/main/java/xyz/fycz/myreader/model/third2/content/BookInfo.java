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

import android.text.TextUtils;
import android.util.Log;


import io.reactivex.Observable;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.InfoRule;
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeByRegex;
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeRule;
import xyz.fycz.myreader.util.utils.StringUtils;

import static android.text.TextUtils.isEmpty;

public class BookInfo {
    private String tag;
    private String sourceName;
    private BookSource source;

    public BookInfo(String tag, String sourceName, BookSource source) {
        this.tag = tag;
        this.sourceName = sourceName;
        this.source = source;
    }

    public Observable<Book> analyzeBookInfo(String s, final Book book) {
        return Observable.create(e -> {
            String baseUrl = book.getInfoUrl();

            InfoRule infoRule = source.getInfoRule();
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("书籍信息获取失败" + baseUrl));
                return;
            } else {
                Log.d(tag, "┌成功获取详情页");
                Log.d(tag, "└" + baseUrl);
            }
            book.setTag(tag);
            book.setSource(source.getSourceUrl());

            AnalyzeRule analyzer = new AnalyzeRule(book);
            analyzer.setContent(s, baseUrl);

            // 获取详情页预处理规则
            String ruleInfoInit = infoRule.getInit();
            boolean isRegex = false;
            if (!isEmpty(ruleInfoInit)) {
                // 仅使用java正则表达式提取书籍详情
                if (ruleInfoInit.startsWith(":")) {
                    isRegex = true;
                    ruleInfoInit = ruleInfoInit.substring(1);
                    Log.d(tag, "┌详情信息预处理");
                    AnalyzeByRegex.getInfoOfRegex(s, ruleInfoInit.split("&&"), 0, book, analyzer, source, tag);
                } else {
                    Object object = analyzer.getElement(ruleInfoInit);
                    if (object != null) {
                        analyzer.setContent(object);
                    }
                }
            }
            if (!isRegex) {
                Log.d(tag, "┌详情信息预处理");
                Object object = analyzer.getElement(ruleInfoInit);
                if (object != null) analyzer.setContent(object);
                Log.d(tag, "└详情预处理完成");

                Log.d(tag, "┌获取书名");
                String bookName = StringUtils.formatHtml(analyzer.getString(infoRule.getName()));
                if (!isEmpty(bookName)) book.setName(bookName);
                Log.d(tag, "└" + bookName);

                Log.d(tag, "┌获取作者");
                String bookAuthor = StringUtils.formatHtml(analyzer.getString(infoRule.getAuthor()));
                if (!isEmpty(bookAuthor)) book.setAuthor(bookAuthor);
                Log.d(tag, "└" + bookAuthor);

                Log.d(tag, "┌获取分类");
                String bookKind = analyzer.getString(infoRule.getType());
                Log.d(tag, "└" + bookKind);

                Log.d(tag, "┌获取最新章节");
                String bookLastChapter = analyzer.getString(infoRule.getLastChapter());
                if (!isEmpty(bookLastChapter)) book.setNewestChapterTitle(bookLastChapter);
                Log.d(tag, "└" + bookLastChapter);

                Log.d(tag, "┌获取简介");
                String bookIntroduce = analyzer.getString(infoRule.getDesc());
                if (!isEmpty(bookIntroduce)) book.setDesc(bookIntroduce);
                Log.d(tag, "└" + bookIntroduce);

                Log.d(tag, "┌获取封面");
                String bookCoverUrl = analyzer.getString(infoRule.getImgUrl(), true);
                if (!isEmpty(bookCoverUrl)) book.setImgUrl(bookCoverUrl);
                Log.d(tag, "└" + bookCoverUrl);

                Log.d(tag, "┌获取目录网址");
                String bookCatalogUrl = analyzer.getString(infoRule.getTocUrl(), true);
                if (isEmpty(bookCatalogUrl)) bookCatalogUrl = baseUrl;
                book.setChapterUrl(bookCatalogUrl);
                //如果目录页和详情页相同,暂存页面内容供获取目录用
                if (bookCatalogUrl.equals(baseUrl)) book.putCathe("ChapterListHtml", s);
                Log.d(tag, "└" + book.getChapterUrl());
                Log.d(tag, "-详情页解析完成");
            }
            e.onNext(book);
            e.onComplete();
        });
    }

}
