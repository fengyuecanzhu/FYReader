package xyz.fycz.myreader.common;

import android.os.Environment;

import com.google.gson.reflect.TypeToken;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.util.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


public class APPCONST {

    public static String publicKey = "fyds2.0";//服务端公钥
    public static final String KEY = "readerByFengyue";

    public static final String[] STORAGE_PERMISSIONS = new String[]{Permission.WRITE_EXTERNAL_STORAGE,
            Permission.READ_EXTERNAL_STORAGE};

    public static final String FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/";
    public static final String SHARE_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/share/";
    public static String LOG_DIR = FileUtils.getCachePath() + "/log/";
    public static String COVER_DIR = FileUtils.getCachePath() + "/covers/";
    public static final String BG_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/bg/";
    public static String TEM_FILE_DIR = FileUtils.getCachePath() + "/tem/";
    public static final String BACKUP_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/backup/";
    public static final String TXT_BOOK_DIR = Environment.getExternalStorageDirectory() + "/FYReader/noveltxt/";
    public static final String FONT_BOOK_DIR = Environment.getExternalStorageDirectory() + "/FYReader/font/";
    public static final String UPDATE_APK_FILE_DIR = Environment.getExternalStorageDirectory() + "/FYReader/apk/";
    public static final String QQ_DATA_DIR = Environment.getExternalStorageDirectory() + "/tencent/MobileQQ/data/";
    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = FileUtils.getCachePath() + File.separator
            + "book_cache" + File.separator;
    public static String HTML_CACHE_PATH = FileUtils.getCachePath() + File.separator
            + "html_cache" + File.separator;

    public static long exitTime;
    public static final int exitConfirmTime = 2000;

    public static final String MATCHER = "Matcher";
    public static final String XPATH = "Xpath";
    public static final String JSON_PATH = "JsonPath";
    public static final String THIRD_SOURCE = "ThirdSource";

    public static final String DATA_KEY = "data_key";
    public static final String FIND_CRAWLER = "findCrawler";
    public static final String CHAPTER_PAGE = "chapter_page";
    public static final String SOURCE_INDEX = "sourceIndex";
    public static final String FONT = "font";
    public static final String BOOK_SOURCE = "bookSource";
    public static final String WEB_DAV = "WebDav";
    public static final String RESULT_NEED_REFRESH = "result_need_refresh";
    public static final String RESULT_IS_COLLECTED = "result_is_collected";
    public static final String RESULT_LAST_READ_POSITION = "result_last_read_position";
    public static final String RESULT_HISTORY_CHAPTER = "result_history_chapter";
    public static final String RESULT_UP_MENU = "result_up_menu";
    public static final String SEARCH_KEY = "searchKey";
    public static final String BOOK_KEY = "bookKey";
    public static final String CHAPTERS_KEY = "chaptersKey";
    public static final String PAGE_LOADER_KEY = "pageLoaderKey";


    public static final String[] READ_STYLE_NIGHT = {"#94928c", "#393431"};//黑夜
    public static final String[] READ_STYLE_PROTECTED_EYE = {"#313031", "#E1F1DA"};//护眼
    public static final String[] READ_STYLE_COMMON = {"#313031", "#f5f4f0"};//普通
    public static final String[] READ_STYLE_BLUE_DEEP = {"#637079", "#001c29"};//深蓝
    public static final String[] READ_STYLE_LEATHER = {"#313031", "#CEC29C"};//羊皮纸
    public static final String[] READ_STYLE_BREEN = {"#313031", "#b5bd9a"};//棕绿色


    public static final String FILE_NAME_SETTING = "setting";
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
    public static final int REQUEST_IMPORT_REPLACE_RULE = 1008;
    public static final int REQUEST_IMPORT_BOOK_SOURCE = 1009;
    public static final int REQUEST_EDIT_BOOK_SOURCE = 1010;
    public static final int REQUEST_BOOK_SOURCE = 1011;
    public static final int REQUEST_SELECT_COVER = 1012;
    public static final int REQUEST_EDIT_BOOK = 1013;
    public static final int REQUEST_GROUP_MANAGER = 1014;
    public static final int REQUEST_SEARCH_WORD = 1015;
    public static final int REQUEST_AUTH_EMAIL = 1016;

    public static final int REQUEST_READ = 1;

    public static final int APP_INSTALL_CODE = 10086;

    public static final int SELECT_FILE_CODE = 10000;

    //设置版本号
    public static final int SETTING_VERSION = 11;

    public static final int SOURCE_VERSION = 7;

    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";

    public final static String channelIdDownload = "channel_download";

    public final static String channelIdRead = "channel_read_aloud";

    public static final String DEFAULT_WEB_DAV_URL = "https://dav.jianguoyun.com/dav/";


    public static Type MAP_STRING = new TypeToken<Map<String, String>>() {
    }.getType();

    public static final Pattern JS_PATTERN = Pattern.compile("(<js>[\\w\\W]*?</js>|@js:[\\w\\W]*$)", Pattern.CASE_INSENSITIVE);
    public static final Pattern EXP_PATTERN = Pattern.compile("\\{\\{([\\w\\W]*?)\\}\\}");
//    public static final Pattern IMG_PATTERN = Pattern.compile("<img .*?src.*?=.*?\"(.*?(?:,\\{.*\\})?)\".*?>", Pattern.CASE_INSENSITIVE);

    public static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");

    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4168.3 Safari/537.36";

    public static final String  UA_NAME = "User-Agent";
}
