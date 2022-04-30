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
import xyz.fycz.myreader.model.third3.http.StrResponse
import xyz.fycz.myreader.model.third3.analyzeRule.AnalyzeUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.entity.Chapter
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.model.third3.Coroutine
import xyz.fycz.myreader.model.third3.NoStackTraceException
import xyz.fycz.myreader.util.utils.NetworkUtils
import kotlin.coroutines.CoroutineContext

@Suppress("MemberVisibilityCanBePrivate")
object WebBook {

    /**
     * 搜索
     */
    fun searchBook(
        scope: CoroutineScope,
        bookSource: BookSource,
        key: String,
        page: Int? = 1,
        context: CoroutineContext = Dispatchers.IO,
    ): Coroutine<ArrayList<Book>> {
        return Coroutine.async(scope, context) {
            searchBookAwait(scope, bookSource, key, page)
        }
    }

    suspend fun searchBookAwait(
        scope: CoroutineScope,
        bookSource: BookSource,
        key: String,
        page: Int? = 1,
    ): ArrayList<Book> {
        val variableBook = Book()
        bookSource.searchRule.searchUrl?.let { searchUrl ->
            val analyzeUrl = AnalyzeUrl(
                mUrl = searchUrl,
                key = key,
                page = page,
                baseUrl = bookSource.sourceUrl,
                headerMapF = bookSource.getHeaderMap(true),
                source = bookSource,
                ruleData = variableBook,
            )
            var res = analyzeUrl.getStrResponseAwait()
            //检测书源是否已登录
            bookSource.loginCheckJs?.let { checkJs ->
                if (checkJs.isNotBlank()) {
                    res = analyzeUrl.evalJS(checkJs, res) as StrResponse
                }
            }
            return BookList.analyzeBookList(
                scope,
                bookSource,
                variableBook,
                analyzeUrl,
                res.url,
                res.body,
                true
            )
        }
        return arrayListOf()
    }

    /**
     * 发现
     */
    fun exploreBook(
        scope: CoroutineScope,
        bookSource: BookSource,
        url: String,
        page: Int? = 1,
        context: CoroutineContext = Dispatchers.IO,
    ): Coroutine<List<Book>> {
        return Coroutine.async(scope, context) {
            exploreBookAwait(scope, bookSource, url, page)
        }
    }

    suspend fun exploreBookAwait(
        scope: CoroutineScope,
        bookSource: BookSource,
        url: String,
        page: Int? = 1,
    ): ArrayList<Book> {
        val variableBook = Book()
        val analyzeUrl = AnalyzeUrl(
            mUrl = url,
            page = page,
            baseUrl = bookSource.sourceUrl,
            source = bookSource,
            ruleData = variableBook,
            headerMapF = bookSource.getHeaderMap(true)
        )
        var res = analyzeUrl.getStrResponseAwait()
        //检测书源是否已登录
        bookSource.loginCheckJs?.let { checkJs ->
            if (checkJs.isNotBlank()) {
                res = analyzeUrl.evalJS(checkJs, result = res) as StrResponse
            }
        }
        return BookList.analyzeBookList(
            scope,
            bookSource,
            variableBook,
            analyzeUrl,
            res.url,
            res.body,
            false
        )
    }

    /**
     * 书籍信息
     */
    fun getBookInfo(
        scope: CoroutineScope,
        bookSource: BookSource,
        book: Book,
        context: CoroutineContext = Dispatchers.IO,
        canReName: Boolean = true,
    ): Coroutine<Book> {
        return Coroutine.async(scope, context) {
            getBookInfoAwait(scope, bookSource, book, canReName)
        }
    }

    suspend fun getBookInfoAwait(
        scope: CoroutineScope,
        bookSource: BookSource,
        book: Book,
        canReName: Boolean = true,
    ): Book {
        //book.type = bookSource.bookSourceType
        if (!book.getCathe("infoHtml").isNullOrEmpty()) {
            BookInfo.analyzeBookInfo(
                scope,
                bookSource,
                book,
                book.infoUrl,
                book.infoUrl,
                book.getCathe("infoHtml"),
                canReName
            )
        } else {
            val analyzeUrl = AnalyzeUrl(
                mUrl = book.infoUrl,
                baseUrl = bookSource.sourceUrl,
                source = bookSource,
                ruleData = book,
                headerMapF = bookSource.getHeaderMap(true)
            )
            var res = analyzeUrl.getStrResponseAwait()
            //检测书源是否已登录
            bookSource.loginCheckJs?.let { checkJs ->
                if (checkJs.isNotBlank()) {
                    res = analyzeUrl.evalJS(checkJs, result = res) as StrResponse
                }
            }
            BookInfo.analyzeBookInfo(
                scope,
                bookSource,
                book,
                book.infoUrl,
                res.url,
                res.body,
                canReName
            )
        }
        return book
    }

    /**
     * 目录
     */
    fun getChapterList(
        scope: CoroutineScope,
        bookSource: BookSource,
        book: Book,
        context: CoroutineContext = Dispatchers.IO
    ): Coroutine<List<Chapter>> {
        return Coroutine.async(scope, context) {
            getChapterListAwait(scope, bookSource, book)
        }
    }

    suspend fun getChapterListAwait(
        scope: CoroutineScope,
        bookSource: BookSource,
        book: Book,
    ): List<Chapter> {
        //book.type = bookSource.bookSourceType
        return if (book.infoUrl == book.chapterUrl && !book.getCathe("tocHtml").isNullOrEmpty()) {
            BookChapterList.analyzeChapterList(
                scope,
                bookSource,
                book,
                book.chapterUrl,
                book.chapterUrl,
                book.getCathe("tocHtml"),
            )
        } else {
            val analyzeUrl = AnalyzeUrl(
                mUrl = book.chapterUrl,
                baseUrl = book.infoUrl,
                source = bookSource,
                ruleData = book,
                headerMapF = bookSource.getHeaderMap(true)
            )
            var res = analyzeUrl.getStrResponseAwait()
            //检测书源是否已登录
            bookSource.loginCheckJs?.let { checkJs ->
                if (checkJs.isNotBlank()) {
                    res = analyzeUrl.evalJS(checkJs, result = res) as StrResponse
                }
            }
            BookChapterList.analyzeChapterList(
                scope,
                bookSource,
                book,
                book.chapterUrl,
                res.url,
                res.body,
            )
        }
    }

    /**
     * 章节内容
     */
    fun getContent(
        scope: CoroutineScope,
        bookSource: BookSource,
        book: Book,
        bookChapter: Chapter,
        nextChapterUrl: String? = null,
        context: CoroutineContext = Dispatchers.IO
    ): Coroutine<String> {
        return Coroutine.async(scope, context) {
            getContentAwait(scope, bookSource, book, bookChapter, nextChapterUrl)
        }
    }

    suspend fun getContentAwait(
        scope: CoroutineScope,
        bookSource: BookSource,
        book: Book,
        bookChapter: Chapter,
        nextChapterUrl: String? = null,
    ): String {
        if (bookSource.contentRule.content.isNullOrEmpty()) {
            Log.d(bookSource.sourceUrl, "⇒正文规则为空,使用章节链接:${bookChapter.url}")
            return bookChapter.url
        }
        val absoluteUrl = NetworkUtils.getAbsoluteURL(book.chapterUrl, bookChapter.url)
        return if (bookChapter.url == book.infoUrl && !book.getCathe("tocHtml").isNullOrEmpty()) {
            BookContent.analyzeContent(
                scope,
                bookSource,
                book,
                bookChapter,
                absoluteUrl,
                absoluteUrl,
                book.getCathe("tocHtml"),
                nextChapterUrl
            )
        } else {
            val analyzeUrl = AnalyzeUrl(
                mUrl = absoluteUrl,
                baseUrl = book.chapterUrl,
                source = bookSource,
                ruleData = book,
                chapter = bookChapter,
                headerMapF = bookSource.getHeaderMap(true)
            )
            var res = analyzeUrl.getStrResponseAwait(
                jsStr = bookSource.contentRule.webJs,
                sourceRegex = bookSource.contentRule.sourceRegex
            )
            //检测书源是否已登录
            bookSource.loginCheckJs?.let { checkJs ->
                if (checkJs.isNotBlank()) {
                    res = analyzeUrl.evalJS(checkJs, result = res) as StrResponse
                }
            }
            BookContent.analyzeContent(
                scope,
                bookSource,
                book,
                bookChapter,
                absoluteUrl,
                res.url,
                res.body,
                nextChapterUrl
            )
        }
    }

    /**
     * 精准搜索
     */
    fun preciseSearch(
        scope: CoroutineScope,
        bookSources: List<BookSource>,
        name: String,
        author: String,
        context: CoroutineContext = Dispatchers.IO,
    ): Coroutine<Pair<BookSource, Book>> {
        return Coroutine.async(scope, context) {
            preciseSearchAwait(scope, bookSources, name, author)
                ?: throw NoStackTraceException("没有搜索到<$name>$author")
        }
    }

    suspend fun preciseSearchAwait(
        scope: CoroutineScope,
        bookSources: List<BookSource>,
        name: String,
        author: String,
    ): Pair<BookSource, Book>? {
        bookSources.forEach { source ->
            kotlin.runCatching {
                if (!scope.isActive) return null
                searchBookAwait(scope, source, name).firstOrNull {
                    it.name == name && it.author == author
                }?.let { searchBook ->
                    if (!scope.isActive) return null
                    var book = searchBook
                    if (book.chapterUrl.isBlank()) {
                        book = getBookInfoAwait(scope, source, book)
                    }
                    return Pair(source, book)
                }
            }
        }
        return null
    }

}