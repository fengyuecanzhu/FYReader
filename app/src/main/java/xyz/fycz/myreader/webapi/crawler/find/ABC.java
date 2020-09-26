package xyz.fycz.myreader.webapi.crawler.find;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.*;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.entity.bookstore.QDBook;
import xyz.fycz.myreader.entity.bookstore.RankBook;
import xyz.fycz.myreader.entity.bookstore.SortBook;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.CommonApi;
import xyz.fycz.myreader.webapi.crawler.FindCrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author fengyue
 * @date 2020/9/16 22:01
 */
public class ABC extends FindCrawler {
    private String rankUrl = "";
    private String sortUrl = "";
    private String[] sex = {"male", "female"};
    private String aParam = "";
    private String imgUrl = "";
    private String defaultCookie = "";
    private String yearmonthFormat = "";
    private LinkedHashMap<String, String> rankName = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> sortName = new LinkedHashMap<>();
    private boolean isFemale;
    private boolean isSort;

    public ABC(boolean isFemale) {
        this.isFemale = isFemale;
    }

    public ABC(boolean isFemale, boolean isSort) {
        this.isFemale = isFemale;
        this.isSort = isSort;
    }

    private void initMaleRankName() {

    }

    private void initSortNames() {
    }


    public List<BookType> getRankTypes() {
        return null;
    }

    public void getRankBooks(BookType bookType, ResultCallback rc) {

    }

    private List<QDBook> getBooksFromJson(String json) {
        return null;
    }

    @Override
    public String getCharset() {
        return null;
    }

    @Override
    public String getFindName() {
        return null;
    }

    @Override
    public String getFindUrl() {
        return null;
    }

    @Override
    public boolean getTypePage(BookType curType, int page) {
        return false;
    }

    @Override
    public boolean hasImg() {
        return true;
    }

    @Override
    public List<BookType> getBookTypeList(String html) {
        return null;
    }

    @Override
    public List<Book> getRankBookList(String html) {
        return null;
    }


    public void initCookie(Context mContext, ResultCallback rc) {

    }

}
