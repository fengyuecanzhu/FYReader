package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;


public class ReXueReadCrawler implements ReadCrawler {
    public static final String NAME_SPACE = "https://www.rexue.org";
    public static final String NOVEL_SEARCH = "https://www.rexue.org/search.php?key={key}";
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
        return false;
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
        Element divContent = doc.getElementById("txt");
        if (divContent != null) {
            Elements aDiv = divContent.getElementsByTag("dd");
            StringBuilder sb = new StringBuilder();
            Collections.sort(aDiv, (o1, o2) -> Integer.parseInt(o1.attr("data-id")) -
                    Integer.parseInt(o2.attr("data-id")));
            for (int i = 0; i < aDiv.size(); i++) {
                Element dd = aDiv.get(i);
                if (i == aDiv.size() - 1) break;
                sb.append(Html.fromHtml(dd.html()).toString());
                sb.append("\n");
            }
            String content = sb.toString();
            char c = 160;
            String spaec = "" + c;
            content = content.replace(spaec, "  ");
            return content;
        } else {
            return "";
        }
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
        Element divList = doc.getElementById("listsss");
        Elements elementsByTag = divList.getElementsByTag("a");
        int i = 0;
        for (int j = 0; j < elementsByTag.size(); j++) {
            Element a = elementsByTag.get(j);
            String title = a.text();
            String url = a.attr("href");
            Chapter chapter = new Chapter();
            chapter.setNumber(i++);
            chapter.setTitle(title);
            chapter.setUrl(NAME_SPACE + url);
            chapters.add(chapter);
        }
        return chapters;
    }

    /**
     * 从搜索html中得到书列表
     *
     * @param html
     * @return
     */
    /*

    <li><a href="/content/3357/" target="_blank" class="book_cov" title="大主宰"><img src="/public/images/default.jpg" data-original="//www.rexue.org/files/article/image/0/24/24s.jpg" class="lazyload_book_cover" alt="大主宰"/></a>
        <div class="book_inf">
            <h3><a href="/content/3357/" title="大主宰" target="_blank" mod="data_book_name"><font style="font-weight:bold;color:#f00">大主宰</font></a></h3>
             <p class="tags"><span>作者：<a title="天蚕土豆">天蚕土豆</a></span><span>分类：<a href="/sort/8_0_0_0_0_1.html" target="_blank">短篇文学</a></span><span>状态：连载中</span><span>总字数：2497万字+</span></p>
            <p><b>最近更新：</b><a href="/content/3357/11940.html" title="第1598章 邪神陨落（大结局）" target="_blank">第1598章 邪神陨落（大结局）</a></p>
            <p class="int">    大千世界，位面交汇，万族林立，群雄荟萃，一位位来自下位面的天之至尊，在这无尽世界，演绎着令人向往的传奇，追求着那主宰之路。
                无尽火域，炎帝执掌，万火焚苍穹。
                武境之内，武祖之威</p>
        </div>
        <div class="right">
            <span>更新时间：09-06 06:47</span>
            <a href="/content/3357/" target="_blank" class="read_btn btn">立即阅读</a>
            <a  href="javascript:BookCaseAdd('3357');" class="store_btn btn" btn="book_fav">加入书架</a>
        </div>
    </li>
     */
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
//        try {
        String urlType = doc.select("meta[property=og:type]").attr("content");
        if ("novel".equals(urlType)) {
            String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
            Book book = new Book();
            book.setChapterUrl(readUrl);
            getBookInfo(doc, book);
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        } else {
            Element div = doc.getElementsByClass("result").first();
            Elements lis = div.getElementsByTag("li");
            for (Element li : lis) {
                Elements as = li.getElementsByTag("a");
                Book book = new Book();
                book.setName(as.get(1).text());
                book.setAuthor(as.get(2).text());
                book.setType(as.get(3).text());
                book.setNewestChapterTitle(as.get(4).text().replace("最新章节：", ""));
                book.setDesc(li.getElementsByClass("int").first().text());
                book.setUpdateDate(li.getElementsByClass("right").first().getElementsByTag("span").text());
                String imgUrl = li.getElementsByTag("img").attr("data-original");
                book.setImgUrl(!imgUrl.contains("http") ? "https:" + imgUrl : imgUrl);
                book.setChapterUrl(NAME_SPACE + as.get(1).attr("href"));
                book.setSource(BookSource.rexue.toString());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return books;
    }

    public Book getBookInfo(Document doc, Book book) {
        //小说源
        book.setSource(BookSource.rexue.toString());
        //图片url
        String imgUrl = doc.select("meta[property=og:image]").attr("content");
        book.setImgUrl(imgUrl);

        //书名
        String title = doc.select("meta[property=og:novel:book_name]").attr("content");
        book.setName(title);

        //作者
        String author = doc.select("meta[property=og:novel:author]").attr("content");
        book.setAuthor(author);

        //更新时间
        String updateDate = doc.select("meta[property=og:novel:update_time]").attr("content");
        book.setUpdateDate(updateDate);

        //最新章节
        String newestChapterTitle = doc.select("meta[property=og:novel:latest_chapter_name]").attr("content");
        book.setNewestChapterTitle(newestChapterTitle);

        //类型
        String type = doc.select("meta[property=og:novel:category]").attr("content");
        book.setType(type);

        //简介
        String desc = doc.select("meta[property=og:description]").attr("content");
        book.setDesc(desc);
        return book;

    }
}
