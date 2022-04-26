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

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.widget.Toolbar
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.base.BaseActivity
import xyz.fycz.myreader.base.adapter2.onClick
import xyz.fycz.myreader.databinding.ActivityRemoveAdBinding
import xyz.fycz.myreader.util.help.RelativeDateHelp
import xyz.fycz.myreader.util.utils.AdUtils

/**
 * @author fengyue
 * @date 2022/3/3 17:04
 */
class RemoveAdActivity : BaseActivity<ActivityRemoveAdBinding>() {
    private var rewardLastTime = 0L

    override fun bindView() {
        binding = ActivityRemoveAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setUpToolbar(toolbar: Toolbar?) {
        super.setUpToolbar(toolbar)
        setStatusBarColor(R.color.colorPrimary, true)
        supportActionBar?.title = getString(R.string.remove_ad)
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        initRewardTime()
    }

    fun initRewardTime() {
        kotlin.runCatching {
            val rewardTime = AdUtils.getSp().getString("rewardLastTime")
            if (!TextUtils.isEmpty(rewardTime)) {
                rewardLastTime = AdUtils.SDF.parse(rewardTime).time
            }
        }
    }

    override fun initWidget() {
        super.initWidget()
        countRewardTime()
        binding.tvTip.text = getString(
            R.string.remove_ad_tip,
            AdUtils.getAdConfig().removeAdTime,
            AdUtils.getAdConfig().totalRemove,
            AdUtils.getAdConfig().maxRemove
        )
    }

    private fun countRewardTime() {
        val dur = rewardLastTime - System.currentTimeMillis()
        if (dur > 0) {
            binding.tvCurRemoveAdTime.text = getString(
                R.string.cur_remove_ad_time,
                RelativeDateHelp.formatDuring(dur)
            )
            App.getHandler().postDelayed({ countRewardTime() }, 1000)
        } else {
            binding.tvCurRemoveAdTime.text = getString(R.string.cur_remove_ad_time, "无记录")
        }
    }

    override fun initClick() {
        super.initClick()
        binding.rlRewardVideo.onClick {
            AdUtils.showRewardVideoAd(this) {
                AdUtils.removeAdReward()
                initRewardTime()
                countRewardTime()
            }
        }
    }
}