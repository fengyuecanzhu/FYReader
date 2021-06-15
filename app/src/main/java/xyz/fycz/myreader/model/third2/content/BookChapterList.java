package xyz.fycz.myreader.model.third2.content;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.nodes.Element;
import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.entity.WebChapterBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.TocRule;
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeByRegex;
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeRule;
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeUrl;
import xyz.fycz.myreader.util.utils.OkHttpUtils;

public class BookChapterList {
    private String tag;
    private BookSource source;
    private TocRule tocRule;
    private List<WebChapterBean> webChapterBeans;
    private boolean dx = false;
    private boolean analyzeNextUrl;
    private CompositeDisposable compositeDisposable;
    private String chapterListUrl;

    public BookChapterList(String tag, BookSource source, boolean analyzeNextUrl) {
        this.tag = tag;
        this.source = source;
        this.analyzeNextUrl = analyzeNextUrl;
        tocRule = source.getTocRule();
    }

    public Observable<List<Chapter>> analyzeChapterList(final String s, final Book book, Map<String, String> headerMap) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("目录获取失败" + book.getChapterUrl()));
                return;
            } else {
                Log.d(tag, "┌成功获取目录页");
                Log.d(tag, "└" + book.getChapterUrl());
            }
            book.setTag(tag);
            AnalyzeRule analyzer = new AnalyzeRule(book);
            String ruleChapterList = tocRule.getChapterList();
            if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
                dx = true;
                ruleChapterList = ruleChapterList.substring(1);
            }
            chapterListUrl = book.getChapterUrl();
            WebChapterBean webChapterBean = analyzeChapterList(s, chapterListUrl, ruleChapterList, analyzeNextUrl, analyzer, dx);
            final List<Chapter> chapterList = webChapterBean.getData();

            final List<String> chapterUrlS = new ArrayList<>(webChapterBean.getNextUrlList());
            if (chapterUrlS.isEmpty() || !analyzeNextUrl) {
                finish(chapterList, e);
            }
            //下一页为单页
            else if (chapterUrlS.size() == 1) {
                List<String> usedUrl = new ArrayList<>();
                usedUrl.add(book.getChapterUrl());
                //循环获取直到下一页为空
                Log.d(tag, "正在加载下一页");
                while (!chapterUrlS.isEmpty() && !usedUrl.contains(chapterUrlS.get(0))) {
                    usedUrl.add(chapterUrlS.get(0));
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterUrlS.get(0), headerMap, tag);
                    try {
                        String body;
                        StrResponse response = OkHttpUtils.getStrResponse(analyzeUrl)
                                .blockingFirst();
                        body = response.body();
                        webChapterBean = analyzeChapterList(body, chapterUrlS.get(0), ruleChapterList, false, analyzer, dx);
                        chapterList.addAll(webChapterBean.getData());
                        chapterUrlS.clear();
                        chapterUrlS.addAll(webChapterBean.getNextUrlList());
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                }
                Log.d(tag, "下一页加载完成共" + usedUrl.size() + "页");
                finish(chapterList, e);
            }
            //下一页为多页
            else {
                Log.d(tag, "正在加载其它" + chapterUrlS.size() + "页");
                compositeDisposable = new CompositeDisposable();
                webChapterBeans = new ArrayList<>();
                AnalyzeNextUrlTask.Callback callback = new AnalyzeNextUrlTask.Callback() {
                    @Override
                    public void addDisposable(Disposable disposable) {
                        compositeDisposable.add(disposable);
                    }

                    @Override
                    public void analyzeFinish(WebChapterBean bean, List<Chapter> chapterListBeans) {
                        if (nextUrlFinish(bean, chapterListBeans)) {
                            for (WebChapterBean chapterBean : webChapterBeans) {
                                chapterList.addAll(chapterBean.getData());
                            }
                            Log.d(tag, "其它页加载完成,目录共" + chapterList.size() + "条");
                            finish(chapterList, e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        compositeDisposable.dispose();
                        e.onError(throwable);
                    }
                };
                for (String url : chapterUrlS) {
                    final WebChapterBean bean = new WebChapterBean(url);
                    webChapterBeans.add(bean);
                }
                for (WebChapterBean bean : webChapterBeans) {
                    BookChapterList bookChapterList = new BookChapterList(tag, source, false);
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(bean.getUrl(), headerMap, tag);
                    new AnalyzeNextUrlTask(bookChapterList, bean, book, headerMap)
                            .setCallback(callback)
                            .analyzeUrl(analyzeUrl);
                }
            }
        });
    }

    private synchronized boolean nextUrlFinish(WebChapterBean webChapterBean, List<Chapter> bookChapterBeans) {
        webChapterBean.setData(bookChapterBeans);
        for (WebChapterBean bean : webChapterBeans) {
            if (bean.noData()) return false;
        }
        return true;
    }

    private void finish(List<Chapter> chapterList, Emitter<List<Chapter>> emitter) {
        //去除重复,保留后面的,先倒序,从后面往前判断
        if (!dx) {
            Collections.reverse(chapterList);
        }
        LinkedHashSet<Chapter> lh = new LinkedHashSet<>(chapterList);
        chapterList = new ArrayList<>(lh);
        Collections.reverse(chapterList);
        Log.d(tag, "-目录解析完成" + analyzeNextUrl);
        if (chapterList.isEmpty()) {
            emitter.onError(new Throwable("目录列表为空"));
            return;
        }
        emitter.onNext(chapterList);
        emitter.onComplete();
    }

    private WebChapterBean analyzeChapterList(String s, String chapterUrl, String ruleChapterList,
                                              boolean printLog, AnalyzeRule analyzer, boolean dx) throws Exception {
        List<String> nextUrlList = new ArrayList<>();
        analyzer.setContent(s, chapterUrl);
        if (!TextUtils.isEmpty(tocRule.getTocUrlNext()) && analyzeNextUrl) {
            if (printLog) Log.d(tag, "┌获取目录下一页网址");
            nextUrlList = analyzer.getStringList(tocRule.getTocUrlNext(), true);
            int thisUrlIndex = nextUrlList.indexOf(chapterUrl);
            if (thisUrlIndex != -1) {
                nextUrlList.remove(thisUrlIndex);
            }
            if (printLog) Log.d(tag, "└" + nextUrlList.toString());
        }

        List<Chapter> chapterBeans = new ArrayList<>();
        if (printLog) Log.d(tag,  "┌解析目录列表");
        // 仅使用java正则表达式提取目录列表
        if (ruleChapterList.startsWith(":")) {
            ruleChapterList = ruleChapterList.substring(1);
            regexChapter(s, ruleChapterList.split("&&"), 0, analyzer, chapterBeans);
            if (chapterBeans.size() == 0) {
                if (printLog)  Log.d(tag, "└找到 0 个章节");
                return new WebChapterBean(chapterBeans, new LinkedHashSet<>(nextUrlList));
            }
        }
        // 使用AllInOne规则模式提取目录列表
        else if (ruleChapterList.startsWith("+")) {
            ruleChapterList = ruleChapterList.substring(1);
            List<Object> collections = analyzer.getElements(ruleChapterList);
            if (collections.size() == 0) {
                Log.d(tag, "└找到 0 个章节");
                return new WebChapterBean(chapterBeans, new LinkedHashSet<>(nextUrlList));
            }
            String nameRule = tocRule.getChapterName();
            String linkRule = tocRule.getChapterUrl();
            String name = "";
            String link = "";
            for (Object object : collections) {
                if (object instanceof NativeObject) {
                    name = String.valueOf(((NativeObject) object).get(nameRule));
                    link = String.valueOf(((NativeObject) object).get(linkRule));
                } else if (object instanceof Element) {
                    name = ((Element) object).text();
                    link = ((Element) object).attr(linkRule);
                }
                addChapter(chapterBeans, name, link);
            }
        }
        // 使用默认规则解析流程提取目录列表
        else {
            List<Object> collections = analyzer.getElements(ruleChapterList);
            if (collections.size() == 0) {
                Log.d(tag, "└找到 0 个章节");
                return new WebChapterBean(chapterBeans, new LinkedHashSet<>(nextUrlList));
            }
            List<AnalyzeRule.SourceRule> nameRule = analyzer.splitSourceRule(tocRule.getChapterName());
            List<AnalyzeRule.SourceRule> linkRule = analyzer.splitSourceRule(tocRule.getChapterUrl());
            for (Object object : collections) {
                analyzer.setContent(object, chapterUrl);
                addChapter(chapterBeans, analyzer.getString(nameRule), analyzer.getString(linkRule));
            }
        }
        if (printLog) Log.d(tag,"└找到 " + chapterBeans.size() + " 个章节");
        Chapter firstChapter;
        if (dx) {
            if (printLog) Log.d(tag, "-倒序");
            firstChapter = chapterBeans.get(chapterBeans.size() - 1);
        } else {
            firstChapter = chapterBeans.get(0);
        }
        if (printLog) Log.d(tag, "┌获取章节名称");
        if (printLog) Log.d(tag,  "└" + firstChapter.getTitle());
        if (printLog) Log.d(tag,  "┌获取章节网址");
        if (printLog) Log.d(tag,  "└" + firstChapter.getUrl());
        return new WebChapterBean(chapterBeans, new LinkedHashSet<>(nextUrlList));
    }

    private void addChapter(final List<Chapter> chapterBeans, String name, String link) {
        if (TextUtils.isEmpty(name)) return;
        if (TextUtils.isEmpty(link)) link = chapterListUrl;
        Chapter chapter = new Chapter();
        chapter.setTitle(name);
        chapter.setUrl(link);
        chapterBeans.add(chapter);
    }

    // region 纯java模式正则表达式获取目录列表
    private void regexChapter(String str, String[] regex, int index, AnalyzeRule analyzer, final List<Chapter> chapterBeans) throws Exception {
        Matcher resM = Pattern.compile(regex[index]).matcher(str);
        if (!resM.find()) {
            return;
        }
        if (index + 1 == regex.length) {
            // 获取解析规则
            String nameRule = tocRule.getChapterName();
            String linkRule = tocRule.getChapterUrl();
            if (TextUtils.isEmpty(nameRule) || TextUtils.isEmpty(linkRule)) return;
            // 替换@get规则
            nameRule = analyzer.replaceGet(tocRule.getChapterName());
            linkRule = analyzer.replaceGet(tocRule.getChapterUrl());
            // 分离规则参数
            List<String> nameParams = new ArrayList<>();
            List<Integer> nameGroups = new ArrayList<>();
            AnalyzeByRegex.splitRegexRule(nameRule, nameParams, nameGroups);
            List<String> linkParams = new ArrayList<>();
            List<Integer> linkGroups = new ArrayList<>();
            AnalyzeByRegex.splitRegexRule(linkRule, linkParams, linkGroups);
            // 是否包含VIP规则(hasVipRule>1 时视为包含vip规则)
            int hasVipRule = 0;
            for (int i = nameGroups.size(); i-- > 0; ) {
                if (nameGroups.get(i) != 0) {
                    ++hasVipRule;
                }
            }
            String vipNameGroup = "";
            int vipNumGroup = 0;
            if ((nameGroups.get(0) != 0) && (hasVipRule > 1)) {
                vipNumGroup = nameGroups.remove(0);
                vipNameGroup = nameParams.remove(0);
            }
            // 创建结果缓存
            StringBuilder cName = new StringBuilder();
            StringBuilder cLink = new StringBuilder();
            // 提取书籍目录
            if (vipNumGroup != 0) {
                do {
                    cName.setLength(0);
                    cLink.setLength(0);
                    for (int i = nameParams.size(); i-- > 0; ) {
                        if (nameGroups.get(i) > 0) {
                            cName.insert(0, resM.group(nameGroups.get(i)));
                        } else if (nameGroups.get(i) < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cName.insert(0, resM.group(nameParams.get(i)));
                        } else {
                            cName.insert(0, nameParams.get(i));
                        }
                    }
                    if (vipNumGroup > 0) {
                        cName.insert(0, resM.group(vipNumGroup) == null ? "" : "\uD83D\uDD12");
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        cName.insert(0, resM.group(vipNameGroup) == null ? "" : "\uD83D\uDD12");
                    } else {
                        cName.insert(0, vipNameGroup);
                    }

                    for (int i = linkParams.size(); i-- > 0; ) {
                        if (linkGroups.get(i) > 0) {
                            cLink.insert(0, resM.group(linkGroups.get(i)));
                        } else if (linkGroups.get(i) < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cLink.insert(0, resM.group(linkParams.get(i)));
                        } else {
                            cLink.insert(0, linkParams.get(i));
                        }
                    }

                    addChapter(chapterBeans, cName.toString(), cLink.toString());
                } while (resM.find());
            } else {
                do {
                    cName.setLength(0);
                    cLink.setLength(0);
                    for (int i = nameParams.size(); i-- > 0; ) {
                        if (nameGroups.get(i) > 0) {
                            cName.insert(0, resM.group(nameGroups.get(i)));
                        } else if (nameGroups.get(i) < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cName.insert(0, resM.group(nameParams.get(i)));
                        } else {
                            cName.insert(0, nameParams.get(i));
                        }
                    }

                    for (int i = linkParams.size(); i-- > 0; ) {
                        if (linkGroups.get(i) > 0) {
                            cLink.insert(0, resM.group(linkGroups.get(i)));
                        } else if (linkGroups.get(i) < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cLink.insert(0, resM.group(linkParams.get(i)));
                        } else {
                            cLink.insert(0, linkParams.get(i));
                        }
                    }

                    addChapter(chapterBeans, cName.toString(), cLink.toString());
                } while (resM.find());
            }
        } else {
            StringBuilder result = new StringBuilder();
            do {
                result.append(resM.group(0));
            } while (resM.find());
            regexChapter(result.toString(), regex, ++index, analyzer, chapterBeans);
        }
    }
    // endregion
}