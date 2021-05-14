package xyz.fycz.myreader.greendao.entity.rule;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static xyz.fycz.myreader.util.utils.StringUtils.stringEquals;

/**
 * @author fengyue
 * @date 2021/2/10 8:57
 */
public class FindRule implements Parcelable {
    private String url;
    private String bookList;
    private String name;
    private String author;
    private String type;
    private String desc;
    private String wordCount;
    private String status;
    private String lastChapter;
    private String updateTime;
    private String imgUrl;
    private String tocUrl;

    public FindRule() {
    }

    protected FindRule(Parcel in) {
        url = in.readString();
        bookList = in.readString();
        name = in.readString();
        author = in.readString();
        type = in.readString();
        desc = in.readString();
        wordCount = in.readString();
        status = in.readString();
        lastChapter = in.readString();
        updateTime = in.readString();
        imgUrl = in.readString();
        tocUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(bookList);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(type);
        dest.writeString(desc);
        dest.writeString(wordCount);
        dest.writeString(status);
        dest.writeString(lastChapter);
        dest.writeString(updateTime);
        dest.writeString(imgUrl);
        dest.writeString(tocUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FindRule> CREATOR = new Creator<FindRule>() {
        @Override
        public FindRule createFromParcel(Parcel in) {
            return new FindRule(in);
        }

        @Override
        public FindRule[] newArray(int size) {
            return new FindRule[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBookList() {
        return bookList;
    }

    public void setBookList(String bookList) {
        this.bookList = bookList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getWordCount() {
        return wordCount;
    }

    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = lastChapter;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTocUrl() {
        return tocUrl;
    }

    public void setTocUrl(String tocUrl) {
        this.tocUrl = tocUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) o = new FindRule();
        if (getClass() != o.getClass()) return false;
        FindRule findRule = (FindRule) o;
        return  stringEquals(url, findRule.url) &&
                stringEquals(bookList, findRule.bookList) &&
                stringEquals(name, findRule.name) &&
                stringEquals(author, findRule.author) &&
                stringEquals(type, findRule.type) &&
                stringEquals(desc, findRule.desc) &&
                stringEquals(wordCount, findRule.wordCount) &&
                stringEquals(status, findRule.status) &&
                stringEquals(lastChapter, findRule.lastChapter) &&
                stringEquals(updateTime, findRule.updateTime) &&
                stringEquals(imgUrl, findRule.imgUrl) &&
                stringEquals(tocUrl, findRule.tocUrl);
    }

}
