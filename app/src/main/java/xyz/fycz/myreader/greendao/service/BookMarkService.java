package xyz.fycz.myreader.greendao.service;

import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.BookMark;
import xyz.fycz.myreader.greendao.gen.BookMarkDao;
import xyz.fycz.myreader.util.help.StringHelper;

import java.util.ArrayList;
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

    /**
     * 通过ID查书签
     * @param id
     * @return
     */
    public BookMark getBookById(String id) {
        BookMarkDao bookMarkDao = DbManager.getInstance().getSession().getBookMarkDao();
        return bookMarkDao.load(id);
    }

    /**
     * 根据内容查找历史记录
     * @param title
     * @return
     */
    public BookMark findBookMarkByTitle(String title){
        return DbManager.getInstance().getSession().getBookMarkDao()
                .queryBuilder()
                .where(BookMarkDao.Properties.Title.eq(title))
                .unique();
    }
    /**
     * 获取书的所有书签
     *
     * @return
     */
    public List<BookMark> findBookAllBookMarkByBookId(String bookId) {
        if (bookId == null) {
            return new ArrayList<>();
        }
        return DbManager.getInstance().getSession().getBookMarkDao()
                .queryBuilder()
                .where(BookMarkDao.Properties.BookId.eq(bookId))
                .orderAsc(BookMarkDao.Properties.Number)
                .list();
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
        DbManager.getInstance().getSession().getBookMarkDao()
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
        BookMarkDao bookMarkDao = DbManager.getInstance().getSession().getBookMarkDao();
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
        /*int num = 0;
        try {
            Cursor cursor = selectBySql("select count(*) n from book where book_id = " + bookId,null);
            if (cursor.moveToNext()){
                num = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return (int) DbManager.getInstance().getSession().getBookMarkDao()
                .queryBuilder()
                .where(BookMarkDao.Properties.BookId.eq(bookId))
                .count();
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
