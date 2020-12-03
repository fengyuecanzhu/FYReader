package xyz.fycz.myreader.util.help;

import android.text.TextUtils;

import com.luhuiguo.chinese.ChineseUtils;

import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.enums.Language;
import xyz.fycz.myreader.greendao.entity.ReplaceRuleBean;
import xyz.fycz.myreader.model.ReplaceRuleManager;


public class ChapterContentHelp {
    private static ChapterContentHelp instance;

    public static synchronized ChapterContentHelp getInstance() {
        if (instance == null)
            instance = new ChapterContentHelp();
        return instance;
    }

    /**
     * 转繁体
     */
    private String toTraditional(String content) {
        Language convertCTS = SysManager.getSetting().getLanguage();
        switch (convertCTS) {
            case normal:
                break;
            case simplified:
                content = ChineseUtils.toSimplified(content);
                break;
            case traditional:
                content = ChineseUtils.toTraditional(content);
                break;
        }
        return content;
    }

    /**
     * 替换净化
     */
    public String replaceContent(String bookName, String bookTag, String content, Boolean replaceEnable) {
        if (!replaceEnable) return toTraditional(content);
        if (ReplaceRuleManager.getEnabled().size() == 0) return toTraditional(content);
        //替换
        for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
            if (isUseTo(replaceRule.getUseTo(), bookTag, bookName)) {
                try {
                    content = content.replaceAll(replaceRule.getFixedRegex(), replaceRule.getReplacement());
                } catch (Exception ignored) {
                }
            }
        }
        return toTraditional(content);
    }

    private boolean isUseTo(String useTo, String bookTag, String bookName) {
        return TextUtils.isEmpty(useTo)
                || useTo.contains(bookTag)
                || useTo.contains(bookName);
    }

}
