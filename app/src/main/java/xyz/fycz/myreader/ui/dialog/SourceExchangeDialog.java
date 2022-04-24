/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.databinding.DialogBookSourceBinding;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.SearchEngine;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
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

    private String sourceSearchStr;

    /***************************************************************************/
    public SourceExchangeDialog(@NonNull Activity activity, Book bookBean) {
        super(activity);
        mActivity = activity;
        mShelfBook = bookBean;
    }

    public void setShelfBook(Book mShelfBook) {
        this.mShelfBook = mShelfBook;
    }

    public void setABooks(List<Book> aBooks) {
        this.aBooks = aBooks;
    }

    public void setSourceIndex(int sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public int getSourceIndex() {
        if (sourceIndex == -1) {
            for (int i = 0; i < aBooks.size(); i++) {
                Book book = aBooks.get(i);
                if (book.getSource().equals(mShelfBook.getSource())) {
                    sourceIndex = i;
                    break;
                }
            }
        }
        return sourceIndex == -1 ? 0 : sourceIndex;
    }

    public boolean hasCurBookSource() {
        return getSourceIndex() == sourceIndex;
    }

    public void setOnSourceChangeListener(OnSourceChangeListener listener) {
        this.listener = listener;
    }

    public List<Book> getaBooks() {
        return aBooks;
    }

    public Book getmShelfBook() {
        return mShelfBook;
    }

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
        if (aBooks.size() == 0) {
            searchEngine.search(mShelfBook.getName(), mShelfBook.getAuthor());
            binding.rpb.setIsAutoLoading(true);
        } else {
            if (mAdapter.getItemCount() == 0) {
                mAdapter.addItems(aBooks);
            }
        }
        binding.ivStopSearch.setVisibility(searchEngine.isSearching() ? View.VISIBLE : View.GONE);
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

        mAdapter = new SourceExchangeAdapter(this, aBooks);
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
                    if (TextUtils.isEmpty(sourceSearchStr)) {
                        mAdapter.addItem(bean);
                    } else {
                        if (BookSourceManager.getSourceNameByStr(bean.getSource()).contains(sourceSearchStr)) {
                            mAdapter.addItem(bean);
                        }
                    }
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
            if ((mShelfBook.getInfoUrl() != null && mShelfBook.getInfoUrl().equals(newBook.getInfoUrl()) ||
                    mShelfBook.getChapterUrl() != null && mShelfBook.getChapterUrl().equals(newBook.getChapterUrl())) &&
                    (mShelfBook.getSource() != null && mShelfBook.getSource().equals(newBook.getSource())))
                return;
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
            binding.rpb.setIsAutoLoading(true);
            binding.ivStopSearch.setVisibility(View.VISIBLE);
            mAdapter.clear();
            aBooks.clear();
            mAdapter.notifyDataSetChanged();
            searchEngine.search(mShelfBook.getName(), mShelfBook.getAuthor());
        });

        binding.searchView.onActionViewExpanded();
        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                sourceSearchStr = newText;
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public void stopSearch() {
        if (searchEngine != null) {
            searchEngine.stopSearch();
        }
    }

    /**************************Interface**********************************/
    public interface OnSourceChangeListener {
        void onSourceChanged(Book bean, int pos);
    }

}
