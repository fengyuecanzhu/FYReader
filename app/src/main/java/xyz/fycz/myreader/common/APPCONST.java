package xyz.fycz.myreader.common;

import android.os.Environment;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.util.utils.FileUtils;

import java.io.File;


public class APPCONST {

    public static String publicKey = "fyds1.0";//服务端公钥
    public static String privateKey;//app私钥
    public final static String s = "11940364935628058505";
    public static final String KEY = "readerByFengyue";

    public static final String ALARM_SCHEDULE_MSG = "alarm_schedule_msg";

    public static final String FILE_DIR =  Environment.getExternalStorageDirectory() + "/FYReader/";
    public static final String TEM_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/tem/";
    public static final String BACKUP_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/backup/";
    public static final String TXT_BOOK_DIR = Environment.getExternalStorageDirectory() + "/FYReader/noveltxt/";
    public static final String FONT_BOOK_DIR = Environment.getExternalStorageDirectory() + "/FYReader/font/";
    public static final String UPDATE_APK_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/apk/";
    public static final String QQ_DATA_DIR = Environment.getExternalStorageDirectory() + "/tencent/MobileQQ/data/";
    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = FileUtils.getCachePath() + File.separator
            + "book_cache"+ File.separator ;
    public static final String LOADING_ERROR = "\t\t \t\t\t\t\n\n　　章节内容转码失败！\n\n\t\t \t\t\t\n\n";

    public static long exitTime;
    public static final int exitConfirmTime = 2000;

    public static final String BOOK = "book";
    public static final String CHAPTER_PAGE = "chapter_page";
    public static final String SETTING = "setting";
    public static final String SEARCH_BOOK_BEAN = "SearchBookBean";
    public static final String FONT = "font";
    public static final String RESULT_RESET_SCREEN = "result_reset_screen";
    public static final String RESULT_IS_COLLECTED = "result_is_collected";
    public static final String RESULT_LAST_READ_POSITION = "result_last_read_position";
    public static final String RESULT_HISTORY_CHAPTER = "result_history_chapter";


    public static final int[] READ_STYLE_NIGHT = {R.color.sys_night_word, R.color.sys_night_bg};//黑夜
    public static final int[] READ_STYLE_PROTECTED_EYE = {R.color.sys_protect_eye_word, R.color.sys_protect_eye_bg};//护眼
    public static final int[] READ_STYLE_COMMON = {R.color.sys_common_word, R.color.sys_common_bg};//普通
    public static final int[] READ_STYLE_BLUE_DEEP = {R.color.sys_blue_deep_word, R.color.sys_blue_deep_bg};//深蓝
    public static final int[] READ_STYLE_LEATHER = {R.color.sys_leather_word, R.color.sys_leather_bg};//羊皮纸
    public static final int[] READ_STYLE_BREEN_EYE = {R.color.sys_breen_word, R.color.sys_breen_bg};//棕绿色


    public static final String FILE_NAME_SETTING = "setting";
    public static final String FILE_NAME_UPDATE_INFO = "updateInfo";

    public static final int REQUEST_FONT = 1001;
    public static final int REQUEST_CHAPTER_PAGE = 1002;
    public static final int REQUEST_RESET_SCREEN_TIME = 1003;


    public static final int REQUEST_READ = 1;

    public static final CharSequence[] DIALOG_DOWNLOAD = {
            "下载后面五十章", "下载前后五十章",
            "下载后面全部章", "下载本书所有章"
    };

    public static final int APP_INSTALL_CODE = 10086;

    public static final int SELECT_FILE_CODE = 10000;

    public static final int PERMISSIONS_REQUEST_STORAGE = 10001;

    //设置版本号
    public static final int SETTING_VERSION = 3;

    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";

    public final static String channelIdDownload = "channel_download";
}
