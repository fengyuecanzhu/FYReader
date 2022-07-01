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

package xyz.fycz.dynamic.fix

import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook
import xyz.fycz.myreader.common.URLCONST
import xyz.fycz.myreader.webapi.LanZouApi

/**
 * @author fengyue
 * @date 2022/6/21 18:30
 */
@AppFix([], ["更新订阅书源链接，仅支持v2.4.3版本及以上版本"], "2022-06-21")
class AppSubSourceFix : AppFixHandle {
    override fun onFix(key: String): BooleanArray {
        return handleFix(
            key,
            "subSource" to { fxSubSource() },
        )
    }

    private fun fxSubSource() {
        MapleUtils.findAndHookMethod(
            LanZouApi::class.java,
            "getFoldFiles",
            String::class.java,
            Int::class.java,
            String::class.java,
            object : MethodHook() {
                override fun beforeHookedMethod(param: MapleBridge.MethodHookParam) {
                    if (param.args[0] == URLCONST.SUB_SOURCE_URL) {
                        param.args[0] = "https://fycz.lanzoum.com/b00pucrch"
                        param.args[2] = "b0ox"
                    }
                }
            }
        )
    }
}