package xyz.fycz.myreader.ui.adapter.holder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.SearchEngine;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.adapter.BookTagAdapter;
import xyz.fycz.myreader.ui.adapter.SearchBookAdapter;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.KeyWordUtils;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.widget.CoverImageView;

/**
 * @author fengyue
 * @date 2020/10/2 10:10
 */
public class SearchBookHolder extends ViewHolderImpl<SearchBookBean> {
    private Activity activity;
    private ConMVMap<SearchBookBean, Book> mBooks;
    private SearchEngine searchEngine;
    private String keyWord;
    private List<String> tagList = new ArrayList<>();

    public SearchBookHolder(Activity activity, ConMVMap<SearchBookBean, Book> mBooks, SearchEngine searchEngine, String keyWord, SearchBookAdapter adapter) {
        this.activity = activity;
        this.mBooks = mBooks;
        this.searchEngine = searchEngine;
        this.keyWord = keyWord;
    }


    private CoverImageView ivBookImg;
    private TextView tvBookName;
    private TagFlowLayout tflBookTag;
    private TextView tvDesc;
    private TextView tvAuthor;
    private TextView tvSource;
    private TextView tvNewestChapter;

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_search_book_item;
    }

    @Override
    public void initView() {
        ivBookImg = findById(R.id.iv_book_img);
        tvBookName = findById(R.id.tv_book_name);
        tflBookTag = findById(R.id.tfl_book_tag);
        tvAuthor = findById(R.id.tv_book_author);
        tvDesc = findById(R.id.tv_book_desc);
        tvSource = findById(R.id.tv_book_source);
        tvNewestChapter = findById(R.id.tv_book_newest_chapter);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBind(RecyclerView.ViewHolder holder, SearchBookBean data, int pos) {
        List<Book> aBooks = mBooks.getValues(data);
        if (aBooks == null || aBooks.size() == 0) {
            aBooks = new ArrayList<>();
            aBooks.add(searchBookBean2Book(data));
        }
        int bookCount = aBooks.size();
        Book book = aBooks.get(0);
        BookSource source = BookSourceManager.getBookSourceByStr(book.getSource());
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(source);
        books2SearchBookBean(data, aBooks);
        if (StringHelper.isEmpty(data.getImgUrl())) {
            data.setImgUrl("");
        }
        if (!App.isDestroy((Activity) getContext())) {
            ivBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), data.getImgUrl()), data.getName(), data.getAuthor());
        }
        KeyWordUtils.setKeyWord(tvBookName, data.getName(), keyWord);
        if (!StringHelper.isEmpty(data.getAuthor())) {
            KeyWordUtils.setKeyWord(tvAuthor, data.getAuthor(), keyWord);
        } else {
            tvAuthor.setText("");
        }
        initTagList(data);
        if (!StringHelper.isEmpty(data.getLastChapter())) {
            tvNewestChapter.setText(getContext().getString(R.string.newest_chapter, data.getLastChapter()));
        } else {
            data.setLastChapter("");
            tvNewestChapter.setText("");
        }
        if (!StringHelper.isEmpty(data.getDesc())) {
            tvDesc.setText(String.format("简介:%s", data.getDesc()));
        } else {
            data.setDesc("");
            tvDesc.setText("");
        }
        tvSource.setText(getContext().getString(R.string.source_title_num, source.getSourceName(), bookCount));
        App.getHandler().postDelayed(() -> {
            if (needGetInfo(data) && rc instanceof BookInfoCrawler) {
                Log.i(data.getName(), "initOtherInfo");
                BookInfoCrawler bic = (BookInfoCrawler) rc;
                searchEngine.getBookInfo(book, bic, isSuccess -> {
                    if (isSuccess) {
                        List<Book> books = new ArrayList<>();
                        books.add(book);
                        books2SearchBookBean(data, books);
                        initOtherInfo(data, rc);
                    }
                });
            }
        }, 1000);
    }

    private void initOtherInfo(SearchBookBean book, ReadCrawler rc) {
        //简介
        if (StringHelper.isEmpty(tvDesc.getText().toString())) {
            tvDesc.setText(String.format("简介:%s", book.getDesc()));
        }
        if (StringHelper.isEmpty(tvNewestChapter.getText().toString())) {
            tvNewestChapter.setText(getContext().getString(R.string.newest_chapter, book.getLastChapter()));
        }
        if (StringHelper.isEmpty(tvAuthor.getText().toString())) {
            KeyWordUtils.setKeyWord(tvAuthor, book.getAuthor(), keyWord);
        }
        //图片
        if (!App.isDestroy((Activity) getContext())) {
            ivBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), book.getImgUrl()), book.getName(), book.getAuthor());
        }
    }

    private void initTagList(SearchBookBean data) {
        tagList.clear();
        String type = data.getType();
        if (!StringHelper.isEmpty(type))
            tagList.add("0:" + type);
        String wordCount = data.getWordCount();
        if (!StringHelper.isEmpty(wordCount))
            tagList.add("1:" + wordCount);
        String status = data.getStatus();
        if (!StringHelper.isEmpty(status))
            tagList.add("2:" + status);
        if (tagList.size() == 0) {
            tflBookTag.setVisibility(View.GONE);
        } else {
            tflBookTag.setVisibility(View.VISIBLE);
            tflBookTag.setAdapter(new BookTagAdapter(activity, tagList, 11));
        }
    }

    private void books2SearchBookBean(SearchBookBean bookBean, List<Book> books) {
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getAuthor())) break;
            String author = book.getAuthor();
            if (!StringHelper.isEmpty(author)) {
                bookBean.setAuthor(author);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getType())) break;
            String type = book.getType();
            if (!StringHelper.isEmpty(type)) {
                bookBean.setType(type);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getDesc())) break;
            String desc = book.getDesc();
            if (!StringHelper.isEmpty(desc)) {
                bookBean.setDesc(desc);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getStatus())) break;
            String status = book.getStatus();
            if (!StringHelper.isEmpty(status)) {
                bookBean.setStatus(status);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getWordCount())) break;
            String wordCount = book.getWordCount();
            if (!StringHelper.isEmpty(wordCount)) {
                bookBean.setWordCount(wordCount);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getLastChapter())) break;
            String lastChapter = book.getNewestChapterTitle();
            if (!StringHelper.isEmpty(lastChapter)) {
                bookBean.setLastChapter(lastChapter);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getUpdateTime())) break;
            String updateTime = book.getUpdateDate();
            if (!StringHelper.isEmpty(updateTime)) {
                bookBean.setUpdateTime(updateTime);
                break;
            }
        }
        for (Book book : books) {
            if (!StringHelper.isEmpty(bookBean.getImgUrl())) break;
            String imgUrl = book.getImgUrl();
            if (!StringHelper.isEmpty(imgUrl)) {
                bookBean.setImgUrl(imgUrl);
                break;
            }
        }
    }

    private Book searchBookBean2Book(SearchBookBean bean) {
        Book book = new Book();
        book.setName(bean.getName());
        book.setAuthor(bean.getAuthor());
        book.setType(bean.getType());
        book.setDesc(bean.getDesc());
        book.setStatus(bean.getStatus());
        book.setUpdateDate(bean.getUpdateTime());
        book.setNewestChapterTitle(bean.getLastChapter());
        book.setWordCount(bean.getWordCount());
        return book;
    }

    private boolean needGetInfo(SearchBookBean bookBean) {
        if (StringHelper.isEmpty(bookBean.getAuthor())) return true;
        if (StringHelper.isEmpty(bookBean.getType())) return true;
        if (StringHelper.isEmpty(bookBean.getDesc())) return true;
        if (StringHelper.isEmpty(bookBean.getLastChapter())) return true;
        return StringHelper.isEmpty(bookBean.getImgUrl());
    }
}
