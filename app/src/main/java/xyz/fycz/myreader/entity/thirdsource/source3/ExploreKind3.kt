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

data class ExploreKind3(
    val title: String,
    val url: String? = null,
    val style: Style? = null
) {

    companion object {
        val defaultStyle = Style()
    }

    fun style(): Style {
        return style ?: defaultStyle
    }

    data class Style(
        val layout_flexGrow: Float = 0F,
        val layout_flexShrink: Float = 1F,
        val layout_alignSelf: String = "auto",
        val layout_flexBasisPercent: Float = -1F,
        val layout_wrapBefore: Boolean = false,
    ) {

        fun alignSelf(): Int {
            return when (layout_alignSelf) {
                "auto" -> -1
                "flex_start" -> 0
                "flex_end" -> 1
                "center" -> 2
                "baseline" -> 3
                "stretch" -> 4
                else -> -1
            }
        }

    }

}