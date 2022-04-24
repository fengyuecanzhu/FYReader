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

package xyz.fycz.myreader.webapi.crawler.find;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import xyz.fycz.myreader.entity.FindKind;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.entity.bookstore.FindBook;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.webapi.crawler.base.BaseFindCrawler;

/**
 * @author fengyue
 * @date 2021/7/22 17:34
 */
public class QB5FindCrawler extends BaseFindCrawler {

    @Override
    public String getName() {
        return "全本小说";
    }

    @Override
    public String getTag() {
        return "https://www.qb50.com";
    }

    @Override
    public Observable<Boolean> initData() {
        return Observable.create((ObservableOnSubscribe<StrResponse>) emitter -> {
            emitter.onNext(OkHttpUtils.getStrResponse(getTag(), "gbk", null));
            emitter.onComplete();
        }).flatMap(response -> OkHttpUtils.setCookie(response, getTag()))
                .flatMap((Function<StrResponse, ObservableSource<Boolean>>) response -> Observable.create(emitter -> {
                    kindsMap.put(getName(), getBookKinds(response.body()));
                    emitter.onNext(true);
                    emitter.onComplete();
                }));
    }

    /**
     * 获取书城小说分类列表
     *
     * @param html
     * @return
     */
    public List<FindKind> getBookKinds(String html) {
        List<FindKind> kinds = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("nav_cont");
        if (divs.size() > 0) {
            Elements uls = divs.get(0).getElementsByTag("ul");
            if (uls.size() > 0) {
                for (Element li : uls.get(0).children()) {
                    Element a = li.child(0);
                    FindKind kind = new FindKind();
                    kind.setName(a.attr("title"));
                    kind.setUrl(a.attr("href"));
                    if (kind.getName().contains("首页") || kind.getName().contains("热门小说"))
                        continue;
                    if (!kind.getName().equals("完本小说")) {
                        kind.setUrl(kind.getUrl().substring(0, kind.getUrl().lastIndexOf("_") + 1) + "{page}" + "/");
                    } else {
                        kind.setUrl(kind.getUrl().substring(0, kind.getUrl().lastIndexOf("/") + 1) + "{page}");
                    }
                    if (!StringHelper.isEmpty(kind.getName())) {
                        kinds.add(kind);
                    }
                }
            }

        }
        return kinds;
    }

    @Override
    public Observable<List<Book>> getFindBooks(StrResponse strResponse, FindKind kind) {
        return Observable.create(emitter -> {
            emitter.onNext(getFindBooks(strResponse.body(), kind));
            emitter.onComplete();
        });
    }

    public List<Book> getFindBooks(String html, FindKind kind) {
        List<Book> books = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        try {
            int pageSize = Integer.parseInt(doc.getElementsByClass("last").first().text());
            kind.setMaxPage(pageSize);
        } catch (Exception ignored) {
        }
        String type = doc.select("meta[name=keywords]").attr("content").replace(",全本小说网", "");
        Element div = doc.getElementById("tlist");
        Elements uls = div.getElementsByTag("ul");
        if (uls.size() > 0) {
            for (Element li : uls.get(0).children()) {
                Book book = new Book();
                Element aName = li.getElementsByClass("name").get(0);
                Element divZz = li.getElementsByClass("zz").get(0);
                Element divAuthor = li.getElementsByClass("author").get(0);
                Element divSj = li.getElementsByClass("sj").get(0);
                book.setType(type);
                book.setName(aName.attr("title"));
                book.setChapterUrl(aName.attr("href"));
                book.setInfoUrl(aName.attr("href"));
                book.setNewestChapterTitle(divZz.text());
                book.setAuthor(divAuthor.text());
                book.setUpdateDate(divSj.text());
                book.setSource(LocalBookSource.qb5.toString());
                books.add(book);
            }
        }
        return books;
    }
}
