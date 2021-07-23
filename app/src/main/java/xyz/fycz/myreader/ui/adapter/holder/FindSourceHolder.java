package xyz.fycz.myreader.ui.adapter.holder;

import android.content.Intent;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.ui.activity.FindBookActivity;

/**
 * @author fengyue
 * @date 2021/7/22 22:27
 */
public class FindSourceHolder extends ViewHolderImpl<BookSource> {

    private TextView tvName;

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_find_source;
    }

    @Override
    public void initView() {
        tvName = findById(R.id.tv_name);
    }

    @Override
    public void onBind(RecyclerView.ViewHolder holder, BookSource data, int pos) {
        tvName.setText(data.getSourceName());
    }
}
