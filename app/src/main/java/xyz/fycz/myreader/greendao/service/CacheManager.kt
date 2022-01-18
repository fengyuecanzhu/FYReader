package xyz.fycz.myreader.greendao.service

import android.database.Cursor
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.entity.Cache
import xyz.fycz.myreader.model.third3.analyzeRule.QueryTTF
import xyz.fycz.myreader.util.utils.ACache
import java.lang.Exception


@Suppress("unused")
object CacheManager {

    private val queryTTFMap = hashMapOf<String, Pair<Long, QueryTTF>>()

    /**
     * saveTime 单位为秒
     */
    @JvmOverloads
    fun put(key: String, value: Any, saveTime: Int = 0) {
        val deadline =
            if (saveTime == 0) 0 else System.currentTimeMillis() + saveTime * 1000
        when (value) {
            is QueryTTF -> queryTTFMap[key] = Pair(deadline, value)
            is ByteArray -> ACache.get(App.getmContext()).put(key, value, saveTime)
            else -> {
                val cache = Cache(key, value.toString(), deadline)
                DbManager.getDaoSession().cacheDao.insertOrReplace(cache)
            }
        }
    }

    fun get(key: String): String? {
        var str: String? = null
        try {
            val sql = "select VALUE from CACHE where key = ? and (DEAD_LINE = 0 or DEAD_LINE > ?)"
            val cursor: Cursor = DbManager.getDaoSession().database.rawQuery(
                sql,
                arrayOf(key, "" + System.currentTimeMillis())
            ) ?: return null
            if (cursor.moveToNext()) {
                str = cursor.getColumnName(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return str
    }

    fun getInt(key: String): Int? {
        return get(key)?.toIntOrNull()
    }

    fun getLong(key: String): Long? {
        return get(key)?.toLongOrNull()
    }

    fun getDouble(key: String): Double? {
        return get(key)?.toDoubleOrNull()
    }

    fun getFloat(key: String): Float? {
        return get(key)?.toFloatOrNull()
    }

    fun getByteArray(key: String): ByteArray? {
        return ACache.get(App.getmContext()).getAsBinary(key)
    }

    fun getQueryTTF(key: String): QueryTTF? {
        val cache = queryTTFMap[key] ?: return null
        if (cache.first == 0L || cache.first > System.currentTimeMillis()) {
            return cache.second
        }
        return null
    }

    fun delete(key: String) {
        DbManager.getDaoSession().cacheDao.deleteByKey(key)
        ACache.get(App.getmContext()).remove(key)
    }
}