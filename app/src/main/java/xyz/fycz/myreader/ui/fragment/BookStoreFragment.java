package xyz.fycz.myreader.ui.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.custom.DragSortGridView;
import xyz.fycz.myreader.ui.presenter.BookStorePresenter;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.widget.RefreshLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookStoreFragment extends Fragment {
    @BindView(R.id.refresh_layout)
    RefreshLayout mRlRefresh;
    @BindView(R.id.rv_type_list)
    RecyclerView rvTypeList;
    @BindView(R.id.rv_book_list)
    RecyclerView rvBookList;
    @BindView(R.id.srl_book_list)
    SmartRefreshLayout srlBookList;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    Unbinder unbinder;
    private BookStorePresenter mBookStorePresenter;
    private boolean isFirstInit;

    public BookStoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        /*mBookStorePresenter = new BookStorePresenter(this);
        mBookStorePresenter.start();*/
        isFirstInit = true;
        return view;

    }


    public void lazyLoad() {
        if (isFirstInit) {
            mBookStorePresenter = new BookStorePresenter(this);
            mBookStorePresenter.start();
            isFirstInit = false;
        }
    }

    public RecyclerView getRvTypeList() {
        return rvTypeList;
    }

    public RecyclerView getRvBookList() {
        return rvBookList;
    }

    public SmartRefreshLayout getSrlBookList() {
        return srlBookList;
    }

    public ProgressBar getPbLoading() {
        return pbLoading;
    }

    public RefreshLayout getmRlRefresh() {
        return mRlRefresh;
    }
}
