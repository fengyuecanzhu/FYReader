package xyz.fycz.myreader.ui.adapter.holder

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.adapter.ViewHolderImpl
import xyz.fycz.myreader.entity.SearchWord2

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