package xyz.fycz.myreader.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kongzue.dialogx.dialogs.BottomMenu;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.activity.ReadActivity;
import xyz.fycz.myreader.ui.adapter.helper.ItemTouchCallback;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.ui.activity.BookDetailedActivity;
import xyz.fycz.myreader.ui.presenter.BookcasePresenter;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;


/**
 * @author fengyue
 * @date 2020/4/19 11:23
 */

public class BookcaseDetailedAdapter extends BookcaseAdapter {
    ViewHolder viewHolder = null;

    public BookcaseDetailedAdapter(Context context, int textViewResourceId, ArrayList<Book> objects,
                                   boolean editState, BookcasePresenter bookcasePresenter, boolean isGroup) {
        super(context, textViewResourceId, objects, editState, bookcasePresenter, isGroup);
        itemTouchCallbackListener = new ItemTouchCallback.OnItemTouchListener() {
            private boolean isMoved = false;

            @Override
            public boolean onMove(int srcPosition, int targetPosition) {
                Collections.swap(list, srcPosition, targetPosition);
                notifyItemMoved(srcPosition, targetPosition);
                isMoved = true;
                return true;
            }

            @Override
            public void onClearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (isMoved){
                    AsyncTask.execute(() -> onDataMove());
                }
                isMoved = false;
            }

        };
    }

    @NonNull
    @NotNull
    @Override
    public BookcaseAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(mResourceId, null));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BookcaseAdapter.ViewHolder holder, int position) {
        viewHolder = (ViewHolder) holder;
        initView(position);
    }

    private void initView(int position) {
        final Book book = getItem(position);
        if (StringHelper.isEmpty(book.getImgUrl())) {
            book.setImgUrl("");
        }
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(book.getSource());
        viewHolder.ivBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), book.getImgUrl()), book.getName(), book.getAuthor());

        viewHolder.tvBookName.setText(book.getName());

        viewHolder.tvBookAuthor.setText(book.getAuthor());
        viewHolder.tvHistoryChapter.setText(book.getHistoryChapterId());
        assert book.getNewestChapterTitle() != null;
        viewHolder.tvNewestChapter.setText(book.getNewestChapterTitle().replace("最近更新 ", ""));

        if (mEditState) {
            viewHolder.tvNoReadNum.setVisibility(View.GONE);
            viewHolder.ivBookImg.setOnClickListener(null);
            viewHolder.llBookRead.setOnClickListener(null);
            viewHolder.pbLoading.setVisibility(View.GONE);
            viewHolder.cbBookChecked.setVisibility(View.VISIBLE);
            viewHolder.cbBookChecked.setChecked(getBookIsChecked(book.getId()));
            viewHolder.llBookRead.setOnClickListener(v -> {
                setCheckedBook(book.getId());
                mListener.onItemCheckedChange(getBookIsChecked(book.getId()));
            });
            viewHolder.ivBookImg.setOnClickListener(v -> {
                setCheckedBook(book.getId());
                mListener.onItemCheckedChange(getBookIsChecked(book.getId()));
            });
            viewHolder.cbBookChecked.setOnClickListener(v -> {
                setCheckedBook(book.getId());
                mListener.onItemCheckedChange(getBookIsChecked(book.getId()));
            });
        } else {
            viewHolder.cbBookChecked.setVisibility(View.GONE);
            boolean isLoading = false;
            try {
                isLoading = isBookLoading(book.getId());
            } catch (Exception ignored) {
            }
            if (isLoading) {
                viewHolder.pbLoading.setVisibility(View.VISIBLE);
                viewHolder.tvNoReadNum.setVisibility(View.GONE);
            } else {
                viewHolder.pbLoading.setVisibility(View.GONE);
                int notReadNum = book.getChapterTotalNum() - book.getHisttoryChapterNum() + book.getNoReadNum() - 1;
                if (notReadNum != 0) {
                    viewHolder.tvNoReadNum.setVisibility(View.VISIBLE);
                    if (book.getNoReadNum() != 0) {
                        viewHolder.tvNoReadNum.setHighlight(true);
                        if (notReadNum == -1) {
                            notReadNum = book.getNoReadNum() - 1;
                        }
                    } else {
                        viewHolder.tvNoReadNum.setHighlight(false);
                    }
                    viewHolder.tvNoReadNum.setBadgeCount(notReadNum);
                } else {
                    viewHolder.tvNoReadNum.setVisibility(View.GONE);
                }
            }
            viewHolder.llBookRead.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, ReadActivity.class);
                BitIntentDataManager.getInstance().putData(intent, book);
                mBookService.updateEntity(book);
                mContext.startActivity(intent);
            });
            viewHolder.ivBookImg.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, BookDetailedActivity.class);
                BitIntentDataManager.getInstance().putData(intent, book);
                mContext.startActivity(intent);
            });
            viewHolder.llBookRead.setOnLongClickListener(v -> {
                if (!ismEditState()) {
                    showBookMenu(book, position);
                    return true;
                }
                return false;
            });
        }
    }

    static class ViewHolder extends BookcaseAdapter.ViewHolder {
        TextView tvBookAuthor;
        TextView tvHistoryChapter;
        TextView tvNewestChapter;
        LinearLayout llBookRead;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            cbBookChecked = itemView.findViewById(R.id.cb_book_select);
            ivBookImg = itemView.findViewById(R.id.iv_book_img);
            tvBookName = itemView.findViewById(R.id.tv_book_name);
            tvNoReadNum = itemView.findViewById(R.id.tv_no_read_num);
            tvBookAuthor = itemView.findViewById(R.id.tv_book_author);
            tvHistoryChapter = itemView.findViewById(R.id.tv_book_history_chapter);
            tvNewestChapter = itemView.findViewById(R.id.tv_book_newest_chapter);
            llBookRead = itemView.findViewById(R.id.ll_book_read);
            pbLoading = itemView.findViewById(R.id.pb_loading);
        }
    }

}
