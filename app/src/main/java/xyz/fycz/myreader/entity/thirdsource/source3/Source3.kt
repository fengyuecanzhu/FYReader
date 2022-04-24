/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.entity.thirdsource.source3

/**
 * @author fengyue
 * @date 2022/1/20 13:47
 */
data class Source3(
    var bookSourceUrl: String = "",                 // 地址，包括 http/https
    var bookSourceName: String = "",                // 名称
    var bookSourceGroup: String? = null,            // 分组
    //未加
    var bookSourceType: Int = 0,     // 类型，0 文本，1 音频, 3 图片
    var bookUrlPattern: String? = null,             // 详情页url正则
    var customOrder: Int = 0,                       // 手动排序编号
    var enabled: Boolean = true,                    // 是否启用
    //未加
    var enabledExplore: Boolean = true,             // 启用发现
    var concurrentRate: String? = null,    // 并发率
    var header: String? = null,            // 请求头
    var loginUrl: String? = null,          // 登录地址
    //未加
    var loginUi: String? = null,      // 登录UI
    var loginCheckJs: String? = null,               // 登录检测js
    var bookSourceComment: String? = null,          // 注释
    var lastUpdateTime: Long = 0,                   // 最后更新时间，用于排序
    //未加
    var respondTime: Long = 180000L,                // 响应时间，用于排序
    var weight: Int = 0,                            // 智能排序的权重
    var exploreUrl: String? = null,                 // 发现url
    var ruleExplore: ExploreRule3? = null,           // 发现规则
    var searchUrl: String? = null,                  // 搜索url
    var ruleSearch: SearchRule3? = null,             // 搜索规则
    var ruleBookInfo: BookInfoRule3? = null,         // 书籍信息页规则
    var ruleToc: TocRule3? = null,                   // 目录页规则
    var ruleContent: ContentRule3? = null            // 正文页规则
)
