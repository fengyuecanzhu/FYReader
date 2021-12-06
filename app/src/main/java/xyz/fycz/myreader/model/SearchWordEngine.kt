package xyz.fycz.myreader.model

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.entity.SearchWord1
import xyz.fycz.myreader.entity.SearchWord2
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.entity.Chapter
import xyz.fycz.myreader.greendao.service.ChapterService
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.util.help.ChapterContentHelp
import xyz.fycz.myreader.widget.page.PageLoader
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author fengyue
 * @date 2021/12/5 21:17
 */
class SearchWordEngine(
    private val book: Book,
    private val chapters: List<Chapter>,
    private val pageLoader: PageLoader
) {
    private val TAG = "SearchWordEngine"

    //线程池
    private var executorService: ExecutorService

    private var scheduler: Scheduler
    private var compositeDisposable: CompositeDisposable
    private lateinit var searchListener: OnSearchListener
    private val threadsNum =
        SharedPreUtils.getInstance().getInt(App.getmContext().getString(R.string.threadNum), 8);
    private var searchSiteIndex = 0
    private var searchSuccessNum = 0
    private var searchFinishNum = 0
    private var isLocalBook = false

    fun setOnSearchListener(searchListener: OnSearchListener) {
        this.searchListener = searchListener
    }

    init {
        executorService = Executors.newFixedThreadPool(threadsNum)
        scheduler = Schedulers.from(executorService)
        compositeDisposable = CompositeDisposable()
    }

    fun stopSearch() {
        compositeDisposable.dispose()
        compositeDisposable = CompositeDisposable()
        searchListener.loadFinish(searchSuccessNum == 0)
    }

    /**
     * 关闭引擎
     */
    fun closeSearchEngine() {
        executorService.shutdown()
        if (!compositeDisposable.isDisposed) compositeDisposable.dispose()
    }

    /**
     * 搜索关键字(模糊搜索)
     *
     * @param keyword
     */
    fun search(keyword: String) {
        if ("本地书籍" == book.type) {
            isLocalBook = true
            if (!File(book.chapterUrl).exists()) {
                ToastUtils.showWarring("当前书籍源文件不存在，无法搜索！")
                searchListener.loadFinish(true)
                return
            }
        }
        if (chapters.isEmpty()) {
            ToastUtils.showWarring("当前书籍章节目录为空，无法搜索！")
            searchListener.loadFinish(true)
            return
        }
        searchSuccessNum = 0
        searchSiteIndex = -1
        searchFinishNum = 0
        for (i in 0 until Math.min(threadsNum, chapters.size)) {
            searchOnEngine(keyword)
        }
    }

    @Synchronized
    private fun searchOnEngine(keyword: String) {
        searchSiteIndex++
        if (searchSiteIndex < chapters.size) {
            val chapterNum = searchSiteIndex
            val chapter = chapters[chapterNum]
            Observable.create(ObservableOnSubscribe<SearchWord1> { emitter ->
                val searchWord1 =
                    SearchWord1(book.id, chapterNum, chapter.title, mutableListOf())
                if (!isLocalBook && !ChapterService.isChapterCached(book.id, chapter.title)) {
                    emitter.onNext(searchWord1)
                    return@ObservableOnSubscribe
                }
                var content = pageLoader.getChapterReader(chapter)
                content = pageLoader.contentHelper.replaceContent(
                    book.name + "-" + book.author,
                    book.source,
                    content,
                    true
                )
                if (book.reSeg) {
                    content = ChapterContentHelp.LightNovelParagraph2(content, chapter.title)
                }
                val allLine: List<String> = content.split("\n")
                var count = 0
                allLine.forEach {
                    var index: Int = -1
                    while (it.indexOf(keyword, index + 1).also { index = it } != -1) {
                        var leftI = 0
                        var rightI = it.length
                        var leftS = ""
                        var rightS = ""
                        if (leftI < index - 20) {
                            leftI = index - 20
                            leftS = "..."
                        }
                        if (rightI > index + 20) {
                            rightI = index + 20
                            rightS = "..."
                        }
                        val str = leftS + it.substring(leftI, rightI) + rightS
                        val searchWord2 =
                            SearchWord2(
                                keyword,
                                chapterNum,
                                str,
                                index - leftI + leftS.length,
                                index,
                                count
                            )
                        searchWord1.searchWord2List.add(searchWord2)
                        count++
                    }
                }
                emitter.onNext(searchWord1)
                emitter.onComplete()
            }).subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<SearchWord1?> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onNext(searchWord1: SearchWord1) {
                        searchFinishNum++
                        if (searchWord1.searchWord2List.isNotEmpty()) {
                            searchSuccessNum++
                            searchListener.loadMore(searchWord1)
                        }
                        searchOnEngine(keyword)
                    }

                    override fun onError(e: Throwable) {
                        searchFinishNum++
                        searchOnEngine(keyword)
                        if (App.isDebug()) e.printStackTrace()
                    }

                    override fun onComplete() {
                    }

                })
        } else {
            if (searchFinishNum == chapters.size) {
                if (searchSuccessNum == 0) {
                    ToastUtils.showWarring("搜索结果为空")
                    searchListener.loadFinish(true)
                } else {
                    searchListener.loadFinish(false)
                }
            }
        }
    }

    interface OnSearchListener {
        fun loadFinish(isEmpty: Boolean)
        fun loadMore(item: SearchWord1)
    }
}