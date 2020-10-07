package xyz.fycz.myreader.model.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xyz.fycz.myreader.application.MyApplication
import xyz.fycz.myreader.application.SysManager
import xyz.fycz.myreader.base.observer.MySingleObserver
import xyz.fycz.myreader.entity.Setting
import xyz.fycz.myreader.greendao.GreenDaoManager
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.entity.BookGroup
import xyz.fycz.myreader.greendao.entity.BookMark
import xyz.fycz.myreader.greendao.entity.SearchHistory
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.utils.*
import java.io.File

object Restore {

    fun restore(context: Context, uri: Uri, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            DocumentFile.fromTreeUri(context, uri)?.listFiles()?.forEach { doc ->
                for (fileName in Backup.backupFileNames) {
                    if (doc.name == fileName) {
                        DocumentUtil.readBytes(context, doc.uri)?.let {
                            FileUtils.getFile(Backup.backupPath + File.separator + fileName)
                                    .writeBytes(it)
                        }
                    }
                }
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        restore(Backup.backupPath, callBack)
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: "ERROR")
                    }
                })
    }

    fun restore(path: String, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            try {
                val file = FileUtils.getFile(path + File.separator + "myBooks.json")
                val json = file.readText()
                GSON.fromJsonArray<Book>(json)?.forEach { bookshelf ->
                    /*if (bookshelf.noteUrl != null) {
                        DbHelper.getDaoSession().bookShelfBeanDao.insertOrReplace(bookshelf)
                    }
                    if (bookshelf.bookInfoBean.noteUrl != null) {
                        DbHelper.getDaoSession().bookInfoBeanDao.insertOrReplace(bookshelf.bookInfoBean)
                    }*/
                    GreenDaoManager.getInstance().session.bookDao.insertOrReplace(bookshelf)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileUtils.getFile(path + File.separator + "mySearchHistory.json")
                val json = file.readText()
                GSON.fromJsonArray<SearchHistory>(json)?.let {
                    GreenDaoManager.getInstance().session.searchHistoryDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileUtils.getFile(path + File.separator + "myBookMark.json")
                val json = file.readText()
                GSON.fromJsonArray<BookMark>(json)?.let {
                    GreenDaoManager.getInstance().session.bookMarkDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileUtils.getFile(path + File.separator + "myBookGroup.json")
                val json = file.readText()
                GSON.fromJsonArray<BookGroup>(json)?.let {
                    GreenDaoManager.getInstance().session.bookGroupDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileUtils.getFile(path + File.separator + "setting.json")
                val json = file.readText()
                SysManager.saveSetting(GSON.fromJsonObject<Setting>(json))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Preferences.getSharedPreferences(MyApplication.getmContext(), path, "config")?.all?.map {
                val edit = SharedPreUtils.getInstance()
                when (val value = it.value) {
                    is Int -> edit.putInt(it.key, value)
                    is Boolean -> edit.putBoolean(it.key, value)
                    is Long -> edit.putLong(it.key, value)
                    is Float -> edit.putFloat(it.key, value)
                    is String -> edit.putString(it.key, value)
                    else -> Unit
                }
                edit.putInt("versionCode", MyApplication.getVersionCode())
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        MyApplication.getApplication().initNightTheme()
                        callBack?.restoreSuccess()
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: "ERROR")
                    }
                })
    }


    interface CallBack {
        fun restoreSuccess()
        fun restoreError(msg: String)
    }

}