package xyz.fycz.myreader.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.custom.DragAdapter;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.ui.activity.BookDetailedActivity;
import xyz.fycz.myreader.ui.adapter.holder.BookStoreBookHolder;
import xyz.fycz.myreader.webapi.crawler.BiQuGeReadCrawler;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.CommonApi;


import java.util.List;


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
}
