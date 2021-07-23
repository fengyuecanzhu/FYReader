package xyz.fycz.myreader.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.activity.CatalogActivity;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.ui.adapter.ChapterTitleAdapter;
import xyz.fycz.myreader.ui.fragment.CatalogFragment;
import xyz.fycz.myreader.webapi.BookApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/7/22 9:14
 */
public class CatalogPresenter implements BasePresenter {
    private static final String TAG = CatalogPresenter.class.getSimpleName();
    private CatalogFragment mCatalogFragment;
    private ChapterService mChapterService;
    private List<Chapter> mChapters = new ArrayList<>();
    private List<Chapter> mConvertChapters = new ArrayList<>();
    private int curSortflag = 0; //0正序  1倒序
    private ChapterTitleAdapter mChapterTitleAdapter;
    private Book mBook;

    public CatalogPresenter(CatalogFragment mCatalogFragment) {
        this.mCatalogFragment = mCatalogFragment;
        mChapterService = ChapterService.getInstance();
    }

    @Override
    public void start() {
        mBook = ((CatalogActivity) mCatalogFragment.getActivity()).getmBook();
        mCatalogFragment.getFcChangeSort().setOnClickListener(view -> {
            if (curSortflag == 0) {//当前正序
                curSortflag = 1;
            } else {//当前倒序
                curSortflag = 0;
            }
            if (mChapterTitleAdapter != null) {
                changeChapterSort();
            }
        });
        mChapters = mChapterService.findBookAllChapterByBookId(mBook.getId());
        if (mChapters.size() != 0) {
            initChapterTitleList();
        } else {
            if ("本地书籍".equals(mBook.getType())) {
                ToastUtils.showWarring("本地书籍请先拆分章节！");
                return;
            }
            mCatalogFragment.getPbLoading().setVisibility(View.VISIBLE);
            BookApi.getBookChapters(mBook, ReadCrawlerUtil.getReadCrawler(mBook.getSource()))
                    .compose(RxUtils::toSimpleSingle).subscribe(new MyObserver<List<Chapter>>() {
                @Override
                public void onNext(@NotNull List<Chapter> chapters) {
                    mChapters = chapters;
                    mCatalogFragment.getPbLoading().setVisibility(View.GONE);
                    initChapterTitleList();
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    ToastUtils.showError("章节目录加载失败！\n" + e.getLocalizedMessage());
                    mCatalogFragment.getPbLoading().setVisibility(View.GONE);
                    if (App.isDebug()) e.printStackTrace();
                }
            });
        }
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
            /*LLog.i(TAG, "position = " + position);
            LLog.i(TAG, "mChapters.size() = " + mChapters.size());*/
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
        if (mChapters.size() == 0) return;
        mChapterTitleAdapter.getFilter().filter(query);
        mCatalogFragment.getLvChapterList().setSelection(0);
    }
}
