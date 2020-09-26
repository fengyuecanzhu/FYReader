package xyz.fycz.myreader.greendao.service;

import xyz.fycz.myreader.greendao.GreenDaoManager;
import xyz.fycz.myreader.greendao.entity.BookGroup;
import xyz.fycz.myreader.greendao.gen.BookGroupDao;
import xyz.fycz.myreader.greendao.gen.BookMarkDao;
import xyz.fycz.myreader.util.StringHelper;

import java.util.List;

/**
 * @author fengyue
 * @date 2020/9/26 12:14
 */
public class BookGroupService extends BaseService{
    private static volatile BookGroupService sInstance;

    public static BookGroupService getInstance() {
        if (sInstance == null){
            synchronized (BookGroupService.class){
                if (sInstance == null){
                    sInstance = new BookGroupService();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取所有书籍分组
     * @return
     */
    public List<BookGroup> getAllGroups(){
        return GreenDaoManager.getInstance().getSession().getBookGroupDao()
                .queryBuilder()
                .orderAsc(BookGroupDao.Properties.Num)
                .list();
    }

    /**
     * 通过I的获取书籍分组
     * @param groupId
     * @return
     */
    public BookGroup getGroupById(String groupId){
        return GreenDaoManager.getInstance().getSession().getBookGroupDao()
                .queryBuilder()
                .where(BookGroupDao.Properties.Id.eq(groupId))
                .unique();
    }

    /**
     * 添加书籍分组
     * @param bookGroup
     */
    public void addBookGroup(BookGroup bookGroup){
        bookGroup.setNum(countBookGroup());
        bookGroup.setId(StringHelper.getStringRandom(25));
        addEntity(bookGroup);
    }

    /**
     * 删除书籍分组
     * @param bookGroup
     */
    public void deleteBookGroup(BookGroup bookGroup){
        deleteEntity(bookGroup);
    }


    private int countBookGroup(){
        return (int) GreenDaoManager.getInstance().getSession().getBookGroupDao()
                .queryBuilder()
                .count();
    }

}
