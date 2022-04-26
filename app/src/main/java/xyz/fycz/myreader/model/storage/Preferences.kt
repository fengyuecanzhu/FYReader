/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.model.storage

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import java.io.File

object Preferences {

    /**
     * 用反射生成 SharedPreferences
     * @param context
     * @param dir
     * @param fileName 文件名,不需要 '.xml' 后缀
     * @return
     */
    fun getSharedPreferences(
            context: Context,
            dir: String,
            fileName: String
    ): SharedPreferences? {
        try {
            // 获取 ContextWrapper对象中的mBase变量。该变量保存了 ContextImpl 对象
            val fieldMBase = ContextWrapper::class.java.getDeclaredField("mBase")
            fieldMBase.isAccessible = true
            // 获取 mBase变量
            val objMBase = fieldMBase.get(context)
            // 获取 ContextImpl.mPreferencesDir变量，该变量保存了数据文件的保存路径
            val fieldMPreferencesDir = objMBase.javaClass.getDeclaredField("mPreferencesDir")
            fieldMPreferencesDir.isAccessible = true
            // 创建自定义路径
            // String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android";
            val file = File(dir)
            // 修改mPreferencesDir变量的值
            fieldMPreferencesDir.set(objMBase, file)
            // 返回修改路径以后的 SharedPreferences :%FILE_PATH%/%fileName%.xml
            return context.getSharedPreferences(fileName, Activity.MODE_PRIVATE)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }
}