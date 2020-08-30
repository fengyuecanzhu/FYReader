package xyz.fycz.myreader.greendao.entity;


import androidx.annotation.Nullable;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.util.utils.FileUtils;

import java.io.File;

/**
 * 章节
 * Created by zhao on 2017/7/24.
 */

@Entity
public class Chapter {
    @Id
    private String id;

    private String bookId;//章节所属书的ID
    private int number;//章节序号
    private String title;//章节标题
    private String url;//章节链接(本地书籍为：字符编码)
    @Nullable
    private String content;//章节正文

    //章节内容在文章中的起始位置(本地)
    private long start;
    //章节内容在文章中的终止位置(本地)
    private long end;


    @Generated(hash = 763230955)
    public Chapter(String id, String bookId, int number, String title, String url,
            String content, long start, long end) {
        this.id = id;
        this.bookId = bookId;
        this.number = number;
        this.title = title;
        this.url = url;
        this.content = content;
        this.start = start;
        this.end = end;
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
        String filePath = APPCONST.BOOK_CACHE_PATH + bookId
                + File.separator + title + FileUtils.SUFFIX_FY;
        File file = new File(filePath);
        if (file.exists() && file.length() > 0){
            this.content = filePath;
        }else {
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
}
