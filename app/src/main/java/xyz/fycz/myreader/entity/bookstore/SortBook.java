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

package xyz.fycz.myreader.entity.bookstore;

/**
 * @author fengyue
 * @date 2020/9/18 20:43
 */
public class SortBook extends QDBook{
    /*bAuth: "老鸡吃蘑菇"
    bName: "全世界都不知道我多强"
    bid: 1021781295
    cat: "玄幻"
    catId: 21
    cid: 406957810
    cnt: "72.68万字"
    desc: "【日更过万，质量保证！】当梁凡从地底爬出来之后，才发现不是变秃才能变强！我要控制我自己，要是不小心一拳打爆这个星球怎么办？安静的隐藏在世俗，全世界都不知道我有多强，虽然这个世界妖魔鬼怪很变态，但苟着享"
    state: "连载"*/

    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
