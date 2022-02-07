package xyz.fycz.myreader.model.third3.webBook

import android.text.TextUtils
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
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.entity.Chapter
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.greendao.entity.rule.TocRule
import xyz.fycz.myreader.model.third3.NoStackTraceException
import xyz.fycz.myreader.model.third3.TocEmptyException


/**
 * 获取目录
 */
object BookChapterList {

    private val falseRegex = "\\s*(?i)(null|false|0)\\s*".toRegex()

    suspend fun analyzeChapterList(
        scope: CoroutineScope,
        bookSource: BookSource,
        book: Book,
        redirectUrl: String,
        baseUrl: String,
        body: String?,
    ): List<Chapter> {
        body ?: throw NoStackTraceException(
            App.getmContext().getString(R.string.error_get_web_content, baseUrl)
        )
        val chapterList = ArrayList<Chapter>()
        Log.d(bookSource.sourceUrl, "≡获取成功:${baseUrl}")
        Log.d(bookSource.sourceUrl, body)
        val tocRule = bookSource.tocRule
        val nextUrlList = arrayListOf(baseUrl)
        var reverse = false
        var listRule = tocRule.chapterList ?: ""
        if (listRule.startsWith("-")) {
            reverse = true
            listRule = listRule.substring(1)
        }
        if (listRule.startsWith("+")) {
            listRule = listRule.substring(1)
        }
        var chapterData =
            analyzeChapterList(
                scope, book, baseUrl, redirectUrl, body,
                tocRule, listRule, bookSource, log = true
            )
        chapterList.addAll(chapterData.first)
        when (chapterData.second.size) {
            0 -> Unit
            1 -> {
                var nextUrl = chapterData.second[0]
                while (nextUrl.isNotEmpty() && !nextUrlList.contains(nextUrl)) {
                    nextUrlList.add(nextUrl)
                    AnalyzeUrl(
                        mUrl = nextUrl,
                        source = bookSource,
                        ruleData = book,
                        headerMapF = bookSource.getHeaderMap()
                    ).getStrResponseAwait().body?.let { nextBody ->
                        chapterData = analyzeChapterList(
                            scope, book, nextUrl, nextUrl,
                            nextBody, tocRule, listRule, bookSource
                        )
                        nextUrl = chapterData.second.firstOrNull() ?: ""
                        chapterList.addAll(chapterData.first)
                    }
                }
                Log.d(bookSource.sourceUrl, "◇目录总页数:${nextUrlList.size}")
            }
            else -> {
                Log.d(bookSource.sourceUrl, "◇并发解析目录,总页数:${chapterData.second.size}")
                withContext(IO) {
                    val asyncArray = Array(chapterData.second.size) {
                        async(IO) {
                            val urlStr = chapterData.second[it]
                            val analyzeUrl = AnalyzeUrl(
                                mUrl = urlStr,
                                source = bookSource,
                                ruleData = book,
                                headerMapF = bookSource.getHeaderMap()
                            )
                            val res = analyzeUrl.getStrResponseAwait()
                            analyzeChapterList(
                                this, book, urlStr, res.url,
                                res.body!!, tocRule, listRule, bookSource, false
                            ).first
                        }
                    }
                    asyncArray.forEach { coroutine ->
                        chapterList.addAll(coroutine.await())
                    }
                }
            }
        }
        if (chapterList.isEmpty()) {
            throw TocEmptyException(App.getmContext().getString(R.string.chapter_list_empty))
        }
        //去重
        if (!reverse) {
            chapterList.reverse()
        }
        val lh = LinkedHashSet(chapterList)
        val list = ArrayList(lh)
        /*if (!book.getReverseToc()) {
            list.reverse()
        }*/
        list.reverse()
        Log.d(book.source, "◇目录总数:${list.size}")
        list.forEachIndexed { index, bookChapter ->
            bookChapter.number = index
        }
        book.newestChapterTitle = list.last().title
        book.historyChapterId =
            list.getOrNull(book.histtoryChapterNum)?.title ?: book.newestChapterTitle
        /*if (book.chapterTotalNum < list.size) {
            book.noReadNum = list.size - book.chapterTotalNum
            book.lastReadTime = System.currentTimeMillis()
        }
        book.lastReadTime = System.currentTimeMillis()
        book.chapterTotalNum = list.size*/
        return list
    }

    private fun analyzeChapterList(
        scope: CoroutineScope,
        book: Book,
        baseUrl: String,
        redirectUrl: String,
        body: String,
        tocRule: TocRule,
        listRule: String,
        bookSource: BookSource,
        getNextUrl: Boolean = true,
        log: Boolean = false
    ): Pair<List<Chapter>, List<String>> {
        val analyzeRule = AnalyzeRule(book, bookSource)
        analyzeRule.setContent(body).setBaseUrl(baseUrl)
        analyzeRule.setRedirectUrl(redirectUrl)
        //获取目录列表
        val chapterList = arrayListOf<Chapter>()
        if (log) Log.d(bookSource.sourceUrl, "┌获取目录列表")
        val elements = analyzeRule.getElements(listRule)
        if (log) Log.d(bookSource.sourceUrl, "└列表大小:${elements.size}",)
        //获取下一页链接
        val nextUrlList = arrayListOf<String>()
        val nextTocRule = tocRule.tocUrlNext
        if (getNextUrl && !nextTocRule.isNullOrEmpty()) {
            if (log) Log.d(bookSource.sourceUrl, "┌获取目录下一页列表")
            analyzeRule.getStringList(nextTocRule, isUrl = true)?.let {
                for (item in it) {
                    if (item != baseUrl) {
                        nextUrlList.add(item)
                    }
                }
            }
            if (log) Log.d(bookSource.sourceUrl, "└" + TextUtils.join("，\n", nextUrlList),)
        }
        scope.ensureActive()
        if (elements.isNotEmpty()) {
            if (log) Log.d(bookSource.sourceUrl, "┌解析目录列表")
            val nameRule = analyzeRule.splitSourceRule(tocRule.chapterName)
            val urlRule = analyzeRule.splitSourceRule(tocRule.chapterUrl)
            val vipRule = analyzeRule.splitSourceRule(tocRule.isVip)
            val payRule = analyzeRule.splitSourceRule(tocRule.isPay)
            val upTimeRule = analyzeRule.splitSourceRule(tocRule.updateTime)
            elements.forEachIndexed { index, item ->
                scope.ensureActive()
                analyzeRule.setContent(item)
                //val bookChapter = Chapter(bookUrl = book.bookUrl, baseUrl = baseUrl)
                val bookChapter = Chapter()
                analyzeRule.chapter = bookChapter
                bookChapter.title = analyzeRule.getString(nameRule)
                bookChapter.url = analyzeRule.getString(urlRule)
                bookChapter.updateTime = analyzeRule.getString(upTimeRule)
                if (bookChapter.url.isEmpty()) {
                    bookChapter.url = baseUrl
                    if (log) Log.d(bookSource.sourceUrl, "目录${index}未获取到url,使用baseUrl替代")
                }
                if (bookChapter.title.isNotEmpty()) {
                    val isVip = analyzeRule.getString(vipRule)
                    val isPay = analyzeRule.getString(payRule)
                    if (isVip.isNotEmpty() && !isVip.matches(falseRegex)) {
                        bookChapter.isVip = true
                    }
                    if (isPay.isNotEmpty() && !isPay.matches(falseRegex)) {
                        bookChapter.isPay = true
                    }
                    chapterList.add(bookChapter)
                }
            }
             if (log) Log.d(bookSource.sourceUrl, "└目录列表解析完成")
             if (log) Log.d(bookSource.sourceUrl, "┌获取首章名称")
             if (log) Log.d(bookSource.sourceUrl, "└${chapterList[0].title}")
             if (log) Log.d(bookSource.sourceUrl, "┌获取首章链接")
             if (log) Log.d(bookSource.sourceUrl, "└${chapterList[0].url}")
             if (log) Log.d(bookSource.sourceUrl, "┌获取首章信息")
             if (log) Log.d(bookSource.sourceUrl, "└${chapterList[0].updateTime}")
        }
        return Pair(chapterList, nextUrlList)
    }

}