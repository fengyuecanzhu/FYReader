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

package xyz.fycz.myreader.entity.thirdsource.source3

import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.greendao.entity.rule.*

/**
 * @author fengyue
 * @date 2022/1/20 13:51
 */
object Third3SourceUtil {
    fun source3ToSource(bean: Source3): BookSource {
        val bookSource = BookSource()
        bookSource.sourceUrl = bean.bookSourceUrl
        bookSource.sourceName = bean.bookSourceName
        bookSource.sourceGroup = bean.bookSourceGroup
        bookSource.sourceType = APPCONST.THIRD_3_SOURCE
        bookSource.orderNum = bean.customOrder
        bookSource.enable = bean.enabled
        bookSource.concurrentRate = bean.concurrentRate
        bookSource.sourceHeaders = bean.header
        bookSource.loginUrl = bean.loginUrl
        bookSource.loginCheckJs = bean.loginCheckJs
        bookSource.sourceComment = bean.bookSourceComment
        bookSource.lastUpdateTime = bean.lastUpdateTime
        bookSource.weight = bean.weight


        val searchRule = SearchRule()
        val ruleSearch = bean.ruleSearch
        searchRule.searchUrl = bean.searchUrl
        searchRule.list = ruleSearch?.bookList
        searchRule.name = ruleSearch?.name
        searchRule.author = ruleSearch?.author
        searchRule.desc = ruleSearch?.intro
        searchRule.type = ruleSearch?.kind
        searchRule.lastChapter = ruleSearch?.lastChapter
        searchRule.updateTime = ruleSearch?.updateTime
        searchRule.infoUrl = ruleSearch?.bookUrl
        searchRule.imgUrl = ruleSearch?.coverUrl
        searchRule.wordCount = ruleSearch?.wordCount
        searchRule.isRelatedWithInfo = true
        bookSource.searchRule = searchRule

        val infoRule = InfoRule()
        val ruleInfo = bean.ruleBookInfo
        infoRule.urlPattern = bean.bookUrlPattern
        infoRule.init = ruleInfo?.init
        infoRule.name = ruleInfo?.name
        infoRule.author = ruleInfo?.author
        infoRule.desc = ruleInfo?.intro
        infoRule.type = ruleInfo?.kind
        infoRule.lastChapter = ruleInfo?.lastChapter
        infoRule.updateTime = ruleInfo?.updateTime
        infoRule.imgUrl = ruleInfo?.coverUrl
        infoRule.tocUrl = ruleInfo?.tocUrl
        infoRule.wordCount = ruleInfo?.wordCount
        bookSource.infoRule = infoRule

        val tocRule = TocRule()
        val ruleToc = bean.ruleToc
        tocRule.chapterList = ruleToc?.chapterList
        tocRule.chapterName = ruleToc?.chapterName
        tocRule.chapterUrl = ruleToc?.chapterUrl
        tocRule.isVip = ruleToc?.isVip
        tocRule.isPay = ruleToc?.isPay
        tocRule.updateTime = ruleToc?.updateTime
        tocRule.tocUrlNext = ruleToc?.nextTocUrl
        bookSource.tocRule = tocRule

        val contentRule = ContentRule()
        val ruleContent = bean.ruleContent
        contentRule.content = ruleContent?.content
        contentRule.contentUrlNext = ruleContent?.nextContentUrl
        contentRule.replaceRegex = ruleContent?.replaceRegex
        bookSource.contentRule = contentRule

        val findRule = FindRule()
        val ruleFind = bean.ruleExplore
        findRule.url = bean.exploreUrl
        findRule.list = ruleFind?.bookList
        findRule.name = ruleFind?.name
        findRule.author = ruleFind?.author
        findRule.desc = ruleFind?.intro
        findRule.type = ruleFind?.kind
        findRule.lastChapter = ruleFind?.lastChapter
        findRule.updateTime = ruleFind?.updateTime
        findRule.imgUrl = ruleFind?.coverUrl
        findRule.infoUrl = ruleFind?.bookUrl
        findRule.wordCount = ruleFind?.wordCount
        bookSource.findRule = findRule
        return bookSource
    }

    fun source3sToSources(source3s: List<Source3>): List<BookSource> {
        val sources = mutableListOf<BookSource>()
        source3s.forEach {
            sources.add(source3ToSource(it))
        }
        return sources
    }
}