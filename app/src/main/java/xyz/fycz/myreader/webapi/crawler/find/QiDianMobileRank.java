package xyz.fycz.myreader.webapi.crawler.find;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.ResultCallback;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.entity.bookstore.QDBook;
import xyz.fycz.myreader.entity.bookstore.RankBook;
import xyz.fycz.myreader.entity.bookstore.SortBook;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author fengyue
 * @date 2020/9/16 22:01
 */
public class QiDianMobileRank extends FindCrawler {
    private String rankUrl = "https://m.qidian.com/majax/rank/{rankName}list?_csrfToken={cookie}&gender={sex}&pageNum={page}&catId=-1";
    private String sortUrl = "https://m.qidian.com/majax/category/list?_csrfToken={cookie}&gender={sex}&pageNum={page}&orderBy=&catId={catId}&subCatId=";
    private String[] sex = {"male", "female"};
    private String yuepiaoParam = "&yearmonth={yearmonth}";
    private String imgUrl = "https://bookcover.yuewen.com/qdbimg/349573/{bid}/150";
    private String defaultCookie = "eXRDlZxmRDLvFAmdgzqvwWAASrxxp2WkVlH4ZM7e";
    private String yearmonthFormat = "yyyyMM";
    private LinkedHashMap<String, String> rankName = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> sortName = new LinkedHashMap<>();
    private boolean isFemale;
    private boolean isSort;
    private int onePageNum = 20;

    public QiDianMobileRank(boolean isFemale) {
        this.isFemale = isFemale;
    }

    public QiDianMobileRank(boolean isFemale, boolean isSort) {
        this.isFemale = isFemale;
        this.isSort = isSort;
    }

    private void initMaleRankName() {
        if (!isFemale) {
            rankName.put("风云榜", "yuepiao");
            rankName.put("畅销榜", "hotsales");
            rankName.put("阅读榜", "readIndex");
            rankName.put("粉丝榜", "newfans");
            rankName.put("推荐榜", "rec");
            rankName.put("更新榜", "update");
            rankName.put("签约榜", "sign");
            rankName.put("新书榜", "newbook");
            rankName.put("新人榜", "newauthor");
        } else {
            rankName.put("风云榜", "yuepiao");
            rankName.put("阅读榜", "readIndex");
            rankName.put("粉丝榜", "newfans");
            rankName.put("推荐榜", "rec");
            rankName.put("更新榜", "update");
            rankName.put("收藏榜", "collect");
            rankName.put("免费榜", "free");
        }
    }

    private void initSortNames() {
        /*
        {value: -1, text: "全站"}
1: {value: 21, text: "玄幻"}
2: {value: 1, text: "奇幻"}
3: {value: 2, text: "武侠"}
4: {value: 22, text: "仙侠"}
5: {value: 4, text: "都市"}
6: {value: 15, text: "现实"}
7: {value: 6, text: "军事"}
8: {value: 5, text: "历史"}
9: {value: 7, text: "游戏"}
10: {value: 8, text: "体育"}
11: {value: 9, text: "科幻"}
12: {value: 10, text: "悬疑"}
13: {value: 12, text: "轻小说"}
         */
        if (!isFemale) {
            sortName.put("玄幻小说", 21);
            sortName.put("奇幻小说", 1);
            sortName.put("武侠小说", 2);
            sortName.put("都市小说", 4);
            sortName.put("现实小说", 15);
            sortName.put("军事小说", 6);
            sortName.put("历史小说", 5);
            sortName.put("体育小说", 8);
            sortName.put("科幻小说", 9);
            sortName.put("悬疑小说", 10);
            sortName.put("轻小说", 12);
            sortName.put("短篇小说", 20076);
        } else {
            sortName.put("古代言情", 80);
            sortName.put("仙侠奇缘", 81);
            sortName.put("现代言情", 82);
            sortName.put("烂漫青春", 83);
            sortName.put("玄幻言情", 84);
            sortName.put("悬疑推理", 85);
            sortName.put("短篇小说", 30083);
            sortName.put("科幻空间", 86);
            sortName.put("游戏竞技", 88);
            sortName.put("轻小说", 87);
            sortName.put("现实生活", 30120);
        }
    }


    public List<BookType> getBookTypes() {
        if (!isSort) {
            initMaleRankName();
        } else {
            initSortNames();
        }
        List<BookType> bookTypes = new ArrayList<>();
        Set<String> names = !isSort ? rankName.keySet() : sortName.keySet();
        for (String name : names) {
            BookType bookType = new BookType();
            bookType.setTypeName(name);
            String url;
            if (!isSort) {
                url = rankUrl.replace("{rankName}", rankName.get(name));
            } else {
                url = sortUrl.replace("{catId}", sortName.get(name) + "");
            }
            url = url.replace("{sex}", !isFemale ? sex[0] : sex[1]);
            SharedPreUtils spu = SharedPreUtils.getInstance();
            String cookie = spu.getString(App.getmContext().getString(R.string.qdCookie), "");
            if (!cookie.equals("")) {
                url = url.replace("{cookie}", StringHelper.getSubString(cookie, "_csrfToken=", ";"));
            } else {
                url = url.replace("{cookie}", defaultCookie);
            }
            if ("风云榜".equals(name)) {
                SimpleDateFormat sdf = new SimpleDateFormat(yearmonthFormat, Locale.CHINA);
                String yearmonth = sdf.format(new Date());
                url = url + yuepiaoParam.replace("{yearmonth}", yearmonth);
            }
            bookType.setUrl(url);
            bookTypes.add(bookType);
        }
        return bookTypes;
    }

    public void getRankBooks(BookType bookType, ResultCallback rc) {
        Single.create((SingleOnSubscribe<List<QDBook>>) emitter -> {
            emitter.onSuccess(getBooksFromJson(OkHttpUtils.getHtml(bookType.getUrl()), bookType));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<List<QDBook>>() {
            @Override
            public void onSuccess(@NotNull List<QDBook> books) {
                rc.onFinish(books, 0);
            }

            @Override
            public void onError(Throwable e) {
                rc.onError((Exception) e);
            }
        });
    }

    private List<QDBook> getBooksFromJson(String json, BookType bookType) {
        List<QDBook> books = new ArrayList<>();
        try {
            JSONObject all = new JSONObject(json);
            JSONObject data = all.getJSONObject("data");
            int total = data.getInt("total");
            bookType.setPageSize(total % onePageNum == 0 ? total / onePageNum : total / onePageNum + 1);
            JSONArray jsonBooks = data.getJSONArray("records");
            for (int i = 0; i < jsonBooks.length(); i++) {
                JSONObject jsonBook = jsonBooks.getJSONObject(i);
                QDBook book = !isSort ? new RankBook() : new SortBook();
                book.setbName(jsonBook.getString("bName"));
                book.setbAuth(jsonBook.getString("bAuth"));
                book.setBid(jsonBook.getString("bid"));
                book.setCat(jsonBook.getString("cat"));
                book.setCatId(jsonBook.getInt("catId"));
                book.setCnt(jsonBook.getString("cnt"));
                book.setDesc(jsonBook.getString("desc"));
                book.setImg(imgUrl.replace("{bid}", jsonBook.getString("bid")));
                if (!isSort) {
                    ((RankBook) book).setRankCnt(jsonBook.getString("rankCnt"));
                    ((RankBook) book).setRankNum(jsonBook.getInt("rankNum"));
                }else {
                    ((SortBook) book).setState(jsonBook.getString("state"));
                }
                books.add(book);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public String getCharset() {
        return null;
    }

    @Override
    public String getFindName() {
        return !isFemale ? !isSort ? "排行榜[起点中文网]" : "分类[起点中文网]" : !isSort ? "排行榜[起点女生网]" : "分类[起点女生网]";
    }

    @Override
    public String getFindUrl() {
        return null;
    }

    @Override
    public boolean getTypePage(BookType curType, int page) {
        if (!isSort) {
            if (curType.getPageSize() <= 0){
                curType.setPageSize(30);
            }
            if (page > curType.getPageSize()) {
                return true;
            }
        }else {
            if (page > 5) {
                return true;
            }
        }
        String pageNum = curType.getUrl().substring(curType.getUrl().indexOf("pageNum=") + 8, curType.getUrl().indexOf("&catId"));
        curType.setUrl(curType.getUrl().replace("pageNum=" + pageNum, "pageNum=" + page));
        return false;
    }

    @Override
    public boolean hasImg() {
        return true;
    }

    @Override
    public boolean needSearch() {
        return true;
    }

    @Override
    public List<Book> getFindBooks(String html, BookType bookType) {
        return null;
    }


    public void initCookie(Context mContext, ResultCallback rc) {
        App.getApplication().newThread(() -> {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(mContext.getAssets().open("_csrfToken.fy")));
                StringBuilder assetText = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    assetText.append(line);
                }
                String[] _csrfTokens = assetText.toString().split(",");
                Random random = new Random();
                rc.onFinish(_csrfTokens[random.nextInt(_csrfTokens.length)], 1);
            } catch (IOException e) {
                rc.onError(e);
            } finally {
                IOUtils.close(br);
            }
        });
    }

}
