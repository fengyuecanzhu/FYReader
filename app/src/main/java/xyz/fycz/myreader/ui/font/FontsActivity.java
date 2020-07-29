package xyz.fycz.myreader.ui.font;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.BindView;
import xyz.fycz.myreader.common.APPCONST;

import static xyz.fycz.myreader.util.UriFileUtil.getPath;

public class FontsActivity extends BaseActivity {

    @BindView(R.id.ll_title_back)
    LinearLayout llTitleBack;
    @BindView(R.id.tv_title_text)
    TextView tvTitleText;
    @BindView(R.id.system_title)
    LinearLayout systemTitle;
    @BindView(R.id.lv_fonts)
    ListView lvFonts;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    private FontsPresenter mFontsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fonts);
        ButterKnife.bind(this);
        setStatusBar(R.color.sys_line);
        mFontsPresenter = new FontsPresenter(this);
        mFontsPresenter.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path;
        if (resultCode == Activity.RESULT_OK && requestCode == APPCONST.SELECT_FILE_CODE) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                path = uri.getPath();
            }else {
                path = getPath(this, uri);
            }
            mFontsPresenter.saveLocalFont(path);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mFontsPresenter.notifyChange();
    }

    public ProgressBar getPbLoading() {
        return pbLoading;
    }

    public LinearLayout getLlTitleBack() {
        return llTitleBack;
    }

    public TextView getTvTitleText() {
        return tvTitleText;
    }

    public LinearLayout getSystemTitle() {
        return systemTitle;
    }

    public ListView getLvFonts() {
        return lvFonts;
    }
}
