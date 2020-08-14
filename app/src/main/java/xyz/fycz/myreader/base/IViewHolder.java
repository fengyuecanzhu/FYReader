package xyz.fycz.myreader.base;

import android.view.View;
import android.view.ViewGroup;

/**
 * @author fengyue
 * @date 2020/8/12 20:02
 */

public interface IViewHolder<T> {
    View createItemView(ViewGroup parent);
    void initView();
    void onBind(T data,int pos);
    void onClick();
}
