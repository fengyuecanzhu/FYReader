package xyz.fycz.myreader.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.base.LazyFragment;
import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.FragmentFindBook2Binding;
import xyz.fycz.myreader.entity.FindKind;
import xyz.fycz.myreader.entity.ad.AdBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.ui.activity.BookDetailedActivity;
import xyz.fycz.myreader.ui.activity.BookstoreActivity;
import xyz.fycz.myreader.ui.activity.ReadActivity;
import xyz.fycz.myreader.ui.adapter.holder.FindBookHolder;
import xyz.fycz.myreader.ui.dialog.SourceExchangeDialog;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.AdUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.BookApi;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.widget.RefreshLayout;

/**
 * @author fengyue
 * @date 2021/7/21 23:06
 */
public class FindBook2Fragment extends LazyFragment {
    private FragmentFindBook2Binding binding;
    private FindKind kind;
    private FindCrawler findCrawler;
    private int page = 1;
    private BaseListAdapter<Book> findBookAdapter;
    private SourceExchangeDialog mSourceDia;

    private static final String KIND = "kind";
    private static final String FIND_CRAWLER = "findCrawler";
    private AdBean adBean;
    private View flow;

    public FindBook2Fragment() {
    }

    public FindBook2Fragment(FindKind kind, FindCrawler findCrawler) {
        this.kind = kind;
        this.findCrawler = findCrawler;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String dataKey = savedInstanceState.getString(APPCONST.DATA_KEY);
            kind = (FindKind) BitIntentDataManager.getInstance().getData(KIND + dataKey);
            findCrawler = (FindCrawler) BitIntentDataManager.getInstance().getData(FIND_CRAWLER + dataKey);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        String dataKey = StringHelper.getStringRandom(25);
        BitIntentDataManager.getInstance().putData(KIND + dataKey, kind);
        BitIntentDataManager.getInstance().putData(FIND_CRAWLER + dataKey, findCrawler);
        outState.putString(APPCONST.DATA_KEY, dataKey);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void lazyInit() {
        initData();
        initWidget();
    }

    @Nullable
    @Override
    public View bindView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        binding = FragmentFindBook2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    protected void initData() {
        adBean = AdUtils.getAdConfig().getFind();
        getFlow();
        findBookAdapter = new BaseListAdapter<Book>() {
            @Override
            protected IViewHolder<Book> createViewHolder(int viewType) {
                return new FindBookHolder();
            }
        };
        binding.rvFindBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFindBooks.setAdapter(findBookAdapter);
        page = 1;
        loadBooks();
    }

    private void getFlow() {
        AdUtils.checkHasAd().subscribe(new MySingleObserver<Boolean>() {
            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                if (aBoolean && AdUtils.adTime("find", adBean)) {
                    if (adBean.getStatus() > 0) {
                        AdUtils.getFlowAd(getActivity(), 1, view -> flow = view, "find");
                    }
                }
            }
        });
    }

    protected void initWidget() {
        binding.loading.setOnReloadingListener(() -> {
            page = 1;
            loadBooks();
        });
        //小说列表下拉加载更多事件
        binding.srlFindBooks.setOnLoadMoreListener(refreshLayout -> loadBooks());

        //小说列表上拉刷新事件
        binding.srlFindBooks.setOnRefreshListener(refreshLayout -> {
            page = 1;
            loadBooks();
        });

        findBookAdapter.setOnItemClickListener((view, pos) -> {
            Book book = findBookAdapter.getItem(pos);
            if (!findCrawler.needSearch()) {
                goToBookDetail(book);
            } else {
                if (BookService.getInstance().isBookCollected(book)) {
                    goToBookDetail(book);
                    return;
                }
                mSourceDia = new SourceExchangeDialog(getActivity(), book);
                mSourceDia.setOnSourceChangeListener((bean, pos1) -> {
                    Intent intent = new Intent(getContext(), BookDetailedActivity.class);
                    BitIntentDataManager.getInstance().putData(intent, mSourceDia.getaBooks());
                    intent.putExtra(APPCONST.SOURCE_INDEX, pos1);
                    getActivity().startActivity(intent);
                    mSourceDia.dismiss();
                });
                mSourceDia.show();
            }
        });
    }

    private void loadBooks() {
        BookApi.findBooks(kind, findCrawler, page).compose(RxUtils::toSimpleSingle).subscribe(new MyObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NotNull List<Book> books) {
                binding.loading.showFinish();
                if (page == 1) {
                    if (books.size() == 0) {
                        binding.srlFindBooks.finishRefreshWithNoMoreData();
                    } else {
                        findBookAdapter.refreshItems(books);
                        binding.srlFindBooks.finishRefresh();
                        if (flow != null) {
                            int index = findBookAdapter.getItemCount() - books.size() + 2;
                            index = Math.min(findBookAdapter.getItemCount() - 1, index);
                            findBookAdapter.addOther(index, flow);
                            flow = null;
                            getFlow();
                        }
                    }
                } else {
                    if (books.size() == 0) {
                        binding.srlFindBooks.finishLoadMoreWithNoMoreData();
                    } else {
                        findBookAdapter.addItems(books);
                        binding.srlFindBooks.finishLoadMore();
                        if (flow != null) {
                            int index = findBookAdapter.getItemCount() - books.size() + 2;
                            index = Math.min(findBookAdapter.getItemCount() - 1, index);
                            findBookAdapter.addOther(index, flow);
                            flow = null;
                            getFlow();
                        }
                    }
                }
                page++;
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                if (page == 1) {
                    ToastUtils.showError("数据加载失败\n" + e.getLocalizedMessage());
                    binding.loading.showError();
                    binding.srlFindBooks.finishRefresh();
                } else {
                    if (e.getMessage() != null && e.getMessage().contains("没有下一页")) {
                        binding.srlFindBooks.finishLoadMoreWithNoMoreData();
                    } else {
                        ToastUtils.showError("数据加载失败\n" + e.getLocalizedMessage());
                        binding.srlFindBooks.finishLoadMore();
                    }
                }
            }
        });
    }

    /**
     * 前往书籍详情
     *
     * @param book
     */
    private void goToBookDetail(Book book) {
        Intent intent = new Intent(getContext(), BookDetailedActivity.class);
        BitIntentDataManager.getInstance().putData(intent, book);
        getActivity().startActivity(intent);
    }


}
