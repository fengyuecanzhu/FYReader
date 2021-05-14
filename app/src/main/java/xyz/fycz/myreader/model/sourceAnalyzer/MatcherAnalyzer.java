package xyz.fycz.myreader.model.sourceAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.ContentRule;
import xyz.fycz.myreader.greendao.entity.rule.InfoRule;
import xyz.fycz.myreader.greendao.entity.rule.SearchRule;
import xyz.fycz.myreader.greendao.entity.rule.TocRule;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

/**
 * @author fengyue
 * @date 2021/2/7 12:01
 */
public class MatcherAnalyzer extends BaseAnalyzer{

    private enum RegexMod {
        None, Both, Left, Right
    }

    /**
     * 匹配中间文本
     *
     * @param rule <text|html>用于占位，正则使用(*)
     * @param obj
     * @return
     */
    public List<String> getStringList(String rule, Object obj, boolean first) {
        String html = (String) obj;
        List<String> innerStrList = new ArrayList<>();
        if (StringHelper.isEmpty(rule)) return innerStrList;
        boolean isHtml = rule.contains("<html>");
        boolean hasFunction = rule.contains("##");
        String funs = "";
        if (hasFunction) {
            funs = rule.substring(rule.indexOf("##") + 2);
            rule = rule.substring(0, rule.indexOf("##"));
        }
        String[] rules = rule.split("<(html|text)>", 2);
        if (rules.length < 2) {
            return innerStrList;
        }
        RegexMod regexMod = getRegexMod(rules);
        int lIndex = 0;
        int rIndex = 0;
        Matcher lMatcher;
        Matcher rMatcher = null;
        switch (regexMod) {
            case None:
                while ((lIndex = html.indexOf(rules[0], lIndex)) != -1) {
                    lIndex += rules[0].length();
                    rIndex = html.indexOf(rules[1], lIndex);
                    if (rIndex < lIndex) break;
                    String str = html.substring(lIndex, rIndex);
                    if (hasFunction) str = evalFunction(funs, str);
                    if (isHtml) str = StringUtils.fromHtml(str);
                    if (StringHelper.isEmpty(str)) continue;
                    innerStrList.add(str);
                    if (first) break;
                }
                break;
            case Both:
                rules[0] = rules[0].replace("(*)", ".*");
                rules[1] = rules[1].replace("(*)", ".*");
                lMatcher = Pattern.compile(rules[0]).matcher(html);
                rMatcher = Pattern.compile(rules[1]).matcher(html);
                while (lMatcher.find()) {
                    lIndex = lMatcher.end();
                    while (rIndex < lIndex && rMatcher.find()) {
                        rIndex = rMatcher.start();
                    }
                    if (rIndex < lIndex) break;
                    String str = html.substring(lIndex, rIndex);
                    if (hasFunction) str = evalFunction(funs, str);
                    if (isHtml) str = StringUtils.fromHtml(str);
                    if (StringHelper.isEmpty(str)) continue;
                    innerStrList.add(str);
                    if (first) break;
                }
                break;
            case Left:
                rules[0] = rules[0].replace("(*)", ".*");
                lMatcher = Pattern.compile(rules[0]).matcher(html);
                while (lMatcher.find()) {
                    lIndex = lMatcher.end();
                    rIndex = html.indexOf(rules[1], lIndex);
                    if (rIndex < lIndex) break;
                    String str = html.substring(lIndex, rIndex);
                    if (hasFunction) str = evalFunction(funs, str);
                    if (isHtml) str = StringUtils.fromHtml(str);
                    if (StringHelper.isEmpty(str)) continue;
                    innerStrList.add(str);
                    if (first) break;
                }
                break;
            case Right:
                rules[1] = rules[1].replace("(*)", ".*");
                rMatcher = Pattern.compile(rules[1]).matcher(html);
                while ((lIndex = html.indexOf(rules[0], lIndex)) != -1) {
                    lIndex += rules[0].length();
                    while (rIndex < lIndex && rMatcher.find()) {
                        rIndex = rMatcher.start();
                    }
                    if (rIndex < lIndex) break;
                    String str = html.substring(lIndex, rIndex);
                    if (hasFunction) str = evalFunction(funs, str);
                    if (isHtml) str = StringUtils.fromHtml(str);
                    if (StringHelper.isEmpty(str)) continue;
                    innerStrList.add(str);
                    if (first) break;
                }
                break;
        }
        return innerStrList;
    }

    /**
     * 使用正则匹配章节列表
     *
     * @param rule <link>和<title>  ##!int:跳过前几个章节
     * @param html
     * @return
     */
    public ArrayList<Chapter> matchChapters(String rule, String html, String baseUrl) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        boolean hasFunction = rule.contains("##");
        String funs = "";
        if (hasFunction) {
            funs = rule.substring(rule.indexOf("##") + 2);
            rule = rule.substring(0, rule.indexOf("##"));
        }
        if (StringHelper.isEmpty(rule)) return chapters;
        if (!rule.contains("<link>") || !rule.contains("<title>")) return chapters;
        rule = rule.replace("(*)", ".*");
        int linkI = 1;
        int titleI = 2;
        if (rule.indexOf("<link>") > rule.indexOf("<title>")) {
            linkI = 2;
            titleI = 1;
        }
        rule = rule.replaceAll("[()]", "");
        rule = rule.replace("<link>", "(.*?)");
        rule = rule.replace("<title>", "(.*?)");
        Matcher matcher = Pattern.compile(rule).matcher(html);
        String lastTile = null;
        int j = 0;
        while (matcher.find()) {
            String title = matcher.group(titleI);
            if (!StringHelper.isEmpty(lastTile) && lastTile.equals(title)) continue;
            Chapter chapter = new Chapter();
            chapter.setNumber(j++);
            chapter.setTitle(title);
            chapter.setUrl(NetworkUtils.getAbsoluteURL(baseUrl, matcher.group(linkI)));
            chapters.add(chapter);
            lastTile = title;
        }
        return !hasFunction ? chapters : (ArrayList<Chapter>) evalListFunction(funs, chapters);
    }

    /**
     * 不使用正则匹配章节列表
     *
     * @param source  ##!int:跳过前几个章节
     * @param html
     * @param baseUrl
     * @return
     */
    public ArrayList<Chapter> matchChapters(BookSource source, String html, String baseUrl) {
        int skip = 0;
        String rule = source.getTocRule().getChapterList();
        if (rule.contains("##!")) {
            try {
                skip = Integer.parseInt(rule.substring(rule.indexOf("##!") + 3));
            } catch (Exception ignored) {
            }
            rule = rule.split("##")[0];
        }
        ArrayList<Chapter> chapters = new ArrayList<>();
        List<String> chapterStrList = getStringList(rule, html);
        String lastTile = null;
        int j = 0;
        for (int i = 0; i < chapterStrList.size(); i++) {
            if (i++ < skip) continue;
            String chapterStr = chapterStrList.get(i);
            String title = getString(source.getTocRule().getChapterName(), chapterStr);
            String url = getString(source.getTocRule().getChapterUrl(), chapterStr);
            if (!StringHelper.isEmpty(lastTile) && title.equals(lastTile)) continue;
            Chapter chapter = new Chapter();
            chapter.setNumber(j++);
            chapter.setTitle(title);
            chapter.setUrl(NetworkUtils.getAbsoluteURL(baseUrl, url));
            chapters.add(chapter);
            lastTile = title;
        }
        return chapters;
    }


    private RegexMod getRegexMod(String[] rules) {
        RegexMod regexMod;
        if (rules[0].contains("(*)") && rules[1].contains("(*)")) {
            regexMod = RegexMod.Both;
        } else if (rules[0].contains("(*)")) {
            regexMod = RegexMod.Left;
        } else if (rules[1].contains("(*)")) {
            regexMod = RegexMod.Right;
        } else {
            regexMod = RegexMod.None;
        }
        return regexMod;
    }

    /**
     * <dl>
     *     <dt><a href="https://www.bxwxorg.com/read/126774/"><img src="https://img.bxwxorg.com/126774/1104941.jpg" alt="都市大主宰" height="150" width="120"></a></dt>
     *     <dd><h3><a href="https://www.bxwxorg.com/read/126774/">都市大主宰</a></h3></dd>
     *     <dd class="book_other">作者：<span>赌霸</span>状态：<span>连载中</span>分类：<span>都市生活</span>字数：<span>327万字</span></dd>
     *     <dd class="book_des">《都市大主宰》乡村的孩子也可以在都市修练仙道，并且自在逍遥。一个奇遇改变了他的屌丝人生，从此，他慢慢地变得很强，直至最强。狂暴直爽的性格，注定他一生的不平凡。都市红尘，世俗世事，都市修真，这一切是多么的美好。因修炼“金刚淬体术”，他的人生变得很精彩。终于有一...</dd>
     *     <dd class="book_other">最新章节：<a href="https://www.bxwxorg.com/read/126774/4046182.html">第1468章　 生龙活虎</a> 更新时间：<span>2021-02-07 11:24</span></dd>
     * 	</dl>
     *
     * @return
     */
    public static BookSource getTestSource() {
        BookSource source = new BookSource();
        source.setSourceName("笔下文学");
        source.setSourceCharset("utf-8");
        source.setSourceUrl("https://www.bxwxorg.com/");
        source.setEnable(true);

        SearchRule searchRule = new SearchRule();
        searchRule.setSearchUrl("/search.html,searchtype=all&searchkey={key}");
        searchRule.setList("<div id=\"sitembox\"><text><div class=\"update_title\">");
        searchRule.setName("<h3><a href=\"(*)\"><text></a>");
        searchRule.setAuthor("作者：<span><text></span>");
        searchRule.setType("分类：<span><text></span>");
        searchRule.setDesc("<dd class=\"book_des\"><text></dd>");
        searchRule.setLastChapter("最新章节：<a href=\"(*)\"><text></a>");
        searchRule.setImgUrl("<img src=\"<text>\"");
        searchRule.setTocUrl("<dt><a href=\"<text>\">");
        searchRule.setRelatedWithInfo(false);
        source.setSearchRule(searchRule);

        TocRule tocRule = new TocRule();
        tocRule.setChapterList("<div id=\"list\"><text></div>");
        tocRule.setChapterName("<dd><a href=\"<link>\"><title></a></dd>##!12");
        source.setTocRule(tocRule);

        ContentRule contentRule = new ContentRule();
        contentRule.setContent("<div id=\"content\"><html></div>##@r(<p>因某些原因.*回家的路！,);@r(<p>喜欢.*最快。,)");
        source.setContentRule(contentRule);
        return source;
    }

    /**
     * <tr id="nr">
     * <td class="odd"><a href="/du/172/172477/">万界大主宰</a></td>
     * <td class="even"><a href="/du/172/172477/47331417.html" target="_blank"> 第一百六十章 死亡禁地</a></td>
     * <td class="odd">九掌柜</td>
     * <td class="even">1386K</td>
     * <td class="odd" align="center">20-09-20</td>
     * <td class="even" align="center">连载</td>
     * </tr>
     *
     * @return
     */
    public static BookSource getTestSource2() {
        BookSource source = new BookSource();
        source.setSourceName("顶点小说");
        source.setSourceCharset("gbk");
        source.setSourceUrl("https://www.23wx.cc");
        source.setEnable(true);

        SearchRule searchRule = new SearchRule();
        searchRule.setSearchUrl("/modules/article/search.php?searchkey={key}");
        searchRule.setCharset("gbk");
        searchRule.setList("<table class=\"grid\" width=\"100%\" align=\"center\"><text></table>");
        searchRule.setName("<td class=\"odd\"><a href=\"(*)\"><text></a></td>");
        searchRule.setAuthor("<td class=\"odd\"><text></td>##@nc(<a)");
        searchRule.setLastChapter("<a href=\"(*)\" target=\"_blank\"><text></a>");
        searchRule.setTocUrl("<td class=\"odd\"><a href=\"<text>\">");
        searchRule.setRelatedWithInfo(true);
        searchRule.setWordCount("<td class=\"even\"><text></td>##@nc(<a)");
        source.setSearchRule(searchRule);

        InfoRule infoRule = new InfoRule();
        infoRule.setName("<meta property=\"og:title\" content=\"<text>\" />");
        infoRule.setAuthor("<meta name=\"og:novel:author\" content=\"<text>\" />");
        infoRule.setLastChapter("<meta name=\"og:novel:latest_chapter_name\" content=\"<text>\" />");
        infoRule.setType("<meta name=\"og:novel:category\" content=\"<text>\" />");
        infoRule.setDesc("<div id=\"intro\"><html></div>");
//        source.setRuleImgUrl("<div id=\"fmimg\"><img src=\"<text>\"");
        infoRule.setImgUrl("<meta property=\"og:image\" content=\"<text>\" />");
        infoRule.setTocUrl("<meta name=\"og:novel:read_url\" content=\"<text>\" />");
        source.setInfoRule(infoRule);

        TocRule tocRule = new TocRule();
        tocRule.setChapterBaseUrl("<meta name=\"og:novel:read_url\" content=\"<text>\" />");
        tocRule.setChapterList("<div id=\"list\"><text></div>");
        tocRule.setChapterName("<dd><a href=\"<link>\"><title></a></dd>");
        source.setTocRule(tocRule);

        ContentRule contentRule = new ContentRule();
        contentRule.setContent("<div id=\"content\"><html></div>");
        source.setContentRule(contentRule);
        return source;
    }

}
