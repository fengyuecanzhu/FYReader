package xyz.fycz.myreader.ui.activity;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.ui.presenter.AboutPresenter;

/**
 * @author fengyue
 * @date 2020/7/31 11:32
 */
public class AboutActivity extends BaseActivity {

    @BindView(R.id.ll_title_back)
    LinearLayout llTitleBack;
    @BindView(R.id.tv_title_text)
    TextView tvTitleText;
    @BindView(R.id.tv_version_name)
    TextView tvVersionName;
    @BindView(R.id.vm_author)
    CardView vmAuthor;
    @BindView(R.id.vw_share)
    CardView vmShare;
    @BindView(R.id.vw_update)
    CardView vmUpdate;
    @BindView(R.id.vw_update_log)
    CardView vmUpdateLog;
    @BindView(R.id.vw_git)
    CardView vmGit;
    @BindView(R.id.vw_disclaimer)
    CardView vmDisclaimer;

    private AboutPresenter mAboutPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        setStatusBar(R.color.sys_line);
        mAboutPresenter = new AboutPresenter(this);
        mAboutPresenter.start();
    }


    public LinearLayout getLlTitleBack() {
        return llTitleBack;
    }

    public TextView getTvTitleText() {
        return tvTitleText;
    }

    public TextView getTvVersionName() {
        return tvVersionName;
    }

    public CardView getVmAuthor() {
        return vmAuthor;
    }

    public CardView getVmShare() {
        return vmShare;
    }

    public CardView getVmUpdate() {
        return vmUpdate;
    }

    public CardView getVmUpdateLog() {
        return vmUpdateLog;
    }

    public CardView getVmGit() {
        return vmGit;
    }

    public CardView getVmDisclaimer() {
        return vmDisclaimer;
    }
}
