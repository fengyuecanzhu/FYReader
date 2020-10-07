package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import me.gujun.android.taggroup.TagGroup;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.base.BaseActivity2;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.model.SearchEngine;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.SearchHistory;
import xyz.fycz.myreader.greendao.service.SearchHistoryService;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.ui.adapter.SearchBookAdapter;
import xyz.fycz.myreader.ui.adapter.SearchHistoryAdapter;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.webapi.CommonApi;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.widget.RefreshProgressBar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/9/18 21:58
 */
public class SearchBookActivity extends BaseActivity2 {
    @BindView(R.id.et_search_key)
    EditText etSearchKey;
    @BindView(R.id.tv_search_conform)
    TextView tvSearchConform;
    @BindView(R.id.ll_refresh_suggest_books)
    LinearLayout llRefreshSuggestBooks;
    @BindView(R.id.rv_search_books_list)
    RecyclerView rvSearchBooksList;
    @BindView(R.id.ll_suggest_books_view)
    LinearLayout llSuggestBooksView;
    @BindView(R.id.rpb)
    RefreshProgressBar rpb;
    @BindView(R.id.lv_history_list)
    ListView lvHistoryList;
    @BindView(R.id.ll_clear_history)
    LinearLayout llClearHistory;
    @BindView(R.id.ll_history_view)
    LinearLayout llHistoryView;
    @BindView(R.id.tg_suggest_book)
    TagGroup tgSuggestBook;
    @BindView(R.id.renew_image)
    ImageView renewByImage;
    @BindView(R.id.renew_text)
    TextView renewByText;
    @BindView(R.id.srl_search_book_list)
    SmartRefreshLayout srlSearchBookList;
    @BindView(R.id.fabSearchStop)
    FloatingActionButton fabSearchStop;


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

    private boolean isStopSearch;

    private int inputConfirm = 0;//搜索输入确认
    private int confirmTime = 1000;//搜索输入确认时间（毫秒）

    private SearchEngine searchEngine;

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
                    if (srlSearchBookList != null) {
                        srlSearchBookList.finishRefresh();
                    }
                    /*if (curThreadCount == 0 && !isStopSearch) {
                        rpb.setIsAutoLoading(false);
                    }*/
                    break;
                case 3:
                    fabSearchStop.setVisibility(View.GONE);
                    break;
            }
        }
    };


    @Override
    protected int getContentId() {
        return R.layout.activity_search_book;
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("搜索");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mSearchHistoryService = SearchHistoryService.getInstance();
        for (int i = 0; i < suggestion.length; i++) {
            mSuggestions.add(suggestion[i]);
        }

        searchEngine = new SearchEngine();
        searchEngine.setOnSearchListener(new SearchEngine.OnSearchListener() {
            @Override
            public void loadMoreFinish(Boolean isAll) {
                if (rpb != null) {
                    rpb.setIsAutoLoading(false);
                }
                fabSearchStop.setVisibility(View.GONE);
            }

            @Override
            public void loadMoreSearchBook(ConcurrentMultiValueMap<SearchBookBean, Book> items) {
                mBooks.addAll(items);
                curThreadCount--;
                mSearchBookAdapter.addAll(new ArrayList<>(items.keySet()), searchKey);
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }

            @Override
            public void loadMoreSearchBook(List<Book> items) {

            }

            @Override
            public void searchBookError(Throwable throwable) {
                curThreadCount = 0;
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }
        });
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        etSearchKey.requestFocus();//get the focus
        //enter事件
        etSearchKey.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                mHandler.sendMessage(mHandler.obtainMessage(1));
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
            return false;
        });
        //搜索框改变事件
        etSearchKey.addTextChangedListener(new TextWatcher() {

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
        rvSearchBooksList.setLayoutManager(new LinearLayoutManager(this));

        //上拉刷新
        srlSearchBookList.setOnRefreshListener(refreshLayout -> {
            stopSearch();
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        initSuggestionBook();
        initHistoryList();
    }

    @Override
    protected void initClick() {
        super.initClick();

        //换一批点击事件
        renewByImage.setOnClickListener(new RenewSuggestionBook());
        //换一批点击事件
        renewByText.setOnClickListener(new RenewSuggestionBook());

        //搜索按钮点击事件
        tvSearchConform.setOnClickListener(view -> mHandler.sendMessage(mHandler.obtainMessage(1)));
        //suggestion搜索事件
        tgSuggestBook.setOnTagClickListener(tag -> {
            etSearchKey.setText(tag);
            etSearchKey.setSelection(tag.length());
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //历史记录搜索事件
        lvHistoryList.setOnItemClickListener((parent, view, position, id) -> {
            etSearchKey.setText(mSearchHistories.get(position).getContent());
            etSearchKey.setSelection(mSearchHistories.get(position).getContent().length());
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //清空历史记录
        llClearHistory.setOnClickListener(v -> {
            mSearchHistoryService.clearHistory();
            initHistoryList();
        });
        //清除单个历史记录
        lvHistoryList.setOnItemLongClickListener((parent, view, position, id) -> {
            if (mSearchHistories.get(position) != null) {
                mSearchHistoryService.deleteHistory(mSearchHistories.get(position));
                initHistoryList();
            }
            return true;
        });

        fabSearchStop.setOnClickListener(v -> {
            stopSearch();
        });
    }


    /**
     * 初始化建议书目
     */
    private void initSuggestionBook() {
        tgSuggestBook.setTags(suggestion);
    }

    private class RenewSuggestionBook implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String[] s = tgSuggestBook.getTags();
            if (Arrays.equals(s, suggestion)) {
                tgSuggestBook.setTags(suggestion2);
            } else {
                tgSuggestBook.setTags(suggestion);
            }
        }
    }

    /**
     * 初始化历史列表
     */
    private void initHistoryList() {
        mSearchHistories = mSearchHistoryService.findAllSearchHistory();
        if (mSearchHistories == null || mSearchHistories.size() == 0) {
            llHistoryView.setVisibility(View.GONE);
        } else {
            mSearchHistoryAdapter = new SearchHistoryAdapter(this, R.layout.listview_search_history_item, mSearchHistories);
            lvHistoryList.setAdapter(mSearchHistoryAdapter);
            llHistoryView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化搜索列表
     */
    private void initSearchList() {
        //initmBooksBean();
        rvSearchBooksList.setVisibility(View.VISIBLE);
        llSuggestBooksView.setVisibility(View.GONE);
        llSuggestBooksView.setVisibility(View.GONE);
    }




    /**
     * 获取搜索数据
     */
    private void getData() {
        initSearchList();
        mBooksBean.clear();
        mBooks.clear();
        ArrayList<ReadCrawler> readCrawlers = ReadCrawlerUtil.getReadCrawlers();
        allThreadCount = readCrawlers.size();
        if (allThreadCount == 0) {
            ToastUtils.showWarring("当前书源已全部禁用，无法搜索！");
            rpb.setIsAutoLoading(false);
            return;
        }
        curThreadCount = allThreadCount;
        /*for (ReadCrawler readCrawler : readCrawlers) {
            searchBookByCrawler(readCrawler, readCrawler.getSearchCharset());
        }*/
        searchEngine.initSearchEngine(ReadCrawlerUtil.getReadCrawlers());
        searchEngine.search(searchKey);
    }

    /**
     * 搜索
     */
    private void search() {
        rpb.setIsAutoLoading(true);
        fabSearchStop.setVisibility(View.VISIBLE);
        if (StringHelper.isEmpty(searchKey)) {
            isStopSearch = true;
            stopSearch();
            rpb.setIsAutoLoading(false);
            rvSearchBooksList.setVisibility(View.GONE);
            llSuggestBooksView.setVisibility(View.VISIBLE);
            initHistoryList();
            rvSearchBooksList.setAdapter(null);
            srlSearchBookList.setEnableRefresh(false);
        } else {
            isStopSearch = false;

            mSearchBookAdapter = new SearchBookAdapter(mBooks, searchEngine);

            rvSearchBooksList.setAdapter(mSearchBookAdapter);
            //进入书籍详情页
            mSearchBookAdapter.setOnItemClickListener((view, pos) -> {
                Intent intent = new Intent(this, BookDetailedActivity.class);
                intent.putExtra(APPCONST.SEARCH_BOOK_BEAN, new ArrayList<>(mBooks.getValues(mSearchBookAdapter.getItem(pos))));
                startActivity(intent);
            });
            srlSearchBookList.setEnableRefresh(true);
            rvSearchBooksList.setVisibility(View.VISIBLE);
            llSuggestBooksView.setVisibility(View.GONE);
            llHistoryView.setVisibility(View.GONE);
            getData();
            mSearchHistoryService.addOrUpadteHistory(searchKey);
            //收起软键盘
            InputMethodManager imm = (InputMethodManager) MyApplication.getmContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(etSearchKey.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void stopSearch() {
        searchEngine.stopSearch();
        mHandler.sendEmptyMessage(3);
    }

    @Override
    public void onBackPressed() {
        if (StringHelper.isEmpty(searchKey)) {
            super.onBackPressed();
        } else {
            etSearchKey.setText("");
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


    @Override
    protected void onDestroy() {
        isStopSearch = true;
        stopSearch();
        for (int i = 0; i < 9; i++) {
            mHandler.removeMessages(i + 1);
        }
        super.onDestroy();
    }
}
