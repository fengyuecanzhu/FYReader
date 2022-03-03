package xyz.fycz.myreader.ui.adapter.holder

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.adapter.ViewHolderImpl
import xyz.fycz.myreader.entity.lanzou.LanZouFile
import xyz.fycz.myreader.greendao.entity.SubscribeFile

/**
 * @author fengyue
 * @date 2022/3/3 10:17
 */
class SourceFileHolder : ViewHolderImpl<SubscribeFile>() {

    private lateinit var name: TextView
    private lateinit var sizeTime: TextView

    override fun getItemLayoutId(): Int {
        return R.layout.item_lan_zou_file
    }

    override fun initView() {
        name = findById(R.id.tv_file_name)
        sizeTime = findById(R.id.tv_file_size_time)
    }

    @SuppressLint("SetTextI18n")
    override fun onBind(holder: RecyclerView.ViewHolder?, data: SubscribeFile?, pos: Int) {
        data?.let {
            name.text = data.name
            sizeTime.text = "${data.size}  ${data.date}"
        }
    }
}