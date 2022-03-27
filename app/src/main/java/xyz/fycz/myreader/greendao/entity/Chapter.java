package xyz.fycz.myreader.greendao.entity;


import static xyz.fycz.myreader.common.APPCONST.MAP_STRING;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.model.third3.analyzeRule.RuleDataInterface;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Chapter implements RuleDataInterface {
    @Id
    private String id;

    private String bookId;//章节所属书的ID
    private int number;//章节序号
    private String title;//章节标题
    private String url;//章节链接(本地书籍为：字符编码)
    private boolean isVip;//是否VIP
    private boolean isPay;//是否已购买
    private String updateTime;//更新时间
    @Nullable
    private String content;//章节正文

    //章节内容在文章中的起始位置(本地)
    private long start;
    //章节内容在文章中的终止位置(本地)
    private long end;

    private String variable;
    @Transient
    private Map<String, String> variableMap;


    @Generated(hash = 1109296579)
    public Chapter(String id, String bookId, int number, String title, String url,
            boolean isVip, boolean isPay, String updateTime, String content,
            long start, long end, String variable) {
        this.id = id;
        this.bookId = bookId;
        this.number = number;
        this.title = title;
        this.url = url;
        this.isVip = isVip;
        this.isPay = isPay;
        this.updateTime = updateTime;
        this.content = content;
        this.start = start;
        this.end = end;
        this.variable = variable;
    }

    @Generated(hash = 393170288)
    public Chapter() {
    }


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return this.bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        if (end > 0) return end + "";
        File file = ChapterService.getChapterFile(this);
        if (file.exists() && file.length() > 0) {
            this.content = file.getAbsolutePath();
        } else {
            this.content = null;
        }
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void putVariable(@NonNull String key, String value) {
        if (variableMap == null) {
            variableMap = new HashMap<>();
        }
        variableMap.put(key, value);
        variable = GsonExtensionsKt.getGSON().toJson(variableMap);
    }

    @NonNull
    public Map<String, String> getVariableMap() {
        if (variableMap == null && !TextUtils.isEmpty(variable)) {
            variableMap = GsonExtensionsKt.getGSON().fromJson(variable, MAP_STRING);
        }
        if (variableMap == null) {
            variableMap = new HashMap<>();
        }
        return variableMap;
    }

    @Nullable
    @Override
    public String getVariable(@NonNull String key) {
        return getVariableMap().get(key);
    }

    public String getVariable() {
        return this.variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }
    
    public String getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public boolean getIsVip() {
        return this.isVip;
    }

    public void setIsVip(boolean isVip) {
        this.isVip = isVip;
    }

    public boolean getIsPay() {
        return this.isPay;
    }

    public void setIsPay(boolean isPay) {
        this.isPay = isPay;
    }
}
