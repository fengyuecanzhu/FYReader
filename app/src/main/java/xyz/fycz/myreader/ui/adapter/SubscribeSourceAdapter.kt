package xyz.fycz.myreader.ui.adapter

import android.widget.Filter
import androidx.fragment.app.Fragment
import xyz.fycz.myreader.base.BaseFragment
import xyz.fycz.myreader.base.adapter.IViewHolder
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.ui.adapter.holder.SubscribeSourceHolder
import java.util.*

/**
 * @author fengyue
 * @date 2022/3/3 12:08
 */
class SubscribeSourceAdapter(
    val fragment: Fragment,
    val sources: List<BookSource>,
    private val onDelListener: OnDelListener
) : BaseSourceAdapter() {
    override fun createViewHolder(viewType: Int): IViewHolder<BookSource> {
        return SubscribeSourceHolder(fragment, checkMap, onDelListener)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()
                var mFilterList: MutableList<BookSource> = ArrayList()
                if (charString.isEmpty()) {
                    //没有过滤的内容，则使用源数据
                    mFilterList = sources.toMutableList()
                } else {
                    for (source in sources) {
                        //这里根据需求，添加匹配规则
                        if (source.sourceName.contains(charString) ||
                            source.sourceGroup.contains(charString)
                        ) {
                            mFilterList.add(source)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = mFilterList
                return filterResults
            }

            //把过滤后的值返回出来
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                refreshItems(results.values as List<BookSource>)
            }
        }
    }
    fun removeItem(pos: Int) {
        mList.removeAt(pos)
        notifyItemRemoved(pos)
        if (pos != mList.size) notifyItemRangeChanged(pos, mList.size - pos)
    }
    interface OnDelListener {
        fun onDel(which: Int, source: BookSource)
    }
}