package xyz.fycz.myreader.greendao.entity.rule;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.Objects;

import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

import static xyz.fycz.myreader.util.utils.StringUtils.stringEquals;

/**
 * @author fengyue
 * @date 2021/2/8 17:53
 */
public class InfoRule implements Parcelable {
    private String urlPattern;
    private String init;
    private String name;
    private String author;
    private String type;
    private String desc;
    private String status;
    private String wordCount;
    private String lastChapter;
    private String updateTime;
    private String imgUrl;
    private String tocUrl;

    public InfoRule() {
    }

    protected InfoRule(Parcel in) {
        urlPattern = in.readString();
        init = in.readString();
        name = in.readString();
        author = in.readString();
        type = in.readString();
        desc = in.readString();
        status = in.readString();
        wordCount = in.readString();
        lastChapter = in.readString();
        updateTime = in.readString();
        imgUrl = in.readString();
        tocUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(urlPattern);
        dest.writeString(init);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(type);
        dest.writeString(desc);
        dest.writeString(status);
        dest.writeString(wordCount);
        dest.writeString(lastChapter);
        dest.writeString(updateTime);
        dest.writeString(imgUrl);
        dest.writeString(tocUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InfoRule> CREATOR = new Creator<InfoRule>() {
        @Override
        public InfoRule createFromParcel(Parcel in) {
            return new InfoRule(in);
        }

        @Override
        public InfoRule[] newArray(int size) {
            return new InfoRule[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) o = new InfoRule();
        if (getClass() != o.getClass()) return false;
        InfoRule infoRule = (InfoRule) o;
        return stringEquals(urlPattern, infoRule.urlPattern) &&
                stringEquals(init, infoRule.init) &&
                stringEquals(name, infoRule.name) &&
                stringEquals(author, infoRule.author) &&
                stringEquals(type, infoRule.type) &&
                stringEquals(desc, infoRule.desc) &&
                stringEquals(status, infoRule.status) &&
                stringEquals(wordCount, infoRule.wordCount) &&
                stringEquals(lastChapter, infoRule.lastChapter) &&
                stringEquals(updateTime, infoRule.updateTime) &&
                stringEquals(imgUrl, infoRule.imgUrl) &&
                stringEquals(tocUrl, infoRule.tocUrl);
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getInit() {
        return init;
    }

    public void setInit(String init) {
        this.init = init;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWordCount() {
        return wordCount;
    }

    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
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
}
