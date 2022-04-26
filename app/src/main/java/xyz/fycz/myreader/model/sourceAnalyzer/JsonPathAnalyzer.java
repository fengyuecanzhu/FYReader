/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.model.sourceAnalyzer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.util.help.StringHelper;

/**
 * @author fengyue
 * @date 2021/2/15 9:08
 */
public class JsonPathAnalyzer extends BaseAnalyzer {

    private final ParseContext parseContext = JsonPath.using(Configuration.builder()
            .jsonProvider(new GsonJsonProvider())
            .options(Option.ALWAYS_RETURN_LIST)
            .build());

    @Override
    public List<String> getStringList(String rule, Object obj, boolean isFirst) {
        return getStringList(rule, getReadContext(obj), isFirst);
    }

    public List<String> getStringList(String rule, ReadContext rc, boolean isFirst) {
        List<String> list = new ArrayList<>();
        if (StringHelper.isEmpty(rule)) return list;
        boolean hasFunction = rule.contains("##");
        String funs = "";
        if (hasFunction) {
            funs = rule.substring(rule.indexOf("##") + 2);
            rule = rule.substring(0, rule.indexOf("##"));
        }
        JsonArray temp = rc.read(rule);
        for (JsonElement element : temp) {
            String str = element.toString();
            if (str.startsWith("\"")) str = str.substring(1, str.length() - 1);
            if (hasFunction) str = evalFunction(funs, str);
            if (StringHelper.isEmpty(str)) continue;
            list.add(str);
            if (isFirst) break;
        }
        return list;
    }

    /**
     * @param rule ##!int:跳过前几个节点
     * @param rc
     * @return
     */
    public List<ReadContext> getReadContextList(String rule, ReadContext rc) {
        List<ReadContext> list = new ArrayList<>();
        boolean hasFunction = rule.contains("##");
        String funs = "";
        if (hasFunction) {
            funs = rule.substring(rule.indexOf("##") + 2);
            rule = rule.substring(0, rule.indexOf("##"));
        }
        if (StringHelper.isEmpty(rule)) return list;
        JsonArray temp = rc.read(rule);
        for (JsonElement element : temp) {
            String str = element.toString();
            if (str.startsWith("\"")) str = str.substring(1, str.length() - 1);
            list.add(getReadContext(str));
        }
        return !hasFunction ? list : evalListFunction(funs, list);
    }

    public ReadContext getReadContext(Object obj) {
        if (obj instanceof ReadContext) return (ReadContext) obj;
        if (obj instanceof String) return parseContext.parse((String) obj);
        return parseContext.parse(obj);
    }
}
