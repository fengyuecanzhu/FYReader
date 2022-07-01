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

package xyz.fycz.dynamic.fix

import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodReplacement
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.entity.Chapter
import xyz.fycz.myreader.greendao.service.ChapterService
import xyz.fycz.myreader.ui.adapter.BookcaseAdapter
import xyz.fycz.myreader.util.utils.FileUtils
import java.io.*

/**
 * @author fengyue
 * @date 2022/4/25 23:00
 */
@AppFix([243, 244], ["修复书籍无法导出缓存的问题"], "2022-04-26")
class App244Fix: AppFixHandle {
    override fun onFix(key: String): BooleanArray {
        return handleFix(
            key,
            "unionChapterCathe" to { fixUnionChapterCathe() },
        )
    }

    private fun fixUnionChapterCathe() {
        MapleUtils.findAndHookMethod(
            BookcaseAdapter::class.java,
            "unionChapterCathe",
            Book::class.java,
            object : MethodReplacement() {
                override fun replaceHookedMethod(param: MapleBridge.MethodHookParam): Any {
                    return unionChapterCathe(param.args[0] as Book)
                }
            }
        )
    }

    @Throws(IOException::class)
    fun unionChapterCathe(book: Book): Boolean {
        val chapters = ChapterService.getInstance().findBookAllChapterByBookId(book.id) as ArrayList<Chapter>
        if (chapters.size == 0) {
            return false
        }
        val bookFile = File(APPCONST.BOOK_CACHE_PATH + book.id)
        if (!bookFile.exists()) {
            return false
        }
        var br: BufferedReader?
        val bw: BufferedWriter?
        val filePath =
            APPCONST.TXT_BOOK_DIR + book.name + (if (book.author.isNullOrEmpty()) "" else " 作者：" + book.author) + ".txt"
        bw = BufferedWriter(FileWriter(FileUtils.getFile(filePath)))
        bw.write("《${book.name}》\n")
        if (!book.author.isNullOrEmpty()) {
            bw.write("作者：${book.author}\n")
        }
        if (!book.desc.isNullOrEmpty()) {
            bw.write("简介：\n${book.desc}\n")
        }
        bw.newLine()
        for (chapter in chapters) {
            if (ChapterService.isChapterCached(chapter)) {
                bw.write("\t" + chapter.title)
                bw.newLine()
                br = BufferedReader(FileReader(ChapterService.getChapterFile(chapter)))
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    bw.write(line)
                    bw.newLine()
                }
                br.close()
            }
        }
        bw.flush()
        bw.close()
        return true
    }
}