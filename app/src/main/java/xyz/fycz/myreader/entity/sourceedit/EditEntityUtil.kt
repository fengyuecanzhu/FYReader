package xyz.fycz.myreader.entity.sourceedit

import xyz.fycz.myreader.R
import xyz.fycz.myreader.greendao.entity.rule.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author fengyue
 * @date 2021/2/9 12:03
 */
object EditEntityUtil {
    fun getSourceEntities(source: BookSource? = BookSource()): List<EditEntity> {
        val sourceEntities: ArrayList<EditEntity> = ArrayList()
        sourceEntities.apply {
            add(EditEntity("sourceUrl", source?.sourceUrl, R.string.source_url, "不能为空且唯一"))
            add(EditEntity("sourceName", source?.sourceName, R.string.source_name, "不能为空"))
            add(EditEntity("sourceGroup", source?.sourceGroup, R.string.source_group, "不同分组以;/,隔开"))
            add(
                EditEntity(
                    "sourceCharset",
                    source?.sourceCharset,
                    R.string.source_charset,
                    "默认UTF-8"
                )
            )
            add(
                EditEntity(
                    "sourceHeaders",
                    source?.sourceHeaders,
                    R.string.source_headers,
                    "json格式"
                )
            )
            add(EditEntity("loginUrl", source?.loginUrl, R.string.login_url, ""))
            add(EditEntity("sourceComment", source?.sourceComment, R.string.comment, "这是您留给使用者的说明"))
        }
        return sourceEntities
    }

    fun getSearchEntities(searchRule: SearchRule? = SearchRule()): List<EditEntity> {
        val searchEntities: ArrayList<EditEntity> = ArrayList()
        searchEntities.apply {
            add(
                EditEntity(
                    "searchUrl", searchRule?.searchUrl, R.string.r_search_url,
                    "搜索关键词以{key}进行占位;post请求以“,”分隔url,“,”前是搜索地址,“,”后是请求体"
                )
            )
            add(EditEntity("charset", searchRule?.charset, R.string.r_search_charset, "默认使用书源字符编码"))
            add(
                EditEntity(
                    "list", searchRule?.list, R.string.r_book_list,
                    "对于Matcher解析器：此处填写书籍列表所在区间，仅支持普通函数；" +
                            "\n对于Xpath/JsonPath解析器：此处填写书籍列表规则，仅支持列表函数"
                )
            )
            add(EditEntity("name", searchRule?.name, R.string.r_book_name))
            add(EditEntity("author", searchRule?.author, R.string.r_author))
            add(EditEntity("type", searchRule?.type, R.string.rule_book_type))
            add(EditEntity("wordCount", searchRule?.wordCount, R.string.rule_word_count))
            add(EditEntity("status", searchRule?.status, R.string.rule_status))
            add(EditEntity("desc", searchRule?.desc, R.string.rule_book_desc))
            add(EditEntity("lastChapter", searchRule?.lastChapter, R.string.rule_last_chapter))
            add(EditEntity("updateTime", searchRule?.updateTime, R.string.rule_update_time))
            add(EditEntity("imgUrl", searchRule?.imgUrl, R.string.rule_img_url))
            add(EditEntity("tocUrl", searchRule?.tocUrl, R.string.rule_toc_url))
            add(EditEntity("infoUrl", searchRule?.infoUrl, R.string.r_info_url, "为空时使用目录URL"))
            add(
                EditEntity(
                    "relatedWithInfo",
                    searchRule?.isRelatedWithInfo?.toString(),
                    R.string.r_search_related_with_info,
                    "搜索时是否关联书籍详情页，填true/t表示关联，false/f表示不关联，测试时不生效"
                )
            )
        }
        return searchEntities
    }

    fun getFindEntities(findRule: FindRule? = FindRule()): List<EditEntity> {
        val findEntities: ArrayList<EditEntity> = ArrayList()
        findEntities.apply {
            add(EditEntity("url", findRule?.url, R.string.r_find_url))
            add(
                EditEntity(
                    "bookList", findRule?.bookList, R.string.r_book_list,
                    "对于Matcher解析器：此处填写书籍列表所在区间，仅支持普通函数；" +
                            "\n对于Xpath/JsonPath解析器：此处填写书籍列表规则，仅支持列表函数"
                )
            )
            add(EditEntity("name", findRule?.name, R.string.r_book_name))
            add(EditEntity("author", findRule?.author, R.string.r_author))
            add(EditEntity("type", findRule?.type, R.string.rule_book_type))
            add(EditEntity("wordCount", findRule?.wordCount, R.string.rule_word_count))
            add(EditEntity("status", findRule?.status, R.string.rule_status))
            add(EditEntity("desc", findRule?.desc, R.string.rule_book_desc))
            add(EditEntity("lastChapter", findRule?.lastChapter, R.string.rule_last_chapter))
            add(EditEntity("updateTime", findRule?.updateTime, R.string.rule_update_time))
            add(EditEntity("imgUrl", findRule?.imgUrl, R.string.rule_img_url))
            add(EditEntity("tocUrl", findRule?.tocUrl, R.string.rule_toc_url))
            add(EditEntity("infoUrl", findRule?.infoUrl, R.string.r_info_url))
        }
        return findEntities
    }

    fun getInfoEntities(infoRule: InfoRule? = InfoRule()): List<EditEntity> {
        val infoEntities: ArrayList<EditEntity> = ArrayList()
        infoEntities.apply {
            add(EditEntity("urlPattern", infoRule?.urlPattern, R.string.book_url_pattern, ""))
            add(EditEntity("init", infoRule?.init, R.string.rule_book_info_init))
            add(EditEntity("name", infoRule?.name, R.string.r_book_name))
            add(EditEntity("author", infoRule?.author, R.string.r_author))
            add(EditEntity("type", infoRule?.type, R.string.rule_book_type))
            add(EditEntity("desc", infoRule?.desc, R.string.rule_book_desc))
            add(EditEntity("status", infoRule?.status, R.string.rule_status))
            add(EditEntity("wordCount", infoRule?.wordCount, R.string.rule_word_count))
            add(EditEntity("lastChapter", infoRule?.lastChapter, R.string.rule_last_chapter))
            add(EditEntity("updateTime", infoRule?.updateTime, R.string.rule_update_time))
            add(EditEntity("imgUrl", infoRule?.imgUrl, R.string.rule_img_url))
            add(EditEntity("tocUrl", infoRule?.tocUrl, R.string.rule_toc_url))
        }
        return infoEntities
    }

    fun getTocEntities(tocRule: TocRule? = TocRule()): List<EditEntity> {
        val tocEntities: ArrayList<EditEntity> = ArrayList()
        tocEntities.apply {
            add(
                EditEntity(
                    "chapterBaseUrl", tocRule?.chapterBaseUrl, R.string.rule_chapter_base_url,
                    "如果章节URL(一般为相对路径)无法定位章节，可填写此规则获取，默认为书源URL"
                )
            )
            add(
                EditEntity(
                    "chapterList", tocRule?.chapterList, R.string.rule_chapter_list,
                    "对于Mathcer解析器：此处填写书籍列表所在区间，仅支持普通函数；" +
                            "\n对于Xpath/JsonPath解析器：此处填写书籍列表规则，仅支持列表函数"
                )
            )
            add(
                EditEntity(
                    "chapterName", tocRule?.chapterName, R.string.rule_chapter_name,
                    "对于Mathcer解析器：此处填写章节名称和URL规则，其中章节名称以<title>占位，章节URL以<link>占位，仅支持列表函数\n" +
                            "对于Xpath/JsonPath解析器：此处填写章节名称，仅支持普通函数"
                )
            )
            add(
                EditEntity(
                    "chapterUrl", tocRule?.chapterUrl, R.string.rule_chapter_url,
                    "对于Mathcer解析器：此处不用填写\n" +
                            "对于Xpath/JsonPath解析器：此处填写章节URL规则"
                )
            )
            add(
                EditEntity(
                    "tocUrlNext", tocRule?.tocUrlNext, R.string.rule_next_toc_url,
                    "填写后获取目录时将会不断地从目录下一页获取章节，直至下一页URL为空时停止，注意：千万不要获取恒存在的URL，否则将出现死循环甚至崩溃"
                )
            )
        }
        return tocEntities
    }

    fun getContentEntities(contentRule: ContentRule? = ContentRule()): List<EditEntity> {
        val contentEntities: ArrayList<EditEntity> = ArrayList()
        contentEntities.apply {
            add(EditEntity("content", contentRule?.content, R.string.rule_book_content))
            add(
                EditEntity(
                    "contentBaseUrl", contentRule?.contentBaseUrl, R.string.rule_base_url_content,
                    "如果下一页URL(一般为相对路径)无法定位下一页，可填写此规则获取，默认为书源URL"
                )
            )
            add(
                EditEntity(
                    "contentUrlNext", contentRule?.contentUrlNext, R.string.rule_next_content,
                    "填写后正文时将会不断地从下一页获取内容，直至下一页URL为空时停止，注意：千万不要获取恒存在的URL，否则将出现死循环甚至崩溃"
                )
            )
        }
        return contentEntities
    }


    fun getSource(bookSource: BookSource, sourceEntities: List<EditEntity>): BookSource {
        val source = bookSource.clone() as BookSource
        sourceEntities.forEach {
            when (it.key) {
                "sourceUrl" -> source.sourceUrl = it.value
                "sourceName" -> source.sourceName = it.value
                "sourceGroup" -> source.sourceGroup = it.value
                "sourceCharset" -> source.sourceCharset = it.value
                "sourceHeaders" -> source.sourceHeaders = it.value
                "loginUrl" -> source.loginUrl = it.value
                "sourceComment" -> source.sourceComment = it.value
            }
        }
        return source
    }

    fun getSearchRule(searchEntities: List<EditEntity>): SearchRule {
        val searchRule = SearchRule()
        searchEntities.forEach {
            when (it.key) {
                "searchUrl" -> searchRule.searchUrl = it.value
                "charset" -> searchRule.charset = it.value
                "list" -> searchRule.list = it.value
                "name" -> searchRule.name = it.value
                "author" -> searchRule.author = it.value
                "type" -> searchRule.type = it.value
                "desc" -> searchRule.desc = it.value
                "wordCount" -> searchRule.wordCount = it.value
                "status" -> searchRule.status = it.value
                "lastChapter" -> searchRule.lastChapter = it.value
                "updateTime" -> searchRule.updateTime = it.value
                "imgUrl" -> searchRule.imgUrl = it.value
                "tocUrl" -> searchRule.tocUrl = it.value
                "infoUrl" -> searchRule.infoUrl = it.value
                "relatedWithInfo" -> searchRule.isRelatedWithInfo = it.value?.contains("t") == true
            }
        }
        return searchRule
    }

    fun getFindRule(findEntities: List<EditEntity>): FindRule {
        val findRule = FindRule()
        findEntities.forEach {
            when (it.key) {
                "url" -> findRule.url = it.value
                "bookList" -> findRule.bookList = it.value
                "name" -> findRule.name = it.value
                "author" -> findRule.author = it.value
                "type" -> findRule.type = it.value
                "desc" -> findRule.desc = it.value
                "wordCount" -> findRule.wordCount = it.value
                "status" -> findRule.status = it.value
                "lastChapter" -> findRule.lastChapter = it.value
                "updateTime" -> findRule.updateTime = it.value
                "imgUrl" -> findRule.imgUrl = it.value
                "tocUrl" -> findRule.tocUrl = it.value
                "infoUrl" -> findRule.infoUrl = it.value
            }
        }
        return findRule
    }

    fun getInfoRule(infoRuleEntities: List<EditEntity>): InfoRule {
        val infoRule = InfoRule()
        infoRuleEntities.forEach {
            when (it.key) {
                "urlPattern" -> infoRule.urlPattern = it.value
                "init" -> infoRule.init = it.value
                "name" -> infoRule.name = it.value
                "author" -> infoRule.author = it.value
                "type" -> infoRule.type = it.value
                "desc" -> infoRule.desc = it.value
                "status" -> infoRule.status = it.value
                "wordCount" -> infoRule.wordCount = it.value
                "lastChapter" -> infoRule.lastChapter = it.value
                "updateTime" -> infoRule.updateTime = it.value
                "imgUrl" -> infoRule.imgUrl = it.value
                "tocUrl" -> infoRule.tocUrl = it.value
            }
        }
        return infoRule
    }

    fun getTocRule(infoRuleEntities: List<EditEntity>): TocRule {
        val tocRule = TocRule()
        infoRuleEntities.forEach {
            when (it.key) {
                "chapterList" -> tocRule.chapterList = it.value
                "chapterBaseUrl" -> tocRule.chapterBaseUrl = it.value
                "chapterName" -> tocRule.chapterName = it.value
                "chapterUrl" -> tocRule.chapterUrl = it.value
                "tocUrlNext" -> tocRule.tocUrlNext = it.value
            }
        }
        return tocRule
    }

    fun getContentRule(infoRuleEntities: List<EditEntity>): ContentRule {
        val contentRule = ContentRule()
        infoRuleEntities.forEach {
            when (it.key) {
                "content" -> contentRule.content = it.value
                "contentBaseUrl" -> contentRule.contentBaseUrl = it.value
                "contentUrlNext" -> contentRule.contentUrlNext = it.value
            }
        }
        return contentRule
    }

}