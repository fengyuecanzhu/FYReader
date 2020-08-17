package xyz.fycz.myreader.ui.adapter;

import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.ui.adapter.holder.CatalogHolder;

/**
 * @author fengyue
 * @date 2020/8/17 15:06
 */
public class DetailCatalogAdapter extends BaseListAdapter<Chapter> {
    @Override
    protected IViewHolder<Chapter> createViewHolder(int viewType) {
        return new CatalogHolder();
    }
}
