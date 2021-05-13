package xyz.fycz.myreader.webapi.crawler.find;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;


public class MiaoBiFindCrawler extends FindCrawler {
    public static final String NAME_SPACE = "https://www.imiaobige.com";
    public static final String CHARSET = "UTF-8";
    public static final String SEARCH_CHARSET = "UTF-8";
    public static final String FIND_NAME = "书城[妙笔阁]";
    private final LinkedHashMap<String, String> mBookTypes = new LinkedHashMap<>();

    @Override
    public String getCharset() {
        return CHARSET;
    }

    @Override
    public String getFindName() {
        return FIND_NAME;
    }

    @Override
    public String getFindUrl() {
        return NAME_SPACE;
    }

    @Override
    public boolean hasImg() {
        return true;
    }

    @Override
    public boolean needSearch() {
        return false;
    }

    @Override
    public List<BookType> getBookTypes() {
        initBookTypes();
        List<BookType> bookTypes = new ArrayList<>();
        for (String name : mBookTypes.keySet()) {
            BookType bookType = new BookType();
            bookType.setTypeName(name);
            bookType.setUrl(mBookTypes.get(name));
            bookType.setPageSize(100);
            bookTypes.add(bookType);
        }
        return bookTypes;
    }

    private void initBookTypes() {
        mBookTypes.put("玄幻奇幻", "https://www.imiaobige.com/xuanhuan/1.html");
        mBookTypes.put("武侠仙侠", "https://www.imiaobige.com/wuxia/1.html");
        mBookTypes.put("都市生活", "https://www.imiaobige.com/dushi/1.html");
        mBookTypes.put("历史军事", "https://www.imiaobige.com/lishi/1.html");
        mBookTypes.put("游戏竞技", "https://www.imiaobige.com/youxi/1.html");
        mBookTypes.put("科幻未来", "https://www.imiaobige.com/kehuan/1.html");
    }

    /*
		<dl>
            <dt><a href="/novel/225809.html"><img src="https://img.imiaobige.com/225809/1177644.jpg" alt="重生之我变成了火星" height="155" width="120"></a></dt>
            <dd><span class="uptime">20-11-26 16:57</span><a href="/novel/225809.html"><h3>重生之我变成了火星</h3></a></dd>
            <dd class="book_other">作者：<a href="/author/仰望黑夜/">仰望黑夜</a>状态：<span>连载中</span></dd>
            <dd class="book_des">重生成了火星？第一阶段科技进化，第二阶段高魔进化，第三阶段超魔进化！宇宙之间除了人类还有无数可怕存在，让我带领人类征服宇宙，我们的目标是星辰大海~</dd>
            <dd class="book_other">最新章节：<a href="/read/225809/863539.html">第三百一十六章 时空道兵</a></dd>
        </dl>
     */
    @Override
    public List<Book> getFindBooks(String html, BookType bookType) {
        List<Book> books = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Element div = doc.getElementById("sitebox");
        Elements dls = div.getElementsByTag("dl");
        for (Element dl : dls) {
            Book book = new Book();
            Elements as = dl.getElementsByTag("a");
            book.setName(as.get(1).text());
            book.setAuthor(as.get(2).text());
            book.setType(bookType.getTypeName());
            book.setNewestChapterTitle(as.get(3).text());
            book.setDesc(dl.getElementsByClass("book_des").first().text());
            book.setImgUrl(as.first().getElementsByTag("img").attr("src"));
            book.setChapterUrl(as.get(1).attr("href").replace("novel", "read").replace(".html", "/"));
            book.setUpdateDate(dl.getElementsByClass("uptime").first().text());
            book.setSource(LocalBookSource.miaobi.toString());
            books.add(book);
        }
        return books;
    }

    @Override
    public boolean getTypePage(BookType curType, int page) {
        if (page > curType.getPageSize()) {
            return true;
        }
        curType.setUrl(curType.getUrl().substring(0, curType.getUrl().lastIndexOf("/") + 1) + page + ".html");
        return false;
    }
}
