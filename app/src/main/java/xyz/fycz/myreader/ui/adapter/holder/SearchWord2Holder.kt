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

package xyz.fycz.myreader.ui.adapter.holder

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.adapter.ViewHolderImpl
import xyz.fycz.myreader.greendao.entity.search.SearchWord2

/**
 * @author fengyue
 * @date 2021/12/5 20:46
 */
class SearchWord2Holder : ViewHolderImpl<SearchWord2>() {

    private lateinit var tvSearchWord: TextView

    override fun getItemLayoutId(): Int {
        return R.layout.item_search_word2
    }

    override fun initView() {
        tvSearchWord = findById(R.id.tv_search_word)
    }

    override fun onBind(holder: RecyclerView.ViewHolder, data: SearchWord2, pos: Int) {
        val spannableString = SpannableString(data.dataStr)
        spannableString.setSpan(
            ForegroundColorSpan(Color.RED),
            data.dataIndex, data.dataIndex + data.keyword.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        tvSearchWord.text = spannableString
    }
}