package xyz.fycz.myreader.greendao.service;

import android.database.Cursor;

import android.text.TextUtils;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.gen.BookDao;
import xyz.fycz.myreader.util.*;
import xyz.fycz.myreader.greendao.GreenDaoManager;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BookService extends BaseService {

    private static Pattern chapterNamePattern = Pattern.compile("^(.*?第([\\d零〇一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟０-９\\s]+)[章节篇回集])[、，。　：:.\\s]*");


    private ChapterService mChapterService;
    private BookMarkService mBookMarkService;
    private static volatile BookService sInstance;

    public BookService() {
        mChapterService = ChapterService.getInstance();
        mBookMarkService = BookMarkService.getInstance();
    }

    public static BookService getInstance() {
        if (sInstance == null) {
            synchronized (BookService.class) {
                if (sInstance == null) {
                    sInstance = new BookService();
                }
            }
        }
        return sInstance;
    }


    /**
     * 通过ID查书
     *
     * @param id
     * @return
     */
    public Book getBookById(String id) {
        BookDao bookDao = GreenDaoManager.getInstance().getSession().getBookDao();
        return bookDao.load(id);
    }

    /**
     * 获取所有的书
     *
     * @return
     */
    public List<Book> getAllBooks() {
        return GreenDaoManager.getInstance().getSession().getBookDao()
                .queryBuilder()
                .orderAsc(BookDao.Properties.SortCode)
                .list();
    }

    /**
     * 获取所有的书
     *
     * @return
     */
    public List<Book> getAllBooksNoHide() {
        List<Book> oldBooks = getAllBooks();
        List<Book> newBooks = new ArrayList<>();
        String privateGroupId = SharedPreUtils.getInstance().getString("privateGroupId");
        for (Book book : oldBooks){
            if (StringHelper.isEmpty(book.getGroupId()) || !privateGroupId.equals(book.getGroupId())) newBooks.add(book);
        }
        return newBooks;
    }

    /**
     * 获取特定分组的书
     *
     * @return
     */
    public List<Book> getGroupBooks(String groupId) {
        if (StringHelper.isEmpty(groupId)){
            return getAllBooksNoHide();
        }
        return GreenDaoManager.getInstance().getSession().getBookDao()
                .queryBuilder()
                .where(BookDao.Properties.GroupId.eq(groupId))
                .orderAsc(BookDao.Properties.GroupSort)
                .list();
    }

    /**
     * 新增书
     *
     * @param book
     */
    public void addBook(Book book) {
        book.setSortCode(0);
        book.setGroupSort(0);
        book.setGroupId(SharedPreUtils.getInstance().getString(App.getmContext().getString(R.string.curBookGroupId), ""));
        if (StringHelper.isEmpty(book.getId())) {
            book.setId(StringHelper.getStringRandom(25));
        }
        addEntity(book);
    }

    public void addBookNoGroup(Book book) {
        book.setSortCode(0);
        book.setGroupSort(0);
        if (StringHelper.isEmpty(book.getId())) {
            book.setId(StringHelper.getStringRandom(25));
        }
        addEntity(book);
    }

    /**
     * 批量添加书籍
     *
     * @param books
     */
    public void addBooks(List<Book> books) {
        for (Book book : books) {
            addBook(book);
        }
    }

    /**
     * 查找书（作者、书名）
     *
     * @param author
     * @param bookName
     * @return
     */
    public Book findBookByAuthorAndName(String bookName, String author) {
        try {
            return GreenDaoManager.getInstance().getSession().getBookDao()
                    .queryBuilder()
                    .where(BookDao.Properties.Name.eq(bookName), BookDao.Properties.Author.eq(author))
                    .unique();
        } catch (Exception e) {
            e.printStackTrace();
            return GreenDaoManager.getInstance().getSession().getBookDao()
                    .queryBuilder()
                    .where(BookDao.Properties.Name.eq(bookName), BookDao.Properties.Author.eq(author))
                    .list().get(0);
        }
    }

    /**
     * 通过路径查书籍（本地书籍）
     *
     * @param path
     * @return
     */
    public Book findBookByPath(String path) {
        try {
            return GreenDaoManager.getInstance().getSession().getBookDao()
                    .queryBuilder()
                    .where(BookDao.Properties.ChapterUrl.eq(path))
                    .unique();
        } catch (Exception e) {
            e.printStackTrace();
            return GreenDaoManager.getInstance().getSession().getBookDao()
                    .queryBuilder()
                    .where(BookDao.Properties.ChapterUrl.eq(path))
                    .list().get(0);
        }
    }

    /**
     * 删除书
     *
     * @param id
     */
    public void deleteBookById(String id) {
        BookDao bookDao = GreenDaoManager.getInstance().getSession().getBookDao();
        bookDao.deleteByKey(id);
        mChapterService.deleteBookALLChapterById(id);
        mBookMarkService.deleteBookALLBookMarkById(id);
    }

    /**
     * 删除书
     *
     * @param book
     */
    public void deleteBook(Book book) {
        try {
            deleteEntity(book);
            mChapterService.deleteBookALLChapterById(book.getId());
            mBookMarkService.deleteBookALLBookMarkById(book.getId());
        }catch (Exception ignored){}
    }

    /**
     * 删除所有书
     */
    public void deleteAllBooks() {
        for (Book book : getAllBooks()) {
            deleteBook(book);
        }
    }

    /**
     * 通过分组id删除书
     */
    public void deleteBooksByGroupId(String groupId) {
        for (Book book : getGroupBooks(groupId)) {
            deleteBook(book);
        }
    }

    /**
     * 查询书籍总数
     *
     * @return
     */
    public int countBookTotalNum() {
        int num = 0;
        try {
            Cursor cursor = selectBySql("select count(*) n from book ", null);
            if (cursor.moveToNext()) {
                num = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    /**
     * 更新书
     *
     * @param books
     */
    public void updateBooks(List<Book> books) {
        BookDao bookDao = GreenDaoManager.getInstance().getSession().getBookDao();
        bookDao.updateInTx(books);
    }


    /**
     * 删除旧书添加新书
     *
     * @param OldBook
     * @param newBook
     */
    public void updateBook(Book OldBook, Book newBook) {
        deleteBook(OldBook);
        newBook.setId(StringHelper.getStringRandom(25));
        addEntity(newBook);
    }

    /**
     * 删除所有章节缓存
     */
    public void deleteAllBookCathe(){
        FileUtils.deleteFile(APPCONST.BOOK_CACHE_PATH);
    }
    /**
     * 判断书籍是否存在
     *
     * @param book
     * @return
     */
    public boolean isBookCollected(Book book) {
        return findBookByAuthorAndName(book.getName(), book.getAuthor()) != null;
    }

    /**
     * 匹配书籍历史章节
     *
     * @param book
     * @param mChapters
     */
    /*public boolean matchHistoryChapterPos(Book book, ArrayList<Chapter> mChapters) {
        assert book.getHistoryChapterId() != null;
        float matchSui = SysManager.getSetting().getMatchChapterSuitability();

        int oldDurChapterIndex = book.getHisttoryChapterNum();
        int oldChapterListSize = book.getChapterTotalNum();
        int newChapterSize = mChapters.size();

        int min = Math.max(0, Math.min(oldDurChapterIndex, oldDurChapterIndex - oldChapterListSize + newChapterSize) - 10);
        int max = Math.min(newChapterSize - 1, Math.max(oldDurChapterIndex, oldDurChapterIndex - oldChapterListSize + newChapterSize) + 10);
        String historyChapter = StringUtils.deleteWhitespace(book.getHistoryChapterId());
        String newChapter;

        for (int i = min; i < max; i++) {
            Chapter chapter = mChapters.get(i);
            newChapter = StringUtils.deleteWhitespace(chapter.getTitle());
            if (historyChapter.contains(newChapter) ||
                    newChapter.contains(historyChapter)) {
                book.setHistoryChapterId(chapter.getTitle());
                book.setHisttoryChapterNum(i);
                updateEntity(book);
                return true;
            }
        }

        for (int i = min; i < max; i++) {
            Chapter chapter = mChapters.get(i);
            newChapter = StringUtils.deleteWhitespace(chapter.getTitle());
            if (StringUtils.levenshtein(historyChapter, newChapter) > matchSui) {
                book.setHistoryChapterId(chapter.getTitle());
                book.setHisttoryChapterNum(i);
                updateEntity(book);
                return true;
            }
        }
        return false;
    }*/

    public boolean matchHistoryChapterPos(Book book, ArrayList<Chapter> mChapters) {
        float matchSui = SysManager.getSetting().getMatchChapterSuitability();
        int index = getDurChapter(book.getHisttoryChapterNum(), book.getChapterTotalNum(), book.getHistoryChapterId(), mChapters);
        if (book.getHistoryChapterId() == null) return false;
        String oldName = StringUtils.deleteWhitespace(book.getHistoryChapterId());
        String newName = StringUtils.deleteWhitespace(mChapters.get(index).getTitle());
        /*int i1 = getChapterNum(oldName);
        int i2 = getChapterNum(newName);
        String s1 = getPureChapterName(oldName);
        String s2 = getPureChapterName(newName);
        int i = 1;*/
        if (oldName.contains(newName) || newName.contains(oldName) ||
                StringUtils.levenshtein(oldName, newName) > matchSui ||
                getChapterNum(oldName) == getChapterNum(newName) ||
                getPureChapterName(oldName).equals(getPureChapterName(newName))){
            book.setHistoryChapterId(mChapters.get(index).getTitle());
            book.setHisttoryChapterNum(index);
            updateEntity(book);
            return true;
        }
        return false;
    }

    /**
     * 根据目录名获取当前章节
     */
    public int getDurChapter(int oldDurChapterIndex, int oldChapterListSize, String oldDurChapterName, List<Chapter> newChapterList) {
        if (oldChapterListSize == 0)
            return 0;
        int oldChapterNum = getChapterNum(oldDurChapterName);
        String oldName = getPureChapterName(oldDurChapterName);
        int newChapterSize = newChapterList.size();
        int min = Math.max(0, Math.min(oldDurChapterIndex, oldDurChapterIndex - oldChapterListSize + newChapterSize) - 10);
        int max = Math.min(newChapterSize - 1, Math.max(oldDurChapterIndex, oldDurChapterIndex - oldChapterListSize + newChapterSize) + 10);
        double nameSim = 0;
        int newIndex = 0;
        int newNum = 0;
        if (!oldName.isEmpty()) {
            StringSimilarityService service = new StringSimilarityServiceImpl(new JaroWinklerStrategy());
            for (int i = min; i <= max; i++) {
                String newName = getPureChapterName(newChapterList.get(i).getTitle());
                double temp = service.score(oldName, newName);
                if (temp > nameSim) {
                    nameSim = temp;
                    newIndex = i;
                }
            }
        }
        if (nameSim < 0.96 && oldChapterNum > 0) {
            for (int i = min; i <= max; i++) {
                int temp = getChapterNum(newChapterList.get(i).getTitle());
                if (temp == oldChapterNum) {
                    newNum = temp;
                    newIndex = i;
                    break;
                } else if (Math.abs(temp - oldChapterNum) < Math.abs(newNum - oldChapterNum)) {
                    newNum = temp;
                    newIndex = i;
                }
            }
        }
        if (nameSim > 0.96 || Math.abs(newNum - oldChapterNum) < 1) {
            return newIndex;
        } else {
            return Math.min(Math.max(0, newChapterList.size() - 1), oldDurChapterIndex);
        }
    }

    private int getChapterNum(String chapterName) {
        if (chapterName != null) {
            Matcher matcher = chapterNamePattern.matcher(chapterName);
            if (matcher.find()) {
                return StringUtils.stringToInt(matcher.group(2));
            }
        }
        return -1;
    }

    private String getPureChapterName(String chapterName) {
        return chapterName == null ? ""
                : StringUtils.fullToHalf(chapterName).replaceAll("\\s", "")
                .replaceAll("^第.*?章|[(\\[][^()\\[\\]]{2,}[)\\]]$", "")
                .replaceAll("[^\\w\\u4E00-\\u9FEF〇\\u3400-\\u4DBF\\u20000-\\u2A6DF\\u2A700-\\u2EBEF]", "");
        // 所有非字母数字中日韩文字 CJK区+扩展A-F区
    }

    public static String formatAuthor(String author) {
        if (author == null) {
            return "";
        }
        return author.replaceAll("作\\s*者[\\s:：]*", "").replaceAll("\\s+", " ").trim();
    }

    public static int guessChapterNum(String name) {
        if (TextUtils.isEmpty(name) || name.matches("第.*?卷.*?第.*[章节回]"))
            return -1;
        Matcher matcher = chapterNamePattern.matcher(name);
        if (matcher.find()) {
            return StringUtils.stringToInt(matcher.group(2));
        }
        return -1;
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
