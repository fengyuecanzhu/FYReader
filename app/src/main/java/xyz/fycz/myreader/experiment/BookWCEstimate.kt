package xyz.fycz.myreader.experiment

import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.service.ChapterService
import xyz.fycz.myreader.util.IOUtils
import xyz.fycz.myreader.util.utils.FileUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

/**
 * @author fengyue
 * @date 2021/12/4 10:33
 */
class BookWCEstimate {
    /**
     * -1：本地文件不存在
     * -2：书籍章节缓存数量少于20
     * -3：epub书籍暂不支持计算字数
     */
    fun getWordCount(book: Book): Int {
        if (book.type == "本地书籍") {
            return getLocalWordCount(book)
        } else if (book.type.isNotEmpty()) {
            return getNetWordCount(book)
        }
        return 0
    }

    private fun getLocalWordCount(book: Book): Int {
        if (book.chapterUrl.endsWith(".epub")) return -3
        val file = File(book.chapterUrl)
        if (!file.exists()) return -1
        return -countChar(file)
    }


    /**
     * 计算方式：使用最小二乘法进行线性回归预测
     */
    private fun getNetWordCount(book: Book): Int {
        val chapterService = ChapterService.getInstance()
        val chapters = chapterService.findBookAllChapterByBookId(book.id)
        val map = mutableMapOf<Double, Double>()
        var sum = 0.0
        var i = 0
        var cachedChapterSize = 0
        for (chapter in chapters) {
            if (cachedChapterSize >= 20) break
            if (ChapterService.isChapterCached(chapter.bookId, chapter.title)) {
                cachedChapterSize++
            }
        }
        if ((cachedChapterSize < 20 && chapters.size > 50) || cachedChapterSize == 0) return -2
        chapters.forEach { chapter ->
            if (ChapterService.isChapterCached(chapter.bookId, chapter.title)) {
                sum += countChar(
                    File(
                        APPCONST.BOOK_CACHE_PATH + chapter.bookId
                                + File.separator + chapter.title + FileUtils.SUFFIX_FY
                    )
                )
                map[i++.toDouble()] = sum
            }
        }
        if (map.size == chapters.size) return sum.toInt()
        val lr = LinearRegression(map)
        return lr.getY(chapters.size.toDouble()).toInt()
    }

    private fun countChar(file: File): Int { //统计字符数
        var charnum = 0 //字符数
        var x: Int
        var fReader: FileReader? = null
        try {
            fReader = FileReader(file)
            while ((fReader.read().also { x = it }) != -1) { //按字符读文件，判断，符合则字符加一
                val a = x.toChar()
                if (a != '\n' && a != '\r') {
                    charnum++
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtils.close(fReader)
        }
        return charnum //返回结果
    }
}