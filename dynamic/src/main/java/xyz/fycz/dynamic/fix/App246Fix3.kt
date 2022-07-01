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

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.widget.NestedScrollView
import androidx.viewbinding.ViewBinding
import me.fycz.maple.MapleBridge
import me.fycz.maple.MapleUtils
import me.fycz.maple.MethodHook
import xyz.fycz.dynamic.AppLoadImpl
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.adapter2.onClick
import xyz.fycz.myreader.entity.PluginConfig
import xyz.fycz.myreader.ui.activity.AboutActivity
import xyz.fycz.myreader.ui.dialog.DialogCreator
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.utils.GSON
import xyz.fycz.myreader.util.utils.ScreenUtils
import xyz.fycz.myreader.util.utils.fromJsonObject

/**
 * @author fengyue
 * @date 2022/6/28 19:22
 */
@AppFix([243, 244, 245, 246], ["关于界面新增插件加载结果"], "2022-06-28")
class App246Fix3 : AppFixHandle {

    override fun onFix(key: String): BooleanArray {
        return handleFix(
            key,
            "pluginView" to { fxPluginView() },
        )
    }

    private fun fxPluginView() {
        MapleUtils.findAndHookMethod(
            AboutActivity::class.java,
            "initWidget",
            object : MethodHook() {
                override fun afterHookedMethod(param: MapleBridge.MethodHookParam) {
                    val binding =
                        MapleUtils.getObjectField(param.thisObject, "binding") as ViewBinding
                    val ilBinding = MapleUtils.getObjectField(binding, "il") as ViewBinding
                    val rootLayout = (ilBinding.root as NestedScrollView)[0] as ViewGroup
                    addPluginView(rootLayout, 1)
                }
            }
        )
    }

    @SuppressLint("SetTextI18n")
    fun addPluginView(rootLayout: ViewGroup, index: Int) {
        val pluginConfig = GSON.fromJsonObject<PluginConfig>(
            SharedPreUtils.getInstance().getString("pluginConfig")
        ) ?: PluginConfig("dynamic.dex", 100)

        val context = rootLayout.context
        val rlPlugin = RelativeLayout(context)
        val pluginLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ScreenUtils.dpToPx(50)
        )
        rlPlugin.background = ContextCompat.getDrawable(context, R.drawable.selector_common_bg)
        rlPlugin.gravity = Gravity.CENTER
        rlPlugin.id = R.id.rl_update
        rlPlugin.setPadding(
            ScreenUtils.dpToPx(20),
            0,
            ScreenUtils.dpToPx(10),
            0
        )
        val textview = TextView(context)
        val textviewLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.CENTER_VERTICAL)
        }
        textview.text = "插件版本：${pluginConfig.version}"
        textview.setTextColor(context.resources.getColor(R.color.textPrimary))
        textview.textSize =
            ScreenUtils.pxToSp(
                context.resources.getDimension(R.dimen.text_normal_size).toInt()
            ).toFloat()
        rlPlugin.addView(textview, textviewLayoutParams)
        val imageView = AppCompatImageView(context)
        val imageViewLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_END)
            addRule(RelativeLayout.CENTER_VERTICAL)
        }
        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_right_arrow))
        imageView.drawable.setTint(context.resources.getColor(R.color.textPrimary))
        rlPlugin.addView(imageView, imageViewLayoutParams)
        rlPlugin.onClick {
            DialogCreator.createTipDialog(
                context,
                "插件版本：${pluginConfig.version}",
                "当前版本更新日志：\n${pluginConfig.changelog}\n\n插件加载结果：\n" +
                        AppLoadImpl.allFixInfoSb.toString()
            )
        }
        rootLayout.addView(rlPlugin, index, pluginLayoutParams)

        val view = View(context)
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ScreenUtils.dpToPx(10)
        )
        rootLayout.addView(view, index + 1, layoutParams)
    }
}