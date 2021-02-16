package xyz.fycz.myreader.ui.adapter.holder

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
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

    override fun onBind(data: EditEntity, pos: Int) {
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