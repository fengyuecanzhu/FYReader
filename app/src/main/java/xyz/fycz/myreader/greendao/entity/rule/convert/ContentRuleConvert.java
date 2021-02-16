package xyz.fycz.myreader.greendao.entity.rule.convert;

import org.greenrobot.greendao.converter.PropertyConverter;

import xyz.fycz.myreader.greendao.entity.rule.ContentRule;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

/**
 * @author fengyue
 * @date 2021/2/8 18:27
 */
public class ContentRuleConvert implements PropertyConverter<ContentRule, String> {
    @Override
    public ContentRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, ContentRule.class);
    }

    @Override
    public String convertToDatabaseValue(ContentRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}
