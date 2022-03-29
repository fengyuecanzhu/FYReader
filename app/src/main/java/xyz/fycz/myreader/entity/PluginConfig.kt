package xyz.fycz.myreader.entity

/**
 * @author fengyue
 * @date 2022/3/29 14:55
 */
data class PluginConfig(
    val name: String,
    val versionCode: Int,
    val version: String,
    val url: String,
    val changelog: String
)
