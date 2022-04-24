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

package xyz.fycz.myreader.entity.sourcedebug

/**
 * @author fengyue
 * @date 2021/2/12 21:11
 */
data class DebugBook(
        /*var name: String? = null,
        var author: String? = null,
        var type: String? = null,
        var desc: String? = null,
        var wordCount: String? = null,
        var status: String? = null,
        var lastChapter: String? = null,
        var updateTime: String? = null,
        var imgUrl: String? = null,
        var tocUrl: String? = null,
        var infoUrl: String? = null,*/
        var 书名: String? = null,
        var 作者: String? = null,
        var 分类: String? = null,
        var 简介: String? = null,
        var 字数: String? = null,
        var 连载状态: String? = null,
        var 最新章节: String? = null,
        var 更新时间: String? = null,
        var 封面链接: String? = null,
        var 目录链接: String? = null,
        var 详情链接: String? = null,
)
