package xyz.fycz.myreader.ui.bookinfo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.widget.*;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.BindView;
import xyz.fycz.myreader.common.APPCONST;

public class BookInfoActivity extends BaseActivity {

    @BindView(R.id.ll_title_back)
    LinearLayout llTitleBack;
    @BindView(R.id.tv_title_text)
    TextView tvTitleText;
    @BindView(R.id.system_title)
    LinearLayout systemTitle;
    @BindView(R.id.iv_book_img)
    ImageView ivBookImg;
    @BindView(R.id.tv_book_name)
    TextView tvBookName;
    @BindView(R.id.tv_book_author)
    TextView tvBookAuthor;
    @BindView(R.id.tv_book_type)
    TextView tvBookType;
    @BindView(R.id.tv_book_desc)
    TextView tvBookDesc;
    @BindView(R.id.btn_add_bookcase)
    Button btnAddBookcase;
    @BindView(R.id.btn_read_book)
    Button btnReadBook;
    @BindView(R.id.tv_book_newest_chapter)
    TextView tvBookNewestChapter;
    @BindView(R.id.tv_disclaimer)
    TextView tvDisclaimer;
    @BindView(R.id.tv_book_source)
    TextView tvBookSource;
    @BindView(R.id.btn_change_source)
    Button btnChangeSource;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    private BookInfoPresenter mBookInfoPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);
        ButterKnife.bind(this);
        setStatusBar(R.color.sys_line);
        mBookInfoPresenter = new BookInfoPresenter(this);
        mBookInfoPresenter.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == APPCONST.REQUEST_READ) {
            if (data == null) {
                return;
            }
            boolean isCollected = data.getBooleanExtra(APPCONST.RESULT_IS_COLLECTED, false);
            if (isCollected) {
                getBtnAddBookcase().setText("移除书籍");
                getBtnReadBook().setText("继续阅读");
            }
        }
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

    public ImageView getIvBookImg() {
        return ivBookImg;
    }

    public TextView getTvBookName() {
        return tvBookName;
    }

    public TextView getTvBookAuthor() {
        return tvBookAuthor;
    }

    public TextView getTvBookType() {
        return tvBookType;
    }

    public TextView getTvBookDesc() {
        return tvBookDesc;
    }

    public Button getBtnAddBookcase() {
        return btnAddBookcase;
    }

    public Button getBtnReadBook() {
        return btnReadBook;
    }

    public TextView getTvBookNewestChapter() {
        return tvBookNewestChapter;
    }

    public TextView getTvDisclaimer() {
        return tvDisclaimer;
    }

    public TextView getTvBookSource() {
        return tvBookSource;
    }

    public Button getBtnChangeSource() {
        return btnChangeSource;
    }

    public ProgressBar getPbLoading() {
        return pbLoading;
    }
}
