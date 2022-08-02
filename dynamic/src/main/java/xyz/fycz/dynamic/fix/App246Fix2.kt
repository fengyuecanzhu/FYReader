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
import xyz.fycz.myreader.widget.page.PageView

/**
 * @author fengyue
 * @date 2022/6/23 20:51
 */
@AppFix([243, 244, 245, 246], ["修复阅读界面概率性闪退的问题"], "2022-06-23")
class App246Fix2 : AppFixHandle {
    override fun onFix(key: String): BooleanArray {
        return handleFix(
            key,
            "pageView" to { fxPageView() },
        )
    }

    private fun fxPageView() {
        MapleUtils.findAndHookMethod(
            PageView::class.java,
            "onDetachedFromWindow",
            object : MethodHook(){
                override fun beforeHookedMethod(param: MapleBridge.MethodHookParam) {
                    val mPageAnim = MapleUtils.getObjectField(param.thisObject, "mPageAnim")
                    if (mPageAnim == null){
                        MapleUtils.setObjectField(param.thisObject, "mPageLoader", null)
                        param.result = null
                    }
                }
            }
        )
    }
}