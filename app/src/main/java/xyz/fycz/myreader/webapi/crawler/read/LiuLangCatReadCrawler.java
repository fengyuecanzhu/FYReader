package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

/**
 * @author fengyue
 * @date 2020/11/27 14:09
 */
public class LiuLangCatReadCrawler implements ReadCrawler {
    public static final String NAME_SPACE = "http://m.liulangcat.com";
    public static final String NOVEL_SEARCH  = "http://m.liulangcat.com/get/get_search_result.php?page=0&keyword={key}";
//    public static final String NOVEL_SEARCH  = "http://www.liulangcat.com/search.php?k={key}&submit=搜索&wgxojg=wc0yz&uwzgzw=yi1p7&amlmvy=mcp50&rybwbm=1s0y7";
    public static final String CHARSET = "utf-8";
    public static final String SEARCH_CHARSET = "utf-8";

    @Override
    public String getSearchLink() {
        return NOVEL_SEARCH;
    }

    @Override
    public String getCharset() {
        return CHARSET;
    }

    @Override
    public String getSearchCharset() {
        return SEARCH_CHARSET;
    }

    @Override
    public String getNameSpace() {
        return NAME_SPACE;
    }

    @Override
    public Boolean isPost() {
        return false;
    }

    /*
    *
    <li><div class="book_block">
		<div class="item_content">
			<div class="item_top">
				<p>
				<span class='top_bookname'><a class="bookname" href="/wgwx/30003.html" title="白夜行">白夜行</a><a class="author" href="/author.php?author=8">[东野圭吾]</a></span>							</p>
			</div>
			<div class="item_bottom">
				<p>
					《白夜行》是日本作家东野圭吾的代表作。小说故事围绕着一对有着不同寻常情愫的小学生展开。1973年，大阪的一栋废弃建筑内发现了一具男尸，此后19年，嫌疑人之女雪穗与被害者之子桐原亮司走上截然不同的人生道路，一个跻身上流社会，一个却在底层游走，而他们身边的人，却接二连三地离奇死去，警察经过19年的艰苦追踪，终于使真相大白。							</p>
			</div>
		</div>
	</div></li>
    * */
    /*@Override
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
        Element div = doc.getElementById("booklist");
        Elements lis = div.getElementsByTag("li");
        for (Element li : lis){
            Book book = new Book();
            Element bookName = li.getElementsByClass("bookname").first();
            Element author = li.getElementsByClass("author").first();
            Element desc = li.getElementsByClass("item_bottom").first();
            book.setName(bookName.text());
            book.setChapterUrl(NAME_SPACE + bookName.attr("href"));
            book.setAuthor(author.text().replace("[", "").replace("]", ""));
            book.setDesc(desc.text());
            book.setNewestChapterTitle("");
            book.setIsCloseUpdate(true);
            book.setSource(BookSource.liulangcat.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
        return books;
    }*/

    /*
        author: "海明威"
        authorid: "9102"
        bookname: "老人与海"
        descrption: "反映了社会经济发展某一阶段的普遍模式，这种模式甚至在现在的发展中国家还可以看到。在二十世纪三四十年代的农业国古巴，传统的渔业文化（和工业化世界隔绝，贴近自然，脱离现代技术，受庞大的家族和紧密联系的村落的约束）开始受到捕鱼产业（依赖工业化的世界，不顾或忽视环境，依靠机械设备获取利润，受庞大的家族和地方村落的约束较少）的冲击。在《老人与海》里，一方面，海明威把圣地亚哥描绘成一个一心一意将捕鱼手艺与自身身份、行为准则和自然法则完美结合在一起的渔民；另一方面，海明威刻画了一些奉行实用主义的年轻渔民，他们把鲨鱼肝卖给美国的肝油产业，用这些利润购买摩托艇及其他机械设备，把捕鱼全然当做改善物质生活的一种手段。"
        no: "31072"
        pycode: "wgwx"
    * */

    @Override
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String json) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                Book book = new Book();
                JSONObject jsonBook = array.getJSONObject(i);
                book.setName(jsonBook.getString("bookname"));
                book.setAuthor(jsonBook.getString("author"));
                book.setDesc(jsonBook.getString("descrption"));
                //http://m.liulangcat.com/wgwx/31072.html
                book.setChapterUrl(NAME_SPACE + "/" + jsonBook.getString("pycode") + "/" +
                        jsonBook.getString("no") + ".html");
                book.setNewestChapterTitle("");
                book.setIsCloseUpdate(true);
                //book.setSource(BookSource.liulangcat.toString());
                SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                books.add(sbb, book);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public ArrayList<Chapter> getChaptersFromHtml(String html) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Element ul = doc.getElementsByClass("booklist").first();
        Elements as = ul.getElementsByTag("a");
        int i = 0;
        for (Element a : as) {
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

    @Override
    public String getContentFormHtml(String html) {
        Document doc = Jsoup.parse(html);
        Element divContent = doc.getElementsByClass("item_content").first();
        Element nameSpan = doc.getElementsByClass("top_categoryname").first();
        if (divContent != null) {
            String content = Html.fromHtml(divContent.html()).toString();
            char c = 160;
            String spaec = "" + c;
            content = content.replace(spaec, "  ").
                    replace(nameSpan.text(), "").
                    replace("主页", "").
                    replace("目录", "");
            return content;
        } else {
            return "";
        }
    }
}
