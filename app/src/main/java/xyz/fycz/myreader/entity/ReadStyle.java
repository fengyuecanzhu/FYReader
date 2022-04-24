/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.entity;

import com.google.gson.Gson;

import java.io.Serializable;

import xyz.fycz.myreader.enums.Font;
import xyz.fycz.myreader.enums.Language;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.widget.page.PageMode;

/**
 * 阅读布局
 */

public class ReadStyle implements Serializable {

    private static final long serialVersionUID = 2295691803219441757L;

    private int readWordSize;//阅读字体大小

    private int brightProgress;//亮度 1- 100

    private boolean brightFollowSystem;//亮度跟随系统

    private Language language;//简繁体

    private Font font;//字体

    private int autoScrollSpeed;//自动滑屏速度

    private PageMode pageMode;//翻页模式

    private boolean isVolumeTurnPage = true;//是否开启音量键翻页

    private int resetScreen = 3;//息屏时间(单位：min，0是不息屏)

    private String localFontName;//本地字体名字

    private boolean isShowStatusBar;//是否显示状态栏

    private boolean alwaysNext;//是否总是翻到下一页

    private int intent;//缩进字符

    private float lineMultiplier;//行间距

    private float paragraphSize;//段间距

    private float textLetterSpacing;//字间距

    private int paddingLeft;//左边距
    private int paddingTop;//上边距
    private int paddingRight;//右边距
    private int paddingBottom;//下边距

    private int composition;//排版选择

    private boolean tightCom;//是否紧凑排版

    private boolean bgIsColor;//背景是否为颜色 不能共用

    private boolean bgIsAssert;//背景是否为assert文件 不能共用

    private int textColor;//文字颜色 不能共用

    private int bgColor;//背景颜色 不能共用

    private String bgPath;//背景图片地址 不能共用

    private boolean protectEye;//开启护眼模式

    private int blueFilterPercent;//蓝光过滤比率

    private boolean enType;//英文排版

    @Override
    public Object clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, ReadStyle.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public int getReadWordSize() {
        return readWordSize;
    }

    public void setReadWordSize(int readWordSize) {
        this.readWordSize = readWordSize;
    }

    public int getBrightProgress() {
        return brightProgress;
    }

    public void setBrightProgress(int brightProgress) {
        this.brightProgress = brightProgress;
    }

    public boolean isBrightFollowSystem() {
        return brightFollowSystem;
    }

    public void setBrightFollowSystem(boolean brightFollowSystem) {
        this.brightFollowSystem = brightFollowSystem;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public int getAutoScrollSpeed() {
        return autoScrollSpeed;
    }

    public void setAutoScrollSpeed(int autoScrollSpeed) {
        this.autoScrollSpeed = autoScrollSpeed;
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

    public int getResetScreen() {
        return resetScreen;
    }

    public void setResetScreen(int resetScreen) {
        this.resetScreen = resetScreen;
    }

    public String getLocalFontName() {
        return localFontName;
    }

    public void setLocalFontName(String localFontName) {
        this.localFontName = localFontName;
    }

    public boolean isShowStatusBar() {
        return isShowStatusBar;
    }

    public void setShowStatusBar(boolean showStatusBar) {
        isShowStatusBar = showStatusBar;
    }

    public boolean isAlwaysNext() {
        return alwaysNext;
    }

    public void setAlwaysNext(boolean alwaysNext) {
        this.alwaysNext = alwaysNext;
    }

    public int getIntent() {
        return intent;
    }

    public void setIntent(int intent) {
        this.intent = intent;
    }

    public float getLineMultiplier() {
        return lineMultiplier;
    }

    public void setLineMultiplier(float lineMultiplier) {
        this.lineMultiplier = lineMultiplier;
    }

    public float getParagraphSize() {
        return paragraphSize;
    }

    public void setParagraphSize(float paragraphSize) {
        this.paragraphSize = paragraphSize;
    }

    public float getTextLetterSpacing() {
        return textLetterSpacing;
    }

    public void setTextLetterSpacing(float textLetterSpacing) {
        this.textLetterSpacing = textLetterSpacing;
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public int getComposition() {
        return composition;
    }

    public void setComposition(int composition) {
        this.composition = composition;
    }

    public boolean isTightCom() {
        return tightCom;
    }

    public void setTightCom(boolean tightCom) {
        this.tightCom = tightCom;
    }

    public boolean bgIsColor() {
        return bgIsColor;
    }

    public void setBgIsColor(boolean bgIsColor) {
        this.bgIsColor = bgIsColor;
    }

    public boolean bgIsAssert() {
        return bgIsAssert;
    }

    public void setBgIsAssert(boolean bgIsAssert) {
        this.bgIsAssert = bgIsAssert;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public String getBgPath() {
        return bgPath;
    }

    public void setBgPath(String bgPath) {
        this.bgPath = bgPath;
    }

    public boolean isProtectEye() {
        return protectEye;
    }

    public void setProtectEye(boolean protectEye) {
        this.protectEye = protectEye;
    }

    public int getBlueFilterPercent() {
        return blueFilterPercent;
    }

    public void setBlueFilterPercent(int blueFilterPercent) {
        this.blueFilterPercent = blueFilterPercent;
    }

    public boolean isEnType() {
        return enType;
    }

    public void setEnType(boolean enType) {
        this.enType = enType;
    }
}
