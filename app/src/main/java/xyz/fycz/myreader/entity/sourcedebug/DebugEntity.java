package xyz.fycz.myreader.entity.sourcedebug;

import android.os.Parcel;
import android.os.Parcelable;

import xyz.fycz.myreader.greendao.entity.rule.BookSource;

/**
 * @author fengyue
 * @date 2021/2/12 19:35
 */
public class DebugEntity implements Parcelable {
    public static final int SEARCH = 0;
    public static final int INFO = 1;
    public static final int TOC = 2;
    public static final int CONTENT = 3;

    private int debugMode;
    private BookSource bookSource;
    private String url;
    private String parseResult;
    private String html;

    public DebugEntity() {
    }

    protected DebugEntity(Parcel in) {
        debugMode = in.readInt();
        bookSource = in.readParcelable(BookSource.class.getClassLoader());
        url = in.readString();
        parseResult = in.readString();
        html = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(debugMode);
        dest.writeParcelable(bookSource, flags);
        dest.writeString(url);
        dest.writeString(parseResult);
        dest.writeString(html);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DebugEntity> CREATOR = new Creator<DebugEntity>() {
        @Override
        public DebugEntity createFromParcel(Parcel in) {
            return new DebugEntity(in);
        }

        @Override
        public DebugEntity[] newArray(int size) {
            return new DebugEntity[size];
        }
    };

    public int getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(int debugMode) {
        this.debugMode = debugMode;
    }

    public BookSource getBookSource() {
        return bookSource;
    }

    public void setBookSource(BookSource bookSource) {
        this.bookSource = bookSource;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParseResult() {
        return parseResult;
    }

    public void setParseResult(String parseResult) {
        this.parseResult = parseResult;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
