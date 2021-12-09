package xyz.fycz.myreader.model.user

import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.SingleSource
import io.reactivex.functions.Function
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.UnzipParameters
import okhttp3.MediaType
import okhttp3.RequestBody
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.common.URLCONST
import xyz.fycz.myreader.entity.StrResponse
import xyz.fycz.myreader.model.storage.Backup
import xyz.fycz.myreader.model.storage.Restore
import xyz.fycz.myreader.model.storage.Restore.restore
import xyz.fycz.myreader.util.*
import xyz.fycz.myreader.util.utils.FileUtils
import xyz.fycz.myreader.util.utils.GSON
import xyz.fycz.myreader.util.utils.OkHttpUtils
import xyz.fycz.myreader.util.utils.RxUtils
import java.io.File
import net.lingala.zip4j.model.enums.EncryptionMethod

import net.lingala.zip4j.model.ZipParameters


/**
 * @author fengyue
 * @date 2021/12/9 10:17
 */
object UserService2 {
    fun login(user: User): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val body = "username=${user.userName}" +
                    "&password=${user.password}" +
                    makeAuth()
            val requestBody = RequestBody.create(mediaType, body)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/login", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun register(user: User, code: String, keyc: String): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val body = "username=${user.userName}" +
                    "&password=${user.password}" +
                    "&email=${user.email}" +
                    "&code=${code}" +
                    "&keyc=${keyc}" +
                    "&key=${CyptoUtils.encode(APPCONST.KEY, APPCONST.publicKey)}" +
                    makeAuth()
            val requestBody = RequestBody.create(mediaType, body)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/reg", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun bindEmail(user: User, code: String, keyc: String): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val body = "username=${user.userName}" +
                    "&email=${user.email}" +
                    "&code=${code}" +
                    "&keyc=${keyc}" +
                    makeAuth()
            val requestBody = RequestBody.create(mediaType, body)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/bindEmail", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun resetPwd(user: User, code: String, keyc: String): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val body = "email=${user.email}" +
                    "&password=${user.password}" +
                    "&code=${code}" +
                    "&keyc=${keyc}" +
                    makeAuth()
            val requestBody = RequestBody.create(mediaType, body)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/resetPwd", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun sendEmail(email: String, type: String, keyc: String = ""): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val body = "email=${email}" +
                    "&type=${type}" +
                    "&keyc=${keyc}" +
                    makeAuth()
            val requestBody = RequestBody.create(mediaType, body)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/sendEmail", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun webBackup(user: User): Single<Result> {
        return Backup.backup(App.getmContext(), APPCONST.FILE_DIR + "webBackup/")
            .flatMap(Function<Boolean, SingleSource<Result>> {
                Single.create {
                    val inputFile = FileUtils.getFile(APPCONST.FILE_DIR + "webBackup")
                    //压缩文件
//                    ZipUtils.zipFile(inputFile, zipFile)
                    val zipParameters = ZipParameters().apply {
                        isEncryptFiles = true
                        encryptionMethod = EncryptionMethod.AES
                    }
                    ZipFile(
                        APPCONST.FILE_DIR + "webBackup.zip",
                        CyptoUtils.decode(APPCONST.KEY, user.password).toCharArray()
                    )
                        .addFolder(inputFile, zipParameters)
                    val zipFile = FileUtils.getFile(APPCONST.FILE_DIR + "webBackup.zip")
                    FileUtils.deleteFile(inputFile.absolutePath)
                    val ret = OkHttpUtils.upload(
                        URLCONST.USER_URL + "/do/bak?" +
                                "username=${user.userName}" +
                                makeAuth(),
                        zipFile.absolutePath,
                        zipFile.name
                    )
                    zipFile.delete()
                    it.onSuccess(GSON.fromJson(ret, Result::class.java))
                }
            }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun webRestore(user: User): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val zipFile = FileUtils.getFile(APPCONST.FILE_DIR + "webBackup.zip")
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val body = "username=${user.userName}" +
                    makeAuth()
            val requestBody = RequestBody.create(mediaType, body)
            val input = OkHttpUtils.getInputStream(
                URLCONST.USER_URL + "/do/ret", requestBody
            )
            val bytes = input.readBytes()
            if (bytes.size < 50) {
                it.onError(Throwable(String(bytes)))
                return@SingleOnSubscribe
            }
            if (FileUtils.writeFile(bytes, zipFile)) {
//                ZipUtils.unzipFile(zipFile.absolutePath, APPCONST.FILE_DIR)
                ZipFile(zipFile, CyptoUtils.decode(APPCONST.KEY, user.password).toCharArray())
                    .extractAll(APPCONST.FILE_DIR)
                zipFile.delete()
                restore(APPCONST.FILE_DIR + "webBackup/", object : Restore.CallBack {
                    override fun restoreSuccess() {
                        FileUtils.deleteFile(APPCONST.FILE_DIR + "webBackup/")
                        it.onSuccess(Result(100, "成功从网络同步到本地"))
                    }

                    override fun restoreError(msg: String) {
                        it.onError(Throwable(msg))
                    }
                })
            }
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun writeConfig(user: User) {
        FileUtils.write(App.getmContext(), "userConfig.fy", GSON.toJson(user))
    }

    fun readConfig(): User? {
        val config = FileUtils.read(App.getmContext(), "userConfig.fy")
        if ("" != config) {
            return GSON.fromJson(config, User::class.java)
        }
        return null
    }

    fun writeUsername(username: String) {
        FileUtils.writeText(username, FileUtils.getFile(APPCONST.QQ_DATA_DIR + "user"))
    }

    fun readUsername(): String {
        return FileUtils.readText(APPCONST.QQ_DATA_DIR + "user")
    }

    /**
     * 判断是否登录
     * @return
     */
    fun isLogin(): Boolean {
        val file = App.getApplication().getFileStreamPath("userConfig.fy")
        return file.exists()
    }

    private fun makeAuth(): String {
        return "&signal=" + AppInfoUtils.getSingInfo(
            App.getmContext(),
            App.getApplication().packageName,
            AppInfoUtils.SHA1
        ) + "&appVersion=" + App.getVersionCode()
    }
}