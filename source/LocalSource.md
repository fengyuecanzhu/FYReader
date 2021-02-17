## 风月读书内置书源说明

* 如何自行制作并添加书源.

  * 基于面向接口开发的思想，对于书源我设计了如下接口：

    * ```java
      // 这个接口位于xyz.fycz.myreader.webapi.crawler.base包下
      public interface ReadCrawler {
          String getSearchLink();  // 书源的搜索url
          String getCharset(); // 书源的字符编码
          String getSearchCharset(); // 书源搜索关键字的字符编码，和书源的字符编码就行
          String getNameSpace(); // 书源主页地址
          Boolean isPost(); // 是否以post请求搜索
          String getContentFormHtml(String html); // 获取书籍内容规则
          ArrayList<Chapter> getChaptersFromHtml(String html); // 获取书籍章节列表规则
          ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html); // 搜索书籍规则
      }
      ```

  * 了解上述接口的方法，我们就可以开始创建书源了

    * 第一步：创建一个书源类实现上述接口，下面以笔趣阁44为例进行说明

      * ```java
        // 注意：如果搜索书籍页没有图片、最新章节、书籍简介等信息，可以通过实现BookInfoCrawler接口，从书籍详情页获取
        public class BiQuGe44ReadCrawler implements ReadCrawler, BookInfoCrawler {
            //网站主页地址
            public static final String NAME_SPACE = "https://www.wqge.cc";
            /*
            	搜索url，搜索关键词以{key}进行占位
            	如果是post请求，以“,”分隔url，“,”前是搜索地址，“,”后是请求体，搜索关键词同样以{key}占位
            		例如："https://www.9txs.com/search.html,searchkey={key}"
            */
            public static final String NOVEL_SEARCH = "https://www.wqge.cc/modules/article/search.php?searchkey={key}";
            // 书源字符编码
            public static final String CHARSET = "GBK";
            // 书源搜索关键词编码
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
             * @param html
             * @return
             */
            public String getContentFormHtml(String html) {
                Document doc = Jsoup.parse(html);
                Element divContent = doc.getElementById("content");
                if (divContent != null) {
                    String content = Html.fromHtml(divContent.html()).toString();
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
                String readUrl = doc.select("meta[property=og:novel:read_url]").attr("content");
                Element divList = doc.getElementById("list");
                String lastTile = null;
                int i = 0;
                Elements elementsByTag = divList.getElementsByTag("dd");
                for (int j = 9; j < elementsByTag.size(); j++) {
                    Element dd = elementsByTag.get(j);
                    Elements as = dd.getElementsByTag("a");
                    if (as.size() > 0) {
                        Element a = as.get(0);
                        String title = a.text() ;
                        if (!StringHelper.isEmpty(lastTile) && title.equals(lastTile)) {
                            continue;
                        }
                        Chapter chapter = new Chapter();
                        chapter.setNumber(i++);
                        chapter.setTitle(title);
                        String url = readUrl + a.attr("href");
                        chapter.setUrl(url);
                        chapters.add(chapter);
                        lastTile = title;
                    }
                }
                return chapters;
            }

            /**
             * 从搜索html中得到书列表
             * @param html
             * @return
             */
            public ConcurrentMultiValueMap<SearchBookBean, Book> getBooksFromSearchHtml(String html) {
                ConcurrentMultiValueMap<SearchBookBean, Book> books = new ConcurrentMultiValueMap<>();
                Document doc = Jsoup.parse(html);
                Elements divs = doc.getElementsByTag("table");
                Element div = divs.get(0);
                Elements elementsByTag = div.getElementsByTag("tr");
                for (int i = 1; i < elementsByTag.size(); i++) {
                    Element element = elementsByTag.get(i);
                    Book book = new Book();
                    Elements info = element.getElementsByTag("td");
                    book.setName(info.get(0).text());
                    book.setChapterUrl(NAME_SPACE + info.get(0).getElementsByTag("a").attr("href"));
                    book.setAuthor(info.get(2).text());
                    book.setNewestChapterTitle(info.get(1).text());
                    book.setSource(BookSource.biquge44.toString());
                    // SearchBookBean用于合并相同书籍
                    SearchBookBean sbb = new SearchBookBean(book.getName(), book.getAuthor());
                    books.add(sbb, book);
                }
                return books;
            }

            /**
             * 获取书籍详细信息
             * @param book
             */
            public Book getBookInfo(String html, Book book){
                Document doc = Jsoup.parse(html);
                Element img = doc.getElementById("fmimg");
                book.setImgUrl(img.getElementsByTag("img").get(0).attr("src"));
                Element desc = doc.getElementById("intro");
                book.setDesc(desc.getElementsByTag("p").get(0).text());
                Element type = doc.getElementsByClass("con_top").get(0);
                book.setType(type.getElementsByTag("a").get(2).text());
                return book;
            }

        }
        ```

    * 第二步：添加书源到数据库。
      ```java
        BookSource source = new BookSource();
        source.setSourceEName("ename");//这是内置书源标识，必填
        source.setSourceName(source.text);//设置书源名称
        source.setSourceGroup("内置书源");//设置书源分组
        source.setEnable(true);//设置书源可用性
        source.setSourceUrl("xyz.fycz.myreader.webapi.crawler.read.BiQuGe44ReadCrawler");//这是书源完整类路径，必填
        source.setOrderNum(0);//内置书源一般设置排序为0
        GreenDaoManager.getDaoSession().getBookSourceDao().insertOrReplace(source);//添加书源进数据库
      ```