package xyz.fycz.myreader.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.kongzue.dialogx.dialogs.BottomMenu
import io.reactivex.disposables.Disposable
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.SysManager
import xyz.fycz.myreader.base.BaseFragment
import xyz.fycz.myreader.base.adapter2.onClick
import xyz.fycz.myreader.base.observer.MySingleObserver
import xyz.fycz.myreader.databinding.FragmentProxySettingBinding
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager
import xyz.fycz.myreader.ui.dialog.DialogCreator.OnMultiDialogListener
import xyz.fycz.myreader.ui.dialog.MultiChoiceDialog
import xyz.fycz.myreader.ui.dialog.MyAlertDialog
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.ToastUtils

/**
 * @author fengyue
 * @date 2022/3/24 10:42
 */
class ProxyFragment : BaseFragment() {

    private lateinit var binding: FragmentProxySettingBinding
    private var proxyType: Int = 0
    private var enableProxy: Boolean = false
    private lateinit var proxyHost: String
    private lateinit var proxyPort: String
    private lateinit var proxyUsername: String
    private lateinit var proxyPassword: String
    private var mNoProxySourcesDia: AlertDialog? = null
    private val proxyTypeArr = arrayOf("http", "socks5")
    private val spu: SharedPreUtils = SharedPreUtils.getInstance()

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentProxySettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initData(savedInstanceState: Bundle?) {
        enableProxy = spu.getBoolean("enableProxy")
        proxyType = spu.getInt("proxyType")
        proxyHost = spu.getString("proxyHost")
        proxyPort = spu.getString("proxyHost")
        proxyUsername = spu.getString("proxyUsername")
        proxyPassword = spu.getString("proxyPassword")
    }

    override fun initWidget(savedInstanceState: Bundle?) {
        binding.scEnableProxy.isChecked = enableProxy
        if (enableProxy) binding.llContent.visibility = View.VISIBLE
        binding.tvProxyType.text = proxyTypeArr[proxyType]
        binding.tvProxyHost.text = proxyHost.ifEmpty { "请输入代理服务器地址" }
        binding.tvProxyPort.text = proxyHost.ifEmpty { "请输入代理服务器端口" }
        binding.tvProxyUsername.text = proxyHost.ifEmpty { "请输入代理认证用户名" }
        binding.tvProxyPassword.text = proxyHost.ifEmpty { "请输入代理认证密码" }
    }

    override fun initClick() {
        binding.rlEnableProxy.onClick {
            enableProxy = !enableProxy
            binding.scEnableProxy.isChecked = enableProxy
            spu.putBoolean("enableProxy", enableProxy)
            binding.llContent.visibility = if (enableProxy) View.VISIBLE else View.GONE
        }

        binding.llProxyType.onClick {
            BottomMenu.show(getString(R.string.proxy_type), proxyTypeArr)
                .setSelection(proxyType)
                .setOnMenuItemClickListener { _: BottomMenu?, _: CharSequence?, which: Int ->
                    proxyType = which
                    SharedPreUtils.getInstance().putInt("proxyType", which)
                    binding.tvProxyType.text = proxyTypeArr[which]
                    false
                }.setCancelButton(R.string.cancel)
        }
        binding.llProxyHost.onClick {
            var tem = ""
            MyAlertDialog.createInputDia(
                context, getString(R.string.proxy_host),
                "", proxyHost, true, 100,
                { text: String -> tem = text }
            ) { dialog: DialogInterface, _: Int ->
                proxyHost = tem
                binding.tvProxyHost.text = proxyHost
                spu.putString("proxyHost", proxyHost)
                dialog.dismiss()
            }
        }
        binding.llProxyPort.onClick {
            val view =
                LayoutInflater.from(context).inflate(R.layout.dialog_number_picker, null)
            val threadPick = view.findViewById<NumberPicker>(R.id.number_picker)
            threadPick.maxValue = 99999
            threadPick.minValue = 10
            threadPick.value = proxyPort.toInt()
            threadPick.setOnScrollListener { _: NumberPicker?, _: Int -> }
            MyAlertDialog.build(context)
                .setTitle(R.string.proxy_port)
                .setView(view)
                .setPositiveButton(R.string.confirm) { _: DialogInterface?, _: Int ->
                    proxyPort = threadPick.value.toString()
                    spu.putString("proxyPort", proxyPort)
                    binding.tvProxyPort.text = proxyPort
                }.setNegativeButton(R.string.cancel, null)
                .show()
        }
        binding.llProxyUsername.onClick {
            var tem = ""
            MyAlertDialog.createInputDia(
                context, getString(R.string.proxy_username),
                "", proxyUsername, true, 100,
                { text: String -> tem = text }
            ) { dialog: DialogInterface, _: Int ->
                proxyUsername = tem
                binding.tvProxyUsername.text = proxyUsername
                spu.putString("proxyUsername", proxyUsername)
                dialog.dismiss()
            }
        }
        binding.llProxyPassword.onClick {
            var tem = ""
            MyAlertDialog.createInputDia(
                context, getString(R.string.proxy_password),
                "", proxyPassword, true, 100,
                { text: String -> tem = text }
            ) { dialog: DialogInterface, _: Int ->
                proxyPassword = tem
                binding.tvProxyPassword.text = proxyPassword
                spu.putString("proxyPassword", proxyPassword)
                dialog.dismiss()
            }
        }

        binding.llNoProxySources.onClick {
            if (mNoProxySourcesDia != null) {
                mNoProxySourcesDia?.show()
                return@onClick
            }
            val sources = BookSourceManager.getAllBookSourceByOrderNum()
            val mSourcesName = arrayOfNulls<CharSequence>(sources.size)
            val isNoProxy = BooleanArray(sources.size)
            var dSourceCount = 0
            for ((i, source) in sources.withIndex()) {
                mSourcesName[i] = source.sourceName
                val noProxy = source.getNoProxy()
                if (noProxy) dSourceCount++
                isNoProxy[i] = noProxy
            }
            mNoProxySourcesDia = MultiChoiceDialog().create(context, getString(R.string.no_proxy_sources_tip),
                mSourcesName, isNoProxy, dSourceCount,
                { _: DialogInterface?, _: Int ->
                    BookSourceManager.saveDatas(sources)
                        .subscribe(object : MySingleObserver<Boolean?>() {
                            override fun onSubscribe(d: Disposable) {
                                addDisposable(d)
                            }

                            override fun onSuccess(aBoolean: Boolean) {
                                if (aBoolean) {
                                    ToastUtils.showSuccess("保存成功")
                                }
                            }
                        })
                }, null, object : OnMultiDialogListener {
                    override fun onItemClick(
                        dialog: DialogInterface,
                        which: Int,
                        isChecked: Boolean
                    ) {
                        sources[which].setNoProxy(isChecked)
                    }

                    override fun onSelectAll(isSelectAll: Boolean) {
                        for (source in sources) {
                            source.setNoProxy(isSelectAll)
                        }
                    }
                })
        }
    }
}