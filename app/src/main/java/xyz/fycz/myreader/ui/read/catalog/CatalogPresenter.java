package xyz.fycz.myreader.ui.read.catalog;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author fengyue
 * @date 2020/7/22 9:14
 */
public class CatalogPresenter implements BasePresenter {
    private CatalogFragment mCatalogFragment;
    private ChapterService mChapterService;
    private ArrayList<Chapter> mChapters = new ArrayList<>();
    private ArrayList<Chapter> mConvertChapters = new ArrayList<>();
    private int curSortflag = 0; //0正序  1倒序
    private boolean isDayStyle;
    private ChapterTitleAdapter mChapterTitleAdapter;
    private Book mBook;

    public CatalogPresenter(CatalogFragment mCatalogFragment) {
        this.mCatalogFragment = mCatalogFragment;
        mChapterService = ChapterService.getInstance();
    }

    @Override
    public void start() {
        mBook = (Book) mCatalogFragment.getActivity().getIntent().getSerializableExtra(APPCONST.BOOK);
        isDayStyle = SysManager.getSetting().isDayStyle();
        if (!isDayStyle) {
            mCatalogFragment.getLvChapterList().setBackground(mCatalogFragment.getActivity().getDrawable(R.color.sys_dialog_setting_bg));
        }
        mCatalogFragment.getFcChangeSort().setOnClickListener(view -> {
            if (curSortflag == 0) {//当前正序
                curSortflag = 1;
                changeChapterSort();
            } else {//当前倒序
                curSortflag = 0;
                changeChapterSort();
            }
        });
        initChapterTitleList();
        mCatalogFragment.getLvChapterList().setOnItemClickListener((adapterView, view, i, l) -> {
            Chapter chapter = mChapterTitleAdapter.getItem(i);
            final int position;
            assert chapter != null;
            if (chapter.getNumber() == 0) {
                if (curSortflag == 0) {
                    position = i;
                } else {
                    position = mChapters.size() - 1 - i;
                }
            } else {
                position = chapter.getNumber();
            }
            Intent intent = new Intent();
            intent.putExtra(APPCONST.CHAPTER_PAGE, new int[]{position, 0});
            mCatalogFragment.getActivity().setResult(Activity.RESULT_OK, intent);
            mCatalogFragment.getActivity().finish();
        });
    }


    /**
     * 初始化章节目录
     */
    private void initChapterTitleList() {
        mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(mBook.getId());
        //初始化倒序章节
        mConvertChapters.addAll(mChapters);
        Collections.reverse(mConvertChapters);
        //设置布局管理器
        int curChapterPosition;
        curChapterPosition = mBook.getHisttoryChapterNum();
        mChapterTitleAdapter = new ChapterTitleAdapter(mCatalogFragment.getContext(), R.layout.listview_chapter_title_item, mChapters, mBook);
        mCatalogFragment.getLvChapterList().setAdapter(mChapterTitleAdapter);
        mCatalogFragment.getLvChapterList().setSelection(curChapterPosition);
    }

    /**
     * 改变章节列表排序（正倒序）
     */
    private void changeChapterSort() {
        if (curSortflag == 0) {
            mChapterTitleAdapter.clear();
            mChapterTitleAdapter.addAll(mChapterTitleAdapter.getmList());
        } else {
            mChapterTitleAdapter.clear();
            mConvertChapters.clear();
            mConvertChapters.addAll(mChapterTitleAdapter.getmList());
            Collections.reverse(mConvertChapters);
            mChapterTitleAdapter.addAll(mConvertChapters);
        }
        mChapterTitleAdapter.notifyDataSetChanged();
        mCatalogFragment.getLvChapterList().setAdapter(mChapterTitleAdapter);
    }

    /**
     * 搜索过滤
     *
     * @param query
     */
    public void startSearch(String query) {
        mChapterTitleAdapter.getFilter().filter(query);
        mCatalogFragment.getLvChapterList().setSelection(0);
    }
}
