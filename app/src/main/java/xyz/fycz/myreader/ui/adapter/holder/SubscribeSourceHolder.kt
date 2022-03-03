package xyz.fycz.myreader.ui.adapter.holder

import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.adapter.ViewHolderImpl
import xyz.fycz.myreader.base.adapter2.onClick
import xyz.fycz.myreader.base.observer.MyObserver
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager
import xyz.fycz.myreader.ui.adapter.SubscribeSourceAdapter
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.util.help.StringHelper
import xyz.fycz.myreader.util.utils.RxUtils
import java.util.*

/**
 * @author fengyue
 * @date 2022/3/3 12:11
 */
class SubscribeSourceHolder(
    private val mCheckMap: HashMap<BookSource, Boolean>,
    private val onDelListener: SubscribeSourceAdapter.OnDelListener
) : ViewHolderImpl<BookSource>() {

    private var cbSource: CheckBox? = null
    private var tvEnable: TextView? = null
    private var tvDisable: TextView? = null
    private var tvDelete: TextView? = null

    override fun getItemLayoutId(): Int {
        return R.layout.item_subscribe_source
    }

    override fun initView() {
        cbSource = findById(R.id.cb_source)
        tvEnable = findById(R.id.tv_enable)
        tvDisable = findById(R.id.tv_disable)
        tvDelete = findById(R.id.tv_delete)
    }

    override fun onBind(holder: RecyclerView.ViewHolder, data: BookSource, pos: Int) {
        banOrUse(data)
        cbSource?.isChecked = mCheckMap[data] == true
        tvEnable?.onClick {
            data.enable = true
            banOrUse(data)
            DbManager.getDaoSession().bookSourceDao.insertOrReplace(data)
        }
        tvDisable?.onClick {
            data.enable = false
            banOrUse(data)
            DbManager.getDaoSession().bookSourceDao.insertOrReplace(data)
        }
        tvDelete?.onClick {
            Observable.create { e: ObservableEmitter<Boolean?> ->
                BookSourceManager.removeBookSource(data)
                e.onNext(true)
            }.compose { RxUtils.toSimpleSingle(it) }
                .subscribe(object : MyObserver<Boolean?>() {
                    override fun onNext(aBoolean: Boolean) {
                        onDelListener.onDel(pos, data)
                    }

                    override fun onError(e: Throwable) {
                        ToastUtils.showError("删除失败")
                    }
                })
        }
    }

    private fun banOrUse(data: BookSource) {
        if (data.enable) {
            cbSource?.setTextColor(context.resources.getColor(R.color.textPrimary))
            if (!StringHelper.isEmpty(data.sourceGroup)) {
                cbSource?.text = String.format("%s [%s]", data.sourceName, data.sourceGroup)
            } else {
                cbSource?.text = data.sourceName
            }
        } else {
            cbSource?.setTextColor(context.resources.getColor(R.color.textSecondary))
            if (!StringHelper.isEmpty(data.sourceGroup)) {
                cbSource?.text = String.format("(禁用中)%s [%s]", data.sourceName, data.sourceGroup)
            } else {
                cbSource?.text = String.format("(禁用中)%s", data.sourceName)
            }
        }
    }
}