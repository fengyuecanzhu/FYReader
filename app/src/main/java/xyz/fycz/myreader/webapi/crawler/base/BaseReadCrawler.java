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

package xyz.fycz.myreader.webapi.crawler.base;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;

/**
 * 因新版书源使用StrResponse，为了兼容旧版本，书源全部继承自此类
 *
 * @author fengyue
 * @date 2021/5/13 22:29
 */
public abstract class BaseReadCrawler implements ReadCrawler {
    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", APPCONST.DEFAULT_USER_AGENT);
        return headers;
    }

    @Override
    public ConMVMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        return null;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        return null;
    }

    @Override
    public String getContentFormHtml(String html) {
        return null;
    }

    @Override
    public Observable<ConMVMap<SearchBookBean, Book>> getBooksFromStrResponse(StrResponse response) {
        return Observable.create(emitter -> {
            ConMVMap<SearchBookBean, Book> bookMap = getBooksFromSearchHtml(response.body());
            for (Book book : bookMap.values()){
                book.setId(StringHelper.getStringRandom(25));
                if (!TextUtils.isEmpty(book.getImgUrl())){
                    book.setImgUrl(NetworkUtils.getAbsoluteURL(getNameSpace(), book.getImgUrl()));
                }
            }
            emitter.onNext(bookMap);
            emitter.onComplete();
        });
    }

    @Override
    public Observable<List<Chapter>> getChaptersFromStrResponse(StrResponse response) {
        return Observable.create(emitter -> {
            emitter.onNext(getChaptersFromHtml(response.body()));
            emitter.onComplete();
        });
    }

    @Override
    public Observable<String> getContentFormStrResponse(StrResponse response) {
        return Observable.create(emitter -> {
            emitter.onNext(getContentFormHtml(response.body()));
            emitter.onComplete();
        });
    }
}
