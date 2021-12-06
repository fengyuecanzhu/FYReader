package xyz.fycz.myreader.ui.adapter.holder

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.adapter.BaseListAdapter
import xyz.fycz.myreader.base.adapter.IViewHolder
import xyz.fycz.myreader.base.adapter.ViewHolderImpl
import xyz.fycz.myreader.entity.SearchWord1
import xyz.fycz.myreader.entity.SearchWord2

/**
 * @author fengyue
 * @date 2021/12/5 20:28
 */
class SearchWord1Holder(var activity: AppCompatActivity) : ViewHolderImpl<SearchWord1>() {

    private lateinit var tvChapterTitle: TextView
    private lateinit var rvWordList: RecyclerView
    private lateinit var adapter: BaseListAdapter<SearchWord2>

    override fun getItemLayoutId(): Int {
        return R.layout.item_search_word1
    }

    override fun initView() {
        tvChapterTitle = findById(R.id.tv_chapter_title)
        rvWordList = findById(R.id.rv_search_word2)
    }

    override fun onBind(holder: RecyclerView.ViewHolder?, data: SearchWord1?, pos: Int) {
        tvChapterTitle.text = data?.chapterTitle
        adapter = object : BaseListAdapter<SearchWord2>() {
            override fun createViewHolder(viewType: Int): IViewHolder<SearchWord2> {
                return SearchWord2Holder()
            }
        }
        rvWordList.layoutManager = LinearLayoutManager(context)
        rvWordList.adapter = adapter
        adapter.refreshItems(data?.searchWord2List)
        adapter.setOnItemClickListener { _, pos1 ->
            val searchWord2 = adapter.getItem(pos1)
            val intent = Intent()
            intent.putExtra("chapterNum", searchWord2.chapterNum)
            intent.putExtra("countInChapter", searchWord2.count)
            intent.putExtra("keyword", searchWord2.keyword)
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        }
    }
}