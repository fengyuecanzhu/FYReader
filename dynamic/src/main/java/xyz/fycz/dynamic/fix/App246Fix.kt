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

import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewbinding.ViewBinding
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.adapter2.onClick
import xyz.fycz.myreader.ui.activity.MoreSettingActivity
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.util.utils.FileUtils
import xyz.fycz.myreader.util.utils.ScreenUtils

/**
 * @author fengyue
 * @date 2022/6/3 15:34
 */
@AppFix([243, 244, 245, 246], ["[设置-缓存设置]新增清除广告文件"], "2022-06-03")
class App246Fix : AppFixHandle {

    override fun onFix(key: String): BooleanArray {
        val result = try {
            fxAdFile()
            true
        } catch (e: Exception) {
            MapleUtils.log(e)
            false
        }
        fixResult(key, "adFile", result)
        return booleanArrayOf(result)
    }

    fun fxAdFile() {
        MapleUtils.findAndHookMethod(
            MoreSettingActivity::class.java,
            "initWidget",
            object : MethodHook() {
                override fun afterHookedMethod(param: MapleBridge.MethodHookParam) {
                    val binding =
                        MapleUtils.getObjectField(param.thisObject, "binding") as ViewBinding
                    val rootLayout =
                        (MapleUtils.getObjectField(
                            binding,
                            "svContent"
                        ) as ScrollView)[0] as ViewGroup
                    addDeleteAdFileView(rootLayout, rootLayout.childCount)
                }
            }
        )
    }

    fun addDeleteAdFileView(rootLayout: ViewGroup, index: Int) {
        val context = rootLayout.context
        val resources = context.resources
        val rlDeleteAdFile = RelativeLayout(context)
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ScreenUtils.dpToPx(50)
        )
        rlDeleteAdFile.setPadding(
            ScreenUtils.dpToPx(20),
            0,
            ScreenUtils.dpToPx(20),
            0
        )
        rlDeleteAdFile.background =
            ContextCompat.getDrawable(context, R.drawable.selector_common_bg)
        rlDeleteAdFile.id = R.id.rl_delete_ad_file
        val textview = TextView(context)
        val textViewParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.CENTER_VERTICAL)
        }
        textview.setText(R.string.delete_ad_file)
        textview.setTextColor(resources.getColor(R.color.textSecondary))
        textview.textSize =
            ScreenUtils.pxToSp(resources.getDimension(R.dimen.text_normal_size).toInt()).toFloat()
        rlDeleteAdFile.addView(textview, textViewParams)
        rlDeleteAdFile.onClick {
            FileUtils.deleteFile(FileUtils.getFilePath())
            ToastUtils.showSuccess("广告文件删除成功")
        }
        rootLayout.addView(rlDeleteAdFile, index, layoutParams)
    }
}