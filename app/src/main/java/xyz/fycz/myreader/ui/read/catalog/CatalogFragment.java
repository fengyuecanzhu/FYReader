package xyz.fycz.myreader.ui.read.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import xyz.fycz.myreader.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CatalogFragment extends Fragment {
    @BindView(R.id.rl_catalog)
    RelativeLayout rlCatalog;
    @BindView(R.id.lv_chapter_list)
    ListView lvChapterList;
    @BindView(R.id.change_sort)
    FloatingActionButton fcChangeSort;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    Unbinder unbinder;

    private CatalogPresenter mCatalogPresent;

    public CatalogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);
        unbinder = ButterKnife.bind(this, view);
        mCatalogPresent = new CatalogPresenter(this);
        mCatalogPresent.start();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public RelativeLayout getRlCatalog() {
        return rlCatalog;
    }

    public ListView getLvChapterList() {
        return lvChapterList;
    }

    public FloatingActionButton getFcChangeSort() {
        return fcChangeSort;
    }

    public CatalogPresenter getmCatalogPresent() {
        return mCatalogPresent;
    }

    public ProgressBar getPbLoading() {
        return pbLoading;
    }
}
