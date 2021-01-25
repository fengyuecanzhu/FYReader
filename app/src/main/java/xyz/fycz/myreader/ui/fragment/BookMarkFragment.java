package xyz.fycz.myreader.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import xyz.fycz.myreader.databinding.FragmentBookmarkBinding;
import xyz.fycz.myreader.ui.presenter.BookMarkPresenter;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookMarkFragment extends Fragment {

    private FragmentBookmarkBinding binding;
    private BookMarkPresenter mBookMarkPresenter;


    public BookMarkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookmarkBinding.inflate(inflater, container, false);
        mBookMarkPresenter = new BookMarkPresenter(this);
        mBookMarkPresenter.start();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public ListView getLvBookmarkList() {
        return binding.lvBookmarkList;
    }

    public BookMarkPresenter getmBookMarkPresenter() {
        return mBookMarkPresenter;
    }
}
