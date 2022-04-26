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

package xyz.fycz.myreader.model.third3.webBook

import android.util.Log
import xyz.fycz.myreader.model.third3.analyzeRule.AnalyzeRule
import xyz.fycz.myreader.model.third3.analyzeRule.AnalyzeUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.entity.rule.BookListRule
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.model.third3.NoStackTraceException
import xyz.fycz.myreader.util.utils.HtmlFormatter
import xyz.fycz.myreader.util.utils.NetworkUtils

/**
 * 获取书籍列表
 */
object BookList {

    @Throws(Exception::class)
    fun analyzeBookList(
        scope: CoroutineScope,
        bookSource: BookSource,
        variableBook: Book,
        analyzeUrl: AnalyzeUrl,
        baseUrl: String,
        body: String?,
        isSearch: Boolean = true,
    ): ArrayList<Book> {
        body ?: throw NoStackTraceException(
            App.getmContext().getString(
                R.string.error_get_web_content,
                analyzeUrl.ruleUrl
            )
        )
        val bookList = ArrayList<Book>()
        Log.d(bookSource.sourceUrl, "≡获取成功:${analyzeUrl.ruleUrl}")
        Log.d(bookSource.sourceUrl, body)
        val analyzeRule = AnalyzeRule(variableBook, bookSource)
        analyzeRule.setContent(body).setBaseUrl(baseUrl)
        analyzeRule.setRedirectUrl(baseUrl)
        bookSource.infoRule.urlPattern?.let {
            scope.ensureActive()
            if (baseUrl.matches(it.toRegex())) {
                Log.d(bookSource.sourceUrl, "≡链接为详情页")
                getInfoItem(
                    scope, bookSource, analyzeRule, analyzeUrl, body, baseUrl, variableBook.variable
                )?.let { searchBook ->
                    searchBook.putCathe("infoHtml", body)
                    bookList.add(searchBook)
                }
                return bookList
            }
        }
        val collections: List<Any>
        var reverse = false
        val bookListRule: BookListRule = when {
            isSearch -> bookSource.searchRule
            bookSource.findRule.list.isNullOrBlank() -> bookSource.searchRule
            else -> bookSource.findRule
        }
        var ruleList: String = bookListRule.list ?: ""
        if (ruleList.startsWith("-")) {
            reverse = true
            ruleList = ruleList.substring(1)
        }
        if (ruleList.startsWith("+")) {
            ruleList = ruleList.substring(1)
        }
        Log.d(bookSource.sourceUrl, "┌获取书籍列表")
        collections = analyzeRule.getElements(ruleList)
        scope.ensureActive()
        if (collections.isEmpty() && bookSource.infoRule.urlPattern.isNullOrEmpty()) {
            Log.d(bookSource.sourceUrl, "└列表为空,按详情页解析")
            getInfoItem(
                scope, bookSource, analyzeRule, analyzeUrl, body, baseUrl, variableBook.variable
            )?.let { searchBook ->
                searchBook.putCathe("infoHtml", body)
                bookList.add(searchBook)
            }
        } else {
            val ruleName = analyzeRule.splitSourceRule(bookListRule.name)
            val ruleBookUrl = analyzeRule.splitSourceRule(bookListRule.infoUrl)
            val ruleAuthor = analyzeRule.splitSourceRule(bookListRule.author)
            val ruleCoverUrl = analyzeRule.splitSourceRule(bookListRule.imgUrl)
            val ruleIntro = analyzeRule.splitSourceRule(bookListRule.desc)
            val ruleKind = analyzeRule.splitSourceRule(bookListRule.type)
            val ruleLastChapter = analyzeRule.splitSourceRule(bookListRule.lastChapter)
            val ruleWordCount = analyzeRule.splitSourceRule(bookListRule.wordCount)
            Log.d(bookSource.sourceUrl, "└列表大小:${collections.size}")
            for ((index, item) in collections.withIndex()) {
                getSearchItem(
                    scope, bookSource, analyzeRule, item, baseUrl, variableBook.variable,
                    index == 0,
                    ruleName = ruleName,
                    ruleBookUrl = ruleBookUrl,
                    ruleAuthor = ruleAuthor,
                    ruleCoverUrl = ruleCoverUrl,
                    ruleIntro = ruleIntro,
                    ruleKind = ruleKind,
                    ruleLastChapter = ruleLastChapter,
                    ruleWordCount = ruleWordCount
                )?.let { searchBook ->
                    if (baseUrl == searchBook.infoUrl) {
                        searchBook.putCathe("infoHtml", body)
                    }
                    bookList.add(searchBook)
                }
            }
            if (reverse) {
                bookList.reverse()
            }
        }
        return bookList
    }

    @Throws(Exception::class)
    private fun getInfoItem(
        scope: CoroutineScope,
        bookSource: BookSource,
        analyzeRule: AnalyzeRule,
        analyzeUrl: AnalyzeUrl,
        body: String,
        baseUrl: String,
        variable: String?
    ): Book? {
        val book = Book()
        book.variable = variable
        book.infoUrl = analyzeUrl.ruleUrl
        book.source = bookSource.sourceUrl
        //book.originName = bookSource.bookSourceName
        //book.originOrder = bookSource.customOrder
        //book.type = bookSource.bookSourceType
        analyzeRule.book = book
        BookInfo.analyzeBookInfo(
            scope,
            book,
            body,
            analyzeRule,
            bookSource,
            baseUrl,
            baseUrl,
            false
        )
        if (book.name.isNullOrBlank()) {
            //return book.toSearchBook()
            return book
        }
        return null
    }

    @Throws(Exception::class)
    private fun getSearchItem(
        scope: CoroutineScope,
        bookSource: BookSource,
        analyzeRule: AnalyzeRule,
        item: Any,
        baseUrl: String,
        variable: String?,
        log: Boolean,
        ruleName: List<AnalyzeRule.SourceRule>,
        ruleBookUrl: List<AnalyzeRule.SourceRule>,
        ruleAuthor: List<AnalyzeRule.SourceRule>,
        ruleKind: List<AnalyzeRule.SourceRule>,
        ruleCoverUrl: List<AnalyzeRule.SourceRule>,
        ruleWordCount: List<AnalyzeRule.SourceRule>,
        ruleIntro: List<AnalyzeRule.SourceRule>,
        ruleLastChapter: List<AnalyzeRule.SourceRule>
    ): Book? {
        val searchBook = Book()
        searchBook.variable = variable
        searchBook.source = bookSource.sourceUrl
       /* searchBook.originName = bookSource.bookSourceName
        searchBook.type = bookSource.bookSourceType
        searchBook.originOrder = bookSource.customOrder*/
        analyzeRule.book = searchBook
        analyzeRule.setContent(item)
        scope.ensureActive()
        if (log) if (log) Log.d(bookSource.sourceUrl, "┌获取书名")
        searchBook.name = formatBookName(analyzeRule.getString(ruleName))
        if (log) Log.d(bookSource.sourceUrl, "└${searchBook.name}")
        if (searchBook.name.isNotEmpty()) {
            scope.ensureActive()
            if (log) Log.d(bookSource.sourceUrl, "┌获取作者")
            searchBook.author = formatBookAuthor(analyzeRule.getString(ruleAuthor))
            if (log) Log.d(bookSource.sourceUrl, "└${searchBook.author}")
            scope.ensureActive()
            if (log) Log.d(bookSource.sourceUrl, "┌获取分类")
            try {
                searchBook.type = analyzeRule.getStringList(ruleKind)?.joinToString(",")
                if (log) Log.d(bookSource.sourceUrl, "└${searchBook.type}")
            } catch (e: Exception) {
                if (log) Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
            }
            scope.ensureActive()
            if (log) Log.d(bookSource.sourceUrl, "┌获取字数")
            try {
                //searchBook.wordCount = wordCountFormat(analyzeRule.getString(ruleWordCount))
                searchBook.wordCount = analyzeRule.getString(ruleWordCount)
                if (log) Log.d(bookSource.sourceUrl, "└${searchBook.wordCount}")
            } catch (e: java.lang.Exception) {
                if (log) Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
            }
            scope.ensureActive()
            if (log) Log.d(bookSource.sourceUrl, "┌获取最新章节")
            try {
                searchBook.newestChapterTitle = analyzeRule.getString(ruleLastChapter)
                if (log) Log.d(bookSource.sourceUrl, "└${searchBook.newestChapterTitle}")
            } catch (e: java.lang.Exception) {
                if (log) Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
            }
            scope.ensureActive()
            if (log) Log.d(bookSource.sourceUrl, "┌获取简介")
            try {
                searchBook.desc = HtmlFormatter.format(analyzeRule.getString(ruleIntro))
                if (log) Log.d(bookSource.sourceUrl, "└${searchBook.desc}")
            } catch (e: java.lang.Exception) {
                if (log) Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
            }
            scope.ensureActive()
            if (log) Log.d(bookSource.sourceUrl, "┌获取封面链接")
            try {
                analyzeRule.getString(ruleCoverUrl).let {
                    if (it.isNotEmpty()) searchBook.imgUrl =
                        NetworkUtils.getAbsoluteURL(baseUrl, it)
                }
                if (log) Log.d(bookSource.sourceUrl, "└${searchBook.imgUrl}")
            } catch (e: java.lang.Exception) {
                if (log) Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
            }
            scope.ensureActive()
            if (log) Log.d(bookSource.sourceUrl, "┌获取详情页链接")
            searchBook.infoUrl = analyzeRule.getString(ruleBookUrl, isUrl = true)
            if (searchBook.infoUrl.isEmpty()) {
                searchBook.infoUrl = baseUrl
            }
            if (log) Log.d(bookSource.sourceUrl, "└${searchBook.infoUrl}")
            return searchBook
        }
        return null
    }

    val nameRegex = Regex("\\s+作\\s*者.*|\\s+\\S+\\s+著")
    val authorRegex = Regex("^\\s*作\\s*者[:：\\s]+|\\s+著")
    /**
     * 格式化书名
     */
    fun formatBookName(name: String): String {
        return name
            .replace(nameRegex, "")
            .trim { it <= ' ' }
    }

    /**
     * 格式化作者
     */
    fun formatBookAuthor(author: String): String {
        return author
            .replace(authorRegex, "")
            .trim { it <= ' ' }
    }

}