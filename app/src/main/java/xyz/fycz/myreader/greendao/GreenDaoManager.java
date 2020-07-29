package xyz.fycz.myreader.greendao;


import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.greendao.gen.DaoMaster;
import xyz.fycz.myreader.greendao.gen.DaoSession;
import xyz.fycz.myreader.greendao.util.MySQLiteOpenHelper;



public class GreenDaoManager {
    private static GreenDaoManager instance;
    private static DaoMaster daoMaster;
    private static MySQLiteOpenHelper mySQLiteOpenHelper;

    public static GreenDaoManager getInstance() {
        if (instance == null) {
            instance = new GreenDaoManager();
        }
        return instance;
    }

    public GreenDaoManager(){
        mySQLiteOpenHelper = new MySQLiteOpenHelper(MyApplication.getmContext(), "read" , null);
        daoMaster = new DaoMaster(mySQLiteOpenHelper.getWritableDatabase());
    }



    public DaoSession getSession(){
       return daoMaster.newSession();
    }

}
