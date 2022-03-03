package xyz.fycz.myreader.ui.activity

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.BaseActivity
import xyz.fycz.myreader.base.adapter.BaseListAdapter
import xyz.fycz.myreader.base.adapter.IViewHolder
import xyz.fycz.myreader.common.URLCONST
import xyz.fycz.myreader.databinding.ActivitySourceSubscribeBinding
import xyz.fycz.myreader.entity.lanzou.LanZouFile
import xyz.fycz.myreader.greendao.entity.SubscribeFile
import xyz.fycz.myreader.ui.adapter.holder.SourceFileHolder
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.webapi.LanZouApi

/**
 * @author fengyue
 * @date 2022/3/3 9:56
 */
class SourceSubscribeActivity : BaseActivity() {
    private lateinit var binding: ActivitySourceSubscribeBinding
    private lateinit var fileAdapter: BaseListAdapter<SubscribeFile>
    private var page = 1

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
    }

    private fun lanZouFile2SubscribeFile(lanZouFile: List<LanZouFile>): ArrayList<SubscribeFile> {
        val files = ArrayList<SubscribeFile>()
        lanZouFile.forEach {
            val param = it.name_all.removeSuffix(".txt").split("#")
            files.add(
                SubscribeFile(
                    param[0],
                    param[1],
                    URLCONST.LAN_ZOU_URL + "/${it.id}",
                    param[2],
                    it.size
                )
            )
        }
        return files
    }
}