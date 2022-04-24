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

package xyz.fycz.myreader.webapi.crawler.base;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import xyz.fycz.myreader.entity.FindKind;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.greendao.entity.Book;

/**
 * @author fengyue
 * @date 2021/7/21 20:26
 */
public abstract class BaseFindCrawler implements FindCrawler {
    protected Map<String, List<FindKind>> kindsMap = new LinkedHashMap<>();

    @Override
    public List<String> getGroups() {
        return new ArrayList<>(kindsMap.keySet());
    }

    @Override
    public Map<String, List<FindKind>> getKindsMap() {
        return kindsMap;
    }

    @Override
    public List<FindKind> getKindsByKey(String key) {
        return kindsMap.get(key);
    }

    @Override
    public abstract Observable<Boolean> initData();

    @Override
    public boolean needSearch() {
        return false;
    }

}
