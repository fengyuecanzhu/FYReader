package xyz.fycz.myreader.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.databinding.DialogBookSourceBinding;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.SearchEngine;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.ui.adapter.SourceExchangeAdapter;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.widget.RefreshProgressBar;

/**
 * 换源dialog
 */

public class SourceExchangeDialog extends Dialog {

    private static final String TAG = "SourceExchangeDialog";

    private DialogBookSourceBinding binding;

    private SearchEngine searchEngine;
    private SourceExchangeAdapter mAdapter;

    private OnSourceChangeListener listener;

    private Activity mActivity;
    private Book mShelfBook;
    private List<Book> aBooks;

    private AlertDialog mErrorDia;

    private int sourceIndex = -1;

    /***************************************************************************/
    public SourceExchangeDialog(@NonNull Activity activity, Book bookBean) {
        super(activity);
        mActivity = activity;
        mShelfBook = bookBean;
    }

    public void setShelfBook(Book mShelfBook) {
        this.mShelfBook = mShelfBook;
    }

    public void setABooks(List<Book> aBooks){
        this.aBooks = aBooks;
    }

    public void setSourceIndex(int sourceIndex){
        this.sourceIndex = sourceIndex;
    }

    public void setOnSourceChangeListener(OnSourceChangeListener listener) {
        this.listener = listener;
    }

    public List<Book> getaBooks(){return aBooks;}
    /*****************************Initialization********************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogBookSourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpWindow();
        initData();
        initClick();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //执行业务逻辑
        if (aBooks.size() == 0) {
            searchEngine.search(mShelfBook.getName(), mShelfBook.getAuthor());
            binding.ivStopSearch.setVisibility(View.VISIBLE);
            binding.rpb.setIsAutoLoading(true);
        }else {
            if (mAdapter.getItemCount() == 0) {
                mAdapter.addItems(aBooks);
            }
        }
    }

    /**
     * 设置Dialog显示的位置
     */
    private void setUpWindow() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        binding.it.toolbar.setTitle(mShelfBook.getName());
        binding.it.toolbar.setSubtitle(mShelfBook.getAuthor());
        //dialogTvTitle.setText(mShelfBook.getName() + "(" + mShelfBook.getAuthor() + ")");

        if (aBooks == null) {
            aBooks = new ArrayList<>();
        }

        mAdapter = new SourceExchangeAdapter();
        binding.dialogRvContent.setLayoutManager(new LinearLayoutManager(mActivity));
        binding.dialogRvContent.setAdapter(mAdapter);

        searchEngine = new SearchEngine();
        searchEngine.initSearchEngine(ReadCrawlerUtil.getEnableReadCrawlers());
    }

    private void initClick() {
        searchEngine.setOnSearchListener(new SearchEngine.OnSearchListener() {
            @Override
            public void loadMoreFinish(Boolean isAll) {
                synchronized (RefreshProgressBar.class) {
                    binding.rpb.setIsAutoLoading(false);
                    binding.ivStopSearch.setVisibility(View.GONE);
                }
            }

            @Override
            public void loadMoreSearchBook(ConMVMap<SearchBookBean, Book> items) {

            }

            @Override
            public void loadMoreSearchBook(List<Book> items) {
                //确保只有一个结果
                if (items != null && items.size() != 0) {
                    Book bean = items.get(0);
                    if (bean.getSource().equals(mShelfBook.getSource())) {
                        bean.setNewestChapterId("true");
                        sourceIndex = mAdapter.getItemSize();
                    }
                    mAdapter.addItem(items.get(0));
                    aBooks.add(bean);
                }
            }

            @Override
            public void searchBookError(Throwable throwable) {
                dismiss();
                DialogCreator.createTipDialog(mActivity, "未搜索到该书籍，书源加载失败！");
            }
        });

        mAdapter.setOnItemClickListener((view, pos) -> {
            if (listener == null) return;
            Book newBook = mAdapter.getItem(pos);
            if (mShelfBook.getSource() == null) {
                listener.onSourceChanged(newBook, pos);
                searchEngine.stopSearch();
                return;
            }
            if (mShelfBook.getSource().equals(newBook.getSource())) return;
            mShelfBook = newBook;
            listener.onSourceChanged(newBook, pos);
            mAdapter.getItem(pos).setNewestChapterId("true");
            if (sourceIndex > -1)
                mAdapter.getItem(sourceIndex).setNewestChapterId("false");
            sourceIndex = pos;
            mAdapter.notifyDataSetChanged();
            dismiss();
        });

        binding.ivStopSearch.setOnClickListener(v -> searchEngine.stopSearch());
        binding.ivRefreshSearch.setOnClickListener(v -> {
            searchEngine.stopSearch();
            binding.ivStopSearch.setVisibility(View.VISIBLE);
            mAdapter.clear();
            aBooks.clear();
            mAdapter.notifyDataSetChanged();
            searchEngine.search(mShelfBook.getName(), mShelfBook.getAuthor());
        });
    }


    /**************************Interface**********************************/
    public interface OnSourceChangeListener {
        void onSourceChanged(Book bean, int pos);
    }

}
