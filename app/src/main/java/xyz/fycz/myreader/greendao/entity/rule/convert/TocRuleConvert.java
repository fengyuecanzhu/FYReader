package xyz.fycz.myreader.greendao.entity.rule.convert;

import org.greenrobot.greendao.converter.PropertyConverter;

import xyz.fycz.myreader.greendao.entity.rule.TocRule;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

/**
 * @author fengyue
 * @date 2021/2/8 18:28
 */
public class TocRuleConvert implements PropertyConverter<TocRule, String> {

    @Override
    public TocRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, TocRule.class);
    }

    @Override
    public String convertToDatabaseValue(TocRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}