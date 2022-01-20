package xyz.fycz.myreader.greendao.entity.rule;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.Objects;

import xyz.fycz.myreader.greendao.convert.ContentRuleConvert;
import xyz.fycz.myreader.greendao.convert.FindRuleConvert;
import xyz.fycz.myreader.greendao.convert.InfoRuleConvert;
import xyz.fycz.myreader.greendao.convert.SearchRuleConvert;
import xyz.fycz.myreader.greendao.convert.TocRuleConvert;

import static xyz.fycz.myreader.util.utils.StringUtils.stringEquals;

/**
 * @author fengyue
 * @date 2021/2/8 17:37
 */
@Entity
public class BookSource implements Parcelable, Cloneable {
    //基本信息
    @Id
    private String sourceUrl;
    private String sourceEName;//内置书源标识
    private String sourceName;
    private String sourceGroup;
    private String sourceCharset;
    private String sourceType;
    private String sourceHeaders;
    private String loginUrl;
    private String loginCheckJs;
    private String sourceComment;
    private String concurrentRate;
    private Long lastUpdateTime;

    @OrderBy
    private int orderNum;
    @OrderBy
    @NotNull
    private int weight;
    private boolean enable;

    @Transient
    private transient ArrayList<String> groupList;

    //搜索规则
    @Convert(converter = SearchRuleConvert.class, columnType = String.class)
    private SearchRule searchRule;
    //详情规则
    @Convert(converter = InfoRuleConvert.class, columnType = String.class)
    private InfoRule infoRule;
    //目录规则
    @Convert(converter = TocRuleConvert.class, columnType = String.class)
    private TocRule tocRule;
    //正文页规则
    @Convert(converter = ContentRuleConvert.class, columnType = String.class)
    private ContentRule contentRule;
    //发现页规则
    @Convert(converter = FindRuleConvert.class, columnType = String.class)
    private FindRule findRule;

    @Generated(hash = 906128088)
    public BookSource(String sourceUrl, String sourceEName, String sourceName, String sourceGroup,
            String sourceCharset, String sourceType, String sourceHeaders, String loginUrl,
            String sourceComment, String concurrentRate, Long lastUpdateTime, int orderNum, int weight,
            boolean enable, SearchRule searchRule, InfoRule infoRule, TocRule tocRule,
            ContentRule contentRule, FindRule findRule) {
        this.sourceUrl = sourceUrl;
        this.sourceEName = sourceEName;
        this.sourceName = sourceName;
        this.sourceGroup = sourceGroup;
        this.sourceCharset = sourceCharset;
        this.sourceType = sourceType;
        this.sourceHeaders = sourceHeaders;
        this.loginUrl = loginUrl;
        this.sourceComment = sourceComment;
        this.concurrentRate = concurrentRate;
        this.lastUpdateTime = lastUpdateTime;
        this.orderNum = orderNum;
        this.weight = weight;
        this.enable = enable;
        this.searchRule = searchRule;
        this.infoRule = infoRule;
        this.tocRule = tocRule;
        this.contentRule = contentRule;
        this.findRule = findRule;
    }

    @Generated(hash = 2045691642)
    public BookSource() {
    }


    protected BookSource(Parcel in) {
        sourceUrl = in.readString();
        sourceEName = in.readString();
        sourceName = in.readString();
        sourceGroup = in.readString();
        sourceCharset = in.readString();
        sourceType = in.readString();
        sourceHeaders = in.readString();
        loginUrl = in.readString();
        loginCheckJs = in.readString();
        sourceComment = in.readString();
        concurrentRate = in.readString();
        if (in.readByte() == 0) {
            lastUpdateTime = null;
        } else {
            lastUpdateTime = in.readLong();
        }
        orderNum = in.readInt();
        weight = in.readInt();
        enable = in.readByte() != 0;
        searchRule = in.readParcelable(SearchRule.class.getClassLoader());
        infoRule = in.readParcelable(InfoRule.class.getClassLoader());
        tocRule = in.readParcelable(TocRule.class.getClassLoader());
        contentRule = in.readParcelable(ContentRule.class.getClassLoader());
        findRule = in.readParcelable(FindRule.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sourceUrl);
        dest.writeString(sourceEName);
        dest.writeString(sourceName);
        dest.writeString(sourceGroup);
        dest.writeString(sourceCharset);
        dest.writeString(sourceType);
        dest.writeString(sourceHeaders);
        dest.writeString(loginUrl);
        dest.writeString(loginCheckJs);
        dest.writeString(sourceComment);
        dest.writeString(concurrentRate);
        if (lastUpdateTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(lastUpdateTime);
        }
        dest.writeInt(orderNum);
        dest.writeInt(weight);
        dest.writeByte((byte) (enable ? 1 : 0));
        dest.writeParcelable(searchRule, flags);
        dest.writeParcelable(infoRule, flags);
        dest.writeParcelable(tocRule, flags);
        dest.writeParcelable(contentRule, flags);
        dest.writeParcelable(findRule, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BookSource> CREATOR = new Creator<BookSource>() {
        @Override
        public BookSource createFromParcel(Parcel in) {
            return new BookSource(in);
        }

        @Override
        public BookSource[] newArray(int size) {
            return new BookSource[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookSource source = (BookSource) o;
        return enable == source.enable &&
                stringEquals(sourceUrl, source.sourceUrl) &&
                stringEquals(sourceEName, source.sourceEName) &&
                stringEquals(sourceName, source.sourceName) &&
                stringEquals(sourceGroup, source.sourceGroup) &&
                stringEquals(sourceCharset, source.sourceCharset) &&
                stringEquals(sourceType, source.sourceType) &&
                stringEquals(sourceHeaders, source.sourceHeaders) &&
                stringEquals(loginUrl, source.loginUrl) &&
                stringEquals(loginCheckJs, source.loginCheckJs) &&
                stringEquals(sourceComment, source.sourceComment) &&
                stringEquals(concurrentRate, source.concurrentRate) &&
                Objects.equals(searchRule, source.searchRule) &&
                Objects.equals(infoRule, source.infoRule) &&
                Objects.equals(tocRule, source.tocRule) &&
                Objects.equals(contentRule, source.contentRule) &&
                Objects.equals(findRule, source.findRule);
    }

    @NonNull
    @Override
    public Object clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, BookSource.class);
        } catch (Exception ignored) {
        }
        return this;
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceGroup() {
        return this.sourceGroup;
    }

    public void setSourceGroup(String sourceGroup) {
        this.sourceGroup = sourceGroup;
        upGroupList();
        this.sourceGroup = TextUtils.join("; ", groupList);
    }

    public String getSourceUrl() {
        return this.sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceCharset() {
        return this.sourceCharset;
    }

    public void setSourceCharset(String sourceCharset) {
        this.sourceCharset = sourceCharset;
    }

    public int getOrderNum() {
        return this.orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean getEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public SearchRule getSearchRule() {
        return this.searchRule;
    }

    public void setSearchRule(SearchRule searchRule) {
        this.searchRule = searchRule;
    }

    public InfoRule getInfoRule() {
        return this.infoRule;
    }

    public void setInfoRule(InfoRule infoRule) {
        this.infoRule = infoRule;
    }

    public TocRule getTocRule() {
        return this.tocRule;
    }

    public void setTocRule(TocRule tocRule) {
        this.tocRule = tocRule;
    }

    public ContentRule getContentRule() {
        return this.contentRule;
    }

    public void setContentRule(ContentRule contentRule) {
        this.contentRule = contentRule;
    }

    public String getSourceEName() {
        return this.sourceEName;
    }

    public void setSourceEName(String sourceEName) {
        this.sourceEName = sourceEName;
    }

    private void upGroupList() {
        if (groupList == null)
            groupList = new ArrayList<>();
        else
            groupList.clear();
        if (!TextUtils.isEmpty(sourceGroup)) {
            for (String group : sourceGroup.split("\\s*[,;，；]\\s*")) {
                group = group.trim();
                if (TextUtils.isEmpty(group) || groupList.contains(group)) continue;
                groupList.add(group);
            }
        }
    }

    public void addGroup(String group) {
        if (groupList == null)
            upGroupList();
        if (!groupList.contains(group)) {
            groupList.add(group);
            updateModTime();
            sourceGroup = TextUtils.join("; ", groupList);
        }
    }

    public void removeGroup(String group) {
        if (groupList == null)
            upGroupList();
        if (groupList.contains(group)) {
            groupList.remove(group);
            updateModTime();
            sourceGroup = TextUtils.join("; ", groupList);
        }
    }

    public boolean containsGroup(String group) {
        if (groupList == null) {
            upGroupList();
        }
        return groupList.contains(group);
    }

    public Long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void updateModTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public String getSourceComment() {
        return this.sourceComment;
    }

    public void setSourceComment(String sourceComment) {
        this.sourceComment = sourceComment;
    }

    public FindRule getFindRule() {
        return this.findRule;
    }

    public void setFindRule(FindRule findRule) {
        this.findRule = findRule;
    }

    public String getSourceType() {
        return this.sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceHeaders() {
        return this.sourceHeaders;
    }

    public void setSourceHeaders(String sourceHeaders) {
        this.sourceHeaders = sourceHeaders;
    }

    public String getLoginUrl() {
        return this.loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLoginCheckJs() {
        return loginCheckJs;
    }

    public void setLoginCheckJs(String loginCheckJs) {
        this.loginCheckJs = loginCheckJs;
    }

    public String getConcurrentRate() {
        return this.concurrentRate;
    }

    public void setConcurrentRate(String concurrentRate) {
        this.concurrentRate = concurrentRate;
    }
}
