package xyz.fycz.myreader.ui.adapter.helper;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author fengyue
 * @date 2021/6/3 18:39
 */
public interface OnStartDragListener {
    /**
     * 当View需要拖拽时回调
     *
     * @param viewHolder The holder of view to drag
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}
