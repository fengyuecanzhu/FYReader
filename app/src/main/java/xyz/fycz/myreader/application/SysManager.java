package xyz.fycz.myreader.application;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.enums.BookcaseStyle;
import xyz.fycz.myreader.enums.Font;
import xyz.fycz.myreader.enums.Language;
import xyz.fycz.myreader.enums.ReadStyle;
import xyz.fycz.myreader.util.CacheHelper;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.widget.page.PageMode;

import static xyz.fycz.myreader.application.MyApplication.getVersionCode;


public class SysManager {

    public static void logout() {

    }

    /**
     * 获取设置
     * @return
     */
    public static Setting getSetting() {
        Setting setting = (Setting) CacheHelper.readObject(APPCONST.FILE_NAME_SETTING);
        if (setting == null){
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
        setting.setReadBgColor(R.color.sys_protect_eye_bg);
        setting.setReadStyle(ReadStyle.protectedEye);
        setting.setReadWordSize(25);
        setting.setReadWordColor(R.color.sys_protect_eye_word);
        setting.setBrightProgress(50);
        setting.setBrightFollowSystem(true);
        setting.setLanguage(Language.simplified);
        setting.setFont(Font.默认字体);
        setting.setAutoScrollSpeed(300);
        setting.setPageMode(PageMode.SIMULATION);
        setting.setVolumeTurnPage(false);
        setting.setBookcaseStyle(BookcaseStyle.listMode);
        setting.setNewestVersionCode(getVersionCode());
        setting.setLocalFontName("");
        setting.setAutoSyn(false);
        setting.setSettingVersion(APPCONST.SETTING_VERSION);
        return setting;
    }


    /**
     * 重置设置
     */

    public static void resetSetting(){
        saveSetting(getDefaultSetting());
    }
}
