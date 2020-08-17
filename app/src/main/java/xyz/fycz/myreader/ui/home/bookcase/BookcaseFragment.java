package xyz.fycz.myreader.ui.home.bookcase;


import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.custom.DragSortGridView;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookcaseFragment extends Fragment {
    @BindView(R.id.srl_content)
    SmartRefreshLayout srlContent;
    @BindView(R.id.ll_no_data_tips)
    LinearLayout llNoDataTips;
    @BindView(R.id.gv_book)
    DragSortGridView gvBook;
    @BindView(R.id.rl_download_tip)
    RelativeLayout rlDownloadTip;
    @BindView(R.id.tv_download_tip)
    TextView tvDownloadTip;
    @BindView(R.id.tv_stop_download)
    TextView tvStopDownload;
    @BindView(R.id.pb_download)
    ProgressBar pbDownload;

    Unbinder unbinder;

    private BookcasePresenter mBookcasePresenter;

    public BookcaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookcase, container, false);
        unbinder = ButterKnife.bind(this, view);
        mBookcasePresenter = new BookcasePresenter(this);
        mBookcasePresenter.start();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mBookcasePresenter.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mBookcasePresenter.getData();
        } else {
            mBookcasePresenter.init();
        }
    }

    public LinearLayout getLlNoDataTips() {
        return llNoDataTips;
    }

    public DragSortGridView getGvBook() {
        return gvBook;
    }

    public SmartRefreshLayout getSrlContent() {
        return srlContent;
    }

    public RelativeLayout getRlDownloadTip() {
        return rlDownloadTip;
    }

    public TextView getTvDownloadTip() {
        return tvDownloadTip;
    }

    public TextView getTvStopDownload() {
        return tvStopDownload;
    }

    public ProgressBar getPbDownload() {
        return pbDownload;
    }

    public BookcasePresenter getmBookcasePresenter() {
        return mBookcasePresenter;
    }
}
