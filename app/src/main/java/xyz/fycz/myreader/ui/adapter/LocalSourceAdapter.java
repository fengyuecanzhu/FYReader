package xyz.fycz.myreader.ui.adapter;

import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.ui.adapter.holder.LocalSourceHolder;

/**
 * @author fengyue
 * @date 2021/2/10 18:27
 */
public class LocalSourceAdapter extends BaseSourceAdapter {

    private List<BookSource> sources;

    public LocalSourceAdapter(List<BookSource> sources) {
        this.sources = sources;
    }

    @Override
    protected IViewHolder<BookSource> createViewHolder(int viewType) {
        return new LocalSourceHolder(getCheckMap());
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                List<BookSource> mFilterList = new ArrayList<>();
                if (charString.isEmpty()) {
                    //没有过滤的内容，则使用源数据
                    mFilterList = sources;
                } else {
                    for (BookSource source : sources) {
                        //这里根据需求，添加匹配规则
                        if (source.getSourceName().contains(charString))
                            mFilterList.add(source);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilterList;
                return filterResults;
            }

            //把过滤后的值返回出来
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                refreshItems((List<BookSource>) results.values);
            }
        };
    }
}
