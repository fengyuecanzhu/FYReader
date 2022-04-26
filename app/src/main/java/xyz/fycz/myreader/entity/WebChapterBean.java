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

package xyz.fycz.myreader.entity;

import java.util.LinkedHashSet;
import java.util.List;

import xyz.fycz.myreader.greendao.entity.Chapter;

public class WebChapterBean {
    private String url;

    private List<Chapter> data;

    private LinkedHashSet<String> nextUrlList;

    public WebChapterBean(String url) {
        this.url = url;
    }

    public WebChapterBean(List<Chapter> data, LinkedHashSet<String> nextUrlList) {
        this.data = data;
        this.nextUrlList = nextUrlList;
    }

    public List<Chapter> getData() {
        return data;
    }

    public void setData(List<Chapter> data) {
        this.data = data;
    }

    public LinkedHashSet<String> getNextUrlList() {
        return nextUrlList;
    }

    public String getUrl() {
        return url;
    }

    public boolean noData() {
        return data == null;
    }
}
