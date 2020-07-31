package xyz.fycz.myreader.ui.bookinfo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.crawler.BookInfoCrawler;
import xyz.fycz.myreader.crawler.ReadCrawler;
import xyz.fycz.myreader.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.creator.ChangeSourceDialog;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.ui.read.ReadActivity;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.TextHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.webapi.CommonApi;


public class BookInfoPresenter implements BasePresenter {

    private BookInfoActivity mBookInfoActivity;
    private Book mBook;
    private ArrayList<Book> aBooks;
    private BookService mBookService;
    private ReadCrawler mReadCrawler;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initBookInfo();
                    break;
                case 2:
                    createChangeSourceDia();
                    break;
                case 3:
                    mBookInfoActivity.getPbLoading().setVisibility(View.GONE);
                    DialogCreator.createTipDialog(mBookInfoActivity, "未搜索到该书籍，书源加载失败！");
                    break;
                case 4:
                    initOtherInfo();
                    break;
            }
        }
    };

    public BookInfoPresenter(BookInfoActivity bookInfoActivity) {
        mBookInfoActivity = bookInfoActivity;
        mBookService = new BookService();
    }

    @Override
    public void start() {
        aBooks = (ArrayList<Book>) mBookInfoActivity.getIntent().getSerializableExtra(APPCONST.SEARCH_BOOK_BEAN);
        if (aBooks != null) {
            mBook = aBooks.get(0);
        } else {
            mBook = (Book) mBookInfoActivity.getIntent().getSerializableExtra(APPCONST.BOOK);
        }
        init();
    }

    private void init() {
        mBookInfoActivity.getBtnChangeSource().setOnClickListener(v -> {
            if (!NetworkUtils.isNetWorkAvailable()){
                TextHelper.showText("无网络连接！");
                return;
            }
            mBookInfoActivity.getPbLoading().setVisibility(View.VISIBLE);
            if (aBooks == null) {
                ChangeSourceDialog csd = new ChangeSourceDialog(mBookInfoActivity, mBook);
                csd.init(new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        aBooks = (ArrayList<Book>) o;
                        mHandler.sendMessage(mHandler.obtainMessage(2));
                    }

                    @Override
                    public void onError(Exception e) {
                        mHandler.sendMessage(mHandler.obtainMessage(3));
                    }
                });
            } else {
                createChangeSourceDia();
            }
        });
        mBookInfoActivity.getTvDisclaimer().setOnClickListener(v -> DialogCreator.createAssetTipDialog(mBookInfoActivity, "免责声明", "disclaimer.fy"));
        if (isBookCollected()) {
            mBookInfoActivity.getBtnAddBookcase().setText("移除书籍");
            mBookInfoActivity.getBtnReadBook().setText("继续阅读");
        } else {
            mBookInfoActivity.getBtnAddBookcase().setText("加入书架");
            mBookInfoActivity.getBtnReadBook().setText("开始阅读");
        }
        mBookInfoActivity.getLlTitleBack().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBookInfoActivity.finish();
            }
        });
        mBookInfoActivity.getBtnAddBookcase().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isBookCollected()) {
                    mBookService.addBook(mBook);
                    TextHelper.showText("成功加入书架");
                    mBookInfoActivity.getBtnAddBookcase().setText("移除书籍");
                } else {
                    mBookService.deleteBookById(mBook.getId());
                    TextHelper.showText("成功移除书籍");
                    mBookInfoActivity.getBtnAddBookcase().setText("加入书架");
                    mBookInfoActivity.getBtnReadBook().setText("开始阅读");
                }

            }
        });
        mReadCrawler = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
        mBookInfoActivity.getBtnReadBook().setOnClickListener(view -> {
            final boolean isCollected;
            if (isBookCollected()) {
                isCollected = true;
            } else {
                mBookService.addBook(mBook);
                isCollected = false;
                CommonApi.getBookChapters(mBook.getChapterUrl(), mReadCrawler, new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        mBookService.updateEntity(mBook);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
            }
            Intent intent = new Intent(mBookInfoActivity, ReadActivity.class);
            intent.putExtra(APPCONST.BOOK, mBook);
            intent.putExtra("isCollected", isCollected);
            mBookInfoActivity.startActivityForResult(intent, APPCONST.REQUEST_READ);
        });
        initBookInfo();
    }

    @SuppressLint("SetTextI18n")
    private void initBookInfo() {
        mBookInfoActivity.getTvTitleText().setText(mBook.getName());
        mBookInfoActivity.getTvBookAuthor().setText(mBook.getAuthor());
        if (StringHelper.isEmpty(mBook.getImgUrl())) {
            mBook.setImgUrl("");
        }
        assert mBook.getNewestChapterTitle() != null;
        mBookInfoActivity.getTvBookNewestChapter().setText("最新章节:" + mBook.getNewestChapterTitle().replace("最近更新 ", ""));
        mBookInfoActivity.getTvBookDesc().setText("");
        mBookInfoActivity.getTvBookType().setText("");
        mBookInfoActivity.getTvBookName().setText(mBook.getName());
        if (!"null".equals(mBook.getSource())) {
            mBookInfoActivity.getTvBookSource().setText("书源：" + BookSource.fromString(mBook.getSource()).text);
        }
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(mBook.getSource());
        if (rc instanceof BookInfoCrawler && StringHelper.isEmpty(mBook.getImgUrl())) {
            BookInfoCrawler bic = (BookInfoCrawler) rc;
            CommonApi.getBookInfo(mBook, bic, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    mHandler.sendMessage(mHandler.obtainMessage(4));
                }

                @Override
                public void onError(Exception e) {

                }
            });
        } else {
            initOtherInfo();
        }
    }

    private void initOtherInfo() {
        mBookInfoActivity.getTvBookDesc().setText(mBook.getDesc());
        mBookInfoActivity.getTvBookType().setText(mBook.getType());
        Glide.with(mBookInfoActivity)
                .load(mBook.getImgUrl())
                .error(R.mipmap.no_image)
                .placeholder(R.mipmap.no_image)
                //设置圆角
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                .into(mBookInfoActivity.getIvBookImg());
    }

    private boolean isBookCollected() {
        Book book = mBookService.findBookByAuthorAndName(mBook.getName(), mBook.getAuthor());
        if (book == null) {
            return false;
        } else {
            mBook = book;
            return true;
        }
    }

    private void createChangeSourceDia() {
        if (aBooks == null){
            mHandler.sendMessage(mHandler.obtainMessage(3));
            return;
        }
        mBookInfoActivity.getPbLoading().setVisibility(View.GONE);
        CharSequence[] sources = new CharSequence[aBooks.size()];
        int checkedItem = 0;
        for (int i = 0; i < sources.length; i++) {
            sources[i] = BookSource.fromString(aBooks.get(i).getSource()).text
                    + "\n" + aBooks.get(i).getNewestChapterTitle();
            if (sources[i].equals(BookSource.fromString(mBook.getSource()).text
                    + "\n" + aBooks.get(i).getNewestChapterTitle())) {
                checkedItem = i;
            }
        }
        final int finalCheckedItem = checkedItem;
        AlertDialog dialog = new AlertDialog.Builder(mBookInfoActivity)
                .setTitle("切换书源")
                .setCancelable(true)
                .setSingleChoiceItems(sources, checkedItem, (dialog1, which) -> {
                    boolean isBookCollected = isBookCollected();
//                        mBookService.deleteBook(mBook);
                    if (finalCheckedItem == which) {
                        dialog1.dismiss();
                        return;
                    }
                    Book book = aBooks.get(which);
                    Book bookTem = new Book(mBook);
                    bookTem.setChapterUrl(book.getChapterUrl());
                    bookTem.setImgUrl(book.getImgUrl());
                    bookTem.setType(book.getType());
                    bookTem.setDesc(book.getDesc());
                    bookTem.setSource(book.getSource());
                    if (isBookCollected) {
                        mBookService.updateBook(mBook, bookTem);
                    }
                    mBook = bookTem;
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                    if (isBookCollected) {
                        DialogCreator.createTipDialog(mBookInfoActivity,
                                "换源成功，由于不同书源的章节数量不一定相同，故换源后历史章节可能出错！");
                    }
                    dialog1.dismiss();
                }).create();
        dialog.show();
                    /*try {
                        Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                        mAlert.setAccessible(true);
                        Object mAlertController = mAlert.get(dialog);
                        Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                        mMessage.setAccessible(true);
                        TextView mMessageView = (TextView) mMessage.get(mAlertController);
                        mMessageView.setTextSize(5);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }*/
    }
}
