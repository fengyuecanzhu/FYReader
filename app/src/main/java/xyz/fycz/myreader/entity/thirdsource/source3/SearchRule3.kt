package xyz.fycz.myreader.entity.thirdsource.source3

/**
 * @author fengyue
 * @date 2022/1/20 13:48
 */
data class SearchRule3(
    var checkKeyWord: String? = null,               // 校验关键字
    var bookList: String? = null,
    var name: String? = null,
    var author: String? = null,
    var intro: String? = null,
    var kind: String? = null,
    var lastChapter: String? = null,
    var updateTime: String? = null,
    var bookUrl: String? = null,
    var coverUrl: String? = null,
    var wordCount: String? = null
)
