package xyz.fycz.myreader.util.utils

import android.content.Context
import android.util.Log
import dalvik.system.DexClassLoader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import xyz.fycz.dynamic.AppParam
import xyz.fycz.dynamic.IAppLoader
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.common.URLCONST.DEFAULT_PLUGIN_CONFIG_URL
import xyz.fycz.myreader.entity.PluginConfig
import xyz.fycz.myreader.model.third3.Coroutine
import xyz.fycz.myreader.model.third3.http.getProxyClient
import xyz.fycz.myreader.model.third3.http.newCallResponse
import xyz.fycz.myreader.model.third3.http.newCallResponseBody
import xyz.fycz.myreader.model.third3.http.text
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.ToastUtils
import java.io.File
import java.util.*


/**
 * @author fengyue
 * @date 2022/3/29 12:36
 */
object PluginUtils {

    val TAG = PluginUtils.javaClass.simpleName

    fun init() {
        val pluginConfigUrl =
            SharedPreUtils.getInstance().getString("pluginConfigUrl", DEFAULT_PLUGIN_CONFIG_URL)
        var config: PluginConfig? = null
        Coroutine.async {
            val oldConfig = GSON.fromJsonObject<PluginConfig>(
                SharedPreUtils.getInstance().getString("pluginConfig")
            ) ?: PluginConfig("dynamic.dex", 100)
            launch { loadAppLoader(App.getmContext(), config) }
            val configJson = getProxyClient().newCallResponseBody {
                url(pluginConfigUrl)
            }.text()
            config = GSON.fromJsonObject<PluginConfig>(configJson)
            if (config != null) {
                if (config!!.versionCode > oldConfig.versionCode) {
                    downloadPlugin(config!!)
                    SharedPreUtils.getInstance().putString("pluginConfig", configJson)
                }
            } else {
                config = oldConfig
            }
            if (!App.isDebug() && config!!.md5.lowercase(Locale.getDefault())
                != getPluginMD5(config!!)?.lowercase(Locale.getDefault())
            ) {
                downloadPlugin(config!!)
            }
        }.onSuccess {
            loadAppLoader(App.getmContext(), config)
        }
    }

    private suspend fun downloadPlugin(config: PluginConfig) {
        val res = getProxyClient().newCallResponseBody {
            url(config.url)
        }
        FileUtils.getFile(APPCONST.PLUGIN_DIR_PATH + config.name)
            .writeBytes(res.byteStream().readBytes())
    }

    private fun getPluginMD5(config: PluginConfig): String? {
        return MD5Utils.getFileMD5s(FileUtils.getFile(APPCONST.PLUGIN_DIR_PATH + config.name), 32)
    }

    private fun loadAppLoader(context: Context, config: PluginConfig?) {
        config?.let {
            val pluginPath = APPCONST.PLUGIN_DIR_PATH + it.name
            val desFile = File(pluginPath)
            if (desFile.exists()) {
                val dexClassLoader = DexClassLoader(
                    pluginPath,
                    FileUtils.getCachePath(),
                    null,
                    context.classLoader
                )
                try {
                    val libClazz = dexClassLoader.loadClass("xyz.fycz.dynamic.AppLoadImpl")
                    val appLoader = libClazz.newInstance() as IAppLoader?
                    appLoader?.run {
                        val appParam = AppParam()
                        appParam.classLoader = context.classLoader
                        appParam.packageName = context.packageName
                        appParam.appInfo = context.applicationInfo
                        onLoad(appParam)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Log.d(TAG, pluginPath + "文件不存在")
            }
        }
    }
}