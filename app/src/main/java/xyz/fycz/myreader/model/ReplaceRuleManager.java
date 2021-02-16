package xyz.fycz.myreader.model;

import android.text.TextUtils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import xyz.fycz.myreader.greendao.GreenDaoManager;
import xyz.fycz.myreader.greendao.entity.ReplaceRuleBean;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.gen.ReplaceRuleBeanDao;
import xyz.fycz.myreader.util.utils.GsonUtils;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

/**
 * Created by GKF on 2018/2/12.
 * 替换规则管理
 */

public class ReplaceRuleManager {
    private static List<ReplaceRuleBean> replaceRuleBeansEnabled;

    public static List<ReplaceRuleBean> getEnabled() {
        if (replaceRuleBeansEnabled == null) {
            replaceRuleBeansEnabled = GreenDaoManager.getDaoSession()
                    .getReplaceRuleBeanDao().queryBuilder()
                    .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                    .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                    .list();
        }
        return replaceRuleBeansEnabled;
    }
    // 合并广告话术规则
    public static Single<Boolean> mergeAdRules(ReplaceRuleBean replaceRuleBean) {


        String rule = formateAdRule(replaceRuleBean.getRegex());

/*        String summary=replaceRuleBean.getReplaceSummary();
        if(summary==null)
            summary="";
        String sumary_pre=summary.split("-")[0];*/

        int sn = replaceRuleBean.getSerialNumber();
        if (sn == 0) {
            sn = (int) (GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().queryBuilder().count() + 1);
            replaceRuleBean.setSerialNumber(sn);
        }

        List<ReplaceRuleBean> list = GreenDaoManager.getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                .where(ReplaceRuleBeanDao.Properties.ReplaceSummary.eq(replaceRuleBean.getReplaceSummary()))
                .where(ReplaceRuleBeanDao.Properties.SerialNumber.notEq(sn))
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
        if (list.size() < 1) {
            replaceRuleBean.setRegex(rule);
            return saveData(replaceRuleBean);
        } else {
            StringBuffer buffer = new StringBuffer(rule);
            for (ReplaceRuleBean li : list) {
                buffer.append('\n');
                buffer.append(li.getRegex());
//                    buffer.append(formateAdRule(rule.getRegex()));
            }
            replaceRuleBean.setRegex(formateAdRule(buffer.toString()));

            return Single.create((SingleOnSubscribe<Boolean>) emitter -> {

                GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().insertOrReplace(replaceRuleBean);
                for (ReplaceRuleBean li : list) {
                    GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().delete(li);
                }
                refreshDataS();
                emitter.onSuccess(true);
            }).compose(RxUtils::toSimpleSingle);

        }
    }

    // 把输入的规则进行预处理（分段、排序、去重）。保存的是普通多行文本。
    public static String formateAdRule(String rule) {

        if (rule == null)
            return "";
        String result = rule.trim();
        if (result.length() < 1)
            return "";

        String string = rule
//                用中文中的.视为。进行分段
                .replaceAll("(?<=([^a-zA-Z\\p{P}]{4,8}))\\.+(?![^a-zA-Z\\p{P}]{4,8})","\n")
//                用常见的适合分段的标点进行分段，句首句尾除外
//                .replaceAll("([^\\p{P}\n^])([…,，:：？。！?!~<>《》【】（）()]+)([^\\p{P}\n$])", "$1\n$3")
//                表达式无法解决句尾连续多个符号的问题
//                .replaceAll("[…,，:：？。！?!~<>《》【】（）()]+(?!\\s*\n|$)", "\n")
                .replaceAll("(?<![\\p{P}\n^])([…,，:：？。！?!~<>《》【】（）()]+)(?![\\p{P}\n$])", "\n")

                ;

        String[] lines = string.split("\n");
        List<String> list = new ArrayList<>();

        for (String s : lines) {
            s = s.trim()
//                    .replaceAll("\\s+", "\\s")
            ;
            if (!list.contains(s)) {
                list.add(s);
            }
        }
        Collections.sort(list);
        StringBuffer buffer = new StringBuffer(rule.length() + 1);
        for (int i = 0; i < list.size(); i++) {
            buffer.append('\n');
            buffer.append(list.get(i));
        }
        return buffer.toString().trim();
    }
    public static Single<List<ReplaceRuleBean>> getAll() {
        return Single.create((SingleOnSubscribe<List<ReplaceRuleBean>>) emitter -> emitter.onSuccess(GreenDaoManager.getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list())).compose(RxUtils::toSimpleSingle);
    }

    public static List<ReplaceRuleBean> getAllRules() {
        return GreenDaoManager.getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
    }

    public static Single<Boolean> saveData(ReplaceRuleBean replaceRuleBean) {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            if (replaceRuleBean.getSerialNumber() == 0) {
                replaceRuleBean.setSerialNumber((int) (GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().queryBuilder().count() + 1));
            }
            GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().insertOrReplace(replaceRuleBean);
            refreshDataS();
            emitter.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle);
    }

    public static void delData(ReplaceRuleBean replaceRuleBean) {
        GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        refreshDataS();
    }

    public static void addDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().insertOrReplaceInTx(replaceRuleBeans);
            refreshDataS();
        }
    }

    public static void delDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        if (replaceRuleBeans == null) return;
        GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().deleteInTx(replaceRuleBeans);
        refreshDataS();
    }

    public static Single<Boolean> toTop(ReplaceRuleBean bean) {
        return Single.create((SingleOnSubscribe<Boolean>) e -> {
            List<ReplaceRuleBean> beans = getAllRules();
            for (int i = 0; i < beans.size(); i++) {
                beans.get(i).setSerialNumber(i + 1);
            }
            bean.setSerialNumber(0);
            GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().insertOrReplaceInTx(beans);
            GreenDaoManager.getDaoSession().getReplaceRuleBeanDao().insertOrReplace(bean);
            e.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle);
    }

    private static void refreshDataS() {
        replaceRuleBeansEnabled = GreenDaoManager.getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
    }

    public static Observable<Boolean> importReplaceRule(String text) {
        if (TextUtils.isEmpty(text)) return null;
        text = text.trim();
        if (text.length() == 0) return null;
        if (StringUtils.isJsonType(text)) {
            return importReplaceRuleO(text)
                    .compose(RxUtils::toSimpleSingle);
        }
        if (NetworkUtils.isUrl(text)) {
            String finalText = text;
            return Observable.create((ObservableOnSubscribe<String>) emitter -> emitter.onNext(OkHttpUtils.getHtml(finalText)))
                    .flatMap(ReplaceRuleManager::importReplaceRuleO)
                    .compose(RxUtils::toSimpleSingle);
        }
        return Observable.error(new Exception("不是Json或Url格式"));
    }

    private static Observable<Boolean> importReplaceRuleO(String json) {
        return Observable.create(e -> {
            try {
                List<ReplaceRuleBean> replaceRuleBeans = GsonUtils.parseJArray(json, ReplaceRuleBean.class);
                addDataS(replaceRuleBeans);
                e.onNext(true);
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onNext(false);
            }
            e.onComplete();
        });
    }

}
