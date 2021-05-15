package xyz.fycz.myreader.greendao.service;

import android.database.Cursor;

import xyz.fycz.myreader.greendao.gen.DaoSession;
import xyz.fycz.myreader.greendao.DbManager;


public class BaseService {

    public void addEntity(Object entity){
        DaoSession daoSession  = DbManager.getInstance().getSession();
        daoSession.insert(entity);
    }

    public void updateEntity(Object entity){
        DaoSession daoSession  = DbManager.getInstance().getSession();
        daoSession.update(entity);
    }

    public void deleteEntity(Object entity){
        DaoSession daoSession  = DbManager.getInstance().getSession();
        daoSession.delete(entity);
    }

    /**
     * 通过SQL查找
     * @param sql
     * @param selectionArgs
     * @return
     */
    public Cursor selectBySql(String sql, String[] selectionArgs){

        Cursor cursor = null;
        try {
            DaoSession daoSession  = DbManager.getInstance().getSession();
            cursor = daoSession.getDatabase().rawQuery(sql, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return cursor;
    }

    /**
     * 执行SQL进行增删改
     * @param sql
     * @param selectionArgs
     */
    public void  rawQuery(String sql, String[] selectionArgs) {
        DaoSession daoSession  = DbManager.getInstance().getSession();
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, selectionArgs);
    }


}
