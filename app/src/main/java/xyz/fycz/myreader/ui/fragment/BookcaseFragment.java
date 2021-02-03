package xyz.fycz.myreader.ui.fragment;


import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import xyz.fycz.myreader.databinding.FragmentBookcaseBinding;
import xyz.fycz.myreader.ui.presenter.BookcasePresenter;
import xyz.fycz.myreader.widget.custom.DragSortGridView;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookcaseFragment extends Fragment {

    private FragmentBookcaseBinding binding;

    private BookcasePresenter mBookcasePresenter;

    public BookcaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookcaseBinding.inflate(inflater, container, false);
        mBookcasePresenter = new BookcasePresenter(this);
        mBookcasePresenter.start();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBookcasePresenter.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBookcasePresenter.init();
    }

    public LinearLayout getLlNoDataTips() {
        return binding.llNoDataTips;
    }

    public DragSortGridView getGvBook() {
        return binding.gvBook;
    }

    public SmartRefreshLayout getSrlContent() {
        return binding.srlContent;
    }

    public RelativeLayout getRlDownloadTip() {
        return binding.rlDownloadTip;
    }

    public TextView getTvDownloadTip() {
        return binding.tvDownloadTip;
    }

    public TextView getTvStopDownload() {
        return binding.tvStopDownload;
    }

    public ProgressBar getPbDownload() {
        return binding.pbDownload;
    }

    public BookcasePresenter getmBookcasePresenter() {
        return mBookcasePresenter;
    }

    public boolean isRecreate() {
        return binding == null;
    }

    public RelativeLayout getRlBookEdit() {
        return binding.rlBookEdit;
    }

    public CheckBox getmCbSelectAll() {
        return binding.bookSelectedAll;
    }

    public Button getmBtnDelete() {
        return binding.bookBtnDelete;
    }

    public Button getmBtnAddGroup() {
        return binding.bookAddGroup;
    }
}
