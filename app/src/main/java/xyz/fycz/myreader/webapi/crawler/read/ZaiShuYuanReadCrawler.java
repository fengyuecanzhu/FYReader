package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.webapi.crawler.base.BaseReadCrawler;


public class ZaiShuYuanReadCrawler extends BaseReadCrawler {
    public static final String NAME_SPACE = "https://www.zhaishuyuan.com";
    public static final String NOVEL_SEARCH = "https://www.zhaishuyuan.com/search/,key={key}";
    public static final String CHARSET = "gbk";
    public static final String SEARCH_CHARSET = "gbk";

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
        Element divContent = doc.getElementById("content");
        String content = Html.fromHtml(divContent.html()).toString();
        char c = 160;
        String spaec = "" + c;
        content = content.replace(spaec, "  ").replaceAll("您可以在.*最新章节！|\\\\", "");
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
        Element divList = doc.getElementById("readerlist");
        Elements elementsByTag = divList.getElementsByTag("a");
        int i = 0;
        for (int j = 0; j < elementsByTag.size(); j++) {
            Element a = elementsByTag.get(j);
            String title = a.text();
            String url = a.attr("href");
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
     * @param html
     * @return <dl>
     * <dt><a href="/book/3"><img class="lazyload" _src="https://img.zhaishuyuan.com/bookpic/s3.jpg" alt="<font color=#F30>大主宰</font>" height="155" width="120"></a></dt>
     * <dd><h3><a href="/read/3"><font color=#F30>大主宰</font></a><span class="alias">别名：<font color=#F30>大主宰</font></span></h3></dd>
     * <dd class="book_other">作者：<span>天蚕土豆</span>状态：<span>已完结</span>小类：<span>异世大陆</span>字数：<span>4944063</span>标签：<a href="/search/?key=%C8%C8%D1%AA" target="_blank" rel="nofollow">热血</a> <a href="/search/?key=%CB%AC%CE%C4" target="_blank" rel="nofollow">爽文</a></dd>
     * <dd class="book_des">大千世界，位面交汇，万族林立，群雄荟萃，一位位来自下位面的天之至尊，在这无尽世界，演绎着令人向往的传奇，追求着那主宰之路。无尽火域，炎帝执掌，万火焚苍穹。武境之内，武祖之威，震慑乾坤。西天之殿，百战之皇，战威无可敌。北荒之丘，万墓之地，不死…</dd>
     * <dd class="book_other">最新章节：<a href="/chapter/3/8855386">第一千五百五十一章 邪神陨落（大结局）</a> 更新时间：<span>2020-2-26 13:26:49</span></dd>
     * </dl>
     */
    public ConMVMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConMVMap<SearchBookBean, Book> books = new ConMVMap<>();
        Document doc = Jsoup.parse(html);
        String urlType = doc.select("meta[property=og:type]").attr("content");
        if ("novel".equals(urlType)) {
            String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
            Book book = new Book();
            book.setChapterUrl(readUrl);
            getBookInfo(doc, book);
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        } else {
            Element div = doc.getElementById("sitembox");
            Elements dls = div.getElementsByTag("dl");
            for (Element dl : dls) {
                Elements as = dl.getElementsByTag("a");
                Book book = new Book();
                book.setName(as.get(1).text());
                book.setImgUrl(as.first().getElementsByTag("img").attr("_src"));
                book.setNewestChapterTitle(as.last().text());
                Elements spans = dl.selectFirst(".book_other").select("span");
                book.setAuthor(spans.get(0).text());
                book.setType(spans.get(2).text());
                book.setDesc(dl.getElementsByClass("book_des").first().text());
                book.setChapterUrl(as.get(1).attr("href").replace("novel", "read").replace(".html", "/"));
                book.setSource(LocalBookSource.zaishuyuan.toString());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        }
        return books;
    }

    private void getBookInfo(Document doc, Book book) {
        book.setSource(LocalBookSource.zaishuyuan.toString());

        String name = doc.select("meta[property=og:title]").attr("content");
        book.setName(name);
        String url = doc.select("meta[property=og:novel:read_url]").attr("content");
        book.setChapterUrl(url);
        String author = doc.select("meta[property=og:novel:author]").attr("content");
        book.setAuthor(author);
        String newestChapter = doc.select("meta[property=og:novel:latest_chapter_name]").attr("content");
        book.setNewestChapterTitle(newestChapter);

        String img = doc.select("meta[property=og:image]").attr("content");
        book.setImgUrl(img);
        Element desc = doc.getElementById("bookintro");
        book.setDesc(Html.fromHtml(desc.html()).toString());
        //类型
        String type = doc.select("meta[property=og:novel:category]").attr("content");
        book.setType(type);
    }
}
