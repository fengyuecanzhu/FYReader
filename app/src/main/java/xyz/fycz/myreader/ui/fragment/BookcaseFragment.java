package xyz.fycz.myreader.ui.fragment;


import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.widget.custom.DragSortGridView;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.Unbinder;
import xyz.fycz.myreader.ui.presenter.BookcasePresenter;

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
    @BindView(R.id.rl_book_edit)
    RelativeLayout rlBookEdit;
    @BindView(R.id.book_selected_all)
    CheckBox mCbSelectAll;
    @BindView(R.id.book_btn_delete)
    Button mBtnDelete;
    @BindView(R.id.book_add_group)
    Button mBtnAddGroup;

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

    public boolean isRecreate() {
        return unbinder == null;
    }

    public RelativeLayout getRlBookEdit() {
        return rlBookEdit;
    }

    public CheckBox getmCbSelectAll() {
        return mCbSelectAll;
    }

    public Button getmBtnDelete() {
        return mBtnDelete;
    }

    public Button getmBtnAddGroup() {
        return mBtnAddGroup;
    }
}
