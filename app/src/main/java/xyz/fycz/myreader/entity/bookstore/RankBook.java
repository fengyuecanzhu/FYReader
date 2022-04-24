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
 * @date 2020/9/16 22:56
 */
public class RankBook extends QDBook{
    /*bAuth: "净无痕"
    bName: "伏天氏"
    bid: "1011058239"
    cat: "玄幻"
    catId: 21
    cnt: "636.1万字"
    desc: "东方神州，有人皇立道统，有圣贤宗门传道，有诸侯雄踞一方王国，诸强林立，神州动乱千万载，值此之时，一代天骄叶青帝及东凰大帝横空出世，东方神州一统！然，叶青帝忽然暴毙，世间雕像尽皆被毁，于世间除名，沦为禁忌；从此神州唯东凰大帝独尊！十五年后，东海青州城，一名为叶伏天的少年，开启了他的传奇之路…"
    rankCnt: "5996月票"
    rankNum: 21*/


    private String rankCnt;
    private int rankNum;


    public String getRankCnt() {
        return rankCnt;
    }

    public void setRankCnt(String rankCnt) {
        this.rankCnt = rankCnt;
    }

    public int getRankNum() {
        return rankNum;
    }

    public void setRankNum(int rankNum) {
        this.rankNum = rankNum;
    }
}
