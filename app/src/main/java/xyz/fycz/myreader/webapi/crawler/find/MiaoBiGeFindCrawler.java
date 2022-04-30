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

package xyz.fycz.myreader.webapi.crawler.find;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Observable;
import xyz.fycz.myreader.entity.FindKind;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.webapi.crawler.base.BaseFindCrawler;

/**
 * @author fengyue
 * @date 2021/7/22 17:11
 */
public class MiaoBiGeFindCrawler extends BaseFindCrawler {
    private final LinkedHashMap<String, String> mBookTypes = new LinkedHashMap<>();

    @Override
    public String getName() {
        return "妙笔阁";
    }

    @Override
    public String getTag() {
        return "https://www.imiaobige.com";
    }

    private void initBookTypes() {
        mBookTypes.put("玄幻奇幻", "https://www.imiaobige.com/xuanhuan/{page}.html");
        mBookTypes.put("武侠仙侠", "https://www.imiaobige.com/wuxia/{page}.html");
        mBookTypes.put("都市生活", "https://www.imiaobige.com/dushi/{page}.html");
        mBookTypes.put("历史军事", "https://www.imiaobige.com/lishi/{page}.html");
        mBookTypes.put("游戏竞技", "https://www.imiaobige.com/youxi/{page}.html");
        mBookTypes.put("科幻未来", "https://www.imiaobige.com/kehuan/{page}.html");
    }

    @Override
    public Observable<Boolean> initData() {
        return Observable.create(emitter -> {
            initBookTypes();
            List<FindKind> kinds = new ArrayList<>();
            for (String name : mBookTypes.keySet()) {
                FindKind findKind = new FindKind();
                findKind.setName(name);
                findKind.setUrl(mBookTypes.get(name));
                findKind.setTag(getTag());
                kinds.add(findKind);
            }
            kindsMap.put(getName(), kinds);
            emitter.onNext(true);
            emitter.onComplete();
        });
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
        Element div = doc.getElementById("sitebox");
        Elements dls = div.getElementsByTag("dl");
        for (Element dl : dls) {
            Book book = new Book();
            Elements as = dl.getElementsByTag("a");
            book.setName(as.get(1).text());
            book.setAuthor(as.get(2).text());
            book.setType(kind.getName());
            book.setNewestChapterTitle(as.get(3).text());
            book.setDesc(dl.getElementsByClass("book_des").first().text());
            book.setImgUrl(as.first().getElementsByTag("img").attr("src"));
            book.setChapterUrl(as.get(1).attr("href").replace("novel", "read").replace(".html", "/"));
            book.setUpdateDate(dl.getElementsByClass("uptime").first().text());
            book.setSource(LocalBookSource.miaobi.toString());
            books.add(book);
        }
        return books;
    }
}
