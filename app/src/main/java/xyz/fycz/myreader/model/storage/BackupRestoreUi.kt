package xyz.fycz.myreader.model.storage

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import xyz.fycz.myreader.base.observer.MySingleObserver
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.model.storage.WebDavHelp.getWebDavFileNames
import xyz.fycz.myreader.model.storage.WebDavHelp.showRestoreDialog
import xyz.fycz.myreader.util.SharedPreUtils
import xyz.fycz.myreader.util.ToastUtils
import java.util.*

object BackupRestoreUi : Backup.CallBack, Restore.CallBack {

    private const val backupSelectRequestCode = 22
    private const val restoreSelectRequestCode = 33

    private fun getBackupPath(): String? {
        return SharedPreUtils.getInstance().getString("backupPath", APPCONST.BACKUP_FILE_DIR)
    }

    private fun setBackupPath(path: String?) {
        if (path.isNullOrEmpty()) {
            SharedPreUtils.getInstance().remove("backupPath")
        } else {
            SharedPreUtils.getInstance().putString("backupPath", path)
        }
    }

    override fun backupSuccess() {
        ToastUtils.showSuccess("备份成功")
    }

    override fun backupError(msg: String) {
        ToastUtils.showError(msg)
    }

    override fun restoreSuccess() {
        ToastUtils.showSuccess("恢复成功")
    }

    override fun restoreError(msg: String) {
        ToastUtils.showError(msg)
    }

    fun backup(activity: Activity) {
        val backupPath = getBackupPath()
        if (backupPath.isNullOrEmpty()) {
//            selectBackupFolder(activity)
            ToastUtils.showError("backupPath.isNullOrEmpty")
        } else {
            if (backupPath.isContentPath()) {
                val uri = Uri.parse(backupPath)
                val doc = DocumentFile.fromTreeUri(activity, uri)
                if (doc?.canWrite() == true) {
                    Backup.backup(activity, backupPath, this)
                } else {
//                    selectBackupFolder(activity)
                    ToastUtils.showError("doc?.canWrite() != true")
                }
            } else {
//                backupUsePermission(activity)
                ToastUtils.showError("backupPath.isNotContentPath")
            }
        }
    }

    /*private fun backupUsePermission(activity: Activity, path: String = Backup.defaultPath) {
        PermissionsCompat.Builder(activity)
                .addPermissions(*Permissions.Group.STORAGE)
                .rationale(R.string.get_storage_per)
                .onGranted {
                    setBackupPath(path)
                    Backup.backup(activity, path, this)
                }
                .request()
    }

    fun selectBackupFolder(activity: Activity) {
        activity.alert {
            titleResource = R.string.select_folder
            items(activity.resources.getStringArray(R.array.select_folder).toList()) { _, index ->
                when (index) {
                    0 -> {
                        setBackupPath(Backup.defaultPath)
                        backupUsePermission(activity)
                    }
                    1 -> {
                        try {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            activity.startActivityForResult(intent, backupSelectRequestCode)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            activity.toast(e.localizedMessage ?: "ERROR")
                        }
                    }
                    2 -> {
                        PermissionsCompat.Builder(activity)
                                .addPermissions(*Permissions.Group.STORAGE)
                                .rationale(R.string.get_storage_per)
                                .onGranted {
                                    selectBackupFolderApp(activity, false)
                                }
                                .request()
                    }
                }
            }
        }.show()
    }

    private fun selectBackupFolderApp(activity: Activity, isRestore: Boolean) {
        val picker = FilePicker(activity, FilePicker.DIRECTORY)
        picker.setBackgroundColor(ContextCompat.getColor(activity, R.color.background))
        picker.setTopBackgroundColor(ContextCompat.getColor(activity, R.color.background))
        picker.setItemHeight(30)
        picker.setOnFilePickListener { currentPath: String ->
            setBackupPath(currentPath)
            if (isRestore) {
                Restore.restore(currentPath, this)
            } else {
                Backup.backup(activity, currentPath, this)
            }
        }
        picker.show()
    }*/

    fun restore(activity: Activity) {
        Single.create { emitter: SingleEmitter<ArrayList<String>?> ->
            emitter.onSuccess(getWebDavFileNames())
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<ArrayList<String>?>() {
                    override fun onSuccess(strings: ArrayList<String>) {
                        if (!showRestoreDialog(activity, strings, this@BackupRestoreUi)) {
                            val path = getBackupPath()
                            if (TextUtils.isEmpty(path)) {
                                //selectRestoreFolder(activity)
                                ToastUtils.showError("TextUtils.isEmpty(path)")
                            } else {
                                if (path.isContentPath()) {
                                    val uri = Uri.parse(path)
                                    val doc = DocumentFile.fromTreeUri(activity, uri)
                                    if (doc?.canWrite() == true) {
                                        Restore.restore(activity, Uri.parse(path), this@BackupRestoreUi)
                                    } else {
//                                        selectRestoreFolder(activity)
                                        ToastUtils.showError("doc?.canWrite() != true")
                                    }
                                } else {
//                                    restoreUsePermission(activity)
                                    ToastUtils.showError("path.isNotContentPath")
                                }
                            }
                        }
                    }
                })
    }

    /*private fun restoreUsePermission(activity: Activity, path: String = Backup.defaultPath) {
        PermissionsCompat.Builder(activity)
                .addPermissions(*Permissions.Group.STORAGE)
                .rationale(R.string.get_storage_per)
                .onGranted {
                    setBackupPath(path)
                    Restore.restore(path, this)
                }
                .request()
    }

    private fun selectRestoreFolder(activity: Activity) {
        activity.alert {
            titleResource = R.string.select_folder
            items(activity.resources.getStringArray(R.array.select_folder).toList()) { _, index ->
                when (index) {
                    0 -> restoreUsePermission(activity)
                    1 -> {
                        try {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            activity.startActivityForResult(intent, restoreSelectRequestCode)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            activity.toast(e.localizedMessage ?: "ERROR")
                        }
                    }
                    2 -> {
                        PermissionsCompat.Builder(activity)
                                .addPermissions(*Permissions.Group.STORAGE)
                                .rationale(R.string.get_storage_per)
                                .onGranted {
                                    selectBackupFolderApp(activity, true)
                                }
                                .request()
                    }
                }
            }
        }.show()
    }*/

    /*fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            backupSelectRequestCode -> if (resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    MApplication.getInstance().contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    setBackupPath(uri.toString())
                    Backup.backup(MApplication.getInstance(), uri.toString(), this)
                }
            }
            restoreSelectRequestCode -> if (resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    MApplication.getInstance().contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    setBackupPath(uri.toString())
                    Restore.restore(MApplication.getInstance(), uri, this)
                }
            }
        }
    }*/

}

fun String?.isContentPath(): Boolean = this?.startsWith("content://") == true