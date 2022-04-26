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

package xyz.fycz.myreader.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.kongzue.dialogx.dialogs.BottomMenu
import io.reactivex.disposables.Disposable
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.BaseActivity
import xyz.fycz.myreader.base.adapter.BaseListAdapter
import xyz.fycz.myreader.base.adapter.IViewHolder
import xyz.fycz.myreader.base.observer.MyObserver
import xyz.fycz.myreader.base.observer.MySingleObserver
import xyz.fycz.myreader.common.URLCONST
import xyz.fycz.myreader.databinding.ActivitySourceSubscribeBinding
import xyz.fycz.myreader.entity.lanzou.LanZouFile
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.entity.SubscribeFile
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager
import xyz.fycz.myreader.ui.adapter.holder.SourceFileHolder
import xyz.fycz.myreader.ui.dialog.DialogCreator
import xyz.fycz.myreader.ui.dialog.LoadingDialog
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.util.utils.AdUtils
import xyz.fycz.myreader.util.utils.AdUtils.FlowAd
import xyz.fycz.myreader.util.utils.RxUtils
import xyz.fycz.myreader.webapi.LanZouApi

/**
 * @author fengyue
 * @date 2022/3/3 9:56
 */
class SourceSubscribeActivity : BaseActivity<ActivitySourceSubscribeBinding>() {
    private lateinit var fileAdapter: BaseListAdapter<SubscribeFile>
    private var page = 1
    private var subscribeDis: Disposable? = null

    override fun bindView() {
        binding = ActivitySourceSubscribeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setUpToolbar(toolbar: Toolbar?) {
        super.setUpToolbar(toolbar)
        setStatusBarColor(R.color.colorPrimary, true)
        supportActionBar?.title = "书源订阅"
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        fileAdapter = object : BaseListAdapter<SubscribeFile>() {
            override fun createViewHolder(viewType: Int): IViewHolder<SubscribeFile> {
                return SourceFileHolder()
            }
        }
        binding.rvFiles.layoutManager = LinearLayoutManager(this)
        binding.rvFiles.adapter = fileAdapter
        loadFiles()
    }

    override fun initWidget() {
        super.initWidget()
        binding.srlFiles.setOnLoadMoreListener { loadFiles() }
        binding.srlFiles.setOnRefreshListener {
            page = 1
            loadFiles()
        }
        binding.loading.setOnReloadingListener {
            page = 1
            loadFiles()
        }
    }

    private fun loadFiles() {
        LanZouApi.getFoldFiles(URLCONST.SUB_SOURCE_URL, page, "fm9a")
            .onSuccess {
                if (it != null) {
                    if (page == 1) {
                        if (it.isEmpty()) {
                            binding.loading.showEmpty()
                        } else {
                            binding.loading.showFinish()
                            fileAdapter.refreshItems(lanZouFile2SubscribeFile(it))
                            if (it.size < 50) {
                                binding.srlFiles.finishRefreshWithNoMoreData()
                            } else {
                                binding.srlFiles.finishRefresh()
                            }
                        }
                    } else {
                        fileAdapter.addItems(lanZouFile2SubscribeFile(it))
                        if (it.size < 50) {
                            binding.srlFiles.finishLoadMoreWithNoMoreData()
                        } else {
                            binding.srlFiles.finishLoadMore()
                        }
                    }
                    page++
                } else {
                    binding.loading.showError()
                }
            }.onError {
                ToastUtils.showError("" + it.localizedMessage)
            }
    }

    override fun initClick() {
        super.initClick()
        fileAdapter.setOnItemClickListener { _, pos ->
            val file = fileAdapter.getItem(pos)
            val menu = mutableListOf<CharSequence>()
            val subscribed = DbManager.getDaoSession().subscribeFileDao.load(file.id)
            if (subscribed != null) {
                menu.add("更新订阅")
                menu.add("取消订阅")
            } else {
                menu.add("订阅该书源")
            }
            val checkSubscribeUpdate =
                SharedPreUtils.getInstance().getBoolean("checkSubscribeUpdate", true)
            if (checkSubscribeUpdate) {
                menu.add("自动检查订阅更新：已开启")
            } else {
                menu.add("自动检查订阅更新：已关闭")
            }
            BottomMenu.show(file.name, menu)
                .setOnMenuItemClickListener { _, text, _ ->
                    when (text) {
                        "更新订阅", "订阅该书源" -> preSubscribe(file, pos)
                        "取消订阅" -> {
                            DbManager.getDaoSession().subscribeFileDao.deleteByKey(file.id)
                            fileAdapter.notifyItemChanged(pos)
                            DialogCreator.createCommonDialog(
                                this, "取消订阅成功",
                                "是否同时删除此订阅获取的书源？", false, { _, _ ->
                                    BookSourceManager.removeSourceBySubscribe(file)
                                    ToastUtils.showSuccess("书源删除成功")
                                    setResult(Activity.RESULT_OK)
                                }, null
                            )
                        }
                        "自动检查订阅更新：已开启" -> {
                            SharedPreUtils.getInstance().putBoolean("checkSubscribeUpdate", false)
                            ToastUtils.showSuccess("自动检查订阅更新已关闭")
                        }
                        "自动检查订阅更新：已关闭" -> {
                            SharedPreUtils.getInstance().putBoolean("checkSubscribeUpdate", true)
                            ToastUtils.showSuccess("自动检查订阅更新已开启")
                        }
                    }
                    false
                }.setCancelButton(R.string.cancel)
        }
    }

    private fun preSubscribe(file: SubscribeFile, pos: Int) {
        AdUtils.checkHasAd().subscribe(object : MySingleObserver<Boolean?>() {
            override fun onSuccess(aBoolean: Boolean) {
                if (aBoolean && AdUtils.getAdConfig().isSubSource) {
                    DialogCreator.createCommonDialog(
                        this@SourceSubscribeActivity, "订阅书源",
                        "确定要订阅该书源吗？\n点击确定观看一段视频后即可完成订阅", true, { _, _ ->
                            AdUtils.showRewardVideoAd(this@SourceSubscribeActivity) {
                                ToastUtils.showSuccess("视频观看完成，正在为您订阅书源")
                                subscribe(file, pos)
                            }
                        }, null
                    )
                } else {
                    DialogCreator.createCommonDialog(
                        this@SourceSubscribeActivity, "订阅书源",
                        "确定要订阅该书源吗？\n点击确定即可完成订阅", true, { _, _ ->
                            subscribe(file, pos)
                        }, null
                    )
                }
            }
        })
    }

    private fun subscribe(file: SubscribeFile, pos: Int) {
        val dialog = LoadingDialog(this, "正在订阅") {
            subscribeDis?.dispose()
        }
        dialog.show()
        val oldSources = BookSourceManager.getSourceBySubscribe(file)
        BookSourceManager.removeBookSources(oldSources)
        BookSourceManager.importSource(file.url, file.id)
            .compose { RxUtils.toSimpleSingle(it) }
            .subscribe(object : MyObserver<List<BookSource>>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                    subscribeDis = d
                }

                override fun onNext(sources: List<BookSource>) {
                    val size: Int = sources.size
                    if (sources.isNotEmpty()) {
                        DbManager.getDaoSession().subscribeFileDao.insertOrReplace(file)
                        fileAdapter.notifyItemChanged(pos)
                        ToastUtils.showSuccess(String.format("书源订阅成功，成功获取到%s个书源", size))
                        setResult(Activity.RESULT_OK)
                    } else {
                        ToastUtils.showError("订阅失败，请联系作者反馈\nsources.size==0")
                        BookSourceManager.addBookSource(oldSources)
                    }
                    dialog.dismiss()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    e.printStackTrace()
                    BookSourceManager.addBookSource(oldSources)
                    ToastUtils.showError("订阅失败，请联系作者反馈\n" + e.localizedMessage)
                    dialog.dismiss()
                }
            })
    }

    private fun lanZouFile2SubscribeFile(lanZouFile: List<LanZouFile>): MutableList<SubscribeFile> {
        val fileMap = LinkedHashMap<String, SubscribeFile>()
        lanZouFile.forEach {
            val param = it.name_all.removeSuffix(".txt").split("#")
            if (fileMap.containsKey(param[0])) {
                if (fileMap[param[0]]!!.date < param[2]) {
                    fileMap[param[0]] =
                        SubscribeFile(
                            param[0],
                            param[1].replace("nv", "女"),
                            URLCONST.LAN_ZOU_URL + "/${it.id}",
                            param[2],
                            it.size
                        )
                }
            } else {
                fileMap[param[0]] =
                    SubscribeFile(
                        param[0],
                        param[1].replace("nv", "女"),
                        URLCONST.LAN_ZOU_URL + "/${it.id}",
                        param[2],
                        it.size
                    )
            }
        }
        return fileMap.values.toMutableList()
    }
}