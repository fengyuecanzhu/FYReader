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

package xyz.fycz.myreader.greendao.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;

import xyz.fycz.myreader.greendao.entity.ReplaceRuleBean;
import xyz.fycz.myreader.greendao.gen.*;

import org.greenrobot.greendao.database.Database;


public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {

    private Context mContext;


    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
        mContext = context;
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        //noinspection unchecked
        MigrationHelper.migrate(db,
                new MigrationHelper.ReCreateAllTableListener() {
                    @Override
                    public void onCreateAllTables(Database db, boolean ifNotExists) {
                        DaoMaster.createAllTables(db, ifNotExists);
                    }

                    @Override
                    public void onDropAllTables(Database db, boolean ifExists) {
                        DaoMaster.dropAllTables(db, ifExists);
                    }
                },
                BookDao.class, ChapterDao.class, SearchHistoryDao.class,
                BookMarkDao.class, BookGroupDao.class, ReplaceRuleBeanDao.class,
                BookSourceDao.class, CookieBeanDao.class, ReadRecordDao.class,
                SearchWordDao.class, SubscribeFileDao.class
        );
    }


}
