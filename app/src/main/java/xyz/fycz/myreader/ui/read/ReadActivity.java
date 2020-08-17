package xyz.fycz.myreader.ui.read;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.widget.page.PageView;

public class ReadActivity extends BaseActivity {


    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;
    @BindView(R.id.read_pv_page)
    PageView srlContent;
    @BindView(R.id.pb_nextPage)
    VerticalSeekBar pbNextPage;

    @BindView(R.id.tv_end_page_tip)
    TextView tvEndPageTip;

    private ReadPresenter mReadPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏应用程序的标题栏，即当前activity的label
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏android系统的状态栏
        setContentView(R.layout.activity_read_new);
        ButterKnife.bind(this);
        mReadPresenter = new ReadPresenter(this);
        mReadPresenter.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mReadPresenter.onActivityResult(requestCode, resultCode, data);
    }



    public ReadPresenter getmReadPresenter() {
        return mReadPresenter;

    }

    public PageView getSrlContent() {
        return srlContent;
    }


    public ProgressBar getPbLoading() {
        return pbLoading;
    }



    public TextView getTvEndPageTip() {
        return tvEndPageTip;
    }


    @Override
    protected void onDestroy() {
        mReadPresenter.onDestroy();
        super.onDestroy();
    }
    /*@Override
    protected void onPause() {
        super.onPause();
    }*/

    @Override
    public void onBackPressed() {
        if (!mReadPresenter.isCollected()){
            DialogCreator.createCommonDialog(ReadActivity.this, "加入书架", "喜欢本书就加入书架吧", true, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mReadPresenter.saveLastChapterReadPosition();
                            mReadPresenter.setCollected(true);
                            exit();
                        }
                    }
                    , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mReadPresenter.deleteBook();
                            exit();
                        }
                    });
        } else {
            mReadPresenter.saveLastChapterReadPosition();
            exit();
        }
    }

    private void exit(){
        // 返回给BookDetail
        Intent result = new Intent();
        result.putExtra(APPCONST.RESULT_IS_COLLECTED, mReadPresenter.isCollected());
        if (mReadPresenter.getmPageLoader() != null) {
            result.putExtra(APPCONST.RESULT_LAST_READ_POSITION, mReadPresenter.getmPageLoader().getPagePos());
        }
        setResult(AppCompatActivity.RESULT_OK, result);
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isVolumeTurnPage = SysManager.getSetting().isVolumeTurnPage();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP :
                if (isVolumeTurnPage) {
                    return mReadPresenter.getmPageLoader().skipToPrePage();
                }
            case KeyEvent.KEYCODE_VOLUME_DOWN :
                if (isVolumeTurnPage) {
                    return mReadPresenter.getmPageLoader().skipToNextPage();
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    public VerticalSeekBar getPbNextPage() {
        return pbNextPage;
    }
}
