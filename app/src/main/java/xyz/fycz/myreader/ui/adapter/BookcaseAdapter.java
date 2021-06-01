package xyz.fycz.myreader.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.*;

import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.interfaces.OnMenuItemSelectListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.widget.CoverImageView;
import xyz.fycz.myreader.widget.custom.DragAdapter;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.ui.presenter.BookcasePresenter;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.widget.BadgeView;

/**
 * @author fengyue
 * @date 2020/4/19 11:23
 */
public abstract class BookcaseAdapter extends DragAdapter {

    private final Map<String, Boolean> isLoading = new HashMap<>();
    private final Map<String, Boolean> mCheckMap = new LinkedHashMap<>();
    private int mCheckedCount = 0;
    protected OnBookCheckedListener mListener;
    protected boolean isCheckedAll;
    protected int mResourceId;
    protected ArrayList<Book> list;
    protected Context mContext;
    protected boolean mEditState;
    protected BookService mBookService;
    protected ChapterService mChapterService;
    protected BookcasePresenter mBookcasePresenter;
    protected boolean isGroup;
    protected String[] menu = {
            App.getmContext().getResources().getString(R.string.menu_book_Top),
            App.getmContext().getResources().getString(R.string.menu_book_download),
            App.getmContext().getResources().getString(R.string.menu_book_cache),
            App.getmContext().getResources().getString(R.string.menu_book_delete)
    };


    public BookcaseAdapter(Context context, int textViewResourceId, ArrayList<Book> objects
            , boolean editState, BookcasePresenter bookcasePresenter, boolean isGroup) {
        mContext = context;
        mResourceId = textViewResourceId;
        list = objects;
        mEditState = editState;
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        mBookcasePresenter = bookcasePresenter;
        this.isGroup = isGroup;
    }


    @Override
    public void onDataModelMove(int from, int to) {
        Book b = list.remove(from);
        list.add(to, b);
        for (int i = 0; i < list.size(); i++) {
            if (!isGroup) {
                list.get(i).setSortCode(i);
            }else {
                list.get(i).setGroupSort(i);
            }
        }
        mBookService.updateBooks(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Book getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return !isGroup ? list.get(position).getSortCode() : list.get(position).getGroupSort();
    }


    public void remove(Book item) {
        list.remove(item);
        notifyDataSetChanged();
        mBookService.deleteBook(item);
    }

    public void add(Book item) {
        list.add(item);
        notifyDataSetChanged();
        mBookService.addBook(item);
    }

    protected void showDeleteBookDialog(final Book book) {
        if (!isGroup) {
            DialogCreator.createCommonDialog(mContext, "删除书籍", "确定删除《" + book.getName() + "》及其所有缓存吗？",
                    true, (dialogInterface, i) -> {
                        remove(book);
                        ToastUtils.showSuccess("书籍删除成功！");
                        mBookcasePresenter.init();
                    }, null);
        }else {
            DialogCreator.createCommonDialog(mContext, "删除/移除书籍", "您是希望删除《" + book.getName() + "》及其所有缓存还是从分组中移除该书籍(不会删除书籍)呢？",
                    true, "删除书籍", "从分组中移除",(dialogInterface, i) -> {
                        remove(book);
                        ToastUtils.showSuccess("书籍删除成功！");
                        mBookcasePresenter.init();
                    }, (dialog, which) -> {
                        book.setGroupId("");
                        mBookService.updateEntity(book);
                        ToastUtils.showSuccess("书籍已从分组中移除！");
                        mBookcasePresenter.init();
                    });
        }
    }

    /**
     * 设置是否处于编辑状态
     *
     * @param mEditState
     */
    public void setmEditState(boolean mEditState) {
        if (mEditState) {
            mCheckMap.clear();
            for (Book book : list) {
                mCheckMap.put(book.getId(), false);
            }
            mCheckedCount = 0;
        }
        this.mEditState = mEditState;
        notifyDataSetChanged();
    }

    public boolean ismEditState() {
        return mEditState;
    }

    /**
     * getter方法
     *
     * @return
     */
    public Map<String, Boolean> getIsLoading() {
        return isLoading;
    }

    public boolean isBookLoading(String bookID) {
        return isLoading.get(bookID);
    }

    public boolean unionChapterCathe(Book book) throws IOException {
        ArrayList<Chapter> chapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
        BufferedReader br = null;
        BufferedWriter bw = null;
        bw = new BufferedWriter(new FileWriter(FileUtils.getFile(APPCONST.TXT_BOOK_DIR + book.getName() + ".txt")));
        if (chapters.size() == 0) {
            return false;
        }
        File bookFile = new File(APPCONST.BOOK_CACHE_PATH + book.getId());
        if (!bookFile.exists()) {
            return false;
        }
        for (Chapter chapter : chapters) {
            if (ChapterService.isChapterCached(chapter.getBookId(), chapter.getTitle())) {
                bw.write("\t" + chapter.getTitle());
                bw.newLine();
                br = new BufferedReader(new FileReader(APPCONST.BOOK_CACHE_PATH + book.getId()
                        + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY));
                String line = null;
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                }
                br.close();
            }
        }
        bw.flush();
        bw.close();
        return true;
    }


    //设置点击切换
    public void setCheckedBook(String bookId) {
        boolean isSelected = mCheckMap.get(bookId);
        if (isSelected) {
            mCheckMap.put(bookId, false);
            --mCheckedCount;
        } else {
            mCheckMap.put(bookId, true);
            ++mCheckedCount;
        }
        notifyDataSetChanged();
    }

    //全选
    public void setCheckedAll(boolean isChecked) {
        mCheckedCount = isChecked ? mCheckMap.size() : 0;
        for (String bookId : mCheckMap.keySet()) {
            mCheckMap.put(bookId, isChecked);
        }
        mListener.onItemCheckedChange(true);
        notifyDataSetChanged();
    }

    public boolean getBookIsChecked(String bookId) {
        return mCheckMap.get(bookId);
    }

    public int getmCheckedCount() {
        return mCheckedCount;
    }

    public int getmCheckableCount() {
        return mCheckMap.size();
    }

    public boolean isCheckedAll() {
        return isCheckedAll;
    }

    public void setIsCheckedAll(boolean isCheckedAll) {
        this.isCheckedAll = isCheckedAll;
    }

    public List<Book> getSelectBooks() {
        List<Book> mSelectBooks = new ArrayList<>();
        for (String bookId : mCheckMap.keySet()) {
            if (mCheckMap.get(bookId)) {
                mSelectBooks.add(mBookService.getBookById(bookId));
            }
        }
        return mSelectBooks;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    /*******************************************缓存书籍*********************************************************/
    private int selectedIndex;//对话框选择下标

    protected void downloadBook(final Book book) {
        if (!NetworkUtils.isNetWorkAvailable()) {
            ToastUtils.showWarring("无网络连接！");
            return;
        }
        if ("本地书籍".equals(book.getType())) {
            ToastUtils.showWarring("《" + book.getName() + "》是本地书籍，不能缓存");
            return;
        }
        final int[] begin = new int[1];
        final int[] end = new int[1];
        /*MyAlertDialog.build(mContext)
                .setTitle("缓存书籍")
                .setSingleChoiceItems(mContext.getResources().getStringArray(R.array.download), selectedIndex,
                        (dialog, which) -> selectedIndex = which).setNegativeButton("取消", ((dialog, which) -> dialog.dismiss())).setPositiveButton("确定",
                (dialog, which) -> {
                    switch (selectedIndex) {
                        case 0:
                            begin[0] = book.getHisttoryChapterNum();
                            end[0] = book.getHisttoryChapterNum() + 50;
                            break;
                        case 1:
                            begin[0] = book.getHisttoryChapterNum() - 50;
                            end[0] = book.getHisttoryChapterNum() + 50;
                            break;
                        case 2:
                            begin[0] = book.getHisttoryChapterNum();
                            end[0] = 99999;
                            break;
                        case 3:
                            begin[0] = 0;
                            end[0] = 99999;
                            break;
                    }
                    Thread downloadThread = new Thread(() -> {
                        ArrayList<Chapter> chapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
                        mBookcasePresenter.addDownload(book, chapters, begin[0], end[0], false);
                    });
                    mBookcasePresenter.getEs().submit(downloadThread);
                }).show();*/
        BottomMenu.show("缓存书籍", mContext.getResources().getStringArray(R.array.download))
                .setSelection(selectedIndex)
                .setOnMenuItemClickListener(new OnMenuItemSelectListener<BottomMenu>() {
                    @Override
                    public void onOneItemSelect(BottomMenu dialog, CharSequence text, int which) {
                        selectedIndex = which;
                    }
                }).setOkButton("确定", (baseDialog, v) -> {
                    switch (selectedIndex) {
                        case 0:
                            begin[0] = book.getHisttoryChapterNum();
                            end[0] = book.getHisttoryChapterNum() + 50;
                            break;
                        case 1:
                            begin[0] = book.getHisttoryChapterNum() - 50;
                            end[0] = book.getHisttoryChapterNum() + 50;
                            break;
                        case 2:
                            begin[0] = book.getHisttoryChapterNum();
                            end[0] = 99999;
                            break;
                        case 3:
                            begin[0] = 0;
                            end[0] = 99999;
                            break;
                    }
                    Thread downloadThread = new Thread(() -> {
                        ArrayList<Chapter> chapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
                        mBookcasePresenter.addDownload(book, chapters, begin[0], end[0], false);
                    });
                    mBookcasePresenter.getEs().submit(downloadThread);
                    return false;
                }).setCancelButton(R.string.cancel);
    }

    static class ViewHolder {
        CheckBox cbBookChecked;
        CoverImageView ivBookImg;
        TextView tvBookName;
        BadgeView tvNoReadNum;
        ProgressBar pbLoading;
    }

    public void setOnBookCheckedListener(OnBookCheckedListener listener) {
        mListener = listener;
    }

    //书籍点击监听器
    public interface OnBookCheckedListener {
        void onItemCheckedChange(boolean isChecked);
    }
}
