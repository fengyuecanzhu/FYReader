package xyz.fycz.myreader.entity

/**
 * @author fengyue
 * @date 2021/12/5 20:18
 */
data class SearchWord1(
    var bookId: String,
    var chapterNum: Int,
    var chapterTitle: String,
    var searchWord2List: MutableList<SearchWord2>
)
