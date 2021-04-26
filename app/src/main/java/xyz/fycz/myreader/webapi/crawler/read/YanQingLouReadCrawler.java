package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.StringUtils;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

@Deprecated
public class YanQingLouReadCrawler implements ReadCrawler {
    public static final String NAME_SPACE = "http://www.yanqinglou.com";
    public static final String NOVEL_SEARCH = "http://www.yanqinglou.com/Home/Search,action=search&q={key}";
    public static final String AJAX_CONTENT = "http://www.yanqinglou.com/home/index/ajaxchapter";
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

    /*
        var preview_page = "/lishi/220987/60.html";
        var next_page = "/lishi/220987/62.html";
        var index_page = "/lishi/220987/";
        var article_id = "220987";
        var chapter_id = "61";
        var nextcid = "62";
        var prevcid = "60";
        var articlename = "临高启明";
        var chaptername = "第十五节 遇伏";
        var hash = "38e338d183600e17";
        var localpre = "www.yanqinglou.com";
     */
    public String getContentFormHtml(String html) {
        Document doc = Jsoup.parse(html);
        Element divContent = doc.getElementById("content");
        String content = "";
        content = Html.fromHtml(divContent.html()).toString();
        try {
            if (content.contains("正在加载") || content.contains("本章未完，点击下一页继续阅读")) {
                content = getAjaxContent(html);
            }
            /*if (content.contains("本章未完，点击下一页继续阅读")){
                String nextUrl = doc.select(".to_nextpage")
                        .first().select("a").first()
                        .attr("href");
                content = content.replace("本章未完，点击下一页继续阅读", "")
                        .replace("-->>", "");
                content += getContentFormHtml(OkHttpUtils.getHtml(nextUrl, CHARSET));
            }*/
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        char c = 160;
        String spaec = "" + c;
        content = content.replace(spaec, "  ")
                .replaceAll("您可以.*最新章节！", "")
                .replaceAll("转码页.*com/", "");
        return content;
    }


    /*
    id: 220987
    eKey: fe9535a08b53e929
    cid: 62
    basecid: 62
     */
    public String getAjaxContent(String html) throws IOException, JSONException {
        String id = StringUtils.getSubString(html, "var article_id = \"", "\";");
        String eKey = StringUtils.getSubString(html, "var hash = \"", "\";");
        String cid = StringUtils.getSubString(html, "var chapter_id = \"", "\";");
        String body = "id=" + id + "&eKey=" + eKey + "&cid=" + cid + "&basecid=" + cid;
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody requestBody = RequestBody.create(mediaType, body);
        String jsonStr = OkHttpUtils.getHtml(AJAX_CONTENT, requestBody, CHARSET);
        JSONObject json = new JSONObject(jsonStr);
        String content = json.getJSONObject("info").getString("content");
        content = Html.fromHtml(content).toString();
        return content;
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
        Element divList = doc.getElementsByClass("fulllistall").first();
        Elements elementsByTag = divList.getElementsByTag("a");
        for (int i = 0; i < elementsByTag.size(); i++) {
            Element a = elementsByTag.get(i);
            String title = a.text();
            String url = a.attr("href");
            Chapter chapter = new Chapter();
            chapter.setNumber(i);
            chapter.setTitle(title);
            chapter.setUrl(url);
            chapters.add(chapter);
        }
        return chapters;
    }

    /**
     * 从搜索html中得到书列表
     * <div class="bookbox">
     * <div class="p10"><span class="num"> <a title="斗罗之大主宰" href="/hunlian/459337/"><img layout="fixed" width="90" height="120" src="https://www.biquduo.com/files/article/image/64/64075/64075s.jpg" alt="斗罗之大主宰" /></a> </span>
     * <div class="bookinfo">
     * <h4 class="bookname"><a title="斗罗之大主宰" href="/hunlian/459337/">斗罗之大主宰</a></h4>
     * <div class="author">作者：<a href="/writter/%E4%B8%8A%E5%BC%A6666.html" target="_blank" title="上弦666的作品大全">上弦666</a></div>
     * <div class="author">分类：婚恋</div>
     * <div class="cat"><span>更新到：</span><a href="/hunlian/459337/">最近更新>></a></div>
     * <div class="update"><span>简介：</span>叶辰，武魂，昊天锤的变异，大须弥锤！昊天锤本就是大陆第一器武魂，而武魂是大须弥锤的叶辰，会有多强？当</div>
     * </div>
     * <div class="delbutton"> <a class="del_but" title="斗罗之大主宰" href="/hunlian/459337/">阅读</a></div>
     * </div>
     * </div>
     */
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
        String bookName = doc.select("meta[property=og:novel:book_name]").attr("content");
        if ("".equals(bookName)) {
            Element div = doc.getElementsByClass("keywords").first();
            Elements divs = div.getElementsByClass("bookbox");
            for (Element divBook : divs) {
                Elements as = divBook.getElementsByTag("a");
                Book book = new Book();
                book.setName(as.get(1).text());
                book.setAuthor(as.get(2).text());
                book.setType(divBook.getElementsByClass("author").get(1).text().replace("分类：", ""));
                book.setNewestChapterTitle(as.get(3).text().replace("最近更新>>", ""));
                book.setDesc(divBook.getElementsByClass("update").first().text().replace("简介：", ""));
                book.setImgUrl(NAME_SPACE + divBook.getElementsByTag("img").attr("src"));
                book.setChapterUrl(as.get(0).attr("href"));
                book.setSource(LocalBookSource.yanqinglou.toString());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        } else {
            Book book = new Book();
            getBookInfo(doc, book);
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
        return books;
    }

    private void getBookInfo(Document doc, Book book) {
        String name = doc.select("meta[property=og:novel:book_name]").attr("content");
        book.setName(name);

        String author = doc.select("meta[property=og:novel:author]").attr("content");
        book.setAuthor(author);

        String type = doc.select("meta[property=og:novel:category]").attr("content");
        book.setType(type);

        String newestTitle = doc.select("meta[property=og:novel:latest_chapter_name]").attr("content");
        book.setNewestChapterTitle(newestTitle);

        String desc = doc.select("meta[property=og:description]").attr("content");
        book.setDesc(desc);

        String img = doc.select("meta[property=og:image]").attr("content");
        book.setImgUrl(NAME_SPACE + img);

        String url = doc.select("meta[property=og:novel:read_url]").attr("content");
        book.setChapterUrl(url);
        book.setSource(LocalBookSource.yanqinglou.toString());
    }


}
