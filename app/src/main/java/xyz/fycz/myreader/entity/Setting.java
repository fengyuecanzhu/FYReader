package xyz.fycz.myreader.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.enums.BookcaseStyle;
import xyz.fycz.myreader.enums.Font;
import xyz.fycz.myreader.enums.Language;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.ZipUtils;
import xyz.fycz.myreader.util.utils.BitmapUtil;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.MeUtils;
import xyz.fycz.myreader.widget.page.PageMode;

import static xyz.fycz.myreader.common.APPCONST.READ_STYLE_BLUE_DEEP;
import static xyz.fycz.myreader.common.APPCONST.READ_STYLE_BREEN;
import static xyz.fycz.myreader.common.APPCONST.READ_STYLE_COMMON;
import static xyz.fycz.myreader.common.APPCONST.READ_STYLE_LEATHER;
import static xyz.fycz.myreader.common.APPCONST.READ_STYLE_NIGHT;
import static xyz.fycz.myreader.common.APPCONST.READ_STYLE_PROTECTED_EYE;
import static xyz.fycz.myreader.widget.page.PageLoader.DEFAULT_MARGIN_WIDTH;

/**
 * 用户设置
 *
 */

public class Setting implements Serializable {

    private static final long serialVersionUID = 2295691810299441757L;

    /**
     * 共七套布局，5套自带布局(0~4)，一套自定义(5)，一套夜间(6)
     */
    private List<ReadStyle> readStyles;//阅读布局

    private int curReadStyleIndex;//当前阅读布局

    private boolean dayStyle;//是否日间模式

    private BookcaseStyle bookcaseStyle;//书架布局

    private int newestVersionCode;//最新版本号

    private boolean isAutoSyn;//是否自动同步书架

    private boolean isMatchChapter = true;//是否开启智能匹配历史章节

    private float matchChapterSuitability;//匹配度

    private int catheGap = 150;//缓存间隔

    private boolean refreshWhenStart;//打开软件自动更新书籍

    private boolean openBookStore;//是否开启书城

    private boolean sharedLayout;//是否共用布局

    private boolean horizontalScreen;//是否横屏

    private boolean noMenuChTitle;//关闭阅读上边菜单章节标题和链接显示

    private boolean readAloudVolumeTurnPage;//朗读时音量键翻页

    private int searchFilter;//搜索过滤：0-不过滤，1-模糊搜索，2-精确搜索

    private int sortStyle;//排序方式:0-手动排序，1-按时间排序，2-按照书名排序

    private boolean canSelectText;//是否长按选择

    private boolean lightNovelParagraph;//是否自动重分段落

    private int sourceVersion;//书源版本号

    private int settingVersion;//设置版本号

    public ReadStyle getCurReadStyle(){
        //Log.d("curReadStyleIndex", String.valueOf(curReadStyleIndex));
        if (readStyles == null || readStyles.size() == 0) {
            initReadStyle();
        }
        if (!dayStyle){
            return readStyles.get(6);
        }
        return readStyles.get(curReadStyleIndex);
    }

    public Drawable getBgDrawable(int readStyleIndex, Context context, int width, int height) {
        ReadStyle readStyle = readStyles.get(readStyleIndex);
        if (readStyle.bgIsColor()){
            return new ColorDrawable(readStyle.getBgColor());
        }else {
            Bitmap bitmap = null;
            try {
                if (readStyle.bgIsAssert()) {
                    bitmap = MeUtils.getFitAssetsSampleBitmap(context.getAssets(), readStyle.getBgPath(), width, height);
                } else {
                    bitmap = BitmapUtil.getFitSampleBitmap(readStyle.getBgPath(), width, height);
                }
                if (bitmap == null) {
                    bitmap = MeUtils.getFitAssetsSampleBitmap(context.getAssets(), "bg/p01.jpg", width, height);
                }
            }catch (Exception e){
                bitmap = MeUtils.getFitAssetsSampleBitmap(context.getAssets(), "bg/p01.jpg", width, height);
            }
            return new BitmapDrawable(context.getResources(), bitmap);
        }
    }

    public void initReadStyle(){
        readStyles = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            ReadStyle readStyle = new ReadStyle();
            String textColor;
            String bgColor;
            switch(i){
                case 0:
                    textColor = READ_STYLE_COMMON[0];
                    bgColor = READ_STYLE_COMMON[1];
                    break;
                case 1:
                default:
                    textColor = READ_STYLE_LEATHER[0];
                    bgColor = READ_STYLE_LEATHER[1];
                    break;
                case 2:
                    textColor = READ_STYLE_PROTECTED_EYE[0];
                    bgColor = READ_STYLE_PROTECTED_EYE[1];
                    break;
                case 3:
                    textColor = READ_STYLE_BREEN[0];
                    bgColor = READ_STYLE_BREEN[1];
                    break;
                case 4:
                    textColor = READ_STYLE_BLUE_DEEP[0];
                    bgColor = READ_STYLE_BLUE_DEEP[1];
                    break;
                case 6:
                    textColor = READ_STYLE_NIGHT[0];
                    bgColor = READ_STYLE_NIGHT[1];
                    break;
            }
            readStyle.setTextColor(Color.parseColor(textColor));
            readStyle.setBgColor(Color.parseColor(bgColor));
            readStyle.setReadWordSize(20);
            readStyle.setBrightProgress(50);
            readStyle.setBrightFollowSystem(true);
            readStyle.setLanguage(Language.normal);
            readStyle.setFont(Font.默认字体);
            readStyle.setLocalFontName("");
            readStyle.setAutoScrollSpeed(60);
            readStyle.setPageMode(PageMode.COVER);
            readStyle.setVolumeTurnPage(true);
            readStyle.setResetScreen(3);
            readStyle.setShowStatusBar(false);
            readStyle.setAlwaysNext(false);
            readStyle.setIntent(2);
            readStyle.setLineMultiplier(1);
            readStyle.setParagraphSize(0.9f);
            readStyle.setTextLetterSpacing(0);
            readStyle.setPaddingLeft(DEFAULT_MARGIN_WIDTH);
            readStyle.setPaddingRight(DEFAULT_MARGIN_WIDTH);
            readStyle.setPaddingTop(0);
            readStyle.setPaddingBottom(0);
            readStyle.setTightCom(false);
            readStyle.setBgIsColor(true);
            readStyle.setBgIsAssert(true);
            readStyle.setBgPath("");
            readStyle.setBlueFilterPercent(30);
            readStyle.setProtectEye(false);
            readStyle.setEnType(false);
            readStyle.setComposition(1);

            readStyles.add(readStyle);
        }
    }

    public void resetLayout(){
        for (int i = 0; i < 7; i++) {
            ReadStyle readStyle = readStyles.get(i);
            String textColor;
            String bgColor;
            switch(i){
                case 0:
                    textColor = READ_STYLE_COMMON[0];
                    bgColor = READ_STYLE_COMMON[1];
                    break;
                case 1:
                default:
                    textColor = READ_STYLE_LEATHER[0];
                    bgColor = READ_STYLE_LEATHER[1];
                    break;
                case 2:
                    textColor = READ_STYLE_PROTECTED_EYE[0];
                    bgColor = READ_STYLE_PROTECTED_EYE[1];
                    break;
                case 3:
                    textColor = READ_STYLE_BREEN[0];
                    bgColor = READ_STYLE_BREEN[1];
                    break;
                case 4:
                    textColor = READ_STYLE_BLUE_DEEP[0];
                    bgColor = READ_STYLE_BLUE_DEEP[1];
                    break;
                case 6:
                    textColor = READ_STYLE_NIGHT[0];
                    bgColor = READ_STYLE_NIGHT[1];
                    break;
            }
            readStyle.setTextColor(Color.parseColor(textColor));
            readStyle.setBgColor(Color.parseColor(bgColor));
            readStyle.setBgIsColor(true);
            readStyle.setBgIsAssert(true);
            readStyle.setBgPath("");
        }
    }

    public void sharedLayout(){
        if (sharedLayout) {
            for (int i = 0; i < 7; i++) {
                if (curReadStyleIndex == i) continue;
                ReadStyle newReadStyle = (ReadStyle) getCurReadStyle().clone();
                ReadStyle oldReadStyle = readStyles.get(i);
                newReadStyle.setTextColor(oldReadStyle.getTextColor());
                newReadStyle.setBgColor(oldReadStyle.getBgColor());
                newReadStyle.setBgIsAssert(oldReadStyle.bgIsAssert());
                newReadStyle.setBgIsColor(oldReadStyle.bgIsColor());
                newReadStyle.setBgPath(oldReadStyle.getBgPath());
                readStyles.set(i, newReadStyle);
            }
        }
    }

    public void saveLayout(int index){
        ReadStyle newReadStyle = (ReadStyle) getCurReadStyle().clone();
        readStyles.set(index, newReadStyle);
    }

    public boolean exportLayout(int index){
        ReadStyle readStyle = readStyles.get(index);
        String json = GsonExtensionsKt.getGSON().toJson(readStyle);
        List<String> filesPath = new ArrayList<>();
        filesPath.add(APPCONST.TEM_FILE_DIR + "readConfig.fyl");
        filesPath.add(APPCONST.TEM_FILE_DIR + "bg.fyl");
        if (!FileUtils.writeFile(json.getBytes(),
                FileUtils.getFile(filesPath.get(0)))){
            return false;
        }
        if (!readStyle.bgIsColor() && !readStyle.bgIsAssert()){
            if (!FileUtils.copy(readStyle.getBgPath(), filesPath.get(1))){
                return false;
            }
        }
        try {
            ZipUtils.zipFiles(filesPath, APPCONST.FILE_DIR + "readConfig.zip", "风月读书布局导出配置");
            FileUtils.deleteFile(filesPath.get(0));
            FileUtils.deleteFile(filesPath.get(1));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void importLayout(int index, ReadStyle readStyle){
        readStyles.set(index, readStyle);
    }

    public int getAutoScrollSpeed() {
        return getCurReadStyle().getAutoScrollSpeed();
    }

    public void setAutoScrollSpeed(int autoScrollSpeed) {
        getCurReadStyle().setAutoScrollSpeed(autoScrollSpeed);
        sharedLayout();
    }

    public Font getFont() {
        return getCurReadStyle().getFont();
    }

    public void setFont(Font font) {
        getCurReadStyle().setFont(font);
        sharedLayout();
    }

    public Language getLanguage() {
        return getCurReadStyle().getLanguage();
    }

    public void setLanguage(Language language) {
        getCurReadStyle().setLanguage(language);
        sharedLayout();
    }

    public boolean isBrightFollowSystem() {
        return getCurReadStyle().isBrightFollowSystem();
    }

    public void setBrightFollowSystem(boolean brightFollowSystem) {
        getCurReadStyle().setBrightFollowSystem(brightFollowSystem);
        sharedLayout();
    }

    public void setBrightProgress(int brightProgress) {
        getCurReadStyle().setBrightProgress(brightProgress);
        sharedLayout();
    }

    public int getBrightProgress() {
        return getCurReadStyle().getBrightProgress();
    }

    public boolean isDayStyle() {
        return dayStyle;
    }

    public void setDayStyle(boolean dayStyle) {
        this.dayStyle = dayStyle;
    }

    public int getReadWordSize() {
        return getCurReadStyle().getReadWordSize();
    }

    public void setReadWordSize(int readWordSize) {
        getCurReadStyle().setReadWordSize(readWordSize);
        sharedLayout();
    }

    public PageMode getPageMode() {
        return getCurReadStyle().getPageMode();
    }

    public void setPageMode(PageMode pageMode) {
        getCurReadStyle().setPageMode(pageMode);
        sharedLayout();
    }

    public boolean isVolumeTurnPage() {
        return getCurReadStyle().isVolumeTurnPage();
    }

    public void setVolumeTurnPage(boolean volumeTurnPage) {
        getCurReadStyle().setVolumeTurnPage(volumeTurnPage);
        sharedLayout();
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
        return getCurReadStyle().getLocalFontName();
    }

    public void setLocalFontName(String localFontName) {
        getCurReadStyle().setLocalFontName(localFontName);
        sharedLayout();
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

    public int getResetScreen() {
        return getCurReadStyle().getResetScreen();
    }

    public void setResetScreen(int resetScreen) {
        getCurReadStyle().setResetScreen(resetScreen);
        sharedLayout();
    }

    public boolean isMatchChapter() {
        return isMatchChapter;
    }

    public void setMatchChapter(boolean matchChapter) {
        isMatchChapter = matchChapter;
    }

    public float getMatchChapterSuitability() {
        return matchChapterSuitability;
    }

    public void setMatchChapterSuitability(float matchChapterSuitability) {
        this.matchChapterSuitability = matchChapterSuitability;
    }

    public int getCatheGap() {
        return catheGap;
    }

    public void setCatheGap(int catheGap) {
        this.catheGap = catheGap;
    }

    public boolean isRefreshWhenStart() {
        return refreshWhenStart;
    }

    public void setRefreshWhenStart(boolean refreshWhenStart) {
        this.refreshWhenStart = refreshWhenStart;
    }

    public boolean isShowStatusBar() {
        return getCurReadStyle().isShowStatusBar();
    }

    public void setShowStatusBar(boolean showStatusBar) {
        getCurReadStyle().setShowStatusBar(showStatusBar);
        sharedLayout();
    }

    public boolean isAlwaysNext() {
        return getCurReadStyle().isAlwaysNext();
    }

    public void setAlwaysNext(boolean alwaysNext) {
        getCurReadStyle().setAlwaysNext(alwaysNext);
        sharedLayout();
    }

    public boolean isOpenBookStore() {
        return openBookStore;
    }

    public void setOpenBookStore(boolean openBookStore) {
        this.openBookStore = openBookStore;
    }

    public boolean isHorizontalScreen() {
        return horizontalScreen;
    }

    public void setHorizontalScreen(boolean horizontalScreen) {
        this.horizontalScreen = horizontalScreen;
    }

    public void setIntent(int intent) {
        getCurReadStyle().setIntent(intent);
        sharedLayout();
    }

    public void setLineMultiplier(float lineMultiplier) {
        getCurReadStyle().setLineMultiplier(lineMultiplier);
        sharedLayout();
    }

    public void setParagraphSize(float paragraphSize) {
        getCurReadStyle().setParagraphSize(paragraphSize);
        sharedLayout();
    }

    public int getIntent() {
        return getCurReadStyle().getIntent();
    }

    public float getLineMultiplier() {
        return getCurReadStyle().getLineMultiplier();
    }

    public float getParagraphSize() {
        return getCurReadStyle().getParagraphSize();
    }

    public float getTextLetterSpacing() {
        return getCurReadStyle().getTextLetterSpacing();
    }

    public void setTextLetterSpacing(float textLetterSpacing) {
        getCurReadStyle().setTextLetterSpacing(textLetterSpacing);
        sharedLayout();
    }

    public int getComposition() {
        return getCurReadStyle().getComposition();
    }

    public void setComposition(int composition) {
        getCurReadStyle().setComposition(composition);
        sharedLayout();
    }

    public int getPaddingLeft() {
        return getCurReadStyle().getPaddingLeft();
    }

    public void setPaddingLeft(int paddingLeft) {
        getCurReadStyle().setPaddingLeft(paddingLeft);
        sharedLayout();
    }

    public int getPaddingTop() {
        return getCurReadStyle().getPaddingTop();
    }

    public void setPaddingTop(int paddingTop) {
        getCurReadStyle().setPaddingTop(paddingTop);
        sharedLayout();
    }

    public int getPaddingRight() {
        return getCurReadStyle().getPaddingRight();
    }

    public void setPaddingRight(int paddingRight) {
        getCurReadStyle().setPaddingRight(paddingRight);
        sharedLayout();
    }

    public int getPaddingBottom() {
        return getCurReadStyle().getPaddingBottom();
    }

    public void setPaddingBottom(int paddingBottom) {
        getCurReadStyle().setPaddingBottom(paddingBottom);
        sharedLayout();
    }

    public boolean isTightCom() {
        return getCurReadStyle().isTightCom();
    }

    public void setTightCom(boolean tightCom) {
        getCurReadStyle().setTightCom(tightCom);
        sharedLayout();
    }

    public boolean bgIsColor() {
        if (StringHelper.isEmpty(getCurReadStyle().getBgPath())) {
            getCurReadStyle().setBgIsColor(true);
        }
        return getCurReadStyle().bgIsColor();
    }

    public void setBgIsColor(boolean bgIsColor) {
        getCurReadStyle().setBgIsColor(bgIsColor);
    }

    public boolean bgIsAssert() {
        return getCurReadStyle().bgIsAssert();
    }

    public void setBgIsAssert(boolean bgIsAssert) {
        getCurReadStyle().setBgIsAssert(bgIsAssert);
    }

    public int getTextColor() {
        if (getCurReadStyle().getTextColor() == 0) {
            getCurReadStyle().setTextColor(Color.parseColor(READ_STYLE_LEATHER[0]));
        }
        return getCurReadStyle().getTextColor();
    }

    public void setTextColor(int textColor) {
        getCurReadStyle().setTextColor(textColor);
    }

    public int getBgColor() {
        if (getCurReadStyle().getBgColor() == 0) {
            getCurReadStyle().setBgColor(Color.parseColor(READ_STYLE_LEATHER[1]));
        }
        return getCurReadStyle().getBgColor();
    }

    public void setBgColor(int bgColor) {
        getCurReadStyle().setBgColor(bgColor);
    }

    public String getBgPath() {
        return getCurReadStyle().getBgPath();
    }

    public void setBgPath(String bgPath) {
        getCurReadStyle().setBgPath(bgPath);
    }

    public int getCurReadStyleIndex() {
        return curReadStyleIndex;
    }

    public void setCurReadStyleIndex(int curReadStyleIndex) {
        this.curReadStyleIndex = curReadStyleIndex;
    }

    public boolean isSharedLayout() {
        return sharedLayout;
    }

    public void setSharedLayout(boolean sharedLayout) {
        this.sharedLayout = sharedLayout;
    }

    public boolean isNoMenuChTitle() {
        return noMenuChTitle;
    }

    public boolean isReadAloudVolumeTurnPage() {
        return readAloudVolumeTurnPage;
    }

    public void setReadAloudVolumeTurnPage(boolean readAloudVolumeTurnPage) {
        this.readAloudVolumeTurnPage = readAloudVolumeTurnPage;
    }

    public void setNoMenuChTitle(boolean noMenuChTitle) {
        this.noMenuChTitle = noMenuChTitle;
    }

    public boolean isProtectEye() {
        return getCurReadStyle().isProtectEye();
    }

    public void setProtectEye(boolean protectEye) {
        getCurReadStyle().setProtectEye(protectEye);
    }

    public int getBlueFilterPercent() {
        if (getCurReadStyle().getBlueFilterPercent() == 0) getCurReadStyle().setBlueFilterPercent(30);
        return getCurReadStyle().getBlueFilterPercent();
    }

    public void setBlueFilterPercent(int blueFilterPercent) {
        getCurReadStyle().setBlueFilterPercent(blueFilterPercent);
    }

    public int getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(int sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    public List<ReadStyle> getReadStyles() {
        return readStyles;
    }

    public void setReadStyles(List<ReadStyle> readStyles) {
        this.readStyles = readStyles;
    }

    public int getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(int searchFilter) {
        this.searchFilter = searchFilter;
    }

    public int getSortStyle() {
        return sortStyle;
    }

    public void setSortStyle(int sortStyle) {
        this.sortStyle = sortStyle;
    }

    public boolean isCanSelectText() {
        return canSelectText;
    }

    public void setCanSelectText(boolean canSelectText) {
        this.canSelectText = canSelectText;
    }

    public boolean isLightNovelParagraph() {
        return lightNovelParagraph;
    }

    public void setLightNovelParagraph(boolean lightNovelParagraph) {
        this.lightNovelParagraph = lightNovelParagraph;
    }
    public boolean isEnType() {
        return getCurReadStyle().isEnType();
    }

    public void setEnType(boolean enType) {
        getCurReadStyle().setEnType(enType);
    }
}
