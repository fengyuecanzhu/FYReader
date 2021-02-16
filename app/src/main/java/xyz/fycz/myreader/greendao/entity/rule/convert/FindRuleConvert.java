package xyz.fycz.myreader.greendao.entity.rule.convert;

import org.greenrobot.greendao.converter.PropertyConverter;

import xyz.fycz.myreader.greendao.entity.rule.FindRule;
import xyz.fycz.myreader.greendao.entity.rule.TocRule;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

/**
 * @author fengyue
 * @date 2021/2/8 18:28
 */
public class FindRuleConvert implements PropertyConverter<FindRule, String> {

    @Override
    public FindRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, FindRule.class);
    }

    @Override
    public String convertToDatabaseValue(FindRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}