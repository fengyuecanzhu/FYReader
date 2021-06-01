package xyz.fycz.myreader.model.sourceAnalyzer;

import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.util.help.StringHelper;

/**
 * @author fengyue
 * @date 2021/2/14 19:03
 */
public class XpathAnalyzer extends BaseAnalyzer {

    @Override
    public List<String> getStringList(String rule, Object obj, boolean first) {
        if (obj instanceof JXDocument) {
            JXDocument jxDoc = (JXDocument) obj;
            return getStringList(rule, jxDoc, first);
        } else if (obj instanceof JXNode) {
            JXNode jxNode = (JXNode) obj;
            return getStringList(rule, jxNode, first);
        } else {
            return new ArrayList<>();
        }
    }

    public List<String> getStringList(String rule, JXDocument jxDoc, boolean isFirst) {
        List<String> list = new ArrayList<>();
        if (StringHelper.isEmpty(rule)) return list;
        boolean hasFunction = rule.contains("##");
        String funs = "";
        if (hasFunction) {
            funs = rule.substring(rule.indexOf("##") + 2);
            rule = rule.substring(0, rule.indexOf("##"));
        }
        for (JXNode jxNode : jxDoc.selN(rule)) {
            String str = jxNode.toString();
            if (hasFunction) str = evalFunction(funs, str);
            if (StringHelper.isEmpty(str)) continue;
            list.add(str);
            if (isFirst) break;
        }
        return list;
    }

    public List<String> getStringList(String rule, JXNode jxNode, boolean isFirst) {
        List<String> list = new ArrayList<>();
        if (StringHelper.isEmpty(rule)) return list;
        boolean hasFunction = rule.contains("##");
        String funs = "";
        if (hasFunction) {
            funs = rule.substring(rule.indexOf("##") + 2);
            rule = rule.substring(0, rule.indexOf("##"));
        }
        for (JXNode jxNode1 : jxNode.sel(rule)) {
            String str = jxNode1.toString();
            if (hasFunction) str = evalFunction(funs, str);
            if (StringHelper.isEmpty(str)) continue;
            list.add(str);
            if (isFirst) break;
        }
        return list;
    }

    /**
     * @param rule  ##!int:跳过前几个节点
     * @param obj
     * @return
     */
    public List<JXNode> getJXNodeList(String rule, Object obj) {
        if (obj instanceof JXDocument) {
            JXDocument jxDoc = (JXDocument) obj;
            return getJXNodeList(rule, jxDoc);
        } else if (obj instanceof JXNode) {
            JXNode jxNode = (JXNode) obj;
            return getJXNodeList(rule, jxNode);
        } else {
            return new ArrayList<>();
        }
    }

    public List<JXNode> getJXNodeList(String rule, JXDocument JXDoc) {
        List<JXNode> list = new ArrayList<>();
        boolean hasFunction = rule.contains("##");
        String funs = "";
        if (hasFunction) {
            funs = rule.substring(rule.indexOf("##") + 2);
            rule = rule.substring(0, rule.indexOf("##"));
        }
        if (StringHelper.isEmpty(rule)) return list;
        list = JXDoc.selN(rule);
        return !hasFunction ? list : evalListFunction(funs, list);
    }

    public List<JXNode> getJXNodeList(String rule, JXNode jxNode) {
        List<JXNode> list = new ArrayList<>();
        boolean hasFunction = rule.contains("##");
        String funs = "";
        if (hasFunction) {
            funs = rule.substring(rule.indexOf("##") + 2);
            rule = rule.substring(0, rule.indexOf("##"));
        }
        if (StringHelper.isEmpty(rule)) return list;
        list = jxNode.sel(rule);
        if (hasFunction) list = evalListFunction(funs, list);
        return list;
    }

}
