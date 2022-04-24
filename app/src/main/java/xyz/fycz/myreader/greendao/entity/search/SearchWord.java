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

package xyz.fycz.myreader.greendao.entity.search;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.List;

import xyz.fycz.myreader.greendao.convert.SearchWord1Convert;

import org.greenrobot.greendao.annotation.Generated;

/**
 * @author fengyue
 * @date 2021/12/7 8:31
 */
@Entity
public class SearchWord {
    @Id
    private String bookId;
    private String keyword;
    @Convert(columnType = String.class, converter = SearchWord1Convert.class)
    private List<SearchWord1> searchWords;

    @Generated(hash = 2054974399)
    public SearchWord(String bookId, String keyword,
            List<SearchWord1> searchWords) {
        this.bookId = bookId;
        this.keyword = keyword;
        this.searchWords = searchWords;
    }

    @Generated(hash = 407254878)
    public SearchWord() {
    }

    public String getBookId() {
        return this.bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public List<SearchWord1> getSearchWords() {
        return this.searchWords;
    }

    public void setSearchWords(List<SearchWord1> searchWords) {
        this.searchWords = searchWords;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
