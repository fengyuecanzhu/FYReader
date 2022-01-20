package xyz.fycz.myreader.greendao.entity.rule;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.converter.PropertyConverter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.GsonUtils;

import static xyz.fycz.myreader.util.utils.StringUtils.stringEquals;

/**
 * @author fengyue
 * @date 2021/2/8 17:48
 */
public class SearchRule implements Parcelable, BookListRule {
    private String searchUrl;
    private String charset;
    private String list;
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
    private String infoUrl;
    private boolean relatedWithInfo;

    public SearchRule() {
    }

    protected SearchRule(Parcel in) {
        searchUrl = in.readString();
        charset = in.readString();
        list = in.readString();
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
        infoUrl = in.readString();
        relatedWithInfo = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(searchUrl);
        dest.writeString(charset);
        dest.writeString(list);
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
        dest.writeString(infoUrl);
        dest.writeByte((byte) (relatedWithInfo ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchRule> CREATOR = new Creator<SearchRule>() {
        @Override
        public SearchRule createFromParcel(Parcel in) {
            return new SearchRule(in);
        }

        @Override
        public SearchRule[] newArray(int size) {
            return new SearchRule[size];
        }
    };

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
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

    public String getInfoUrl() {
        return infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }

    public boolean isRelatedWithInfo() {
        return relatedWithInfo;
    }

    public void setRelatedWithInfo(boolean relatedWithInfo) {
        this.relatedWithInfo = relatedWithInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchRule that = (SearchRule) o;
        return relatedWithInfo == that.relatedWithInfo &&
                stringEquals(searchUrl, that.searchUrl) &&
                stringEquals(charset, that.charset) &&
                stringEquals(list, that.list) &&
                stringEquals(name, that.name) &&
                stringEquals(author, that.author) &&
                stringEquals(type, that.type) &&
                stringEquals(desc, that.desc) &&
                stringEquals(wordCount, that.wordCount) &&
                stringEquals(status, that.status) &&
                stringEquals(lastChapter, that.lastChapter) &&
                stringEquals(updateTime, that.updateTime) &&
                stringEquals(imgUrl, that.imgUrl) &&
                stringEquals(tocUrl, that.tocUrl) &&
                stringEquals(infoUrl, that.infoUrl);
    }

}
