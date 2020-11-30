package xyz.fycz.myreader.ui.adapter.holder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.model.SearchEngine;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.widget.CoverImageView;

import java.util.List;

/**
 * @author fengyue
 * @date 2020/10/2 10:10
 */
public class SearchBookHolder extends ViewHolderImpl<SearchBookBean> {
    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks;
    private SearchEngine searchEngine;
    private Handler mHandle = new Handler(message -> {

        switch (message.what) {
            case 1:
                Book book = (Book) message.obj;
                int pos = message.arg1;
                initOtherInfo(book);
                break;
        }
        return false;
    });

    public SearchBookHolder(ConcurrentMultiValueMap<SearchBookBean, Book> mBooks, SearchEngine searchEngine) {
        this.mBooks = mBooks;
        this.searchEngine = searchEngine;
    }

    CoverImageView ivBookImg;
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


    @SuppressLint("SetTextI18n")
    @Override
    public void onBind(SearchBookBean data, int pos) {
        List<Book> aBooks = mBooks.getValues(data);
        int bookCount = aBooks.size();
        Book book = aBooks.get(0);
        if (StringHelper.isEmpty(book.getImgUrl())){
            book.setImgUrl("");
        }
        if (!StringHelper.isEmpty(book.getDesc())) {
            tvDesc.setText("简介:" + book.getDesc());
        }
        if (!StringHelper.isEmpty(book.getType())) {
            tvType.setText(book.getType());
        }
        if (!StringHelper.isEmpty(book.getNewestChapterTitle())) {
            tvNewestChapter.setText(getContext().getString(R.string.newest_chapter, book.getNewestChapterTitle()));
        }
        if (!StringHelper.isEmpty(book.getAuthor())) {
            tvAuthor.setText(book.getAuthor());
        }
        tvBookName.setText(book.getName());
        tvSource.setText(getContext().getString(R.string.source_title_num, BookSource.fromString(book.getSource()).text, bookCount));
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
            searchEngine.getBookInfo(book, bic, isSuccess -> {
                if (isSuccess)
                    mHandle.sendMessage(mHandle.obtainMessage(1, pos, 0, book));
                else
                    tvBookName.setTag(false);
            });
        }else {
            initOtherInfo(book);
        }
    }

    private void initOtherInfo(Book book){
        //简介
        if (StringHelper.isEmpty(tvDesc.getText().toString())) {
            tvDesc.setText("简介:" + book.getDesc());
        }
        if (StringHelper.isEmpty(tvType.getText().toString())) {
            tvType.setText(book.getType());
        }
        if (StringHelper.isEmpty(tvNewestChapter.getText().toString())) {
            tvNewestChapter.setText(getContext().getString(R.string.newest_chapter, book.getNewestChapterTitle()));
        }
        if (StringHelper.isEmpty(tvAuthor.getText().toString())) {
            tvAuthor.setText(book.getAuthor());
        }
        //图片
        if (!MyApplication.isDestroy((Activity) getContext())) {
            ivBookImg.load(book.getImgUrl(), book.getName(), book.getAuthor());
        }

    }

}
