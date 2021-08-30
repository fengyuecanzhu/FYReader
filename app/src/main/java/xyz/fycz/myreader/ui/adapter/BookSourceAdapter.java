package xyz.fycz.myreader.ui.adapter;


import android.os.AsyncTask;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.ui.adapter.helper.ItemTouchCallback;
import xyz.fycz.myreader.ui.adapter.helper.OnStartDragListener;
import xyz.fycz.myreader.ui.adapter.holder.BookSourceHolder;

/**
 * @author fengyue
 * @date 2020/8/12 20:02
 */

public class BookSourceAdapter extends BaseSourceAdapter {
    private final FragmentActivity activity;
    private final OnSwipeListener onSwipeListener;
    private  OnStartDragListener onStartDragListener;
    private boolean mEditState;
    private final ItemTouchCallback.OnItemTouchListener itemTouchListener = new ItemTouchCallback.OnItemTouchListener() {

        private boolean isMoved = true;
        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            swapItem(srcPosition, targetPosition);
            isMoved = true;
            return true;
        }


        @Override
        public void onClearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (isMoved) {
                App.getHandler().postDelayed(() -> notifyDataSetChanged(), 500);
                AsyncTask.execute(() -> {
                    for (int i = 1; i <= mList.size(); i++) {
                        mList.get(i - 1).setOrderNum(i);
                    }
                    DbManager.getDaoSession().getBookSourceDao().insertOrReplaceInTx(mList);
                });
            }
            isMoved = false;
        }

    };

    public BookSourceAdapter(FragmentActivity activity, OnSwipeListener onSwipeListener,
                             OnStartDragListener onStartDragListener) {
        this.activity = activity;
        this.onSwipeListener = onSwipeListener;
        this.onStartDragListener = onStartDragListener;
    }

    @Override
    protected IViewHolder<BookSource> createViewHolder(int viewType) {
        return new BookSourceHolder(activity, this, onSwipeListener, onStartDragListener);
    }

    public ItemTouchCallback.OnItemTouchListener getItemTouchListener() {
        return itemTouchListener;
    }

    public boolean ismEditState() {
        return mEditState;
    }

    public void setmEditState(boolean mEditState) {
        this.mEditState = mEditState;
        setCheckedAll(false);
    }

    public void removeItem(int pos) {
        mList.remove(pos);
        notifyItemRemoved(pos);
        if (pos != mList.size())
            notifyItemRangeChanged(pos, mList.size() - pos);
    }

    public void toTop(int which, BookSource bean) {
        mList.remove(bean);
        notifyItemInserted(0);
        mList.add(0, bean);
        notifyItemRemoved(which);
        notifyItemRangeChanged(0, which + 1);
    }

    public interface OnSwipeListener {
        void onDel(int which, BookSource source);

        void onTop(int which, BookSource source);
    }
}
