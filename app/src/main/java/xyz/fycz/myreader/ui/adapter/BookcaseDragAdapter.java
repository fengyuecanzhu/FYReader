package xyz.fycz.myreader.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.kongzue.dialogx.dialogs.BottomMenu;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

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


public class BookcaseDragAdapter extends BookcaseAdapter {
    ViewHolder viewHolder = null;
    protected String[] menu = {
            App.getmContext().getResources().getString(R.string.menu_book_detail),
            App.getmContext().getResources().getString(R.string.menu_book_Top),
            App.getmContext().getResources().getString(R.string.menu_book_download),
            App.getmContext().getResources().getString(R.string.menu_book_cache),
            App.getmContext().getResources().getString(R.string.menu_book_delete)
    };

    public BookcaseDragAdapter(Context context, int textViewResourceId, ArrayList<Book> objects,
                               boolean editState, BookcasePresenter bookcasePresenter, boolean isGroup) {
        super(context, textViewResourceId, objects, editState, bookcasePresenter, isGroup);
        itemTouchCallbackListener = new ItemTouchCallback.OnItemTouchListener() {
            @Override
            public void onSwiped(int adapterPosition) {

            }

            @Override
            public boolean onMove(int srcPosition, int targetPosition) {
                Book shelfBean = list.get(srcPosition);
                list.remove(srcPosition);
                list.add(targetPosition, shelfBean);
                notifyItemMoved(srcPosition, targetPosition);
                /*int start = srcPosition;
                int end = targetPosition;
                if (start > end) {
                    start = targetPosition;
                    end = srcPosition;
                }
                notifyItemRangeChanged(start, end - start + 1);*/
                return true;
            }

            @Override
            public void onEnd() {
                App.getHandler().postDelayed(() -> notifyDataSetChanged(), 500);
            }

        };
    }

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

        if (mEditState) {
            viewHolder.tvNoReadNum.setVisibility(View.GONE);
            viewHolder.pbLoading.setVisibility(View.GONE);
            viewHolder.ivBookImg.setOnClickListener(v -> {
                setCheckedBook(book.getId());
                mListener.onItemCheckedChange(getBookIsChecked(book.getId()));
            });
            viewHolder.cbBookChecked.setVisibility(View.VISIBLE);
            viewHolder.cbBookChecked.setChecked(getBookIsChecked(book.getId()));
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
            viewHolder.ivBookImg.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, ReadActivity.class);
                BitIntentDataManager.getInstance().putData(intent, book);
                mBookService.updateEntity(book);
                mContext.startActivity(intent);
            });
            viewHolder.ivBookImg.setOnLongClickListener(v -> {
                if (!ismEditState()) {
                    showBookMenu(book, position);
                    return true;
                }
                return false;
            });
        }

    }

    static class ViewHolder extends BookcaseAdapter.ViewHolder {
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            cbBookChecked = itemView.findViewById(R.id.cb_book_select);
            ivBookImg = itemView.findViewById(R.id.iv_book_img);
            tvBookName = itemView.findViewById(R.id.tv_book_name);
            tvNoReadNum = itemView.findViewById(R.id.tv_no_read_num);
            pbLoading = itemView.findViewById(R.id.pb_loading);
        }
    }
}
