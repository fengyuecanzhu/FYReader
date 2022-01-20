package xyz.fycz.myreader.model.third3.webBook

import android.util.Log
import xyz.fycz.myreader.model.third3.analyzeRule.AnalyzeRule
import xyz.fycz.myreader.model.third3.analyzeRule.AnalyzeUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.entity.Chapter
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.greendao.entity.rule.ContentRule
import xyz.fycz.myreader.greendao.service.ChapterService
import xyz.fycz.myreader.model.third3.ContentEmptyException
import xyz.fycz.myreader.model.third3.NoStackTraceException
import xyz.fycz.myreader.util.utils.HtmlFormatter
import xyz.fycz.myreader.util.utils.NetworkUtils

/**
 * 获取正文
 */
object BookContent {

    @Throws(Exception::class)
    suspend fun analyzeContent(
        scope: CoroutineScope,
        bookSource: BookSource,
        book: Book,
        bookChapter: Chapter,
        redirectUrl: String,
        baseUrl: String,
        body: String?,
        nextChapterUrl: String? = null
    ): String {
        body ?: throw NoStackTraceException(
            App.getmContext().getString(R.string.error_get_web_content, baseUrl)
        )
        Log.d(bookSource.sourceUrl, "≡获取成功:${baseUrl}")
        Log.d(bookSource.sourceUrl, body)
        val mNextChapterUrl = if (!nextChapterUrl.isNullOrEmpty()) {
            nextChapterUrl
        } else {
            ChapterService.getInstance().findBookAllChapterByBookId(book.id)[bookChapter.number + 1].url
        }
        val content = StringBuilder()
        val nextUrlList = arrayListOf(baseUrl)
        val contentRule = bookSource.contentRule
        val analyzeRule = AnalyzeRule(book, bookSource).setContent(body, baseUrl)
        analyzeRule.setRedirectUrl(baseUrl)
        analyzeRule.nextChapterUrl = mNextChapterUrl
        scope.ensureActive()
        var contentData = analyzeContent(
            book, baseUrl, redirectUrl, body, contentRule, bookChapter, bookSource, mNextChapterUrl
        )
        content.append(contentData.first)
        if (contentData.second.size == 1) {
            var nextUrl = contentData.second[0]
            while (nextUrl.isNotEmpty() && !nextUrlList.contains(nextUrl)) {
                if (!mNextChapterUrl.isNullOrEmpty()
                    && NetworkUtils.getAbsoluteURL(baseUrl, nextUrl)
                    == NetworkUtils.getAbsoluteURL(baseUrl, mNextChapterUrl)
                ) break
                nextUrlList.add(nextUrl)
                scope.ensureActive()
                val res = AnalyzeUrl(
                    mUrl = nextUrl,
                    source = bookSource,
                    ruleData = book,
                    //headerMapF = bookSource.getHeaderMap()
                ).getStrResponseAwait()
                res.body?.let { nextBody ->
                    contentData = analyzeContent(
                        book, nextUrl, res.url, nextBody, contentRule,
                        bookChapter, bookSource, mNextChapterUrl, false
                    )
                    nextUrl =
                        if (contentData.second.isNotEmpty()) contentData.second[0] else ""
                    content.append("\n").append(contentData.first)
                }
            }
            Log.d(bookSource.sourceUrl, "◇本章总页数:${nextUrlList.size}")
        } else if (contentData.second.size > 1) {
            Log.d(bookSource.sourceUrl, "◇并发解析目录,总页数:${contentData.second.size}")
            withContext(IO) {
                val asyncArray = Array(contentData.second.size) {
                    async(IO) {
                        val urlStr = contentData.second[it]
                        val analyzeUrl = AnalyzeUrl(
                            mUrl = urlStr,
                            source = bookSource,
                            ruleData = book,
                            //headerMapF = bookSource.getHeaderMap()
                        )
                        val res = analyzeUrl.getStrResponseAwait()
                        analyzeContent(
                            book, urlStr, res.url, res.body!!, contentRule,
                            bookChapter, bookSource, mNextChapterUrl, false
                        ).first
                    }
                }
                asyncArray.forEach { coroutine ->
                    scope.ensureActive()
                    content.append("\n").append(coroutine.await())
                }
            }
        }
        var contentStr = content.toString()
        val replaceRegex = contentRule.replaceRegex
        if (!replaceRegex.isNullOrEmpty()) {
            contentStr = analyzeRule.getString(replaceRegex, contentStr)
        }
        Log.d(bookSource.sourceUrl, "┌获取章节名称")
        Log.d(bookSource.sourceUrl, "└${bookChapter.title}")
        Log.d(bookSource.sourceUrl, "┌获取正文内容")
        Log.d(bookSource.sourceUrl, "└\n$contentStr")
        if (contentStr.isBlank()) {
            throw ContentEmptyException("内容为空")
        }
        //BookHelp.saveContent(bookSource, book, bookChapter, contentStr)
        return contentStr
    }

    @Throws(Exception::class)
    private fun analyzeContent(
        book: Book,
        baseUrl: String,
        redirectUrl: String,
        body: String,
        contentRule: ContentRule,
        chapter: Chapter,
        bookSource: BookSource,
        nextChapterUrl: String?,
        printLog: Boolean = true
    ): Pair<String, List<String>> {
        val analyzeRule = AnalyzeRule(book, bookSource)
        analyzeRule.setContent(body, baseUrl)
        val rUrl = analyzeRule.setRedirectUrl(redirectUrl)
        analyzeRule.nextChapterUrl = nextChapterUrl
        val nextUrlList = arrayListOf<String>()
        analyzeRule.chapter = chapter
        //获取正文
        var content = analyzeRule.getString(contentRule.content)
        //content = HtmlFormatter.formatKeepImg(content, rUrl)
        content = HtmlFormatter.format(content)
        //获取下一页链接
        val nextUrlRule = contentRule.contentUrlNext
        if (!nextUrlRule.isNullOrEmpty()) {
            if (printLog) Log.d(bookSource.sourceUrl, "┌获取正文下一页链接")
            analyzeRule.getStringList(nextUrlRule, isUrl = true)?.let {
                nextUrlList.addAll(it)
            }
            if (printLog) Log.d(bookSource.sourceUrl, "└" + nextUrlList.joinToString("，"))
        }
        return Pair(content, nextUrlList)
    }
}
