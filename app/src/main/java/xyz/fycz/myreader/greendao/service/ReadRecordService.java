package xyz.fycz.myreader.greendao.service;

import java.util.List;

import io.reactivex.Single;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.ReadRecord;
import xyz.fycz.myreader.greendao.gen.BookDao;
import xyz.fycz.myreader.greendao.gen.ReadRecordDao;

/**
 * @author fengyue
 * @date 2021/6/1 19:09
 */
public class ReadRecordService extends BaseService {
    private static volatile ReadRecordService sInstance;

    public static ReadRecordService getInstance() {
        if (sInstance == null) {
            synchronized (ReadRecordService.class) {
                if (sInstance == null) {
                    sInstance = new ReadRecordService();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取所有书籍分组
     *
     * @return
     */
    public Single<List<ReadRecord>> getAllRecordsByTime() {
        return Single.create(emitter -> emitter.onSuccess(DbManager.getDaoSession().getReadRecordDao()
                .queryBuilder()
                .orderDesc(ReadRecordDao.Properties.UpdateTime)
                .list()));
    }

    public Single<Boolean> removeAll() {
        return Single.create(emitter -> {
            DbManager.getDaoSession().getReadRecordDao().deleteAll();
            emitter.onSuccess(true);
        });
    }

    public Single<Boolean> removeAllTime(List<ReadRecord> records) {
        return Single.create(emitter -> {
            for (ReadRecord record : records) {
                record.setReadTime(0);
                record.setUpdateTime(0);
            }
            DbManager.getDaoSession().getReadRecordDao().insertOrReplaceInTx(records);
            emitter.onSuccess(true);
        });
    }

    public ReadRecord get(String bookName, String bookAuthor) {
        try {
            return DbManager.getInstance().getSession().getReadRecordDao()
                    .queryBuilder()
                    .where(ReadRecordDao.Properties.BookName.eq(bookName),
                            ReadRecordDao.Properties.BookAuthor.eq(bookAuthor))
                    .unique();
        } catch (Exception e) {
            e.printStackTrace();
            return DbManager.getInstance().getSession().getReadRecordDao()
                    .queryBuilder()
                    .where(ReadRecordDao.Properties.BookName.eq(bookName),
                            ReadRecordDao.Properties.BookAuthor.eq(bookAuthor))
                    .list().get(0);
        }
    }
}
