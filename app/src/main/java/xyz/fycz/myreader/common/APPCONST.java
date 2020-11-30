package xyz.fycz.myreader.common;

import android.os.Environment;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.util.utils.FileUtils;

import java.io.File;


public class APPCONST {

    public static String publicKey = "";//服务端公钥
    public static String privateKey;//app私钥
    public final static String s = "11940364935628058505";
    public static final String KEY = "";

    public static final String ALARM_SCHEDULE_MSG = "alarm_schedule_msg";

    public static final String FILE_DIR =  Environment.getExternalStorageDirectory() + "/FYReader/";
    public static final String SHARE_FILE_DIR =  Environment.getExternalStorageDirectory() + "/FYReader/share/";
    public static final String LOG_DIR =  Environment.getExternalStorageDirectory() + "/FYReader/log/";
    public static final String BG_FILE_DIR =  Environment.getExternalStorageDirectory() + "/FYReader/bg/";
    public static final String TEM_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/tem/";
    public static final String BACKUP_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/backup/";
    public static final String TXT_BOOK_DIR = Environment.getExternalStorageDirectory() + "/FYReader/noveltxt/";
    public static final String FONT_BOOK_DIR = Environment.getExternalStorageDirectory() + "/FYReader/font/";
    public static final String UPDATE_APK_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/apk/";
    public static final String QQ_DATA_DIR = Environment.getExternalStorageDirectory() + "/tencent/MobileQQ/data/";
    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = FileUtils.getCachePath() + File.separator
            + "book_cache"+ File.separator ;
    public static String HTML_CACHE_PATH = FileUtils.getCachePath() + File.separator
            + "html_cache"+ File.separator ;
    public static final String LOADING_ERROR = "\t\t \t\t\t\t\n\n　　章节内容转码失败！\n\n\t\t \t\t\t\n\n";

    public static long exitTime;
    public static final int exitConfirmTime = 2000;

    public static final String BOOK = "book";
    public static final String TITLE = "title";
    public static final String FIND_CRAWLER = "findCrawler";
    public static final String CHAPTER_PAGE = "chapter_page";
    public static final String SETTING = "setting";
    public static final String SEARCH_BOOK_BEAN = "SearchBookBean";
    public static final String SOURCE_INDEX = "sourceIndex";
    public static final String FONT = "font";
    public static final String RESULT_NEED_REFRESH = "result_need_refresh";
    public static final String RESULT_IS_COLLECTED = "result_is_collected";
    public static final String RESULT_LAST_READ_POSITION = "result_last_read_position";
    public static final String RESULT_HISTORY_CHAPTER = "result_history_chapter";
    public static final String RESULT_UP_MENU = "result_up_meu";


    public static final String[] READ_STYLE_NIGHT = {"#94928c", "#393431"};//黑夜
    public static final String[] READ_STYLE_PROTECTED_EYE = {"#313031", "#E1F1DA"};//护眼
    public static final String[] READ_STYLE_COMMON = {"#313031", "#f5f4f0"};//普通
    public static final String[] READ_STYLE_BLUE_DEEP = {"#637079", "#001c29"};//深蓝
    public static final String[] READ_STYLE_LEATHER = {"#313031", "#CEC29C"};//羊皮纸
    public static final String[] READ_STYLE_BREEN = {"#313031", "#b5bd9a"};//棕绿色


    public static final String FILE_NAME_SETTING = "setting";
    public static final String FILE_NAME_UPDATE_INFO = "updateInfo";
    public static final String FILE_NAME_SPLASH_IMAGE = "splashImage";

    public static final int SELECT_TEXT_COLOR = 201;
    public static final int SELECT_BG_COLOR = 301;

    public static final int REQUEST_FONT = 1001;
    public static final int REQUEST_CHAPTER_PAGE = 1002;
    public static final int REQUEST_LOGIN = 1003;
    public static final int REQUEST_REFRESH_READ_UI = 1004;
    public static final int REQUEST_SELECT_BG = 1005;
    public static final int REQUEST_IMPORT_LAYOUT = 1006;
    public static final int REQUEST_QR_SCAN = 1007;


    public static final int REQUEST_READ = 1;

    public static final CharSequence[] DIALOG_DOWNLOAD = {
            "下载后面五十章", "下载前后五十章",
            "下载后面全部章", "下载本书所有章"
    };

    public static final int APP_INSTALL_CODE = 10086;

    public static final int SELECT_FILE_CODE = 10000;

    public static final int PERMISSIONS_REQUEST_STORAGE = 10001;

    //设置版本号
    public static final int SETTING_VERSION = 11;

    public static final int SOURCE_VERSION = 1;

    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";

    public final static String channelIdDownload = "channel_download";

    public final static String channelIdRead = "channel_read_aloud";

    public static final String DEFAULT_WEB_DAV_URL = "https://dav.jianguoyun.com/dav/";

}
