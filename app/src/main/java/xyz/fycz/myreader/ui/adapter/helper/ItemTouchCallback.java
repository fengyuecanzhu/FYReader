package xyz.fycz.myreader.ui.adapter.helper;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;

/**
 * @author fengyue
 * @date 2021/2/9 10:08
 */

public class ItemTouchCallback extends ItemTouchHelper.Callback {

    private ViewPager viewPager;

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    /**
     * Item操作的回调
     */
    private OnItemTouchListener onItemTouchListener;

    /**
     * 是否可以拖拽
     */
    private boolean isCanDrag = false;
    /**
     * 是否可以被滑动
     */
    private boolean isCanSwipe = false;

    /**
     * 设置Item操作的回调，去更新UI和数据源
     */
    public void setOnItemTouchListener(OnItemTouchListener onItemTouchListener) {
        this.onItemTouchListener = onItemTouchListener;
    }

    /**
     * 设置是否可以被长按拖拽
     *
     * @param canDrag 是true，否false
     */
    public void setLongPressDragEnable(boolean canDrag) {
        isCanDrag = canDrag;
    }

    /**
     * 设置是否可以被滑动
     *
     * @param canSwipe 是true，否false
     */
    public void setSwipeEnable(boolean canSwipe) {
        isCanSwipe = canSwipe;
    }

    /**
     * 当Item被长按的时候是否可以被拖拽
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return isCanDrag;
    }

    /**
     * Item是否可以被滑动(H：左右滑动，V：上下滑动)
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return isCanSwipe;
    }

    /**
     * 当用户拖拽或者滑动Item的时候需要我们告诉系统滑动或者拖拽的方向
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {// GridLayoutManager
            // flag如果值是0，相当于这个功能被关闭
            int dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlag = 0;
            // create make
            return makeMovementFlags(dragFlag, swipeFlag);
        } else if (layoutManager instanceof LinearLayoutManager) {// linearLayoutManager
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int orientation = linearLayoutManager.getOrientation();

            int dragFlag = 0;
            int swipeFlag = 0;

            // 为了方便理解，相当于分为横着的ListView和竖着的ListView
            if (orientation == LinearLayoutManager.HORIZONTAL) {// 如果是横向的布局
                swipeFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else if (orientation == LinearLayoutManager.VERTICAL) {// 如果是竖向的布局，相当于ListView
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                swipeFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            }
            return makeMovementFlags(dragFlag, swipeFlag);
        }
        return 0;
    }

    /**
     * 当Item被拖拽的时候被回调
     *
     * @param recyclerView     recyclerView
     * @param srcViewHolder    拖拽的ViewHolder
     * @param targetViewHolder 目的地的viewHolder
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder srcViewHolder, @NonNull RecyclerView.ViewHolder targetViewHolder) {
        if (onItemTouchListener != null) {
            return onItemTouchListener.onMove(srcViewHolder.getAdapterPosition(), targetViewHolder.getAdapterPosition());
        }
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (onItemTouchListener != null) {
            onItemTouchListener.onSwiped(viewHolder.getAdapterPosition());
        }
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            //不为空闲状态，即为拖拽或侧滑状态
            if (viewHolder instanceof IItemTouchHelperViewHolder) {
                IItemTouchHelperViewHolder itemTouchHelperViewHolder =
                        (IItemTouchHelperViewHolder) viewHolder;
                itemTouchHelperViewHolder.onItemSelected(viewHolder);
            }
        }
        /*if (onItemTouchListener != null) {
            if (viewHolder == null) {
                onItemTouchListener.onEnd();
            }
        }*/
        super.onSelectedChanged(viewHolder, actionState);
        final boolean swiping = actionState == ItemTouchHelper.ACTION_STATE_DRAG;
        if (viewPager != null) {
            viewPager.requestDisallowInterceptTouchEvent(swiping);
        }
    }

    @Override
    public void clearView(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (viewHolder instanceof IItemTouchHelperViewHolder) {
            IItemTouchHelperViewHolder itemTouchHelperViewHolder =
                    (IItemTouchHelperViewHolder) viewHolder;
            itemTouchHelperViewHolder.onItemClear(viewHolder);
        }
        if (onItemTouchListener != null) {
            onItemTouchListener.onClearView(recyclerView, viewHolder);
        }
    }

    @Override
    public void onMoved(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull @NotNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
    }

    public interface OnItemTouchListener {
        /**
         * 当某个Item被滑动删除的时候
         *
         * @param adapterPosition item的position
         */
        default void onSwiped(int adapterPosition){}

        /**
         * 当两个Item位置互换的时候被回调
         *
         * @param srcPosition    拖拽的item的position
         * @param targetPosition 目的地的Item的position
         * @return 开发者处理了操作应该返回true，开发者没有处理就返回false
         */
        boolean onMove(int srcPosition, int targetPosition);

        /**
         * 当滑动删除或拖拽结束时调用
         */
        void onClearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);
    }
}
