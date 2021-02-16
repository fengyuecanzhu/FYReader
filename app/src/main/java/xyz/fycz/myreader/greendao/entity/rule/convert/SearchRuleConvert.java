package xyz.fycz.myreader.greendao.entity.rule.convert;

import org.greenrobot.greendao.converter.PropertyConverter;

import xyz.fycz.myreader.greendao.entity.rule.SearchRule;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

/**
 * @author fengyue
 * @date 2021/2/8 18:29
 */
public class SearchRuleConvert implements PropertyConverter<SearchRule, String> {

    @Override
    public SearchRule convertToEntityProperty(String databaseValue) {
        return GsonExtensionsKt.getGSON().fromJson(databaseValue, SearchRule.class);
    }

    @Override
    public String convertToDatabaseValue(SearchRule entityProperty) {
        return GsonExtensionsKt.getGSON().toJson(entityProperty);
    }
}