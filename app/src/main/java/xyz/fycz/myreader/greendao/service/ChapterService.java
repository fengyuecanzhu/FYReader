package xyz.fycz.myreader.greendao.service;

import android.database.Cursor;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.gen.ChapterDao;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.util.utils.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ChapterService extends BaseService {
    private static volatile ChapterService sInstance;

    public static ChapterService getInstance() {
        if (sInstance == null){
            synchronized (ChapterService.class){
                if (sInstance == null){
                    sInstance = new ChapterService();
                }
            }
        }
        return sInstance;
    }
    private List<Chapter> findChapters(String sql, String[] selectionArgs) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        try {
            Cursor cursor = selectBySql(sql, selectionArgs);
            if (cursor == null) return chapters;
            while (cursor.moveToNext()) {
                Chapter chapter = new Chapter();
                chapter.setId(cursor.getString(0));
                chapter.setBookId(cursor.getString(1));
                chapter.setNumber(cursor.getInt(2));
                chapter.setTitle(cursor.getString(3));
                chapter.setUrl(cursor.getString(4));
                chapter.setIsVip(cursor.getInt(5) != 0);
                chapter.setIsPay(cursor.getInt(6) != 0);
                chapter.setUpdateTime(cursor.getString(7));
                chapter.setContent(cursor.getString(8));
                chapter.setStart(cursor.getInt(9));
                chapter.setEnd(cursor.getInt(10));
                chapters.add(chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return chapters;
        }
        return chapters;
    }

    /**
     * 通过ID查章节
     *
     * @param id
     * @return
     */
    public Chapter getChapterById(String id) {
        ChapterDao chapterDao = DbManager.getInstance().getSession().getChapterDao();
        return chapterDao.load(id);
    }

    /**
     * 获取书的所有章节
     *
     * @return
     */
    public List<Chapter> findBookAllChapterByBookId(String bookId) {

        if (StringHelper.isEmpty(bookId)) return new ArrayList<>();


        String sql = "select * from chapter where book_id = ? order by number";

        return findChapters(sql, new String[]{bookId});
    }

    /**
     * 新增章节
     *
     * @param chapter
     */
    public void addChapter(Chapter chapter, String content) {
        chapter.setId(StringHelper.getStringRandom(25));
        addEntity(chapter);
        saveChapterCacheFile(chapter, content);
    }

    /**
     * 查找章节
     *
     * @param bookId
     * @param title
     * @return
     */
    public Chapter findChapterByBookIdAndTitle(String bookId, String title) {
        Chapter chapter = null;
        try {
            String sql = "select id from chapter where book_id = ? and title = ?";
            Cursor cursor = selectBySql(sql, new String[]{bookId, title});
            if (cursor == null) return null;
            if (cursor.moveToNext()) {
                String id = cursor.getString(0);
                chapter = getChapterById(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chapter;
    }

    /**
     * 删除书的所有章节
     *
     * @param bookId
     */
    public void deleteBookALLChapterById(String bookId) {
        DbManager.getInstance().getSession().getChapterDao()
                .queryBuilder()
                .where(ChapterDao.Properties.BookId.eq(bookId))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
        deleteAllChapterCacheFile(bookId);
    }

    /**
     * 更新章节
     */
    public void updateChapter(Chapter chapter) {
        ChapterDao chapterDao = DbManager.getInstance().getSession().getChapterDao();
        chapterDao.update(chapter);
    }

    /**
     * 分段查找章节
     *
     * @param bookId
     * @param from
     * @param to
     * @return
     */
    public List<Chapter> findChapter(String bookId, int from, int to) {
        String sql = "select * from " +
                "(select row_number()over(order by number)rownumber,* from chapter where bookId = ? order by number)a " +
                "where rownumber >= ? and rownumber <= ?";

        return findChapters(sql, new String[]{bookId, String.valueOf(from), String.valueOf(to)});
    }


    /**
     * 保存或更新章节
     *
     * @param chapter
     */
    public void saveOrUpdateChapter(Chapter chapter, String content) {
        chapter.setContent(APPCONST.BOOK_CACHE_PATH + chapter.getBookId()
                + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY);
        if (!StringHelper.isEmpty(chapter.getId())) {
            updateEntity(chapter);
        } else {
            addChapter(chapter, content);
        }
        saveChapterCacheFile(chapter, content);
    }

    /**
     * 批量添加章节
     */
    public void addChapters(List<Chapter> chapters) {
        ChapterDao chapterDao = DbManager.getInstance().getSession().getChapterDao();
        chapterDao.insertInTx(chapters);
    }

    /**
     * 缓存章节
     *
     * @param chapter
     */
    public void saveChapterCacheFile(Chapter chapter, String content) {
        if (StringHelper.isEmpty(content)) {
            return;
        }

        File file = getBookFile(chapter.getBookId(), chapter.getTitle());
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bw.write(content.replace(chapter.getTitle(), ""));
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bw);
        }
    }

    /**
     * 删除章节缓存
     *
     * @param chapter
     */
    public void deleteChapterCacheFile(Chapter chapter) {
        File file = getBookFile(chapter.getBookId(), chapter.getTitle());
        file.delete();
    }

    /**
     * 获取缓存章节内容
     * @param chapter
     * @return
     */
    public String getChapterCatheContent(Chapter chapter){
        File file = new File(APPCONST.BOOK_CACHE_PATH + chapter.getBookId()
                + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY);
        if (!file.exists()) return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            StringBuilder s = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null){
                s.append(line);
                s.append("\n");
            }
            return s.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            IOUtils.close(br);
        }
    }

    /**
     * 更新所有章节
     *
     * @param newChapters
     */
    public void updateAllOldChapterData(List<Chapter> mChapters, List<Chapter> newChapters, String bookId) {
        int i;
        for (i = 0; i < mChapters.size() && i < newChapters.size(); i++) {
            Chapter oldChapter = mChapters.get(i);
            Chapter newChapter = newChapters.get(i);
            if (!oldChapter.getTitle().equals(newChapter.getTitle())) {
                oldChapter.setTitle(newChapter.getTitle());
                oldChapter.setUrl(newChapter.getUrl());
                oldChapter.setContent(null);
                saveOrUpdateChapter(oldChapter, null);
            }
        }
        if (mChapters.size() < newChapters.size()) {
            int start = mChapters.size();
            for (int j = mChapters.size(); j < newChapters.size(); j++) {
                newChapters.get(j).setId(StringHelper.getStringRandom(25));
                newChapters.get(j).setBookId(bookId);
                mChapters.add(newChapters.get(j));
//                mChapterService.addChapter(newChapters.get(j));
            }
            addChapters(mChapters.subList(start, mChapters.size()));
        } else if (mChapters.size() > newChapters.size()) {
            for (int j = newChapters.size(); j < mChapters.size(); j++) {
                deleteEntity(mChapters.get(j));
                deleteChapterCacheFile(mChapters.get(j));
            }
            mChapters.subList(0, newChapters.size());
        }
    }


    /**
     * 根据文件名判断是否被缓存过 (因为可能数据库显示被缓存过，但是文件中却没有的情况，所以需要根据文件判断是否被缓存
     * 过)
     *
     * @param folderName : bookId
     * @param fileName:  chapterName
     * @return
     */
    public static boolean isChapterCached(String folderName, String fileName) {
        File file = new File(APPCONST.BOOK_CACHE_PATH + folderName
                + File.separator + fileName + FileUtils.SUFFIX_FY);
        return file.exists();
    }

    public static int countChar(String folderName, String fileName) {//统计字符数
        int charnum = 0;//字符数
        File file = new File(APPCONST.BOOK_CACHE_PATH + folderName
                + File.separator + fileName + FileUtils.SUFFIX_FY);
        int x;
        FileReader fReader = null;
        try {
            fReader = new FileReader(file);
            while ((x = fReader.read()) != -1) {//按字符读文件，判断，符合则字符加一
                char a = (char) x;
                if (a != '\n' && a != '\r') {
                    charnum++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(fReader);
        }
        return charnum;//返回结果
    }

    private void deleteAllChapterCacheFile(String bookId) {
        FileUtils.deleteFile(APPCONST.BOOK_CACHE_PATH + bookId);
    }

    /**
     * 创建或获取存储文件
     *
     * @param folderName
     * @param fileName
     * @return
     */
    public static File getBookFile(String folderName, String fileName) {
        return FileUtils.getFile(APPCONST.BOOK_CACHE_PATH + folderName
                + File.separator + fileName + FileUtils.SUFFIX_FY);
    }

}
