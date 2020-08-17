package xyz.fycz.myreader.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;
import java.util.ArrayList;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.ui.activity.ReadActivity;
import xyz.fycz.myreader.ui.presenter.BookcasePresenter;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.TextHelper;



public class BookcaseDragAdapter extends BookcaseAdapter {


    public BookcaseDragAdapter(Context context, int textViewResourceId, ArrayList<Book> objects,
                               boolean editState, BookcasePresenter bookcasePresenter) {
        super(context, textViewResourceId, objects, editState, bookcasePresenter);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null || convertView.getTag() instanceof BookcaseDetailedAdapter.ViewHolder) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(mResourceId, null);
            viewHolder.ivBookImg = convertView.findViewById(R.id.iv_book_img);
            viewHolder.tvBookName = convertView.findViewById(R.id.tv_book_name);
            viewHolder.tvNoReadNum = convertView.findViewById(R.id.tv_no_read_num);
            viewHolder.ivDelete = convertView.findViewById(R.id.iv_delete);
            viewHolder.pbLoading = convertView.findViewById(R.id.pb_loading);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(position, viewHolder);
        return convertView;
    }

    private void initView(int position, ViewHolder viewHolder) {
        final Book book = getItem(position);
        if (StringHelper.isEmpty(book.getImgUrl())) {
            book.setImgUrl("");
        }

        Glide.with(mContext)
                .load(book.getImgUrl())
                .error(R.mipmap.no_image)
                .placeholder(R.mipmap.no_image)
                //设置圆角
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                .into(viewHolder.ivBookImg);

        viewHolder.tvBookName.setText(book.getName());
        viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteBookDialog(book);
            }
        });


        if (mEditState) {
            viewHolder.tvNoReadNum.setVisibility(View.GONE);
            viewHolder.ivDelete.setVisibility(View.VISIBLE);
            viewHolder.pbLoading.setVisibility(View.GONE);
            viewHolder.ivBookImg.setOnClickListener(null);
        } else {
            viewHolder.ivDelete.setVisibility(View.GONE);
            boolean isLoading = false;
            try {
                isLoading = isBookLoading(book.getId());
            }catch (Exception ignored){
            }
            if (isLoading){
                viewHolder.pbLoading.setVisibility(View.VISIBLE);
                viewHolder.tvNoReadNum.setVisibility(View.GONE);
            }else {
                viewHolder.pbLoading.setVisibility(View.GONE);
                int notReadNum = book.getChapterTotalNum() - book.getHisttoryChapterNum() + book.getNoReadNum() - 1;
                if (notReadNum != 0){
                    viewHolder.tvNoReadNum.setVisibility(View.VISIBLE);
                    if(book.getNoReadNum() != 0){
                        viewHolder.tvNoReadNum.setHighlight(true);
                        if (notReadNum == -1){
                            notReadNum = book.getNoReadNum() - 1;
                        }
                    }else {
                        viewHolder.tvNoReadNum.setHighlight(false);
                    }
                    viewHolder.tvNoReadNum.setBadgeCount(notReadNum);
                }else {
                    viewHolder.tvNoReadNum.setVisibility(View.GONE);
                }
            }
            viewHolder.ivBookImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent( mContext, ReadActivity.class);
                    intent.putExtra(APPCONST.BOOK, book);
                    book.setNoReadNum(0);
                    mBookService.updateEntity(book);
                    mContext.startActivity(intent);
                }
            });
            viewHolder.ivBookImg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!ismEditState()){
                        AlertDialog bookDialog = new AlertDialog.Builder(mContext)
                                .setTitle(book.getName())
                                .setAdapter(new ArrayAdapter<>(mContext,
                                                android.R.layout.simple_list_item_1, menu),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case 0:
                                                        book.setSortCode(0);
                                                        mBookService.updateBook(book);
                                                        mBookcasePresenter.init();
                                                        TextHelper.showText("书籍《" + book.getName() + "》移至顶部成功！");
                                                        break;
                                                    case 1:
                                                        downloadBook(book);
                                                        break;
                                                    case 2:
                                                        MyApplication.getApplication().newThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    if (unionChapterCathe(book)) {
                                                                        DialogCreator.createTipDialog(mContext,
                                                                                "缓存导出成功，导出目录："
                                                                                        + APPCONST.TXT_BOOK_DIR);
                                                                    }else {
                                                                        DialogCreator.createTipDialog(mContext,
                                                                                "章节目录为空或未找到缓存文件，缓存导出失败！");
                                                                    }
                                                                } catch (IOException e) {
                                                                    e.printStackTrace();
                                                                    DialogCreator.createTipDialog(mContext,
                                                                            "章节目录为空或未找到缓存文件，缓存导出失败！");
                                                                }
                                                            }
                                                        });
                                                        break;
                                                    case 3:
                                                        showDeleteBookDialog(book);
                                                        break;
                                                }
                                            }
                                        })
                                .setNegativeButton(null, null)
                                .setPositiveButton(null, null)
                                .create();
                        bookDialog.show();
                        return true;
                    }
                    return false;
                }
            });
        }

    }
    class ViewHolder extends BookcaseAdapter.ViewHolder {
    }
}
