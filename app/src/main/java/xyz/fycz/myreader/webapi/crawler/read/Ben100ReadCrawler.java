package xyz.fycz.myreader.webapi.crawler.read;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.entity.bookstore.BookType;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Deprecated
public class Ben100ReadCrawler extends FindCrawler implements ReadCrawler, BookInfoCrawler {
    public static final String NAME_SPACE = "https://www.100ben.net";
    public static final String NOVEL_SEARCH = "https://www.100ben.net/plus/search.php?keyword={key}";
    public static final String CHARSET = "UTF-8";
    public static final String SEARCH_CHARSET = "utf-8";
    public static final String FIND_NAME = "书城[100本书·实体]";
    private LinkedHashMap<String, String> mBookTypes = new LinkedHashMap<>();


    @Override
    public String getSearchLink() {
        return NOVEL_SEARCH;
    }

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
        Element divContent = doc.getElementById("content");
        String content = Html.fromHtml(divContent.html()).toString();
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
        try {
            Element divList = doc.getElementById("dir");
            int i = 0;
            Elements elementsByTag = divList.getElementsByTag("dd");
            for (int j = 0; j < elementsByTag.size(); j++) {
                Element dd = elementsByTag.get(j);
                Elements as = dd.getElementsByTag("a");
                Element a = as.get(0);
                String title = a.text();
                Chapter chapter = new Chapter();
                chapter.setNumber(i++);
                chapter.setTitle(title);
                String url = a.attr("href");
                chapter.setUrl(url);
                chapters.add(chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chapters;
    }

    /**
     * 从搜索html中得到书列表
     *
     * @param html
     * @return
     */
    public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
        ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
        Document doc = Jsoup.parse(html);
//        try {
        Elements divs = doc.getElementsByClass("recommand");
        Element div = divs.get(0);
        Elements elementsByTag = div.getElementsByTag("li");
        for (Element element : elementsByTag) {
            Book book = new Book();
            String name = element.getElementsByClass("titles").first().getElementsByTag("a").first().text();
            book.setName(name);
            String author = element.getElementsByClass("author").first().text().replace("作者：", "");
            book.setAuthor(author);
            String imgUrl = element.getElementsByTag("img").first().attr("src");
            book.setImgUrl(imgUrl);
            String chapterUrl = element.getElementsByClass("titles").first().getElementsByTag("a").first().attr("href");
            book.setChapterUrl(chapterUrl);
            String desc = element.getElementsByClass("intro").first().text();
            book.setDesc(desc);
            book.setNewestChapterTitle("");
            book.setIsCloseUpdate(true);
            book.setSource(LocalBookSource.ben100.toString());
            SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
            books.add(sbb, book);
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return books;
    }

    /**
     * 获取书籍详细信息
     *
     * @param book
     */
    public Book getBookInfo(String html, Book book) {
        Document doc = Jsoup.parse(html);
        Element img = doc.getElementById("fmimg");
        book.setImgUrl(img.getElementsByTag("img").get(0).attr("src"));
        Element desc = doc.getElementById("intro");
        book.setDesc(desc.getElementsByTag("p").get(0).text());
        Element type = doc.getElementsByClass("con_top").get(0);
        book.setType(type.getElementsByTag("a").get(2).text());
        return book;
    }


    @Override
    public List<BookType> getBookTypes(String html) {
        List<BookType> bookTypes = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Element menu = doc.getElementsByClass("menu").first();
        for (Element a : menu.children()) {
            String name = a.text();
            String url = a.attr("href");
            if ("首页".equals(name)) {
                name = "100本书";
            } else if (name.contains("全部小说")) {
                name = "全部小说";
            }
            BookType bookType = new BookType();
            bookType.setTypeName(name);
            bookType.setUrl(url);
            bookTypes.add(bookType);
            System.out.println("bookTypes.put(\"" + bookType.getTypeName() + "\", \"" + bookType.getUrl() + "\");");
        }
        return bookTypes;
    }

    @Override
    public List<BookType> getBookTypes() {
        initBookTypes();
        List<BookType> bookTypes = new ArrayList<>();
        for (String name : mBookTypes.keySet()) {
            BookType bookType = new BookType();
            bookType.setTypeName(name);
            bookType.setUrl(mBookTypes.get(name));
            bookTypes.add(bookType);
        }
        return bookTypes;
    }

    private void initBookTypes() {
        mBookTypes.put("世界名著", "https://www.100ben.net/shijiemingzhu/");
        mBookTypes.put("现代文学", "https://www.100ben.net/xiandaiwenxue/");
        mBookTypes.put("外国小说", "https://www.100ben.net/waiguoxiaoshuo/");
        mBookTypes.put("励志书籍", "https://www.100ben.net/lizhishuji/");
        mBookTypes.put("古典文学", "https://www.100ben.net/gudianwenxue/");
        mBookTypes.put("武侠小说", "https://www.100ben.net/wuxiaxiaoshuo/");
        mBookTypes.put("言情小说", "https://www.100ben.net/yanqingxiaoshuo/");
        mBookTypes.put("推理小说", "https://www.100ben.net/tuilixiaoshuo/");
        mBookTypes.put("科幻小说", "https://www.100ben.net/kehuanxiaoshuo/");
        mBookTypes.put("人物传记", "https://www.100ben.net/renwuzhuanji/");
        mBookTypes.put("盗墓悬疑", "https://www.100ben.net/daomuxuanyi/");
        mBookTypes.put("玄幻穿越", "https://www.100ben.net/xuanhuanchuanyue/");
        mBookTypes.put("科普书籍", "https://www.100ben.net/kepushuji/");
        mBookTypes.put("100本书", "https://www.100ben.net/");
        mBookTypes.put("全部小说", "https://www.100ben.net/all/");
    }

    @Override
    public List<Book> getFindBooks(String html, BookType bookType) {
        List<Book> books = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        try {
            Element pageDiv = doc.getElementsByClass("page").first();
            String page = pageDiv.getElementsByTag("a").last().text();
            String pageStr = page.replace("末页(", "").replace(")", "");
            bookType.setPageSize(Integer.parseInt(pageStr));
        } catch (Exception ignored) {
        }

        Elements divs = doc.getElementsByClass("recommand");
        Element div = divs.get(0);
        Elements elementsByTag = div.getElementsByTag("li");
        for (Element element : elementsByTag) {
            Book book = new Book();
            String name = element.getElementsByClass("titles").first().getElementsByTag("a").first().text();
            book.setName(name);
            String author = element.getElementsByClass("author").first().text().replace("作者：", "");
            book.setAuthor(author);
            String imgUrl = element.getElementsByTag("img").first().attr("src");
            book.setImgUrl(imgUrl);
            String chapterUrl = element.getElementsByClass("titles").first().getElementsByTag("a").first().attr("href");
            book.setChapterUrl(chapterUrl);
            String desc = element.getElementsByClass("intro").first().text();
            book.setDesc(desc);
            book.setNewestChapterTitle("");
            book.setType(bookType.getTypeName());
            book.setSource(LocalBookSource.ben100.toString());
            books.add(book);
        }
        return books;
    }

    @Override
    public boolean getTypePage(BookType curType, int page) {
        if (curType.getPageSize() <= 0) {
            curType.setPageSize(1);
        }
        if (page > curType.getPageSize()) {
            return true;
        }
        if (curType.getTypeName().equals("100本书")) return false;
        if (curType.getUrl().contains("list")) {
            curType.setUrl(curType.getUrl().substring(0, curType.getUrl().lastIndexOf("_") + 1) + page + ".html");
        } else {
            curType.setUrl(curType.getUrl() + "list_" + page + ".html");
        }
        return false;
    }
}
