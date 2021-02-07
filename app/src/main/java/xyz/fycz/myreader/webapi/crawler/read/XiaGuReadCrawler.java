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


public class XiaGuReadCrawler implements ReadCrawler {
    public static final String NAME_SPACE = "https://www.xiagu.org";
    public static final String NOVEL_SEARCH = "https://www.xiagu.org/search/?keyword={key}&t=0";
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
        String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
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
            chapter.setUrl(readUrl + url);
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
        <li class="subject-item">
            <div class="pic">
              <a class="nbg" href="/xs/5584.html" ><img class="" data-original="//www.xiagu.org/files/article/image/0/29/29s.jpg"  src="/statics/images/default.jpg" width="90"></a>
            </div>
            <div class="info">
              <h2 class=""><a href="/xs/5584.html"><font style="font-weight:bold;color:#f00">大主宰</font></a></h2>
              <div class="pub">作者：<a target="_blank" href="/author/id/186.html">天蚕土豆</a> / 类型：<a target="_blank" href="/shuku/8_0_0_0_0_1.html">其他类型</a> /  2020-09-06 / 连载中 / 2497.0808万字</div>

            <p>【内容简介】&nbsp;&nbsp;&nbsp;&nbsp;大千世界，位面交汇，万族林立，群雄荟萃，一位位来自下位面的天之至尊，在这无尽世界，演绎着令人向往的传奇，追求着那主宰之路。<br />
        &nbsp;&nbsp;&nbsp;&nbsp;无尽火域，炎帝执掌，万火焚苍穹。<br />
        &nbsp;&nbsp;&nbsp;&nbsp;武境之内，武祖之威... </p>
            <div class="ft">

            <div class="pub">角色：牧尘，唐芊儿，红绫，牧哥，苏凌，刘彻，柳阳，牧锋，芊儿，柳慕白，薛东，童哥，柳眉，唐芊儿蹙，姬玄，牧哥比，那柳阳，乐乐，凌撇，杨柳，林修，牧尘沉，莫师，代柳阳</div>
            <div class="cart-actions">
            <span class="market-info"><a href="/read/5/5584/" target="_blank">【最新章节】第1598章 邪神陨落（大结局）</a></span>
            </div>
            <div class="ebook-link"><a target="_blank" href="/xs/5584.html">详细介绍</a></div>
            </div>
            </div>
        </li>
     */
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
//        try {
        Element div = doc.getElementsByClass("subject-list").first();
        Elements lis = div.getElementsByTag("li");
        for (Element li : lis) {
            Elements as = li.getElementsByTag("a");
            Book book = new Book();
            book.setName(as.get(1).text());
            book.setAuthor(as.get(2).text());
            book.setType(as.get(3).text());
            book.setNewestChapterTitle(as.get(4).text().replace("【最新章节】", ""));
            book.setDesc(li.getElementsByTag("p").first().text());
            String imgUrl = li.getElementsByTag("img").attr("data-original");
            book.setImgUrl(!imgUrl.contains("http") ? "https:" + imgUrl : imgUrl);
            //https://www.xiagu.org/xs/5584.html -> https://www.xiagu.org/read/5/5584/
            book.setChapterUrl(NAME_SPACE + as.get(1).attr("href").replace("xs", "read/1").replace(".html", "/"));
            book.setSource(BookSource.xiagu.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return books;
    }


}
