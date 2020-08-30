package xyz.fycz.myreader.greendao.service;

import android.database.Cursor;
import xyz.fycz.myreader.greendao.GreenDaoManager;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.BookMark;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.SearchHistory;
import xyz.fycz.myreader.greendao.gen.BookDao;
import xyz.fycz.myreader.greendao.gen.BookMarkDao;
import xyz.fycz.myreader.greendao.gen.ChapterDao;
import xyz.fycz.myreader.greendao.gen.SearchHistoryDao;
import xyz.fycz.myreader.util.DateHelper;
import xyz.fycz.myreader.util.StringHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BookMarkService extends BaseService {
    private static volatile BookMarkService sInstance;

    public static BookMarkService getInstance() {
        if (sInstance == null){
            synchronized (BookMarkService.class){
                if (sInstance == null){
                    sInstance = new BookMarkService();
                }
            }
        }
        return sInstance;
    }
    private ArrayList<BookMark> findBookMarks(String sql, String[] selectionArgs) {
        ArrayList<BookMark> bookMarks = new ArrayList<>();
        try {
            Cursor cursor = selectBySql(sql, selectionArgs);
            if (cursor == null) return bookMarks;
            while (cursor.moveToNext()) {
                BookMark bookMark = new BookMark();
                bookMark.setId(cursor.getString(0));
                bookMark.setBookId(cursor.getString(1));
                bookMark.setNumber(cursor.getInt(2));
                bookMark.setTitle(cursor.getString(3));
                bookMark.setBookMarkChapterNum(cursor.getInt(4));
                bookMark.setBookMarkReadPosition(cursor.getInt(5));
                bookMarks.add(bookMark);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return bookMarks;
        }
        return bookMarks;
    }

    /**
     * 通过ID查书签
     * @param id
     * @return
     */
    public BookMark getBookById(String id) {
        BookMarkDao bookMarkDao = GreenDaoManager.getInstance().getSession().getBookMarkDao();
        return bookMarkDao.load(id);
    }

    /**
     * 根据内容查找历史记录
     * @param title
     * @return
     */
    public BookMark findBookMarkByTitle(String title){
        BookMark bookMark = null;
        String sql = "select * from book_mark where title = ?";
        Cursor cursor = selectBySql(sql,new String[]{title});
        if (cursor == null) return null;
        if (cursor.moveToNext()){
            bookMark = new BookMark();
            bookMark.setId(cursor.getString(0));
            bookMark.setBookId(cursor.getString(1));
            bookMark.setNumber(cursor.getInt(2));
            bookMark.setTitle(cursor.getString(3));
            bookMark.setBookMarkChapterNum(cursor.getInt(4));
            bookMark.setBookMarkReadPosition(cursor.getInt(5));
        }
        return bookMark;
    }
    /**
     * 获取书的所有书签
     *
     * @return
     */
    public List<BookMark> findBookAllBookMarkByBookId(String bookId) {

        if (StringHelper.isEmpty(bookId)) return new ArrayList<>();

        String sql = "select * from book_mark where book_id = ? order by number";

        return findBookMarks(sql, new String[]{bookId});
    }


    /**
     * 添加书签
     * @param bookMark
     */
    public void addBookMark(BookMark bookMark) {
        bookMark.setId(StringHelper.getStringRandom(25));
        bookMark.setNumber(countBookMarkTotalNumByBookId(bookMark.getBookId()) + 1);
        addEntity(bookMark);
    }

    /**
     * 删除书的所有书签
     *
     * @param bookId
     */
    public void deleteBookALLBookMarkById(String bookId) {
        GreenDaoManager.getInstance().getSession().getBookMarkDao()
                .queryBuilder()
                .where(BookMarkDao.Properties.BookId.eq(bookId))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }
    /**
     * 批量删除书签
     *
     * @param bookMarks
     */
    public void deleteBookALLBookMarks(ArrayList<BookMark> bookMarks){
        for (BookMark bookMark : bookMarks){
            deleteBookMark(bookMark);
        }
    }

    /**
     * 通过ID删除书签
     * @param id
     */
    public void deleteBookMarkById(String id){
        BookMarkDao bookMarkDao = GreenDaoManager.getInstance().getSession().getBookMarkDao();
        bookMarkDao.deleteByKey(id);
    }

    /**
     * 删除书签
     * @param bookMark
     */
    public void deleteBookMark(BookMark bookMark){
        deleteEntity(bookMark);
    }


    /**
     * 通过id查询书籍书签总数
     * @return
     */
    public int countBookMarkTotalNumByBookId(String bookId){
        int num = 0;
        try {
            Cursor cursor = selectBySql("select count(*) n from book where book_id = " + bookId,null);
            if (cursor.moveToNext()){
                num = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    /**
     * 添加或更新书签
     * @param newBookMark
     */
    public void addOrUpdateBookMark(BookMark newBookMark){
        BookMark oldBookMark = findBookMarkByTitle(newBookMark.getTitle());
        if (oldBookMark == null){
            addBookMark(newBookMark);
        }else {
            oldBookMark.setBookId(newBookMark.getBookId());
            oldBookMark.setBookMarkReadPosition(newBookMark.getBookMarkReadPosition());
            oldBookMark.setNumber(countBookMarkTotalNumByBookId(oldBookMark.getBookId() + 1));
            updateEntity(oldBookMark);
        }
    }
}
