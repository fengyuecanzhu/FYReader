package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivitySearchBookBinding;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.SearchHistory;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.service.SearchHistoryService;
import xyz.fycz.myreader.model.SearchEngine;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.adapter.SearchBookAdapter;
import xyz.fycz.myreader.ui.adapter.SearchHistoryAdapter;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MultiChoiceDialog;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;

/**
 * @author fengyue
 * @date 2020/9/18 21:58
 */
public class SearchBookActivity extends BaseActivity {

    private ActivitySearchBookBinding binding;

    private SearchBookAdapter mSearchBookAdapter;
    private String searchKey;//搜索关键字
    private ArrayList<SearchBookBean> mBooksBean = new ArrayList<>();
    private ConMVMap<SearchBookBean, Book> mBooks = new ConMVMap<>();
    private ArrayList<SearchHistory> mSearchHistories = new ArrayList<>();
    private ArrayList<String> mSuggestions = new ArrayList<>();

    private SearchHistoryService mSearchHistoryService;

    private SearchHistoryAdapter mSearchHistoryAdapter;

    private int allThreadCount;

    private SearchEngine searchEngine;

    private Setting mSetting;

    private Menu menu;

    private AlertDialog mDisableSourceDia;


    private static String[] suggestion = {"第一序列", "大道朝天", "伏天氏", "终极斗罗", "我师兄实在太稳健了", "烂柯棋缘", "诡秘之主"};
    private static String[] suggestion2 = {"不朽凡人", "圣墟", "我是至尊", "龙王传说", "太古神王", "一念永恒", "雪鹰领主", "大主宰"};

    private boolean showHot;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    search();
                    break;
                case 2:
                    binding.srlSearchBookList.finishRefresh();
                    /*if (curThreadCount == 0 && !isStopSearch) {
                        rpb.setIsAutoLoading(false);
                    }*/
                    break;
                case 3:
                    binding.fabSearchStop.setVisibility(View.GONE);
                    break;
            }
        }
    };


    @Override
    protected void bindView() {
        binding = ActivitySearchBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        mSetting = SysManager.getSetting();
        mSearchHistoryService = SearchHistoryService.getInstance();
        showHot = !App.isDebug();
        searchEngine = new SearchEngine();
        searchEngine.setOnSearchListener(new SearchEngine.OnSearchListener() {
            @Override
            public void loadMoreFinish(Boolean isAll) {
                binding.rpb.setIsAutoLoading(false);
                binding.fabSearchStop.setVisibility(View.GONE);
            }

            @Override
            public void loadMoreSearchBook(ConMVMap<SearchBookBean, Book> items) {
                mBooks.addAll(items);
                mSearchBookAdapter.addAll(new ArrayList<>(items.keySet()), searchKey);
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }

            @Override
            public void loadMoreSearchBook(List<Book> items) {

            }

            @Override
            public void searchBookError(Throwable throwable) {
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }
        });
    }


    @Override
    protected void initWidget() {
        super.initWidget();
        initSuggestionList();
        //enter事件
        binding.etSearchKey.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                mHandler.sendMessage(mHandler.obtainMessage(1));
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
            return false;
        });

        switch (mSetting.getSearchFilter()) {
            case 0:
                binding.rbAllSearch.setChecked(true);
                break;
            case 1:
            default:
                binding.rbFuzzySearch.setChecked(true);
                break;
            case 2:
                binding.rbPreciseSearch.setChecked(true);
                break;
        }

        binding.rgSearchFilter.setOnCheckedChangeListener((group, checkedId) -> {
            int searchFilter;
            switch (checkedId) {
                case R.id.rb_all_search:
                default:
                    searchFilter = 0;
                    break;
                case R.id.rb_fuzzy_search:
                    searchFilter = 1;
                    break;
                case R.id.rb_precise_search:
                    searchFilter = 2;
                    break;
            }
            mSetting.setSearchFilter(searchFilter);
            SysManager.saveSetting(mSetting);
        });

        //搜索框改变事件
        binding.etSearchKey.addTextChangedListener(new TextWatcher() {

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

        binding.rvSearchBooksList.setLayoutManager(new LinearLayoutManager(this));

        //上拉刷新
        binding.srlSearchBookList.setOnRefreshListener(refreshLayout -> {
            stopSearch();
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        initHistoryList();
        mHandler.postDelayed(() -> binding.etSearchKey.requestFocus(), 200);
    }

    @Override
    protected void initClick() {
        super.initClick();

        //换一批点击事件
        binding.llRefreshSuggestBooks.setOnClickListener(new RenewSuggestionBook());

        //搜索按钮点击事件
        binding.tvSearchConform.setOnClickListener(view -> mHandler.sendMessage(mHandler.obtainMessage(1)));
        //suggestion搜索事件
        binding.tgSuggestBook.setOnTagClickListener(tag -> {
            binding.etSearchKey.setText(tag);
            binding.etSearchKey.setSelection(tag.length());
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //历史记录搜索事件
        binding.lvHistoryList.setOnItemClickListener((parent, view, position, id) -> {
            binding.etSearchKey.setText(mSearchHistories.get(position).getContent());
            binding.etSearchKey.setSelection(mSearchHistories.get(position).getContent().length());
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });
        //清空历史记录
        binding.llClearHistory.setOnClickListener(v -> {
            mSearchHistoryService.clearHistory();
            initHistoryList();
        });
        //清除单个历史记录
        binding.lvHistoryList.setOnItemLongClickListener((parent, view, position, id) -> {
            if (mSearchHistories.get(position) != null) {
                mSearchHistoryService.deleteHistory(mSearchHistories.get(position));
                initHistoryList();
            }
            return true;
        });

        binding.fabSearchStop.setOnClickListener(v -> {
            stopSearch();
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        this.menu = menu;
        initSourceGroupMenu();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!showHot) {
            menu.findItem(R.id.action_hot).setVisible(true);
            menu.findItem(R.id.action_disable_source).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_hot) {
            showHot = !showHot;
            initSuggestionList();
        } else if (item.getItemId() == R.id.action_disable_source) {
            showDisableSourceDia();
        } else if (item.getItemId() == R.id.action_source_man) {
            startActivityForResult(new Intent(this, BookSourceActivity.class),
                    APPCONST.REQUEST_BOOK_SOURCE);
        } else {
            if (item.getGroupId() == R.id.source_group) {
                item.setChecked(true);
                SharedPreUtils sp = SharedPreUtils.getInstance();
                if (getString(R.string.all_source).equals(item.getTitle().toString())) {
                    sp.putString("searchGroup", "");
                } else {
                    sp.putString("searchGroup", item.getTitle().toString());
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == APPCONST.REQUEST_BOOK_SOURCE) {
                initSourceGroupMenu();
            }
        }
    }

    /**
     * 初始化书源分组菜单
     */
    public void initSourceGroupMenu() {
        if (menu == null) return;
        String searchGroup = SharedPreUtils.getInstance().getString("searchGroup");
        menu.removeGroup(R.id.source_group);
        MenuItem item = menu.add(R.id.source_group, Menu.NONE, Menu.NONE, R.string.all_source);
        MenuItem localItem = menu.add(R.id.source_group, Menu.NONE, Menu.NONE, R.string.local_source);
        if ("".equals(searchGroup)) {
            item.setChecked(true);
        } else if (getString(R.string.local_source).equals(searchGroup)) {
            localItem.setChecked(true);
        }
        List<String> groupList = BookSourceManager.getEnableNoLocalGroupList();
        for (String groupName : groupList) {
            item = menu.add(R.id.source_group, Menu.NONE, Menu.NONE, groupName);
            if (groupName.equals(searchGroup)) item.setChecked(true);
        }
        menu.setGroupCheckable(R.id.source_group, true, true);
    }

    private void showDisableSourceDia() {
        if (mDisableSourceDia != null) {
            mDisableSourceDia.show();
            return;
        }
        List<BookSource> sources = BookSourceManager.getAllBookSourceByOrderNum();
        CharSequence[] mSourcesName = new CharSequence[sources.size()];
        boolean[] isDisables = new boolean[sources.size()];
        int dSourceCount = 0;
        int i = 0;
        for (BookSource source : sources) {
            mSourcesName[i] = source.getSourceName();
            boolean isDisable = !source.getEnable();
            if (isDisable) dSourceCount++;
            isDisables[i++] = isDisable;
        }

        mDisableSourceDia = new MultiChoiceDialog().create(this, "选择禁用的书源",
                mSourcesName, isDisables, dSourceCount, (dialog, which) -> {
                    BookSourceManager.saveDatas(sources)
                            .subscribe(new MySingleObserver<Boolean>() {
                                @Override
                                public void onSuccess(@NonNull Boolean aBoolean) {
                                    if (aBoolean) {
                                        ToastUtils.showSuccess("保存成功");
                                    }
                                }
                            });
                }, null, new DialogCreator.OnMultiDialogListener() {
                    @Override
                    public void onItemClick(DialogInterface dialog, int which, boolean isChecked) {
                        sources.get(which).setEnable(!isChecked);
                    }

                    @Override
                    public void onSelectAll(boolean isSelectAll) {
                        for (BookSource source : sources) {
                            source.setEnable(!isSelectAll);
                        }
                    }
                });
    }

    /**
     * 初始化建议书目
     */
    private void initSuggestionList() {
        if (!showHot) {
            binding.tgSuggestBook.setTags(suggestion);
        } else {
            SharedPreUtils spu = SharedPreUtils.getInstance();
            Single.create((SingleOnSubscribe<String>) emitter -> {
                String cookie = spu.getString(getString(R.string.qdCookie), "");
                String url = "https://m.qidian.com/majax/search/auto?kw=&";
                if (cookie.equals("")) {
                    cookie = "_csrfToken=eXRDlZxmRDLvFAmdgzqvwWAASrxxp2WkVlH4ZM7e; newstatisticUUID=1595991935_2026387981";
                }
                url += cookie.split(";")[0];
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", cookie);
                emitter.onSuccess(OkHttpUtils.getHtml(url, null,
                        "utf-8", headers));
            }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<String>() {
                @Override
                public void onSuccess(@NotNull String s) {
                    parseSuggestionList(s);
                    if (mSuggestions.size() > 0) {
                        binding.tgSuggestBook.setTags(mSuggestions.subList(0, 5));
                    } else {
                        binding.llSuggestBooksView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    binding.llSuggestBooksView.setVisibility(View.GONE);
                }
            });
        }
    }

    private void parseSuggestionList(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray names = json.getJSONObject("data").getJSONArray("popWords");
            for (int i = 0; i < names.length(); i++) {
                mSuggestions.add(names.getJSONObject(i).getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class RenewSuggestionBook implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!showHot) {
                String[] s = binding.tgSuggestBook.getTags();
                if (Arrays.equals(s, suggestion)) {
                    binding.tgSuggestBook.setTags(suggestion2);
                } else {
                    binding.tgSuggestBook.setTags(suggestion);
                }
            } else {
                if (mSuggestions.size() > 0) {
                    String[] s = binding.tgSuggestBook.getTags();
                    if (s[0].equals(mSuggestions.get(0))) {
                        binding.tgSuggestBook.setTags(mSuggestions.subList(5, 10));
                    } else {
                        binding.tgSuggestBook.setTags(mSuggestions.subList(0, 5));
                    }
                }
            }
        }
    }

    /**
     * 初始化历史列表
     */
    private void initHistoryList() {
        mSearchHistories = mSearchHistoryService.findAllSearchHistory();
        if (mSearchHistories == null || mSearchHistories.size() == 0) {
            binding.llHistoryView.setVisibility(View.GONE);
        } else {
            mSearchHistoryAdapter = new SearchHistoryAdapter(this, R.layout.listview_search_history_item, mSearchHistories);
            binding.lvHistoryList.setAdapter(mSearchHistoryAdapter);
            binding.llHistoryView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化搜索列表
     */
    private void initSearchList() {
        //initmBooksBean();
        binding.rvSearchBooksList.setVisibility(View.VISIBLE);
        binding.llSuggestBooksView.setVisibility(View.GONE);
        binding.llSuggestBooksView.setVisibility(View.GONE);
    }


    /**
     * 获取搜索数据
     */
    private void getData() {
        initSearchList();
        mBooksBean.clear();
        mBooks.clear();
        List<ReadCrawler> readCrawlers = ReadCrawlerUtil
                .getEnableReadCrawlers(SharedPreUtils.getInstance().getString("searchGroup"));
        allThreadCount = readCrawlers.size();
        if (allThreadCount == 0) {
            ToastUtils.showWarring("当前书源已全部禁用，无法搜索！");
            binding.rpb.setIsAutoLoading(false);
            return;
        }
        /*for (ReadCrawler readCrawler : readCrawlers) {
            searchBookByCrawler(readCrawler, readCrawler.getSearchCharset());
        }*/
        searchEngine.initSearchEngine(readCrawlers);
        searchEngine.search(searchKey);
    }

    /**
     * 搜索
     */
    private void search() {
        binding.rpb.setIsAutoLoading(true);
        if (StringHelper.isEmpty(searchKey)) {
            stopSearch();
            binding.rpb.setIsAutoLoading(false);
            binding.rvSearchBooksList.setVisibility(View.GONE);
            binding.llSuggestBooksView.setVisibility(View.VISIBLE);
            binding.fabSearchStop.setVisibility(View.GONE);
            initHistoryList();
            binding.rvSearchBooksList.setAdapter(null);
            binding.srlSearchBookList.setEnableRefresh(false);
        } else {
            mSearchBookAdapter = new SearchBookAdapter(this, mBooks, searchEngine, searchKey);
            binding.rvSearchBooksList.setAdapter(mSearchBookAdapter);
            //进入书籍详情页
            mSearchBookAdapter.setOnItemClickListener((view, pos) -> {
                SearchBookBean data = mSearchBookAdapter.getItem(pos);
                ArrayList<Book> books = (ArrayList<Book>) mBooks.getValues(data);
                if (books == null || books.size() == 0) return;
                searchBookBean2Book(data, books.get(0));
                Intent intent = new Intent(this, BookDetailedActivity.class);
                intent.putExtra(APPCONST.SEARCH_BOOK_BEAN, books);
                startActivity(intent);
            });
            binding.srlSearchBookList.setEnableRefresh(true);
            binding.rvSearchBooksList.setVisibility(View.VISIBLE);
            binding.llSuggestBooksView.setVisibility(View.GONE);
            binding.llHistoryView.setVisibility(View.GONE);
            binding.fabSearchStop.setVisibility(View.VISIBLE);
            getData();
            mSearchHistoryService.addOrUpadteHistory(searchKey);
            //收起软键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.etSearchKey.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
            binding.etSearchKey.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        stopSearch();
        for (int i = 0; i < 9; i++) {
            mHandler.removeMessages(i + 1);
        }
        super.onDestroy();
    }

    private void searchBookBean2Book(SearchBookBean bean, Book book) {
        if (StringHelper.isEmpty(book.getType()) && !StringHelper.isEmpty(bean.getType()))
            book.setType(bean.getType());
        if (StringHelper.isEmpty(book.getType()) && !StringHelper.isEmpty(bean.getType()))
            book.setType(bean.getType());
        if (StringHelper.isEmpty(book.getDesc()) && !StringHelper.isEmpty(bean.getDesc()))
            book.setDesc(bean.getDesc());
        if (StringHelper.isEmpty(book.getStatus()) && !StringHelper.isEmpty(bean.getStatus()))
            book.setStatus(bean.getStatus());
        if (StringHelper.isEmpty(book.getWordCount()) && !StringHelper.isEmpty(bean.getWordCount()))
            book.setWordCount(bean.getWordCount());
        if (StringHelper.isEmpty(book.getNewestChapterTitle()) && !StringHelper.isEmpty(bean.getLastChapter()))
            book.setNewestChapterTitle(bean.getLastChapter());
        if (StringHelper.isEmpty(book.getUpdateDate()) && !StringHelper.isEmpty(bean.getUpdateTime()))
            book.setUpdateDate(bean.getUpdateTime());
        if (StringHelper.isEmpty(book.getImgUrl()) && !StringHelper.isEmpty(bean.getImgUrl()))
            book.setImgUrl(bean.getImgUrl());
    }
}
