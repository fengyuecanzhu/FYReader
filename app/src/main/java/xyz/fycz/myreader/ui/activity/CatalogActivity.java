package xyz.fycz.myreader.ui.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.material.tabs.TabLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.ui.presenter.CatalogActivityPresenter;

/**
 * @author fengyue
 * @date 2020/7/22 8:04
 */
public class CatalogActivity extends BaseActivity {
    @BindView(R.id.tl_tab_menu)
    TabLayout tlTabMenu;
    @BindView(R.id.iv_search)
    ImageView ivSearch;
    @BindView(R.id.rl_common_title)
    RelativeLayout rlCommonTitle;
    @BindView(R.id.vp_content)
    ViewPager vpContent;
    @BindView(R.id.iv_back)
    ImageView tvBack;

    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.iv_cancel)
    ImageView ivCancel;


    private CatalogActivityPresenter mCatalogPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        ButterKnife.bind(this);
        setStatusBar(0, false);
        mCatalogPresenter = new CatalogActivityPresenter(this);
        mCatalogPresenter.start();
    }

    public TabLayout getTlTabMenu() {
        return tlTabMenu;
    }

    public ImageView getIvSearch() {
        return ivSearch;
    }

    public RelativeLayout getRlCommonTitle() {
        return rlCommonTitle;
    }

    public ViewPager getVpContent() {
        return vpContent;
    }

    public ImageView getTvBack() {
        return tvBack;
    }


    public EditText getEtSearch() {
        return etSearch;
    }

    public ImageView getIvCancel() {
        return ivCancel;
    }
}
