package xyz.fycz.myreader.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.custom.DragAdapter;
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
    protected int mResourceId;
    protected ArrayList<Book> list;
    protected Context mContext;
    protected boolean mEditState;
    protected BookService mBookService;
    protected ChapterService mChapterService;
    protected BookcasePresenter mBookcasePresenter;
    protected String[] menu = {
            MyApplication.getmContext().getResources().getString(R.string.menu_book_Top),
            MyApplication.getmContext().getResources().getString(R.string.menu_book_download),
            MyApplication.getmContext().getResources().getString(R.string.menu_book_cache),
            MyApplication.getmContext().getResources().getString(R.string.menu_book_delete)
    };


    public BookcaseAdapter(Context context, int textViewResourceId, ArrayList<Book> objects
            , boolean editState, BookcasePresenter bookcasePresenter) {
        mContext = context;
        mResourceId = textViewResourceId;
        list = objects;
        mEditState = editState;
        mBookService = BookService.getInstance();
        mChapterService = ChapterService.getInstance();
        mBookcasePresenter = bookcasePresenter;
    }


    @Override
    public void onDataModelMove(int from, int to) {
        Book b = list.remove(from);
        list.add(to, b);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSortCode(i);
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
        return list.get(position).getSortCode();
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
    protected void showDeleteBookDialog(final Book book){
        DialogCreator.createCommonDialog(mContext, "删除书籍", "确定删除《" + book.getName() + "》及其所有缓存吗？",
                true, (dialogInterface, i) -> {
                    remove(book);
                    dialogInterface.dismiss();
                }, (dialogInterface, i) -> dialogInterface.dismiss());
    }

    /**
     * 设置是否处于编辑状态
     * @param mEditState
     */
    public void setmEditState(boolean mEditState) {
        this.mEditState = mEditState;
        notifyDataSetChanged();
    }

    public boolean ismEditState() {
        return mEditState;
    }
    /**
     * getter方法
     * @return
     */
    public Map<String, Boolean> getIsLoading() {
        return isLoading;
    }

    public boolean isBookLoading(String bookID){
        return isLoading.get(bookID);
    }

    public boolean unionChapterCathe(Book book) throws IOException {
        ArrayList<Chapter> chapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(book.getId());
        BufferedReader br = null;
        BufferedWriter bw = null;
        bw = new BufferedWriter(new FileWriter(FileUtils.getFile(APPCONST.TXT_BOOK_DIR + book.getName() + ".txt")));
        if (chapters.size() == 0){
            return false;
        }
        File bookFile = new File(APPCONST.BOOK_CACHE_PATH + book.getId());
        if (!bookFile.exists()){
            return false;
        }
        for (Chapter chapter : chapters){
            if(ChapterService.isChapterCached(chapter.getBookId(), chapter.getTitle())){
                bw.write("\t" + chapter.getTitle());
                bw.newLine();
                br = new BufferedReader(new FileReader(APPCONST.BOOK_CACHE_PATH + book.getId()
                        + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY));
                String line = null;
                while ((line = br.readLine()) != null){
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

    /*******************************************缓存书籍*********************************************************/
    private int selectedIndex;//对话框选择下标

    protected void downloadBook(final Book book) {
        if (!NetworkUtils.isNetWorkAvailable()) {
            ToastUtils.showWarring("无网络连接！");
            return;
        }
        if ("本地书籍".equals(book.getType())){
            ToastUtils.showWarring("《" + book.getName() + "》是本地书籍，不能缓存");
            return;
        }
        final int[] begin = new int[1];
        final int[] end = new int[1];
        new AlertDialog.Builder(mContext)
                .setTitle("缓存书籍")
                .setSingleChoiceItems(APPCONST.DIALOG_DOWNLOAD, selectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedIndex = which;
                    }
                }).setNegativeButton("取消", ((dialog, which) -> dialog.dismiss())).setPositiveButton("确定",
                (dialog, which) -> {
                    switch (selectedIndex) {
                        case 0:
                            begin[0] =  book.getHisttoryChapterNum();
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
                }).show();
    }

    static class ViewHolder {
        ImageView ivBookImg;
        TextView tvBookName;
        BadgeView tvNoReadNum;
        ImageView ivDelete;
        ProgressBar pbLoading;
    }
}
