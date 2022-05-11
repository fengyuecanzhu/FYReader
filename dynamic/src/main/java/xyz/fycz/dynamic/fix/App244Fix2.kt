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
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler

/**
 * @author fengyue
 * @date 2022/5/11 9:31
 */
@AppFix([243, 244], ["修复搜索时当前分组不存在时无法搜索的问题"], "2022-05-11")
class App244Fix2 : AppFixHandle{
    override fun onFix(key: String): BooleanArray {
        var fx = false
        try {
            fixGetEnableReadCrawlers()
            fx = true
            fixResult(key, "getEnableReadCrawlers", true)
        } catch (e: Exception) {
            MapleUtils.log(e)
            fixResult(key, "getEnableReadCrawlers", false)
        }
        return booleanArrayOf(fx)
    }

    private fun fixGetEnableReadCrawlers() {
        MapleUtils.findAndHookMethod(
            ReadCrawlerUtil::class.java,
            "getEnableReadCrawlers",
            String::class.java,
            object : MethodReplacement() {
                override fun replaceHookedMethod(param: MapleBridge.MethodHookParam): Any {
                    return getEnableReadCrawlers(param.args[0] as String)
                }
            }
        )
    }

    fun getEnableReadCrawlers(group: String?): List<ReadCrawler> {
        val crawlers = ArrayList<ReadCrawler>()
        var sources =
            if (group.isNullOrEmpty())
                BookSourceManager.getEnabledBookSource()
            else BookSourceManager.getEnableSourceByGroup(group)
        if (sources.size == 0) {
            sources = BookSourceManager.getEnabledBookSource()
        }
        for (source in sources) {
            crawlers.add(ReadCrawlerUtil.getReadCrawler(source))
        }
        return crawlers
    }
}