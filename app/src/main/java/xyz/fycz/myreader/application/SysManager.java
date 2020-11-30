package xyz.fycz.myreader.application;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.enums.BookcaseStyle;
import xyz.fycz.myreader.enums.Font;
import xyz.fycz.myreader.enums.Language;
import xyz.fycz.myreader.enums.ReadStyle;
import xyz.fycz.myreader.model.backup.UserService;
import xyz.fycz.myreader.model.storage.Backup;
import xyz.fycz.myreader.util.CacheHelper;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.widget.page.PageMode;

import static xyz.fycz.myreader.application.MyApplication.getVersionCode;
import static xyz.fycz.myreader.common.APPCONST.READ_STYLE_LEATHER;
import static xyz.fycz.myreader.common.APPCONST.s;
import static xyz.fycz.myreader.widget.page.PageLoader.DEFAULT_MARGIN_WIDTH;


public class SysManager {

    private static Setting mSetting;

    /**
     * 获取设置
     * @return
     */
    public static Setting getSetting() {
        if (mSetting != null){
            return mSetting;
        }
        mSetting = (Setting) CacheHelper.readObject(APPCONST.FILE_NAME_SETTING);
        if (mSetting == null){
            mSetting = getDefaultSetting();
            saveSetting(mSetting);
        }
        return mSetting;
    }
    public static Setting getNewSetting() {
        Setting setting = (Setting) CacheHelper.readObject(APPCONST.FILE_NAME_SETTING);
        if (setting == null) {
            setting = getDefaultSetting();
            saveSetting(setting);
        }
        return setting;
    }


        /**
         * 保存设置
         * @param setting
         */
    public static void saveSetting(Setting setting) {
        CacheHelper.saveObject(setting, APPCONST.FILE_NAME_SETTING);
    }


    /**
     * 默认设置
     * @return
     */
    private static Setting getDefaultSetting(){
        Setting setting = new Setting();
        setting.setDayStyle(true);
        setting.setBookcaseStyle(BookcaseStyle.listMode);
        setting.setNewestVersionCode(getVersionCode());
        setting.setAutoSyn(false);
        setting.setMatchChapter(true);
        setting.setMatchChapterSuitability(0.7f);
        setting.setCatheGap(150);
        setting.setRefreshWhenStart(true);
        setting.setOpenBookStore(true);
        setting.setSettingVersion(APPCONST.SETTING_VERSION);
        setting.setSourceVersion(APPCONST.SOURCE_VERSION);
        setting.setHorizontalScreen(false);
        setting.initReadStyle();
        setting.setCurReadStyleIndex(1);
        return setting;
    }

    public static void regetmSetting(){
        mSetting = (Setting) CacheHelper.readObject(APPCONST.FILE_NAME_SETTING);
    }


    /**
     * 重置设置
     */

    public static void resetSetting(){
        Setting setting = getSetting();
        setting.initReadStyle();
        setting.setCurReadStyleIndex(1);
        setting.setSharedLayout(true);
        setting.setSettingVersion(APPCONST.SETTING_VERSION);
        saveSetting(setting);
    }

    public static void resetSource(){
        Setting setting = getSetting();
        ReadCrawlerUtil.addReadCrawler(BookSource.miaobi, BookSource.dstq, BookSource.xs7, BookSource.du1du,BookSource.paiotian);
        ReadCrawlerUtil.removeReadCrawler("cangshu99");
        setting.setSourceVersion(APPCONST.SOURCE_VERSION);
        saveSetting(setting);
    }
}
