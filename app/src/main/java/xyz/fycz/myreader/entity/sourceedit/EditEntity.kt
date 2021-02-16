package xyz.fycz.myreader.entity.sourceedit

/**
 * @author fengyue
 * @date 2021/2/9 10:15
 */
data class EditEntity(
        var key: String,
        var value: String? = "",
        var hint: Int,
        var tip: String? = "规则后可接##加@r/@a/@c/@nc函数"
)
