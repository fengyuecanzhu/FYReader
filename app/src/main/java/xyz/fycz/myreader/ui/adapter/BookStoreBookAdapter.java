package xyz.fycz.myreader.ui.adapter;

import android.app.Activity;
import android.os.Handler;

import java.util.List;

import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.ui.adapter.holder.BookStoreBookHolder;


public class BookStoreBookAdapter extends BaseListAdapter<Book> {
    private boolean hasImg;
    private Activity mActivity;


    public BookStoreBookAdapter(boolean hasImg, Activity mActivity) {
        this.hasImg = hasImg;
        this.mActivity = mActivity;
    }

    @Override
    protected IViewHolder<Book> createViewHolder(int viewType) {
        return new BookStoreBookHolder(hasImg, mActivity);
    }

    @Override
    public void addItems(List<Book> values) {
        mList.addAll(values);
        App.runOnUiThread(this::notifyDataSetChanged);
    }
}
