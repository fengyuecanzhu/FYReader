package xyz.fycz.myreader.ui.activity

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.appcompat.widget.Toolbar
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.base.BaseActivity
import xyz.fycz.myreader.base.BitIntentDataManager
import xyz.fycz.myreader.base.MyTextWatcher
import xyz.fycz.myreader.base.observer.MySingleObserver
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.databinding.ActivityAuthEmailBinding
import xyz.fycz.myreader.model.user.Result
import xyz.fycz.myreader.model.user.User
import xyz.fycz.myreader.model.user.UserService.bindEmail
import xyz.fycz.myreader.model.user.UserService.resetPwd
import xyz.fycz.myreader.model.user.UserService.sendEmail
import xyz.fycz.myreader.ui.dialog.DialogCreator
import xyz.fycz.myreader.ui.dialog.LoadingDialog
import xyz.fycz.myreader.util.CyptoUtils
import xyz.fycz.myreader.util.ToastUtils
import java.util.*

/**
 * @author fengyue
 * @date 2021/12/9 15:20
 */
class AuthEmailActivity : BaseActivity<ActivityAuthEmailBinding>(), SingleObserver<Result> {

    private var email = ""
    private var password = ""
    private var emailCode = ""
    private var keyc = ""
    private lateinit var dialog: LoadingDialog
    private var disp: Disposable? = null
    private lateinit var operator: String
    private var user: User? = null
    private var isBind: Boolean = false
    override fun bindView() {
        binding = ActivityAuthEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initData(savedInstanceState: Bundle?) {
        user = BitIntentDataManager.getInstance().getData(intent) as User?
        isBind = user != null
        operator = if (isBind) "绑定邮箱" else "重置密码"
        dialog = LoadingDialog(this, "正在请求") {
            disp?.dispose()
        }
    }

    override fun setUpToolbar(toolbar: Toolbar?) {
        super.setUpToolbar(toolbar)
        setStatusBarColor(R.color.colorPrimary, true)
        supportActionBar?.title = operator
    }

    override fun initWidget() {
        binding.tvTitle.text = operator
        binding.btSubmit.text = operator
        binding.etEmail.editText?.addTextChangedListener(object : MyTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                email = s.toString()
                if (!email.matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$".toRegex())) {
                    showTip("邮箱格式错误")
                } else {
                    binding.tvRegisterTip.visibility = View.GONE
                }
                checkNotNone()
            }
        })

        binding.etEmailCode.editText?.addTextChangedListener(object : MyTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                emailCode = s.toString().trim()
                checkNotNone()
            }
        })
        binding.etPassword.editText!!.addTextChangedListener(object : MyTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                password = s.toString()
                if (password.length < 8 || password.length > 16) {
                    showTip("密码必须在8-16位之间")
                } else if (password.matches("^\\d+$".toRegex())) {
                    showTip("密码不能是纯数字")
                } else {
                    binding.tvRegisterTip.visibility = View.GONE
                }
                checkNotNone()
            }
        })
        if (!isBind) {
            binding.etPassword.visibility = View.VISIBLE
        }
    }

    override fun initClick() {
        binding.tvGetEmailCode.setOnClickListener {
            if (!email.matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$".toRegex())) {
                ToastUtils.showWarring("请正确输入邮箱")
                return@setOnClickListener
            }
            dialog.show()
            dialog.setmMessage("正在发送")
            sendEmail(email, if (isBind) "bind" else "auth", keyc)
                .subscribe(object : MySingleObserver<Result?>() {
                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                        disp = d
                    }

                    override fun onSuccess(result: Result) {
                        if (result.code == 106) {
                            ToastUtils.showSuccess("验证码发送成功")
                            keyc = result.result.toString()
                            timeDown(60)
                        } else {
                            ToastUtils.showWarring(result.result.toString())
                        }
                        dialog.dismiss()
                    }

                    override fun onError(e: Throwable) {
                        ToastUtils.showError("验证码发送失败：${e.localizedMessage}")
                        dialog.dismiss()
                    }
                })
        }

        binding.btSubmit.setOnClickListener {
            if (!isBind && (password.matches("^\\d+$".toRegex()) || !password.matches("^.{8,16}$".toRegex()))) {
                DialogCreator.createTipDialog(
                    this, "密码格式错误",
                    "密码必须在8-16位之间\n密码不能是纯数字"
                )
            } else if (!email.matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$".toRegex())) {
                DialogCreator.createTipDialog(
                    this, "邮箱格式错误",
                    "电子邮箱的正确写法为：用户名@邮箱网站.com(.cn等)"
                )
            } else if ("" == keyc) {
                DialogCreator.createTipDialog(this, "请先获取邮箱验证码")
            } else if (emailCode.length < 6) {
                DialogCreator.createTipDialog(this, "请输入6位邮箱验证码")
            } else {
                dialog.show()
                dialog.setmMessage("正在请求")
                if (isBind) {
                    val user = User().apply {
                        email = this@AuthEmailActivity.email
                        userName = this@AuthEmailActivity.user?.userName
                    }
                    bindEmail(user, emailCode, keyc)
                        .subscribe(this)
                } else {
                    val user = User().apply {
                        email = this@AuthEmailActivity.email
                        password = CyptoUtils.encode(APPCONST.KEY, this@AuthEmailActivity.password)
                    }
                    resetPwd(user, emailCode, keyc)
                        .subscribe(this)
                }

            }
        }
    }

    fun showTip(tip: String?) {
        binding.tvRegisterTip.visibility = View.VISIBLE
        binding.tvRegisterTip.text = tip
    }

    private fun timeDown(time: Int) {
        if (time == 0) {
            binding.tvGetEmailCode.text = getString(R.string.re_get_email_code, "")
            binding.tvGetEmailCode.isEnabled = true
        } else {
            binding.tvGetEmailCode.isEnabled = false
            val timeStr = "($time)"
            binding.tvGetEmailCode.text = getString(R.string.re_get_email_code, timeStr)
            App.getHandler().postDelayed({ timeDown(time - 1) }, 1000)
        }
    }

    fun checkNotNone() {
        binding.btSubmit.isEnabled = "" != email && "" != emailCode && (isBind || "" != password)
    }

    override fun onDestroy() {
        dialog.dismiss()
        super.onDestroy()
    }

    override fun onSubscribe(d: Disposable) {
        addDisposable(d)
        disp = d
    }

    override fun onSuccess(result: Result) {
        if (result.code < 200) {
            ToastUtils.showSuccess(result.result.toString())
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            ToastUtils.showWarring(result.result.toString())
        }
        dialog.dismiss()
    }

    override fun onError(e: Throwable) {
        ToastUtils.showError("失败：${e.localizedMessage}")
        dialog.dismiss()
    }
}