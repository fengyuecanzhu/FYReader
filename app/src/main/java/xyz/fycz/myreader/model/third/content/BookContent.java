package xyz.fycz.myreader.model.third.content;

import android.text.TextUtils;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import io.reactivex.Observable;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.ContentRule;
import xyz.fycz.myreader.greendao.gen.ChapterDao;
import xyz.fycz.myreader.model.third.analyzeRule.AnalyzeRule;
import xyz.fycz.myreader.model.third.analyzeRule.AnalyzeUrl;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

import static xyz.fycz.myreader.common.APPCONST.JS_PATTERN;


public class BookContent {
    private String tag;
    private ContentRule contentRule;
    private String ruleBookContent;
    private String baseUrl;

    public BookContent(String tag, BookSource bookSource) {
        this.tag = tag;
        this.contentRule = bookSource.getContentRule();
        ruleBookContent = contentRule.getContent();
        if (ruleBookContent.startsWith("$") && !ruleBookContent.startsWith("$.")) {
            ruleBookContent = ruleBookContent.substring(1);
            Matcher jsMatcher = JS_PATTERN.matcher(ruleBookContent);
            if (jsMatcher.find()) {
                ruleBookContent = ruleBookContent.replace(jsMatcher.group(), "");
            }
        }
    }

   public Observable<String> analyzeBookContent(final StrResponse response, final Chapter chapterBean, final Chapter nextChapterBean, Book book, Map<String, String> headerMap) {
        baseUrl = NetworkUtils.getUrl(response.getResponse());
        return analyzeBookContent(response.body(), chapterBean, nextChapterBean, book, headerMap);
    }

    public Observable<String> analyzeBookContent(final String s, final Chapter chapterBean, final Chapter nextChapterBean, Book book, Map<String, String> headerMap) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("内容获取失败" + chapterBean.getUrl()));
                return;
            }
            if (TextUtils.isEmpty(baseUrl)) {
                baseUrl = NetworkUtils.getAbsoluteURL(book.getChapterUrl(), chapterBean.getUrl());
            }
            Log.d(tag, "┌成功获取正文页");
            Log.d(tag, "└" + baseUrl);

            AnalyzeRule analyzer = new AnalyzeRule(book);
            WebContentBean webContentBean = analyzeBookContent(analyzer, s, chapterBean.getUrl(), baseUrl);

            StringBuilder content = new StringBuilder();
            content.append(webContentBean.content);

             /* 处理分页
             */
            if (!TextUtils.isEmpty(webContentBean.nextUrl)) {
                List<String> usedUrlList = new ArrayList<>();
                usedUrlList.add(chapterBean.getUrl());
                Chapter nextChapter;
                if (nextChapterBean != null) {
                    nextChapter = nextChapterBean;
                } else {
                    nextChapter = DbManager.getDaoSession().getChapterDao().queryBuilder()
                            .where(ChapterDao.Properties.Url.eq(chapterBean.getUrl()),
                                    ChapterDao.Properties.Number.eq(chapterBean.getNumber() + 1))
                            .build().unique();
                }

                while (!TextUtils.isEmpty(webContentBean.nextUrl) && !usedUrlList.contains(webContentBean.nextUrl)) {
                    usedUrlList.add(webContentBean.nextUrl);
                    if (nextChapter != null && NetworkUtils.getAbsoluteURL(baseUrl, webContentBean.nextUrl).equals(NetworkUtils.getAbsoluteURL(baseUrl, nextChapter.getUrl()))) {
                        break;
                    }
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(webContentBean.nextUrl, headerMap, tag);
                    try {
                        String body;
                        StrResponse response = OkHttpUtils.getStrResponse(analyzeUrl).blockingFirst();
                        body = response.body();
                        webContentBean = analyzeBookContent(analyzer, body, webContentBean.nextUrl, baseUrl);
                        if (!TextUtils.isEmpty(webContentBean.content)) {
                            content.append("\n").append(webContentBean.content);
                        }
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                }
            }
            /*String replaceRule = bookSourceBean.getRuleBookContentReplace();
            if (replaceRule != null && replaceRule.trim().length() > 0) {
                analyzer.setContent(bookContentBean.getDurChapterContent());
                bookContentBean.setDurChapterContent(analyzer.getString(replaceRule));
            }*/
            e.onNext(content.toString());
            e.onComplete();
        });
    }

    public WebContentBean analyzeBookContent(AnalyzeRule analyzer, final String s, final String chapterUrl, String baseUrl) throws Exception {
        WebContentBean webContentBean = new WebContentBean();

        analyzer.setContent(s, NetworkUtils.getAbsoluteURL(baseUrl, chapterUrl));
        Log.d(tag, "┌解析正文内容");
        if (ruleBookContent.equals("all") || ruleBookContent.contains("@all")) {
            webContentBean.content = analyzer.getString(ruleBookContent);
        }
        else {
            webContentBean.content = StringUtils.formatHtml(analyzer.getString(ruleBookContent));
        }
        Log.d(tag,  "└" + webContentBean.content);
        String nextUrlRule = contentRule.getContentUrlNext();
        if (!TextUtils.isEmpty(nextUrlRule)) {
            Log.d(tag, "┌解析下一页url");
            webContentBean.nextUrl = analyzer.getString(nextUrlRule, true);
            Log.d(tag, "└" + webContentBean.nextUrl);
        }

        return webContentBean;
    }

    private static class WebContentBean {
        private String content;
        private String nextUrl;

        private WebContentBean() {

        }
    }
}
