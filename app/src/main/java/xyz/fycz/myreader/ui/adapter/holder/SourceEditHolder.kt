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

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.adapter.ViewHolderImpl
import xyz.fycz.myreader.entity.sourceedit.EditEntity

/**
 * @author fengyue
 * @date 2021/2/9 10:25
 */
class SourceEditHolder : ViewHolderImpl<EditEntity>() {
    private var textInputLayout: TextInputLayout? = null
    private var editText: TextInputEditText? = null
    private var tvTip: TextView? = null

    override fun getItemLayoutId(): Int {
        return R.layout.item_source_edit
    }

    override fun initView() {
        textInputLayout = findById(R.id.text_input_layout)
        editText = findById(R.id.edit_text)
        tvTip = findById(R.id.tv_tip)
    }

    override fun onBind(holder: RecyclerView.ViewHolder, data: EditEntity, pos: Int) {
        if (editText?.getTag(R.id.tag1) == null) {
            val listener = object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    editText?.isCursorVisible = false
                    editText?.isCursorVisible = true
                    editText?.isFocusable = true
                    editText?.isFocusableInTouchMode = true
                }

                override fun onViewDetachedFromWindow(v: View) {

                }
            }
            editText?.addOnAttachStateChangeListener(listener)
            editText?.setTag(R.id.tag1, listener)
        }
        editText?.getTag(R.id.tag2)?.let {
            if (it is TextWatcher) {
                editText?.removeTextChangedListener(it)
            }
        }
        textInputLayout?.hint = context.getString(data.hint)
        editText?.setText(data.value)
        if (data.tip.isNullOrEmpty()) {
            tvTip?.visibility = View.GONE
        } else {
            tvTip?.visibility = View.VISIBLE
            tvTip?.text = data.tip
        }
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                data.value = (s.toString())
            }
        }
        editText?.addTextChangedListener(textWatcher)
        editText?.setTag(R.id.tag2, textWatcher)
    }
}