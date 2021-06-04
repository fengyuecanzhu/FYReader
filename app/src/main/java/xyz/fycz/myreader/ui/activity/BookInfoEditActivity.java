package xyz.fycz.myreader.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivityBookInfoEditBinding;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.UriFileUtil;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;

/**
 * @author fengyue
 * @date 2021/4/24 15:05
 */
public class BookInfoEditActivity extends BaseActivity {
    private ActivityBookInfoEditBinding binding;
    private Book mBook;
    private String imgUrl;
    private String bookName;
    private String author;
    private String desc;

    @Override
    protected void bindView() {
        binding = ActivityBookInfoEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("书籍信息编辑");
    }

    @Override
    protected boolean initSwipeBackEnable() {
        return false;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mBook = (Book) BitIntentDataManager.getInstance().getData(getIntent());
        if (mBook == null) {
            ToastUtils.showError("未读取到书籍信息");
            finish();
        }
        imgUrl = NetworkUtils.getAbsoluteURL(ReadCrawlerUtil.getReadCrawler(mBook.getSource()).getNameSpace(), mBook.getImgUrl());
        bookName = mBook.getName();
        author = mBook.getAuthor();
        desc = mBook.getDesc();
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        initImg(imgUrl);
        binding.tieBookName.setText(bookName);
        binding.tieBookAuthor.setText(author);
        binding.tieCoverUrl.setText(imgUrl);
        binding.tieBookDesc.setText(desc);
    }

    @Override
    protected void initClick() {
        binding.btSelectLocalPic.setOnClickListener(v -> {
            ToastUtils.showInfo("请选择一张图片");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, APPCONST.REQUEST_SELECT_COVER);
        });
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_info_edit, menu);
        return true;
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveInfo();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APPCONST.REQUEST_SELECT_COVER) {
            if (resultCode == RESULT_OK && null != data) {
                String imgUrl = UriFileUtil.getPath(this, data.getData());
                binding.tieCoverUrl.setText(imgUrl);
                initImg(imgUrl);
            }
        }
    }

    @Override
    public void finish() {
        if (hasChange()) {
            DialogCreator.createThreeButtonDialog(this, "退出"
                    , "当前书籍信息已更改，是否保存？", true,
                    "直接退出", "取消", "保存并退出",
                    (dialog, which) -> super.finish(), null,
                    (dialog, which) -> {
                        saveInfo();
                        super.finish();
                    }
            );
        } else {
            super.finish();
        }
    }

    private void initImg(String imgUrl) {
        if (!App.isDestroy(this)) {
            binding.ivCover.load(imgUrl, mBook.getName(), mBook.getAuthor());
        }
    }

    private void saveInfo() {
        bookName = binding.tieBookName.getText().toString();
        author = binding.tieBookAuthor.getText().toString();
        imgUrl = binding.tieCoverUrl.getText().toString();
        desc = binding.tieBookDesc.getText().toString();
        mBook.setName(bookName);
        mBook.setAuthor(author);
        mBook.setImgUrl(imgUrl);
        mBook.setDesc(desc);
        BookService.getInstance().updateEntity(mBook);
        ToastUtils.showSuccess("书籍信息保存成功");
        setResult(Activity.RESULT_OK);
    }

    private boolean hasChange() {
        return !Objects.equals(bookName, binding.tieBookName.getText().toString()) ||
                !Objects.equals(author, binding.tieBookAuthor.getText().toString()) ||
                !Objects.equals(imgUrl, binding.tieCoverUrl.getText().toString()) ||
                !Objects.equals(desc, binding.tieBookDesc.getText().toString());
    }
}
