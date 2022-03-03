package xyz.fycz.myreader.ui.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Single
import io.reactivex.SingleEmitter
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.BaseFragment
import xyz.fycz.myreader.base.adapter2.onClick
import xyz.fycz.myreader.base.observer.MySingleObserver
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.databinding.FragmentSubscribeSourceBinding
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager
import xyz.fycz.myreader.ui.activity.BookSourceActivity
import xyz.fycz.myreader.ui.activity.SourceSubscribeActivity
import xyz.fycz.myreader.ui.adapter.SubscribeSourceAdapter
import xyz.fycz.myreader.ui.dialog.DialogCreator
import xyz.fycz.myreader.ui.dialog.MyAlertDialog
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.util.utils.RxUtils
import xyz.fycz.myreader.widget.DividerItemDecoration
import java.util.ArrayList

/**
 * @author fengyue
 * @date 2022/3/3 11:36
 */
class SubscribeSourceFragment(private val sourceActivity: BookSourceActivity) : BaseFragment() {
    private lateinit var binding: FragmentSubscribeSourceBinding
    private var mBookSources: MutableList<BookSource>? = null
    private var mAdapter: SubscribeSourceAdapter? = null
    private var featuresMenu: PopupMenu? = null
    override fun bindView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = FragmentSubscribeSourceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        getSources()
    }

    private fun getSources(){
        Single.create { emitter: SingleEmitter<List<BookSource>> ->
            emitter.onSuccess(BookSourceManager.getAllSubSource())
        }.compose { RxUtils.toSimpleSingle(it) }
            .subscribe(object : MySingleObserver<List<BookSource>>() {
                override fun onSuccess(sources: List<BookSource>) {
                    mBookSources = sources.toMutableList()
                    initSourceList()
                }

                override fun onError(e: Throwable) {
                    ToastUtils.showError(" 数据加载失败\n${e.localizedMessage}")
                }
            })
    }

    private fun initSourceList() {
        mAdapter =
            SubscribeSourceAdapter(mBookSources!!, object : SubscribeSourceAdapter.OnDelListener {
                override fun onDel(which: Int, source: BookSource) {
                    mBookSources?.remove(source)
                    mAdapter?.removeItem(which)
                }
            })
        mAdapter?.setOnItemClickListener { _, pos ->
            mAdapter?.setCheckedItem(pos)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = mAdapter
        //设置分割线
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context))
        mAdapter?.refreshItems(mBookSources)
    }

    override fun initWidget(savedInstanceState: Bundle?) {
        super.initWidget(savedInstanceState)
    }

    override fun initClick() {
        super.initClick()
        binding.tvSubscribeSource.onClick {
            startActivityForResult(Intent(context, SourceSubscribeActivity::class.java),
                APPCONST.REQUEST_SUBSCRIBE)
        }
        binding.ivGroup.setOnClickListener { view: View? ->
            showSourceGroupMenu(view)
        }
        binding.ivMenu.setOnClickListener { v ->
            if (featuresMenu == null) {
                initFeaturesMenu(v)
            }
            featuresMenu?.show()
        }
        binding.tvSubscribeSourceTip.onClick {
            MyAlertDialog.showTipDialogWithLink(context, getString(R.string.subscribe_source_tip), R.string.subscribe_source_detail_tip)
        }
    }

    /**
     * 显示书源分组菜单
     */
    private fun showSourceGroupMenu(view: View?) {
        val popupMenu = PopupMenu(sourceActivity, view, Gravity.END)
        val groupList = BookSourceManager.getGroupList(true)
        popupMenu.menu.add(0, 0, 0, "所有书源")
        for (i in groupList.indices) {
            popupMenu.menu.add(0, 0, i + 1, groupList[i])
        }
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            if (menuItem.order > 0) {
                sourceActivity.searchView.onActionViewExpanded()
                sourceActivity.searchView.clearFocus()
                sourceActivity.searchView.setQuery(menuItem.title, false)
            } else {
                sourceActivity.searchView.onActionViewCollapsed()
            }
            true
        }
        popupMenu.show()
    }

    private fun initFeaturesMenu(view: View) {
        featuresMenu = PopupMenu(sourceActivity, view, Gravity.END)
        //获取菜单填充器
        val inflater = featuresMenu?.menuInflater
        //填充菜单
        inflater?.inflate(R.menu.menu_subscribe_source, featuresMenu?.menu)
        featuresMenu?.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_select_all -> {
                    mAdapter?.setCheckedAll(true)
                }
                R.id.action_reverse_selected -> {
                    mAdapter?.reverseChecked()
                }
                R.id.action_reverse_enable_selected -> {
                    val bookSources =
                        mAdapter?.checkedBookSources
                    reverseSources(bookSources)
                }
                R.id.action_delete_selected -> {
                    val bookSources =
                        mAdapter?.checkedBookSources
                    deleteSources(bookSources, false)
                }
            }
            true
        }
    }

    private fun reverseSources(mBookSources: MutableList<BookSource>?) {
        if (mBookSources != null) {
            for (source in mBookSources) {
                source.enable = !source.enable
            }
            DbManager.getDaoSession().bookSourceDao.insertOrReplaceInTx(mBookSources)
            mAdapter?.notifyDataSetChanged()
        }
    }

    private fun deleteSources(mBookSources: MutableList<BookSource>?, isDisabled: Boolean) {
        if (mBookSources == null || mBookSources.size == 0) {
            ToastUtils.showWarring("当前没有选择任何书源，无法删除！")
            return
        }
        val title: String
        val msg: String
        val successTip: String
        if (isDisabled) {
            title = "删除禁用书源"
            msg = "确定要删除所有禁用书源吗？"
            successTip = "禁用书源删除成功"
        } else {
            title = "删除选中书源"
            msg = "确定要删除所有选中书源吗？"
            successTip = "选中书源删除成功"
        }
        DialogCreator.createCommonDialog(
            sourceActivity, title,
            msg, true,
            { _: DialogInterface?, _: Int ->
                val sources: MutableList<BookSource> =
                    ArrayList()
                if (isDisabled) {
                    for (source in mBookSources) {
                        if (!source.enable) {
                            sources.add(source)
                        }
                    }
                } else {
                    sources.addAll(mBookSources)
                }
                BookSourceManager.removeBookSources(sources)
                mBookSources.removeAll(sources)
                mAdapter?.removeItems(sources)
                ToastUtils.showSuccess(successTip)
            }, null
        )
    }

    fun startSearch(newText: String?) {
        mAdapter?.filter?.filter(newText)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == APPCONST.REQUEST_SUBSCRIBE) {
                getSources()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}