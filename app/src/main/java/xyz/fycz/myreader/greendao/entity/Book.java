package xyz.fycz.myreader.greendao.entity;


import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.util.SharedPreUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * 书
 * Created by fengyue on 2020/08/23.
 */

@Entity
public class Book implements Serializable {
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String name;//书名
    private String chapterUrl;//书目Url(本地书籍为：本地书籍地址)
    private String infoUrl;//书目详情Url(本地书籍为：文件编码)
    private String imgUrl;//封面图片url
    private String desc;//简介
    private String author;//作者
    @Nullable
    private String type;//类型(本地书籍为：本地书籍)
    private String updateDate;//更新时间
    @Nullable
    private String newestChapterId;//最新章节id
    @Nullable
    private String newestChapterTitle;//最新章节标题
    @Nullable
    private String newestChapterUrl;//最新章节url
    @Nullable
    private String historyChapterId;//上次关闭时的章节ID
    @Nullable
    private int histtoryChapterNum;//上次关闭时的章节数

    private int sortCode;//排序编码

    private int noReadNum;//未读章数量

    private int chapterTotalNum;//总章节数

    private int lastReadPosition;//上次阅读到的章节的位置

    @Nullable
    private String source;

    private boolean isCloseUpdate;//是否关闭更新

    @Nullable
    private boolean isDownLoadAll = true;//是否一键缓存

    private String groupId;//分组id

    private int groupSort;//分组排序

    private String tag;

    private Boolean replaceEnable = SharedPreUtils.getInstance().getBoolean("replaceEnableDefault", true);

    private long lastReadTime;

    @Generated(hash = 1269102537)
    public Book(String id, String name, String chapterUrl, String infoUrl, String imgUrl, String desc,
            String author, String type, String updateDate, String newestChapterId, String newestChapterTitle,
            String newestChapterUrl, String historyChapterId, int histtoryChapterNum, int sortCode, int noReadNum,
            int chapterTotalNum, int lastReadPosition, String source, boolean isCloseUpdate,
            boolean isDownLoadAll, String groupId, int groupSort, String tag, Boolean replaceEnable,
            long lastReadTime) {
        this.id = id;
        this.name = name;
        this.chapterUrl = chapterUrl;
        this.infoUrl = infoUrl;
        this.imgUrl = imgUrl;
        this.desc = desc;
        this.author = author;
        this.type = type;
        this.updateDate = updateDate;
        this.newestChapterId = newestChapterId;
        this.newestChapterTitle = newestChapterTitle;
        this.newestChapterUrl = newestChapterUrl;
        this.historyChapterId = historyChapterId;
        this.histtoryChapterNum = histtoryChapterNum;
        this.sortCode = sortCode;
        this.noReadNum = noReadNum;
        this.chapterTotalNum = chapterTotalNum;
        this.lastReadPosition = lastReadPosition;
        this.source = source;
        this.isCloseUpdate = isCloseUpdate;
        this.isDownLoadAll = isDownLoadAll;
        this.groupId = groupId;
        this.groupSort = groupSort;
        this.tag = tag;
        this.replaceEnable = replaceEnable;
        this.lastReadTime = lastReadTime;
    }


    @Generated(hash = 1839243756)
    public Book() {
    }

    @Override
    public Object clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, Book.class);
        } catch (Exception ignored) {
        }
        return this;
    }

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getChapterUrl() {
        return this.chapterUrl;
    }
    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }
    public String getImgUrl() {
        return this.imgUrl;
    }
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
    public String getDesc() {
        return this.desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getAuthor() {
        return this.author;
    }
    public void setAuthor(String author) {
        this.author = BookService.formatAuthor(author);
    }
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getUpdateDate() {
        return this.updateDate;
    }
    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
    public String getNewestChapterId() {
        return this.newestChapterId;
    }
    public void setNewestChapterId(String newestChapterId) {
        this.newestChapterId = newestChapterId;
    }
    public String getNewestChapterTitle() {
        return this.newestChapterTitle;
    }
    public void setNewestChapterTitle(String newestChapterTitle) {
        this.newestChapterTitle = newestChapterTitle;
    }
    public String getNewestChapterUrl() {
        return this.newestChapterUrl;
    }
    public void setNewestChapterUrl(String newestChapterUrl) {
        this.newestChapterUrl = newestChapterUrl;
    }
    public String getHistoryChapterId() {
        return this.historyChapterId;
    }
    public void setHistoryChapterId(String historyChapterId) {
        this.historyChapterId = historyChapterId;
    }
    public int getHisttoryChapterNum() {
        return this.histtoryChapterNum;
    }
    public void setHisttoryChapterNum(int histtoryChapterNum) {
        this.histtoryChapterNum = histtoryChapterNum;
    }
    public int getSortCode() {
        return this.sortCode;
    }
    public void setSortCode(int sortCode) {
        this.sortCode = sortCode;
    }
    public int getNoReadNum() {
        return this.noReadNum;
    }
    public void setNoReadNum(int noReadNum) {
        this.noReadNum = noReadNum;
    }
    public int getChapterTotalNum() {
        return this.chapterTotalNum;
    }
    public void setChapterTotalNum(int chapterTotalNum) {
        this.chapterTotalNum = chapterTotalNum;
    }
    public int getLastReadPosition() {
        return this.lastReadPosition;
    }
    public void setLastReadPosition(int lastReadPosition) {
        this.lastReadPosition = lastReadPosition;
    }

    @Nullable
    public String getSource() {
        return source;
    }

    public void setSource(@Nullable String source) {
        this.source = source;
    }

    public boolean getIsCloseUpdate() {
        return this.isCloseUpdate;
    }

    public void setIsCloseUpdate(boolean isCloseUpdate) {
        this.isCloseUpdate = isCloseUpdate;
    }

    public boolean getIsDownLoadAll() {
        return this.isDownLoadAll;
    }

    public void setIsDownLoadAll(boolean isDownLoadAll) {
        this.isDownLoadAll = isDownLoadAll;
    }
    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return name.equals(book.name) &&
                chapterUrl.equals(book.chapterUrl) &&
                author.equals(book.author) &&
                source.equals(book.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, chapterUrl, author, source);
    }

    public int getGroupSort() {
        return this.groupSort;
    }

    public void setGroupSort(int groupSort) {
        this.groupSort = groupSort;
    }

    public String getInfoUrl() {
        return this.infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }


    public String getTag() {
        return this.tag;
    }


    public void setTag(String tag) {
        this.tag = tag;
    }


    public Boolean getReplaceEnable() {
        return this.replaceEnable;
    }


    public void setReplaceEnable(Boolean replaceEnable) {
        this.replaceEnable = replaceEnable;
    }


    public long getLastReadTime() {
        return this.lastReadTime;
    }


    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }


}
