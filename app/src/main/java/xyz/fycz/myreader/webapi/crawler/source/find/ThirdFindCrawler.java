/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.webapi.crawler.source.find;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import javax.script.SimpleBindings;

import io.reactivex.Observable;
import xyz.fycz.myreader.entity.FindKind;
import xyz.fycz.myreader.entity.StrResponse;
import xyz.fycz.myreader.entity.thirdsource.source3.ExploreKind3;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.entity.rule.FindRule;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.model.third2.analyzeRule.AnalyzeRule;
import xyz.fycz.myreader.util.utils.GsonUtils;
import xyz.fycz.myreader.util.utils.StringUtils;
import xyz.fycz.myreader.webapi.crawler.base.BaseFindCrawler;

import static xyz.fycz.myreader.common.APPCONST.SCRIPT_ENGINE;

/**
 * @author fengyue
 * @date 2021/7/22 22:30
 */
public class ThirdFindCrawler extends BaseFindCrawler {
    private BookSource source;
    private FindRule findRuleBean;
    private AnalyzeRule analyzeRule;
    private String findError = "发现规则语法错误";

    public ThirdFindCrawler(BookSource source) {
        this.source = source;
        findRuleBean = source.getFindRule();
    }

    public BookSource getSource() {
        return source;
    }

    @Override
    public String getName() {
        return source.getSourceName();
    }

    @Override
    public String getTag() {
        return source.getSourceUrl();
    }

    @Override
    public Observable<Boolean> initData() {
        return Observable.create(emitter -> {
            try {
                if (StringUtils.isJsonArray(findRuleBean.getUrl())) {
                    List<ExploreKind3> kinds = GsonUtils.parseJArray(findRuleBean.getUrl(), ExploreKind3.class);
                    StringBuilder sb = new StringBuilder();
                    for (ExploreKind3 kind : kinds){
                        String url = kind.getUrl() == null ? "" : kind.getUrl();
                        sb.append(kind.getTitle()).append("::").append(url).append("\n");
                    }
                    if (sb.length() > 0){
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    findRuleBean.setUrl(sb.toString());
                }
                String[] kindA;
                String findRule;
                if (!TextUtils.isEmpty(findRuleBean.getUrl()) && !source.containsGroup(findError)) {
                    boolean isJs = findRuleBean.getUrl().startsWith("<js>");
                    if (isJs) {
                        String jsStr = findRuleBean.getUrl().substring(4, findRuleBean.getUrl().lastIndexOf("<"));
                        findRule = evalJS(jsStr, source.getSourceUrl()).toString();
                    } else {
                        findRule = findRuleBean.getUrl();
                    }
                    kindA = findRule.split("(&&|\n)+");
                    List<FindKind> children = new ArrayList<>();
                    String groupName = getName();
                    int nameCount = 0;
                    for (String kindB : kindA) {
                        kindB = StringUtils.trim(kindB);
                        if (TextUtils.isEmpty(kindB)) continue;
                        String[] kind = kindB.split("::");
                        if (kind.length == 0) {
                            if (children.size() > 0) {
                                nameCount++;
                                kindsMap.put(groupName, children);
                                children = new ArrayList<>();
                            }
                            groupName = getName() + "[" + nameCount + "]";
                        } else if (kind.length == 1) {
                            if (children.size() > 0) {
                                kindsMap.put(groupName, children);
                                children = new ArrayList<>();
                            }
                            groupName = kind[0].replaceAll("\\s", "");
                        } else {
                            FindKind findKindBean = new FindKind();
                            findKindBean.setTag(source.getSourceUrl());
                            findKindBean.setName(kind[0].replaceAll("\\s", ""));
                            findKindBean.setUrl(kind[1]);
                            children.add(findKindBean);
                        }
                    }
                    if (children.size() > 0) {
                        kindsMap.put(groupName, children);
                    }
                }
                emitter.onNext(true);
            } catch (Exception exception) {
                source.addGroup(findError);
                BookSourceManager.addBookSource(source);
                emitter.onNext(false);
            }
            emitter.onComplete();
        });
    }

    @Override
    public Observable<List<Book>> getFindBooks(StrResponse strResponse, FindKind kind) {
        return null;
    }

    /**
     * 执行JS
     */
    private Object evalJS(String jsStr, String baseUrl) throws Exception {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("java", getAnalyzeRule());
        bindings.put("baseUrl", baseUrl);
        return SCRIPT_ENGINE.eval(jsStr, bindings);
    }

    private AnalyzeRule getAnalyzeRule() {
        if (analyzeRule == null) {
            analyzeRule = new AnalyzeRule(null);
        }
        return analyzeRule;
    }
}
