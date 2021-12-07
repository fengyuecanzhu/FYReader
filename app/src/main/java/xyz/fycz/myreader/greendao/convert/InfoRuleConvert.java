package xyz.fycz.myreader.greendao.convert;

import org.greenrobot.greendao.converter.PropertyConverter;

import xyz.fycz.myreader.greendao.entity.rule.InfoRule;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

/**
 * @author fengyue
 * @date 2021/2/8 18:28
 */
public class InfoRuleConvert implements PropertyConverter<InfoRule, String> {

    @Override
    public InfoRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, InfoRule.class);
    }

    @Override
    public String convertToDatabaseValue(InfoRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}
