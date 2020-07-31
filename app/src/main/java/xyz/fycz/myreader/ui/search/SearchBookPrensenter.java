package xyz.fycz.myreader.ui.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import android.widget.TextView;
import androidx.annotation.NonNull;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.crawler.*;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.ui.bookinfo.BookInfoActivity;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.SearchHistory;
import xyz.fycz.myreader.greendao.service.SearchHistoryService;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.CommonApi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import me.gujun.android.taggroup.TagGroup;


public class SearchBookPrensenter implements BasePresenter {

    private SearchBookActivity mSearchBookActivity;
    private SearchBookAdapter mSearchBookAdapter;
    private String searchKey;//搜索关键字
    private ArrayList<SearchBookBean> mBooksBean = new ArrayList<>();
    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks = new ConcurrentMultiValueMap<>();
    private ArrayList<SearchHistory> mSearchHistories = new ArrayList<>();
    private ArrayList<String> mSuggestions = new ArrayList<>();

    private SearchHistoryService mSearchHistoryService;

    private SearchHistoryAdapter mSearchHistoryAdapter;


    private int curThreadCount;

    private int allThreadCount;

    private int inputConfirm = 0;//搜索输入确认
    private int confirmTime = 1000;//搜索输入确认时间（毫秒）

    private static String[] suggestion = {"第一序列", "大道朝天", "伏天氏", "终极斗罗", "我师兄实在太稳健了", "烂柯棋缘", "诡秘之主"};
    private static String[] suggestion2 = {"不朽凡人", "圣墟", "我是至尊", "龙王传说", "太古神王", "一念永恒", "雪鹰领主", "大主宰"};

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    search();
                    break;
                case 2:
                    if (curThreadCount == 0) {
                        initSearchList();
                        mSearchBookActivity.getSrlSearchBookList().finishRefresh();
                        mSearchBookActivity.getPbLoading().setVisibility(View.GONE);
                    }/*else {
                        notifyDataSetChanged();
                        mSearchBookActivity.getSrlSearchBookList().finishRefresh();
                    }*/
                    break;
            }
        }
    };

    public SearchBookPrensenter(SearchBookActivity searchBookActivity) {
        mSearchBookActivity = searchBookActivity;
        mSearchHistoryService = new SearchHistoryService();
        for (int i = 0; i < suggestion.length; i++) {
            mSuggestions.add(suggestion[i]);
        }
    }

    @Override
    public void start() {
        mSearchBookActivity.getTvTitleText().setText("搜索");
        mSearchBookActivity.etSearchKey.requestFocus();//get the focus
        //enter事件
        mSearchBookActivity.getEtSearchKey().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                    return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
                }
                return false;
            }
        });
        //换一批点击事件
        mSearchBookActivity.getRenewByImage().setOnClickListener(new RenewSuggestionBook());
        //换一批点击事件
        mSearchBookActivity.getRenewByText().setOnClickListener(new RenewSuggestionBook());
        //返回
        mSearchBookActivity.getLlTitleBack().setOnClickListener(view -> mSearchBookActivity.finish());
        //搜索框改变事件
        mSearchBookActivity.getEtSearchKey().addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                searchKey = editable.toString();
                if (StringHelper.isEmpty(searchKey)) {
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                }

            }

        });
        //进入书籍详情页
        mSearchBookActivity.getLvSearchBooksList().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(mSearchBookActivity, BookInfoActivity.class);
                intent.putExtra(APPCONST.SEARCH_BOOK_BEAN, new ArrayList<>(mBooks.getValues(mBooksBean.get(i))));
                mSearchBookActivity.startActivity(intent);
            }
        });
        //搜索按钮点击事件
        mSearchBookActivity.getTvSearchConform().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendMessage(mHandler.obtainMessage(1));
            }
        });
        //suggestion搜索事件
        mSearchBookActivity.getTgSuggestBook().setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                mSearchBookActivity.getEtSearchKey().setText(tag);
                mSearchBookActivity.getEtSearchKey().setSelection(tag.length());
                mHandler.sendMessage(mHandler.obtainMessage(1));
            }
        });
        //历史记录搜索事件
        mSearchBookActivity.getLvHistoryList().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSearchBookActivity.getEtSearchKey().setText(mSearchHistories.get(position).getContent());
                mSearchBookActivity.getEtSearchKey().setSelection(mSearchHistories.get(position).getContent().length());
                mHandler.sendMessage(mHandler.obtainMessage(1));
            }
        });
        //清空历史记录
        mSearchBookActivity.getLlClearHistory().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchHistoryService.clearHistory();
                initHistoryList();
            }
        });
        //清除单个历史记录
        mSearchBookActivity.getLvHistoryList().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSearchHistories.get(position) != null) {
                    mSearchHistoryService.deleteHistory(mSearchHistories.get(position));
                    initHistoryList();
                }
                return true;
            }
        });
        //上拉刷新
        mSearchBookActivity.getSrlSearchBookList().setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mHandler.sendMessage(mHandler.obtainMessage(1));
            }
        });
        mSearchBookActivity.getSrlSearchBookList().setEnableRefresh(false);
        initSuggestionBook();
        initHistoryList();
    }


    /**
     * 初始化建议书目
     */
    private void initSuggestionBook() {
        mSearchBookActivity.getTgSuggestBook().setTags(suggestion);
    }

    private class RenewSuggestionBook implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String[] s = mSearchBookActivity.getTgSuggestBook().getTags();
            if (Arrays.equals(s, suggestion)) {
                mSearchBookActivity.getTgSuggestBook().setTags(suggestion2);
            } else {
                mSearchBookActivity.getTgSuggestBook().setTags(suggestion);
            }
        }
    }

    /**
     * 初始化历史列表
     */
    private void initHistoryList() {
        mSearchHistories = mSearchHistoryService.findAllSearchHistory();
        if (mSearchHistories == null || mSearchHistories.size() == 0) {
            mSearchBookActivity.getLlHistoryView().setVisibility(View.GONE);
        } else {
            mSearchHistoryAdapter = new SearchHistoryAdapter(mSearchBookActivity, R.layout.listview_search_history_item, mSearchHistories);
            mSearchBookActivity.getLvHistoryList().setAdapter(mSearchHistoryAdapter);
            mSearchBookActivity.getLlHistoryView().setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化搜索列表
     */
    private void initSearchList() {
        initmBooksBean();
        mSearchBookAdapter = new SearchBookAdapter(mSearchBookActivity,
                R.layout.listview_search_book_item, mBooksBean, mBooks);
        mSearchBookActivity.getLvSearchBooksList().setAdapter(mSearchBookAdapter);
        mSearchBookActivity.getLvSearchBooksList().setVisibility(View.VISIBLE);
        mSearchBookActivity.getLlSuggestBooksView().setVisibility(View.GONE);
        mSearchBookActivity.getLlHistoryView().setVisibility(View.GONE);
    }

    /**
     * 更新搜索列表
     */
    private void notifyDataSetChanged() {
        if (curThreadCount == 0) {
            mSearchBookActivity.getPbLoading().setVisibility(View.GONE);
        }
        initmBooksBean();
    }

    /**
     * 初始化mBooksBean
     */
    private void initmBooksBean() {
        synchronized (this) {
            mBooksBean.clear();
            mBooksBean.addAll(mBooks.keySet());
            //排序，基于最符合关键字的搜书结果应该是最短的
            //TODO ;这里只做了简单的比较排序，还需要继续完善
            Collections.sort(mBooksBean, new Comparator<SearchBookBean>() {
                @Override
                public int compare(SearchBookBean o1, SearchBookBean o2) {
                    if (searchKey.equals(o1.getName()))
                        return -1;
                    if (searchKey.equals(o2.getName()))
                        return 1;
                    if (searchKey.equals(o1.getAuthor()))
                        return -1;
                    if (searchKey.equals(o2.getAuthor()))
                        return 1;
                    if (o1.getName().length() < o2.getName().length())
                        return -1;
                    if (o1.getName().length() == o2.getName().length())
                        return 0;
                    return 1;
                }
            });
            /*MyApplication.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSearchBookAdapter.notifyDataSetChanged();
                }
            });*/
        }
    }

    /**
     * 获取搜索数据
     */
    private void getData() {
        mBooksBean.clear();
        mBooks.clear();
        allThreadCount = 4;
        curThreadCount = allThreadCount;
        searchBookByCrawler(new BiQuGe44ReadCrawler(), "");
        searchBookByCrawler(new TianLaiReadCrawler(), "");
        searchBookByCrawler(new BiQuGeReadCrawler(), "gbk");
        searchBookByCrawler(new PinShuReadCrawler(), "gbk");
    }

    /**
     * 搜索
     */
    private void search() {
        mSearchBookActivity.getPbLoading().setVisibility(View.VISIBLE);
        if (StringHelper.isEmpty(searchKey)) {
            mSearchBookActivity.getPbLoading().setVisibility(View.GONE);
            mSearchBookActivity.getLvSearchBooksList().setVisibility(View.GONE);
            mSearchBookActivity.getLlSuggestBooksView().setVisibility(View.VISIBLE);
            initHistoryList();
            mSearchBookActivity.getLvSearchBooksList().setAdapter(null);
            mSearchBookActivity.getSrlSearchBookList().setEnableRefresh(false);
        } else {
            mSearchBookActivity.getLvSearchBooksList().setVisibility(View.VISIBLE);
            mSearchBookActivity.getLlSuggestBooksView().setVisibility(View.GONE);
            mSearchBookActivity.getLlHistoryView().setVisibility(View.GONE);
            getData();
            mSearchBookActivity.getSrlSearchBookList().setEnableRefresh(true);
            mSearchHistoryService.addOrUpadteHistory(searchKey);
            //收起软键盘
            InputMethodManager imm = (InputMethodManager) MyApplication.getmContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(mSearchBookActivity.getEtSearchKey().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public boolean onBackPressed() {
        if (StringHelper.isEmpty(searchKey)) {
            return false;
        } else {
            mSearchBookActivity.getEtSearchKey().setText("");
            return true;
        }
    }

    private void searchBookByCrawler(ReadCrawler rc, String charset) {
        String searchKey = this.searchKey;
        if (charset.toLowerCase().equals("gbk")) {
            try {
                searchKey = URLEncoder.encode(this.searchKey, charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        CommonApi.search(searchKey, rc, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                ConcurrentMultiValueMap<SearchBookBean, Book> sbb =
                        (ConcurrentMultiValueMap<SearchBookBean, Book>) o;
                mBooks.addAll(sbb);
                curThreadCount--;
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }

            @Override
            public void onError(Exception e) {
                curThreadCount--;
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }
        });
    }

}

