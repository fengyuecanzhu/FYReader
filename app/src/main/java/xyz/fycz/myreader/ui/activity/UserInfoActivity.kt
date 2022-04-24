/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.startActivityForResult
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.base.BaseActivity
import xyz.fycz.myreader.base.adapter2.onClick
import xyz.fycz.myreader.base.observer.MySingleObserver
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.databinding.ActivityUserInfoBinding
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.model.user.Result
import xyz.fycz.myreader.model.user.User
import xyz.fycz.myreader.model.user.UserService
import xyz.fycz.myreader.ui.dialog.LoadingDialog
import xyz.fycz.myreader.ui.dialog.MyAlertDialog
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.util.utils.*

/**
 * @author fengyue
 * @date 2022/3/4 18:38
 */
class UserInfoActivity : BaseActivity<ActivityUserInfoBinding>() {

    private var user: User? = null
    private lateinit var dialog: LoadingDialog
    private var dis: Disposable? = null

    override fun bindView() {
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setUpToolbar(toolbar: Toolbar?) {
        super.setUpToolbar(toolbar)
        setStatusBarColor(R.color.colorPrimary, true)
        supportActionBar?.title = getString(R.string.userinfo)
    }

    override fun initData(savedInstanceState: Bundle?) {
        dialog = LoadingDialog(this, "正在请求") {
            dis?.dispose()
            if (user == null || user?.email.isNullOrEmpty())
                finish()
        }
        initData()
    }

    private fun initData() {
        dialog.show()
        user = UserService.readConfig()
        if (user == null) {
            ToastUtils.showWarring("用户未登录")
            dialog.dismiss()
            finish()
            return
        }
        UserService.getInfo(user!!).subscribe(object : MySingleObserver<Result>() {
            override fun onSubscribe(d: Disposable) {
                addDisposable(d)
                dis = d
            }

            override fun onSuccess(t: Result) {
                if (t.code < 200) {
                    user = GSON.fromJsonObject<User>(GSON.toJson(t.result))
                    dialog.dismiss()
                    initInfo()
                } else {
                    ToastUtils.showError(t.result.toString())
                    dialog.dismiss()
                    finish()
                }
            }

            override fun onError(e: Throwable) {
                ToastUtils.showError("用户信息获取失败\n" + e.localizedMessage)
                dialog.dismiss()
                finish()
            }
        })
    }

    private fun initInfo() {
        binding.tvUsername.text = user?.userName
        binding.tvEmail.text =
            StringUtils.getStarString2(
                user?.email, 3,
                user?.email!!.length - user?.email!!.indexOf("@")
            )
        if (user?.backupTime.isNullOrEmpty()) {
            binding.tvLastWebBackTime.text = "未同步"
        } else {
            binding.tvLastWebBackTime.text = user?.backupTime
        }
        if (user?.noAdTime.isNullOrEmpty()) {
            binding.tvNoAdTime.text = "无记录"
        } else {
            binding.tvNoAdTime.text = user?.noAdTime
        }
        if (UserService.getUUID() == user?.noAdId) {
            binding.tvNoAdDevice.text = "已绑定此设备"
        } else {
            binding.tvNoAdDevice.text = "点击绑定"
        }
    }

    override fun initWidget() {
        AdUtils.checkHasAd(true, false)
            .subscribe(object : MySingleObserver<Boolean>() {
                override fun onSubscribe(d: Disposable) {
                    addDisposable(d)
                }

                override fun onSuccess(t: Boolean) {
                    if (t) {
                        binding.llNoAdService.visibility = View.VISIBLE
                    } else {
                        binding.llNoAdService.visibility = View.GONE
                    }
                }

                override fun onError(e: Throwable) {
                    binding.llNoAdService.visibility = View.GONE
                }
            })
    }

    override fun initClick() {
        binding.rlResetPwd.onClick {
            val intent = Intent(this, AuthEmailActivity::class.java)
            startActivityForResult(intent, APPCONST.REQUEST_RESET_PWD)
        }

        binding.rlNoAdDevice.onClick {
            if (UserService.getUUID() != user?.noAdId) {
                dialog.show()
                UserService.bindId(user!!.userName)
                    .subscribe(object : MySingleObserver<Result>() {
                        override fun onSubscribe(d: Disposable) {
                            addDisposable(d)
                            dis = d
                        }

                        override fun onSuccess(t: Result) {
                            if (t.code < 200) {
                                user?.noAdId = UserService.getUUID()
                                ToastUtils.showSuccess(t.result.toString())
                                binding.tvNoAdDevice.text = "已绑定此设备"
                            } else {
                                ToastUtils.showError(t.result.toString())
                            }
                            dialog.dismiss()
                        }

                        override fun onError(e: Throwable) {
                            ToastUtils.showError("设备绑定失败\n" + e.localizedMessage)
                            dialog.dismiss()
                        }
                    })
            }
        }

        binding.rlCammyEnter.onClick {
            var cammy = ""
            MyAlertDialog.createInputDia(this, getString(R.string.cammy_enter),
                "请输入兑换码", "", true, 25, {
                    cammy = it
                }, { _, _ ->
                    dialog.show()
                    UserService.bindCammy(user?.userName!!, cammy)
                        .subscribe(object : MySingleObserver<Result>() {
                            override fun onSubscribe(d: Disposable) {
                                addDisposable(d)
                                dis = d
                            }

                            override fun onSuccess(t: Result) {
                                if (t.code < 200) {
                                    ToastUtils.showSuccess(t.result.toString())
                                    dialog.dismiss()
                                    initData()
                                } else {
                                    dialog.dismiss()
                                    ToastUtils.showError(t.result.toString())
                                }
                            }

                            override fun onError(e: Throwable) {
                                ToastUtils.showError("兑换码使用失败\n" + e.localizedMessage)
                                dialog.dismiss()
                            }
                        })
                })
        }

        binding.rlCammyTip.onClick {
            MyAlertDialog.showTipDialogWithLink(
                this,
                getString(R.string.cammy_get_method),
                R.string.cammy_tip
            )
        }

        binding.tvLogout.onClick {
            val file = App.getApplication().getFileStreamPath("userConfig.fy")
            if (file.delete()) {
                ToastUtils.showSuccess("退出成功")
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                ToastUtils.showError("退出失败(Error：file.delete())")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == APPCONST.REQUEST_RESET_PWD){
                binding.tvLogout.performClick()
            }
        }
    }
}