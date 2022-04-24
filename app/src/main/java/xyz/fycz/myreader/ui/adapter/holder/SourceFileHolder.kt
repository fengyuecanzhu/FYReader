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

package xyz.fycz.myreader.ui.adapter.holder

import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.adapter.ViewHolderImpl
import xyz.fycz.myreader.entity.lanzou.LanZouFile
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.entity.SubscribeFile

/**
 * @author fengyue
 * @date 2022/3/3 10:17
 */
class SourceFileHolder : ViewHolderImpl<SubscribeFile>() {

    private lateinit var name: TextView
    private lateinit var sizeTime: TextView

    override fun getItemLayoutId(): Int {
        return R.layout.item_lan_zou_file
    }

    override fun initView() {
        name = findById(R.id.tv_file_name)
        sizeTime = findById(R.id.tv_file_size_time)
    }

    @SuppressLint("SetTextI18n")
    override fun onBind(holder: RecyclerView.ViewHolder, data: SubscribeFile, pos: Int) {
        val subscribed = DbManager.getDaoSession().subscribeFileDao.load(data.id)
        var nameStr = data.name
        var isSubscribed = false
        var hasUpdate = false
        if (subscribed != null) {
            isSubscribed = true
            nameStr = "[已订阅]$nameStr"
            if (subscribed.date < data.date) {
                hasUpdate = true
                nameStr = "$nameStr(有更新)"
            }
        }
        val spannableString = SpannableString(nameStr)

        if (isSubscribed) {
            spannableString.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.toast_blue)),
                0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        if (hasUpdate) {
            spannableString.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.toast_red)),
                nameStr.length - 5, nameStr.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        name.text = spannableString

        sizeTime.text = "${data.size}  ${data.date}"
    }
}