package xyz.fycz.myreader.greendao;


import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.greendao.gen.DaoMaster;
import xyz.fycz.myreader.greendao.gen.DaoSession;
import xyz.fycz.myreader.greendao.util.MySQLiteOpenHelper;



public class DbManager {
    private static DbManager instance;
    private static DaoMaster daoMaster;
    private DaoSession mDaoSession;

    private static MySQLiteOpenHelper mySQLiteOpenHelper;

    public static DbManager getInstance() {
        if (instance == null) {
            instance = new DbManager();
        }
        return instance;
    }

    public DbManager(){
        mySQLiteOpenHelper = new MySQLiteOpenHelper(App.getmContext(), "read" , null);
        daoMaster = new DaoMaster(mySQLiteOpenHelper.getWritableDatabase());
        mDaoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return getInstance().mDaoSession;
    }

    public DaoSession getSession(){
       return mDaoSession;
    }

}
