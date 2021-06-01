package xyz.fycz.myreader.model.storage

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.selector
import xyz.fycz.myreader.R
import xyz.fycz.myreader.base.observer.MySingleObserver
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.ToastUtils
import xyz.fycz.myreader.util.ZipUtils
import xyz.fycz.myreader.util.utils.FileUtils
import xyz.fycz.myreader.util.webdav.WebDav
import xyz.fycz.myreader.util.webdav.http.HttpAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

object WebDavHelp {
    private val zipFilePath = FileUtils.getCachePath() + "/backup" + ".zip"
    private val unzipFilesPath by lazy {
        FileUtils.getCachePath()
    }

    private fun getWebDavUrl(): String {
        var url = SharedPreUtils.getInstance().getString("webdavUrl", APPCONST.DEFAULT_WEB_DAV_URL)
        if (url.isNullOrEmpty()) {
            url = APPCONST.DEFAULT_WEB_DAV_URL
        }
        if (!url.endsWith("/")) url += "/"
        return url
    }

    private fun initWebDav(): Boolean {
        val account = SharedPreUtils.getInstance().getString("webdavAccount", "")
        val password = SharedPreUtils.getInstance().getString("webdavPassword", "")
        if (!account.isNullOrBlank() && !password.isNullOrBlank()) {
            HttpAuth.auth = HttpAuth.Auth(account, password)
            return true
        }
        return false
    }

    fun getWebDavFileNames(): ArrayList<String> {
        val url = getWebDavUrl()
        val names = arrayListOf<String>()
        try {
            if (initWebDav()) {
                var files = WebDav(url + "FYReader/").listFiles()
                files = files.reversed()
                val max = SharedPreUtils.getInstance().getInt("restoreNum", 30)
                for (index: Int in 0 until min(max, files.size)) {
                    files[index].displayName?.let {
                        names.add(it)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return names
    }

    fun showRestoreDialog(context: Context, names: ArrayList<String>, callBack: Restore.CallBack?): Boolean {
        return if (names.isNotEmpty()) {
            /*context.selector(title = "选择恢复文件", items = names) { _, index ->
                if (index in 0 until 30.coerceAtLeast(names.size)) {
                    restoreWebDav(names[index], callBack)
                }
            }*/
            BottomMenu.build().setTitle("选择恢复文件")
                .setMenuStringList(names)
                .setOnMenuItemClickListener { dialog, text, which ->
                    if (which in 0 until 30.coerceAtLeast(names.size)) {
                        restoreWebDav(names[which], callBack)
                    }
                    false
                }
                .setCancelButton(R.string.cancel)
                .show()
            true
        } else {
            false
        }
    }

    private fun restoreWebDav(name: String, callBack: Restore.CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            getWebDavUrl().let {
                val file = WebDav(it + "FYReader/" + name)
                file.downloadTo(zipFilePath, true)
                @Suppress("BlockingMethodInNonBlockingContext")
                ZipUtils.unzipFile(zipFilePath, unzipFilesPath)
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        Restore.restore(unzipFilesPath, callBack)
                    }
                })
    }

    fun backUpWebDav(path: String) {
        try {
            if (initWebDav()) {
                val paths = arrayListOf(*Backup.backupFileNames)
                for (i in 0 until paths.size) {
                    paths[i] = path + File.separator + paths[i]
                }
                FileUtils.deleteFile(zipFilePath)
                if (ZipUtils.zipFiles(paths, zipFilePath)) {
                    WebDav(getWebDavUrl() + "FYReader").makeAsDir()
                    val putUrl = getWebDavUrl() + "FYReader/backup" +
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(Date(System.currentTimeMillis())) + ".zip"
                    WebDav(putUrl).upload(zipFilePath)
                }
            }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                ToastUtils.showError("WebDav\n${e.localizedMessage}")
            }
        }
    }
}