package xyz.fycz.myreader.ui.adapter.holder;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.CommonApi;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

import java.util.List;

/**
 * @author fengyue
 * @date 2020/10/2 10:10
 */
public class SearchBookHolder extends ViewHolderImpl<SearchBookBean> {
    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks;
    private Handler mHandle = new Handler(message -> {

        switch (message.what) {
            case 1:
                Book book = (Book) message.obj;
                initOtherInfo(book);
                break;
        }
        return false;
    });

    public SearchBookHolder(ConcurrentMultiValueMap<SearchBookBean, Book> mBooks) {
        this.mBooks = mBooks;
    }

    ImageView ivBookImg;
    TextView tvBookName;
    TextView tvDesc;
    TextView tvAuthor;
    TextView tvType;
    TextView tvSource;
    TextView tvNewestChapter;
    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_search_book_item;
    }

    @Override
    public void initView() {
        ivBookImg = findById(R.id.iv_book_img);
        tvBookName = findById(R.id.tv_book_name);
        tvAuthor = findById(R.id.tv_book_author);
        tvDesc =  findById(R.id.tv_book_desc);
        tvType = findById(R.id.tv_book_type);
        tvSource = findById(R.id.tv_book_source);
        tvNewestChapter = findById(R.id.tv_book_newest_chapter);
    }


    @Override
    public void onBind(SearchBookBean data, int pos) {
        List<Book> aBooks = mBooks.getValues(data);
        int bookCount = aBooks.size();
        Book book = aBooks.get(0);
        if (StringHelper.isEmpty(book.getImgUrl())){
            book.setImgUrl("");
        }
        tvBookName.setText(book.getName());
        tvNewestChapter.setText(getContext().getString(R.string.newest_chapter, book.getNewestChapterTitle()));
        tvAuthor.setText(book.getAuthor());
        tvSource.setText(getContext().getString(R.string.source_title_num, BookSource.fromString(book.getSource()).text, bookCount));
        tvDesc.setText("");
        tvType.setText("");
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(book.getSource());
        if (rc instanceof BookInfoCrawler){
            if (tvBookName.getTag() == null || !(Boolean) tvBookName.getTag()) {
                tvBookName.setTag(true);
            } else {
                initOtherInfo(book);
                return;
            }
            Log.i(book.getName(), "initOtherInfo");
            BookInfoCrawler bic = (BookInfoCrawler) rc;
            CommonApi.getBookInfo(book, bic, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    mHandle.sendMessage(mHandle.obtainMessage(1, pos, 0, book));
                }

                @Override
                public void onError(Exception e) {
                    tvBookName.setTag(false);
                }
            });
        }else {
            initOtherInfo(book);
        }
    }

    private void initOtherInfo(Book book){
        //图片
        if (!MyApplication.isDestroy((Activity) getContext())) {
            Glide.with(getContext())
                    .load(book.getImgUrl())
//                .override(DipPxUtil.dip2px(getContext(), 80), DipPxUtil.dip2px(getContext(), 150))
                    .error(R.mipmap.no_image)
                    .placeholder(R.mipmap.no_image)
                    //设置圆角
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                    .into(ivBookImg);
        }
        //简介
        if (book.getDesc() == null) {
            tvDesc.setText("");
        }else {
            tvDesc.setText("简介:" + book.getDesc());
        }
        tvType.setText(book.getType());
        tvNewestChapter.setText("最新章节:" + book.getNewestChapterTitle());
        tvAuthor.setText(book.getAuthor());
    }
}
