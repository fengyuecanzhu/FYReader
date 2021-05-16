package xyz.fycz.myreader.greendao.entity;


import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.StringHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static xyz.fycz.myreader.common.APPCONST.MAP_STRING;

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
    private String type;//类型(本地书籍为：本地书籍)
    private String updateDate;//更新时间
    private String wordCount;
    private String status;
    private String newestChapterId;//最新章节id
    private String newestChapterTitle;//最新章节标题
    private String historyChapterId;//上次关闭时的章节ID
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

    private String variable;

    @Transient
    private Map<String, String> variableMap;

    @Transient
    private Map<String, String> catheMap;

    @Generated(hash = 1839243756)
    public Book() {
    }

    @Generated(hash = 1910282685)
    public Book(String id, String name, String chapterUrl, String infoUrl, String imgUrl, String desc,
                String author, String type, String updateDate, String wordCount, String status,
                String newestChapterId, String newestChapterTitle, String historyChapterId, int histtoryChapterNum,
                int sortCode, int noReadNum, int chapterTotalNum, int lastReadPosition, String source,
                boolean isCloseUpdate, boolean isDownLoadAll, String groupId, int groupSort, String tag,
                Boolean replaceEnable, long lastReadTime, String variable) {
        this.id = id;
        this.name = name;
        this.chapterUrl = chapterUrl;
        this.infoUrl = infoUrl;
        this.imgUrl = imgUrl;
        this.desc = desc;
        this.author = author;
        this.type = type;
        this.updateDate = updateDate;
        this.wordCount = wordCount;
        this.status = status;
        this.newestChapterId = newestChapterId;
        this.newestChapterTitle = newestChapterTitle;
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
        this.variable = variable;
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


    public String getWordCount() {
        return this.wordCount;
    }


    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
    }


    public String getStatus() {
        return this.status;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    public void putVariable(String key, String value) {
        if (variableMap == null) {
            variableMap = new HashMap<>();
        }
        variableMap.put(key, value);
        variable = new Gson().toJson(variableMap);
    }

    public Map<String, String> getVariableMap() {
        if (variableMap == null && !TextUtils.isEmpty(variable)) {
            variableMap = new Gson().fromJson(variable, MAP_STRING);
        }
        return variableMap;
    }


    public String getVariable() {
        return this.variable;
    }


    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void putCathe(String key, String value) {
        if (catheMap == null) {
            catheMap = new HashMap<>();
        }
        catheMap.put(key, value);
    }

    public String getCathe(String key){
        if (catheMap == null){
            return "";
        }
        return catheMap.get(key);
    }
}
