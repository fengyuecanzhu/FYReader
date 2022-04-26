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

package xyz.fycz.myreader.widget.page

import android.graphics.Point


class TxtChar {
    var chardata: Char = ' '//字符数据

    var selected: Boolean? = false//当前字符是否被选中

    //记录文字的左上右上左下右下四个点坐标
    var topLeftPosition: Point? = null//左上
    var topRightPosition: Point? = null//右上
    var bottomLeftPosition: Point? = null//左下
    var bottomRightPosition: Point? = null//右下

    var charWidth = 0f//字符宽度
    var Index = 0//当前字符位置

    override fun toString(): String {
        return ("ShowChar [chardata=" + chardata + ", Selected=" + selected + ", TopLeftPosition=" + topLeftPosition
                + ", TopRightPosition=" + topRightPosition + ", BottomLeftPosition=" + bottomLeftPosition
                + ", BottomRightPosition=" + bottomRightPosition + ", charWidth=" + charWidth + ", Index=" + Index
                + "]");
    }
}