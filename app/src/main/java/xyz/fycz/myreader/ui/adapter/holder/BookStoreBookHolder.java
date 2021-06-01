package xyz.fycz.myreader.ui.adapter.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.widget.CoverImageView;

/**
 * @author fengyue
 * @date 2020/9/7 7:35
 */
public class BookStoreBookHolder extends ViewHolderImpl<Book> {

    private CoverImageView tvBookImg;
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
        tvBookNewestChapter.setText(StringHelper.isEmpty(data.getNewestChapterTitle()) ?
                data.getDesc() : data.getNewestChapterTitle());
        tvBookTime.setText(data.getUpdateDate());
        BookSource source = BookSourceManager.getBookSourceByStr(data.getSource());
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(source);
        if (hasImg){
            tvBookImg.setVisibility(View.VISIBLE);
            if (!App.isDestroy(mActivity)) {
                tvBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), data.getImgUrl()), data.getName(), data.getAuthor());
            }
        }
        if (data.getSource() != null) {
            tvBookSource.setText(String.format("书源：%s", source.getSourceName()));
        }else {
            tvBookSource.setText(data.getNewestChapterId());
        }
    }

}
