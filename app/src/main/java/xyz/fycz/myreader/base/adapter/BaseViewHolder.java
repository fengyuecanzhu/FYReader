package xyz.fycz.myreader.base.adapter;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author fengyue
 * @date 2020/8/12 20:02
 */

public class BaseViewHolder<T> extends RecyclerView.ViewHolder{
    public IViewHolder<T> holder;

    public BaseViewHolder(View itemView, IViewHolder<T> holder) {
        super(itemView);
        this.holder = holder;
        holder.initView();
    }
}
