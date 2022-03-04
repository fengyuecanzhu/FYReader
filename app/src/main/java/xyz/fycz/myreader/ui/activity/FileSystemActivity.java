package xyz.fycz.myreader.ui.activity;


import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseTabActivity;
import xyz.fycz.myreader.databinding.ActivityFileSystemBinding;
import xyz.fycz.myreader.enums.LocalBookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.fragment.BaseFileFragment;
import xyz.fycz.myreader.ui.fragment.FileCategoryFragment;
import xyz.fycz.myreader.ui.fragment.LocalBookFragment;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.FileUtils;


/**
 * @author fengyue
 * @date 2020/8/12 20:02
 */

public class FileSystemActivity extends BaseTabActivity<ActivityFileSystemBinding> {
    private static final String TAG = "FileSystemActivity";

    private LocalBookFragment mLocalFragment;
    private FileCategoryFragment mCategoryFragment;
    private BaseFileFragment mCurFragment;

    private BaseFileFragment.OnFileCheckedListener mListener = new BaseFileFragment.OnFileCheckedListener() {
        @Override
        public void onItemCheckedChange(boolean isChecked) {
            changeMenuStatus();
        }

        @Override
        public void onCategoryChanged() {
            //状态归零
            mCurFragment.setCheckedAll(false);
            //改变菜单
            changeMenuStatus();
            //改变是否能够全选
            changeCheckedAllStatus();
        }
    };

    @Override
    protected void bindView() {
        binding = ActivityFileSystemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        super.bindView();
    }

    @Override
    protected List<Fragment> createTabFragments() {
        mLocalFragment = new LocalBookFragment();
        mCategoryFragment = new FileCategoryFragment();
        return Arrays.asList(mLocalFragment, mCategoryFragment);
    }

    @Override
    protected List<String> createTabTitles() {
        return Arrays.asList("智能导入", "手机目录");
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("本地导入");
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.fileSystemCbSelectedAll.setOnClickListener(
                (view) -> {
                    //设置全选状态
                    boolean isChecked = binding.fileSystemCbSelectedAll.isChecked();
                    mCurFragment.setCheckedAll(isChecked);
                    //改变菜单状态
                    changeMenuStatus();
                }
        );

        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mCurFragment = mLocalFragment;
                } else {
                    mCurFragment = mCategoryFragment;
                }
                //改变菜单状态
                changeMenuStatus();
                //改变是否能够全选
                changeCheckedAllStatus();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.fileSystemBtnAddBook.setOnClickListener(
                (v) -> {
                    //获取选中的文件
                    List<File> files = mCurFragment.getCheckedFiles();
                    //转换成Book,并存储
                    List<Book> books = convertBook(files);
                    BookService.getInstance()
                            .addBooks(books);
                    //设置HashMap为false
                    mCurFragment.setCheckedAll(false);
                    //改变菜单状态
                    changeMenuStatus();
                    //改变是否可以全选
                    changeCheckedAllStatus();
                    //提示加入书架成功
                    ToastUtils.showSuccess(getResources().getString(R.string.file_add_succeed, books.size()));

                }
        );

        binding.fileSystemBtnDelete.setOnClickListener(
                (v) -> {
                    //弹出，确定删除文件吗。
                    DialogCreator.createCommonDialog(this, "删除文件", "确定删除文件吗?",
                            true, (dialog, which) -> {
                                //删除选中的文件
                                mCurFragment.deleteCheckedFiles();
                                //改变菜单状态
                                changeMenuStatus();
                                //改变是否可以全选
                                changeCheckedAllStatus();
                                //提示删除文件成功
                                ToastUtils.showSuccess("删除文件成功");
                            }, null);
                }
        );

        mLocalFragment.setOnFileCheckedListener(mListener);
        mCategoryFragment.setOnFileCheckedListener(mListener);
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        mCurFragment = mLocalFragment;
    }


    @Override
    public void onBackPressed() {
        if (mCurFragment == mCategoryFragment) {
            if (mCategoryFragment.backLast()) return;
        }
        super.onBackPressed();
    }

    /**
     * 将文件转换成CollBook
     *
     * @param files:需要加载的文件列表
     * @return
     */
    private List<Book> convertBook(List<File> files) {
        List<Book> books = new ArrayList<>(files.size());
        for (File file : files) {
            //判断文件是否存在
            if (!file.exists()) continue;

            Book book = new Book();
            book.setName(file.getName().replace(FileUtils.SUFFIX_EPUB, "").replace(FileUtils.SUFFIX_TXT, ""));
            book.setChapterUrl(file.getAbsolutePath());
            book.setType("本地书籍");
            book.setHistoryChapterId("未开始阅读");
            book.setNewestChapterTitle("未拆分章节");
            book.setAuthor("本地书籍");
            book.setSource(LocalBookSource.local.toString());
            book.setDesc("无");
            book.setIsCloseUpdate(true);
            books.add(book);
        }
        return books;
    }

    /**
     * 改变底部选择栏的状态
     */
    private void changeMenuStatus() {

        //点击、删除状态的设置
        if (mCurFragment.getCheckedCount() == 0) {
            binding.fileSystemBtnAddBook.setText(getString(R.string.file_add_shelf));
            //设置某些按钮的是否可点击
            setMenuClickable(false);

            if (binding.fileSystemCbSelectedAll.isChecked()) {
                mCurFragment.setChecked(false);
                binding.fileSystemCbSelectedAll.setChecked(mCurFragment.isCheckedAll());
            }

        } else {
            binding.fileSystemBtnAddBook.setText(getString(R.string.file_add_shelves, mCurFragment.getCheckedCount()));
            setMenuClickable(true);

            //全选状态的设置

            //如果选中的全部的数据，则判断为全选
            if (mCurFragment.getCheckedCount() == mCurFragment.getCheckableCount()) {
                //设置为全选
                mCurFragment.setChecked(true);
                binding.fileSystemCbSelectedAll.setChecked(mCurFragment.isCheckedAll());
            }
            //如果曾今是全选则替换
            else if (mCurFragment.isCheckedAll()) {
                mCurFragment.setChecked(false);
                binding.fileSystemCbSelectedAll.setChecked(mCurFragment.isCheckedAll());
            }
        }

        //重置全选的文字
        if (mCurFragment.isCheckedAll()) {
            binding.fileSystemCbSelectedAll.setText("取消");
        } else {
            binding.fileSystemCbSelectedAll.setText("全选");
        }

    }

    private void setMenuClickable(boolean isClickable) {

        //设置是否可删除
        binding.fileSystemBtnDelete.setEnabled(isClickable);
        binding.fileSystemBtnDelete.setClickable(isClickable);

        //设置是否可添加书籍
        binding.fileSystemBtnAddBook.setEnabled(isClickable);
        binding.fileSystemBtnAddBook.setClickable(isClickable);
    }

    /**
     * 改变全选按钮的状态
     */
    private void changeCheckedAllStatus() {
        //获取可选择的文件数量
        int count = mCurFragment.getCheckableCount();

        //设置是否能够全选
        if (count > 0) {
            binding.fileSystemCbSelectedAll.setClickable(true);
            binding.fileSystemCbSelectedAll.setEnabled(true);
        } else {
            binding.fileSystemCbSelectedAll.setClickable(false);
            binding.fileSystemCbSelectedAll.setEnabled(false);
        }
    }
}
