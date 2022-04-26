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
    val md5: String,//32位
    val changelog: String
) {
    constructor(name: String, versionCode: Int) :
            this(name, versionCode, "", "", "", "")
}
