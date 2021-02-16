package xyz.fycz.myreader.greendao.entity.rule;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.Objects;

import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

import static xyz.fycz.myreader.util.utils.StringUtils.stringEquals;

/**
 * @author fengyue
 * @date 2021/2/8 18:01
 */
public class ContentRule implements Parcelable {
    private String content;
    private String contentBaseUrl;
    private String contentUrlNext;

    protected ContentRule(Parcel in) {
        content = in.readString();
        contentBaseUrl = in.readString();
        contentUrlNext = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeString(contentBaseUrl);
        dest.writeString(contentUrlNext);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ContentRule> CREATOR = new Creator<ContentRule>() {
        @Override
        public ContentRule createFromParcel(Parcel in) {
            return new ContentRule(in);
        }

        @Override
        public ContentRule[] newArray(int size) {
            return new ContentRule[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) o = new ContentRule();
        if (getClass() != o.getClass()) return false;
        ContentRule that = (ContentRule) o;
        return  stringEquals(content, that.content) &&
                stringEquals(contentBaseUrl, that.contentBaseUrl) &&
                stringEquals(contentUrlNext, that.contentUrlNext);
    }


    public ContentRule() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentBaseUrl() {
        return contentBaseUrl;
    }

    public void setContentBaseUrl(String contentBaseUrl) {
        this.contentBaseUrl = contentBaseUrl;
    }

    public String getContentUrlNext() {
        return contentUrlNext;
    }

    public void setContentUrlNext(String contentUrlNext) {
        this.contentUrlNext = contentUrlNext;
    }


}
