package xyz.fycz.myreader.ui.adapter;

import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.entity.sourceedit.EditEntity;
import xyz.fycz.myreader.ui.adapter.holder.SourceEditHolder;

/**
 * @author fengyue
 * @date 2021/2/9 10:08
 */
public class SourceEditAdapter extends BaseListAdapter<EditEntity> {
    @Override
    protected IViewHolder<EditEntity> createViewHolder(int viewType) {
        return new SourceEditHolder();
    }
}
