package xyz.fycz.myreader.entity.ad

/**
 * @author fengyue
 * @date 2022/3/3 15:00
 */
data class AdBean(
    val status: Int,// 广告展示状态：0：不展示，1：展示信息流广告，2：展示插屏广告
    val interval: Int,// 广告展示间隔(单位：分钟)
)
