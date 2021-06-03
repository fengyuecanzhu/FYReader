package xyz.fycz.myreader.base.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author fengyue
 * @date 2020/8/12 20:02
 */

public interface IViewHolder<T> {
    View createItemView(ViewGroup parent);
    void initView();
    void onBind(RecyclerView.ViewHolder holder, T data, int pos);
    void onClick();
}
