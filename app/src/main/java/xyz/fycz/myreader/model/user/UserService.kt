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

package xyz.fycz.myreader.model.user

import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.SingleSource
import io.reactivex.functions.Function
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.common.URLCONST
import xyz.fycz.myreader.model.storage.Backup
import xyz.fycz.myreader.model.storage.Restore
import xyz.fycz.myreader.model.storage.Restore.restore
import xyz.fycz.myreader.util.*
import xyz.fycz.myreader.util.utils.FileUtils
import xyz.fycz.myreader.util.utils.GSON
import xyz.fycz.myreader.util.utils.OkHttpUtils
import xyz.fycz.myreader.util.utils.RxUtils
import java.io.File
import java.util.*

/**
 * @author fengyue
 * @date 2021/12/9 10:17
 */
object UserService {

    fun login(user: User): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "username=${user.userName}" +
                    "&password=${user.password}" +
                    makeAuth()
            val requestBody = body.toRequestBody(mediaType)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/login", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun register(user: User, code: String, keyc: String): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "username=${user.userName}" +
                    "&password=${user.password}" +
                    "&email=${user.email}" +
                    "&code=${code}" +
                    "&keyc=${keyc}" +
                    "&key=${CyptoUtils.encode(APPCONST.KEY, APPCONST.publicKey)}" +
                    makeAuth()
            val requestBody = body.toRequestBody(mediaType)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/reg", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun bindEmail(user: User, code: String, keyc: String): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "username=${user.userName}" +
                    "&email=${user.email}" +
                    "&code=${code}" +
                    "&keyc=${keyc}" +
                    makeAuth()
            val requestBody = body.toRequestBody(mediaType)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/bindEmail", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun resetPwd(user: User, code: String, keyc: String): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "email=${user.email}" +
                    "&password=${user.password}" +
                    "&code=${code}" +
                    "&keyc=${keyc}" +
                    makeAuth()
            val requestBody = body.toRequestBody(mediaType)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/resetPwd", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun sendEmail(email: String, type: String, keyc: String = ""): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "email=${email}" +
                    "&type=${type}" +
                    "&keyc=${keyc}" +
                    makeAuth()
            val requestBody = body.toRequestBody(mediaType)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/sendEmail", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun getInfo(user: User): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "username=${user.userName}" +
                    "&password=${user.password}" +
                    makeAuth()
            val requestBody = body.toRequestBody(mediaType)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/getInfo", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun bindId(username: String): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "username=${username}" +
                    "&deviceId=${getUUID()}" +
                    makeAuth()
            val requestBody = body.toRequestBody(mediaType)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/bindId", requestBody, "UTF-8")
            it.onSuccess(GSON.fromJson(ret, Result::class.java))
        }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun bindCammy(username: String, cammy: String): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "username=${username}" +
                    "&cammy=$cammy" +
                    makeAuth()
            val requestBody = body.toRequestBody(mediaType)
            val ret = OkHttpUtils.getHtml(URLCONST.USER_URL + "/do/bindCammy", requestBody, "UTF-8")
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
                    var zipFile = File(APPCONST.FILE_DIR + "webBackup.zip")
                    if (zipFile.exists()) zipFile.delete()
                    ZipFile(
                        APPCONST.FILE_DIR + "webBackup.zip",
                        CyptoUtils.decode(APPCONST.KEY, user.password).toCharArray()
                    ).addFolder(inputFile, zipParameters)
                    zipFile = FileUtils.getFile(APPCONST.FILE_DIR + "webBackup.zip")
                    FileUtils.deleteFile(inputFile.absolutePath)
                    val ret = OkHttpUtils.upload(
                        URLCONST.USER_URL + "/do/bak?" +
                                "username=${user.userName}" +
                                makeAuth(),
                        zipFile.absolutePath,
                        zipFile.name
                    )
                    if (!App.isDebug()) zipFile.delete()
                    it.onSuccess(GSON.fromJson(ret, Result::class.java))
                }
            }).compose { RxUtils.toSimpleSingle(it) }
    }

    fun webRestore(user: User): Single<Result> {
        return Single.create(SingleOnSubscribe<Result> {
            val zipFile = FileUtils.getFile(APPCONST.FILE_DIR + "webBackup.zip")
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "username=${user.userName}" +
                    makeAuth()
            val requestBody = body.toRequestBody(mediaType)
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
        if (!config.isNullOrEmpty()) {
            return try {
                GSON.fromJson(config, User::class.java)
            } catch (e: Exception) {
                null
            }
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
        return readConfig() != null
    }

    fun getUUID(): String {
        val file = FileUtils.getFile(APPCONST.QQ_DATA_DIR + "monId")
        var uuid = file.readText()
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString()
            file.writeText(uuid)
        }
        return uuid
    }

    fun makeAuth(): String {
        return "&signal=" + AppInfoUtils.getSingInfo(
            App.getmContext(),
            App.getApplication().packageName,
            AppInfoUtils.SHA1
        ) + "&appVersion=" + App.getVersionCode() +
                "&deviceId=" + getUUID() + "&isDebug=" + App.isDebug()
    }
}