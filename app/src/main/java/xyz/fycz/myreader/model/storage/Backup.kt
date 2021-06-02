package xyz.fycz.myreader.model.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.application.SysManager
import xyz.fycz.myreader.base.observer.MySingleObserver
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.service.BookService
import xyz.fycz.myreader.greendao.service.SearchHistoryService
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.utils.DocumentUtil
import xyz.fycz.myreader.util.utils.FileUtils
import xyz.fycz.myreader.util.utils.GSON
import java.io.File
import java.util.concurrent.TimeUnit


object Backup {

    val backupPath = App.getApplication().filesDir.absolutePath + File.separator + "backup"

    val defaultPath by lazy {
        APPCONST.BACKUP_FILE_DIR
    }

    val backupFileNames by lazy {
        arrayOf(
                "myBooks.json",
                "mySearchHistory.json",
                "myBookMark.json",
                "myBookGroup.json",
                "setting.json",
                "readStyles.json",
                "replaceRule.json",
                "bookSource.json",
                "readRecord.json",
                "config.xml"
        )
    }

    fun autoBack() {
        val lastBackup = SharedPreUtils.getInstance().getLong("lastBackup", 0)
        if (System.currentTimeMillis() - lastBackup < TimeUnit.DAYS.toMillis(1)) {
            return
        }
        val path = SharedPreUtils.getInstance().getString("backupPath", defaultPath)
        if (path == null) {
            backup(App.getmContext(), defaultPath, null, true)
        } else {
            backup(App.getmContext(), path, null, true)
        }
    }

    fun backup(context: Context, path: String, callBack: CallBack?, isAuto: Boolean = false) {
        SharedPreUtils.getInstance().putLong("lastBackup", System.currentTimeMillis())
        Single.create(SingleOnSubscribe<Boolean> { e ->
            BookService.getInstance().allBooks.let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileUtils.getFile(backupPath + File.separator + "myBooks.json").writeText(json)
                }
            }
            SearchHistoryService.getInstance().findAllSearchHistory().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileUtils.getFile(backupPath + File.separator + "mySearchHistory.json")
                            .writeText(json)
                }
            }
            DbManager.getInstance().session.bookMarkDao.queryBuilder().list().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileUtils.getFile(backupPath + File.separator + "myBookMark.json")
                            .writeText(json)
                }
            }
            DbManager.getInstance().session.bookGroupDao.queryBuilder().list().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileUtils.getFile(backupPath + File.separator + "myBookGroup.json")
                            .writeText(json)
                }
            }
            DbManager.getInstance().session.replaceRuleBeanDao.queryBuilder().list().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileUtils.getFile(backupPath + File.separator + "replaceRule.json")
                            .writeText(json)
                }
            }
            DbManager.getInstance().session.bookSourceDao.queryBuilder().list().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileUtils.getFile(backupPath + File.separator + "bookSource.json")
                            .writeText(json)
                }
            }
            DbManager.getInstance().session.readRecordDao.queryBuilder().list().let {
                if (it.isNotEmpty()) {
                    val json = GSON.toJson(it)
                    FileUtils.getFile(backupPath + File.separator + "readRecord.json")
                        .writeText(json)
                }
            }
            try {
                val setting = SysManager.getNewSetting()
                val readStyles = setting.readStyles
                val readStylesJson = GSON.toJson(readStyles)
                setting.readStyles = null
                val settingJson = GSON.toJson(setting)
                FileUtils.getFile(backupPath + File.separator + "setting.json")
                        .writeText(settingJson)
                FileUtils.getFile(backupPath + File.separator + "readStyles.json")
                        .writeText(readStylesJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Preferences.getSharedPreferences(context, backupPath, "config")?.let { sp ->
                val edit = sp.edit()
                SharedPreUtils.getInstance().all.map {
                    when (val value = it.value) {
                        is Int -> edit.putInt(it.key, value)
                        is Boolean -> edit.putBoolean(it.key, value)
                        is Long -> edit.putLong(it.key, value)
                        is Float -> edit.putFloat(it.key, value)
                        is String -> edit.putString(it.key, value)
                        else -> Unit
                    }
                }
                edit.commit()
            }
            WebDavHelp.backUpWebDav(backupPath)
            if (path.isContentPath()) {
                copyBackup(context, Uri.parse(path), isAuto)
            } else {
                copyBackup(path, isAuto)
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        callBack?.backupSuccess()
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.backupError(e.localizedMessage ?: "ERROR")
                    }
                })
    }

    @Throws(Exception::class)
    private fun copyBackup(context: Context, uri: Uri, isAuto: Boolean) {
        synchronized(this) {
            DocumentFile.fromTreeUri(context, uri)?.let { treeDoc ->
                for (fileName in backupFileNames) {
                    val file = File(backupPath + File.separator + fileName)
                    if (file.exists()) {
                        if (isAuto) {
                            treeDoc.findFile("auto")?.findFile(fileName)?.delete()
                            var autoDoc = treeDoc.findFile("auto")
                            if (autoDoc == null) {
                                autoDoc = treeDoc.createDirectory("auto")
                            }
                            autoDoc?.createFile("", fileName)?.let {
                                DocumentUtil.writeBytes(context, file.readBytes(), it)
                            }
                        } else {
                            treeDoc.findFile(fileName)?.delete()
                            treeDoc.createFile("", fileName)?.let {
                                DocumentUtil.writeBytes(context, file.readBytes(), it)
                            }
                        }
                    }
                }
            }
        }
    }

    @Throws(java.lang.Exception::class)
    private fun copyBackup(path: String, isAuto: Boolean) {
        synchronized(this) {
            for (fileName in backupFileNames) {
                if (isAuto) {
                    val file = File(backupPath + File.separator + fileName)
                    if (file.exists()) {
                        file.copyTo(FileUtils.getFile(path + File.separator + "auto" + File.separator + fileName), true)
                    }
                } else {
                    val file = File(backupPath + File.separator + fileName)
                    if (file.exists()) {
                        file.copyTo(FileUtils.getFile(path + File.separator + fileName), true)
                    }
                }
            }
        }
    }

    interface CallBack {
        fun backupSuccess()
        fun backupError(msg: String)
    }
}