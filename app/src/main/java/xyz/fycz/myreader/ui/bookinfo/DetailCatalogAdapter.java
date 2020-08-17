package xyz.fycz.myreader.ui.bookinfo;

import xyz.fycz.myreader.base.IViewHolder;
import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.greendao.entity.Chapter;

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
