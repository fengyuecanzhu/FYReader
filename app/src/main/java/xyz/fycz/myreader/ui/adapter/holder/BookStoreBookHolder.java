package xyz.fycz.myreader.ui.adapter.holder;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.ui.activity.BookDetailedActivity;

/**
 * @author fengyue
 * @date 2020/9/7 7:35
 */
public class BookStoreBookHolder extends ViewHolderImpl<Book> {

    private ImageView tvBookImg;
    private TextView tvBookName;
    private TextView tvBookAuthor;
    private TextView tvBookTime;
    private  TextView tvBookNewestChapter;
    private TextView tvBookSource;

    private boolean hasImg;
    private Activity mActivity;

    public BookStoreBookHolder(boolean hasImg, Activity mActivity) {
        this.hasImg = hasImg;
        this.mActivity = mActivity;
    }


    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_book_store_book_item;
    }

    @Override
    public void initView() {
        tvBookImg = findById(R.id.iv_book_img);
        tvBookName =  findById(R.id.tv_book_name);
        tvBookAuthor = findById(R.id.tv_book_author);
        tvBookNewestChapter = findById(R.id.tv_book_newest_chapter);
        tvBookTime =  findById(R.id.tv_book_time);
        tvBookSource = findById(R.id.tv_book_source);
    }

    @Override
    public void onBind(Book data, int pos) {
        tvBookName.setText(data.getName());
        tvBookAuthor.setText(data.getAuthor());
        tvBookNewestChapter.setText(data.getNewestChapterTitle());
        tvBookTime.setText(data.getUpdateDate());
        if (hasImg){
            tvBookImg.setVisibility(View.VISIBLE);
            if (!MyApplication.isDestroy(mActivity)) {
                Glide.with(getContext())
                        .load(data.getImgUrl())
                        .error(R.mipmap.no_image)
                        .placeholder(R.mipmap.no_image)
                        //设置圆角
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                        .into(tvBookImg);
            }
        }
        if (data.getSource() != null) {
            tvBookSource.setText("书源：" + BookSource.fromString(data.getSource()).text);
        }else {
            tvBookSource.setText(data.getNewestChapterId());
        }
    }

}
