package xyz.fycz.myreader.greendao.service;

import android.database.Cursor;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.gen.BookDao;
import xyz.fycz.myreader.util.*;
import xyz.fycz.myreader.greendao.GreenDaoManager;
import xyz.fycz.myreader.greendao.entity.Book;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BookService extends BaseService {

    private ChapterService mChapterService;
    private BookMarkService mBookMarkService;
    private static volatile BookService sInstance;
    public BookService(){
        mChapterService = ChapterService.getInstance();
        mBookMarkService = BookMarkService.getInstance();
    }

    public static BookService getInstance() {
        if (sInstance == null){
            synchronized (BookService.class){
                if (sInstance == null){
                    sInstance = new BookService();
                }
            }
        }
        return sInstance;
    }

    private List<Book> findBooks(String sql, String[] selectionArgs) {
        ArrayList<Book> books = new ArrayList<>();
        try {
            Cursor cursor = selectBySql(sql, selectionArgs);
            while (cursor.moveToNext()) {
                Book book = new Book();
                book.setId(cursor.getString(0));
                book.setName(cursor.getString(1));
                book.setChapterUrl(cursor.getString(2));
                book.setImgUrl(cursor.getString(3));
                book.setDesc(cursor.getString(4));
                book.setAuthor(cursor.getString(5));
                book.setType(cursor.getString(6));
                book.setUpdateDate(cursor.getString(7));
                book.setNewestChapterId(cursor.getString(8));
                book.setNewestChapterTitle(cursor.getString(9));
                book.setNewestChapterUrl(cursor.getString(10));
                book.setHistoryChapterId(cursor.getString(11));
                book.setHisttoryChapterNum(cursor.getInt(12));
                book.setSortCode(cursor.getInt(13));
                book.setNoReadNum(cursor.getInt(14));
                book.setChapterTotalNum(cursor.getInt(15));
                book.setLastReadPosition(cursor.getInt(16));
                book.setSource(cursor.getString(17));
                books.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }

    /**
     * 通过ID查书
     * @param id
     * @return
     */
    public Book getBookById(String id) {
        BookDao bookDao = GreenDaoManager.getInstance().getSession().getBookDao();
        return bookDao.load(id);
    }

    /**
     * 获取所有的书
     * @return
     */
    public List<Book> getAllBooks(){
        String sql = "select * from book order by sort_code";
        return findBooks(sql, null);
    }

    /**
     * 新增书
     * @param book
     */
    public void addBook(Book book){
//        book.setSortCode(countBookTotalNum() + 1);
        book.setSortCode(0);
        book.setId(StringHelper.getStringRandom(25));
        addEntity(book);
    }

    /**
     * 批量添加书籍
     * @param books
     */
    public void addBooks(List<Book> books){
        for (Book book : books){
            addBook(book);
        }
    }

    /**
     * 查找书（作者、书名）
     * @param author
     * @param bookName
     * @return
     */
    public Book findBookByAuthorAndName(String bookName, String author){
        Book book = null;
        try {
            Cursor cursor = selectBySql("select id from book where author = ? and name = ?",new String[]{author,bookName});
            if (cursor.moveToNext()){
                String id = cursor.getString(0);
                book = getBookById(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return book;
    }

    /**
     * 通过路径查书籍（本地书籍）
     * @param path
     * @return
     */
    public Book findBookByPath(String path){
        Book book = null;
        try {
            Cursor cursor = selectBySql("select id from book where CHAPTER_URL = ?",new String[]{path});
            if (cursor.moveToNext()){
                String id = cursor.getString(0);
                book = getBookById(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return book;
    }

    /**
     * 删除书
     * @param id
     */
    public void deleteBookById(String id){
        BookDao bookDao = GreenDaoManager.getInstance().getSession().getBookDao();
        bookDao.deleteByKey(id);
        mChapterService.deleteBookALLChapterById(id);
        mBookMarkService.deleteBookALLBookMarkById(id);
    }

    /**
     * 删除书
     * @param book
     */
    public void deleteBook(Book book){
       deleteEntity(book);
       mChapterService.deleteBookALLChapterById(book.getId());
       mBookMarkService.deleteBookALLBookMarkById(book.getId());
    }

    /**
     * 删除所有书
     */
    public void deleteAllBooks(){
        for(Book book : getAllBooks()){
            deleteBook(book);
        }
    }

    /**
     * 查询书籍总数
     * @return
     */
    public int countBookTotalNum(){
        int num = 0;
        try {
            Cursor cursor = selectBySql("select count(*) n from book ",null);
            if (cursor.moveToNext()){
                num = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    /**
     * 更新书
     * @param books
     */
    public void updateBooks(List<Book> books){
        BookDao bookDao = GreenDaoManager.getInstance().getSession().getBookDao();
        bookDao.updateInTx(books);
    }

    /**
     * 更新单本书
     * @param book
     */
    public void updateBook(Book book){
        deleteBook(book);
        book.setId(StringHelper.getStringRandom(25));
        addEntity(book);
    }

    /**
     * 删除旧书添加新书
     * @param OldBook
     * @param newBook
     */
    public void updateBook(Book OldBook, Book newBook){
        deleteBook(OldBook);
        newBook.setId(StringHelper.getStringRandom(25));
        addEntity(newBook);
    }

    /**
     * 判断书籍是否存在
     * @param book
     * @return
     */
    public boolean isBookCollected(Book book){
        return findBookByAuthorAndName(book.getName(), book.getAuthor()) != null;
    }
    /**
     * 保存全部章节名称和url
     * @param book
     * @param chapters
     *//*
    public void saveAllChapters(Book book, ArrayList<Chapter> chapters){
        String filePath = APPCONST.BOOK_CACHE_PATH + book.getId() +
                File.separator + "chapters.fy";
        StringBuilder s = new StringBuilder();
        for (Chapter chapter : chapters){
            s.append(chapter.getTitle());
            s.append("=");
            s.append(chapter.getUrl());
            s.append(",\n");
        }
        s.deleteCharAt(s.lastIndexOf(","));
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(s.toString());
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    *//**
     * 读取全部章节对象
     * @param book
     * @return
     *//*
    public Map<String, String> readAllChapters(Book book){
        if (!isChaptersObjExist(book)) return null;
        String filePath = APPCONST.BOOK_CACHE_PATH + book.getId() +
                File.separator + "chapters.fy";
        Map<String, String> chapterMap = new HashMap<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String tem = "";
            StringBuilder s = new StringBuilder();
            while ((tem = br.readLine()) != null){
                s.append(tem).append("\n");
            }
            String[] chapters = s.toString().split(",");
            for (String chapter : chapters){
                String[] chapterInfo = chapter.split("=");
                chapterMap.put(chapterInfo[0], chapterInfo[1]);
            }
            return chapterMap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    *//**
     * 判断全部章节对象是否存在
     * @param book
     * @return
     *//*
    public boolean isChaptersObjExist(Book book){
        return new File(APPCONST.BOOK_CACHE_PATH + book.getId() +
                File.separator + "chapters.fy").exists();
    }
*/
}
