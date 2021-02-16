package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;


public class ChaoXingReadCrawler implements ReadCrawler {
    public static final String NAME_SPACE = "http://yz4.chaoxing.com";
    public static final String NOVEL_SEARCH = "http://yz4.chaoxing.com/circlemarket/getsearch,start=0&size=25&sw={key}&channelId=52";
    public static final String CHAPTERS_URL = "https://special.zhexuezj.cn/mobile/mooc/tocourse/";
    public static final String DESC = "★★★     超星·出版     ★★★\n★★★   本书暂无简介  ★★★";
    public static final String CHARSET = "UTF-8";
    public static final String SEARCH_CHARSET = "UTF-8";

    @Override
    public String getSearchLink() {
        return NOVEL_SEARCH;
    }

    @Override
    public String getCharset() {
        return CHARSET;
    }

    @Override
    public String getNameSpace() {
        return NAME_SPACE;
    }

    @Override
    public Boolean isPost() {
        return true;
    }

    @Override
    public String getSearchCharset() {
        return SEARCH_CHARSET;
    }

    /**
     * 从html中获取章节正文
     *
     * @param html
     * @return
     */
    public String getContentFormHtml(String html) {
        Document doc = Jsoup.parse(html);
        Element divContent = doc.getElementById("contentBox");
        Elements ps = divContent.getElementsByTag("p");
        StringBuilder sb = new StringBuilder();
        for (Element p : ps){
            String content = Html.fromHtml(p.html()).toString();
            char c = 160;
            String spaec = "" + c;
            content = content.replace(spaec, "  ");
            sb.append(content);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 从html中获取章节列表
     *
     * @param html
     * @return
     */
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Element divList = doc.getElementsByClass("con").first();
        Elements elementsByTag = divList.getElementsByTag("a");
        int i = 0;
        for (Element a : elementsByTag) {
            String title = a.text();
            String url = a.attr("attr");
            Chapter chapter = new Chapter();
            chapter.setNumber(i++);
            chapter.setTitle(title);
            chapter.setUrl(url);
            chapters.add(chapter);
        }
        return chapters;
    }

    /**
     * 从搜索html中得到书列表
     *
     * @param json
     * @return
     */
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String json) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        try {
            JSONArray booksArray = new JSONArray(json);
            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject bookJson = booksArray.getJSONObject(i);
                Book book = new Book();
                book.setName(bookJson.getString("name"));
                book.setAuthor(bookJson.getString("author"));
                book.setImgUrl(bookJson.getString("coverUrl"));
                book.setNewestChapterTitle("");
                book.setChapterUrl(CHAPTERS_URL + bookJson.getInt("course_Id"));
                book.setDesc(DESC);
                book.setSource(LocalBookSource.chaoxing.toString());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return books;
    }


}
