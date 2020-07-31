package xyz.fycz.myreader.entity;

import xyz.fycz.myreader.enums.BookcaseStyle;
import xyz.fycz.myreader.enums.Font;
import xyz.fycz.myreader.enums.Language;
import xyz.fycz.myreader.enums.ReadStyle;
import xyz.fycz.myreader.widget.page.PageMode;

import java.io.Serializable;

/**
 * 用户设置
 *
 */

public class Setting implements Serializable {

    private static final long serialVersionUID = 2295691810299441757L;

    private int readWordColor;//阅读字体颜色
    private int readBgColor;//阅读背景颜色
    private float readWordSize;//阅读字体大小

    private ReadStyle readStyle;//阅读模式

    private boolean dayStyle;//是否日间模式
    private int brightProgress;//亮度 1- 100
    private boolean brightFollowSystem;//亮度跟随系统
    private Language language;//简繁体
    private Font font;//字体

    private int autoScrollSpeed = 5;//自动滑屏速度

    private PageMode pageMode;//翻页模式

    private boolean isVolumeTurnPage;//是否开启音量键翻页

    private BookcaseStyle bookcaseStyle;//书架布局

    private int newestVersionCode;//最新版本号

    private String localFontName;//本地字体名字

    private boolean isAutoSyn;//是否自动同步书架

    private int settingVersion;//设置版本号

    public int getAutoScrollSpeed() {
        return autoScrollSpeed;
    }

    public void setAutoScrollSpeed(int autoScrollSpeed) {
        this.autoScrollSpeed = autoScrollSpeed;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isBrightFollowSystem() {
        return brightFollowSystem;
    }

    public void setBrightFollowSystem(boolean brightFollowSystem) {
        this.brightFollowSystem = brightFollowSystem;
    }

    public void setBrightProgress(int brightProgress) {
        this.brightProgress = brightProgress;
    }

    public int getBrightProgress() {
        return brightProgress;
    }

    public boolean isDayStyle() {
        return dayStyle;
    }

    public void setDayStyle(boolean dayStyle) {
        this.dayStyle = dayStyle;
    }

    public int getReadWordColor() {
        return readWordColor;
    }

    public void setReadWordColor(int readWordColor) {
        this.readWordColor = readWordColor;
    }

    public int getReadBgColor() {
        return readBgColor;
    }

    public void setReadBgColor(int readBgColor) {
        this.readBgColor = readBgColor;
    }

    public float getReadWordSize() {
        return readWordSize;
    }

    public void setReadWordSize(float readWordSize) {
        this.readWordSize = readWordSize;
    }

    public ReadStyle getReadStyle() {
        return readStyle;
    }

    public void setReadStyle(ReadStyle readStyle) {
        this.readStyle = readStyle;
    }

    public PageMode getPageMode() {
        return pageMode;
    }

    public void setPageMode(PageMode pageMode) {
        this.pageMode = pageMode;
    }

    public boolean isVolumeTurnPage() {
        return isVolumeTurnPage;
    }

    public void setVolumeTurnPage(boolean volumeTurnPage) {
        isVolumeTurnPage = volumeTurnPage;
    }

    public BookcaseStyle getBookcaseStyle() {
        return bookcaseStyle;
    }

    public void setBookcaseStyle(BookcaseStyle bookcaseStyle) {
        this.bookcaseStyle = bookcaseStyle;
    }

    public int getNewestVersionCode() {
        return newestVersionCode;
    }

    public void setNewestVersionCode(int newestVersionCode) {
        this.newestVersionCode = newestVersionCode;
    }

    public String getLocalFontName() {
        return localFontName;
    }

    public void setLocalFontName(String localFontName) {
        this.localFontName = localFontName;
    }

    public boolean isAutoSyn() {
        return isAutoSyn;
    }

    public void setAutoSyn(boolean autoSyn) {
        isAutoSyn = autoSyn;
    }

    public int getSettingVersion() {
        return settingVersion;
    }

    public void setSettingVersion(int settingVersion) {
        this.settingVersion = settingVersion;
    }
}
