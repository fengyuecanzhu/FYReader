package xyz.fycz.myreader.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.ui.presenter.BookMarkPresenter;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookMarkFragment extends Fragment {
    @BindView(R.id.lv_bookmark_list)
    ListView lvBookmarkList;

    private BookMarkPresenter mBookMarkPresenter;
    Unbinder unbinder;


    public BookMarkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        unbinder = ButterKnife.bind(this, view);
        mBookMarkPresenter = new BookMarkPresenter(this);
        mBookMarkPresenter.start();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public ListView getLvBookmarkList() {
        return lvBookmarkList;
    }

    public BookMarkPresenter getmBookMarkPresenter() {
        return mBookMarkPresenter;
    }
}
