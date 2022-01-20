package xyz.fycz.myreader.entity.thirdsource.source3

/**
 * @author fengyue
 * @date 2022/1/20 13:50
 */
data class ContentRule3(
    var content: String? = null,
    var nextContentUrl: String? = null,
    var webJs: String? = null,
    var sourceRegex: String? = null,
    var replaceRegex: String? = null, //替换规则
    var imageStyle: String? = null,   //默认大小居中,FULL最大宽度
    var payAction: String? = null,    //购买操作,url/js
)
