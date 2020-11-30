package xyz.fycz.myreader.widget.page2;

import android.annotation.SuppressLint;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.util.utils.FileUtils;

/**
 * 网络页面加载器
 */

public class PageLoaderNet extends PageLoader {
    private static final String TAG = "PageLoaderNet";
    private List<String> downloadingChapterList = new ArrayList<>();

    PageLoaderNet(PageView pageView, Book bookShelfBean, Callback callback) {
        super(pageView, bookShelfBean, callback);
    }

    @Override
    public void refreshChapterList() {
        if (!callback.getChapterList().isEmpty()) {
            isChapterListPrepare = true;
            // 打开章节
            skipToChapter(book.getHisttoryChapterNum(), book.getLastReadPosition());
        } else {
            /*WebBookModel.getInstance().getChapterList(book)
                    .compose(RxUtils::toSimpleSingle)
                    .subscribe(new MyObserver<List<BookChapterBean>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(List<BookChapterBean> chapterBeanList) {
                            isChapterListPrepare = true;
                            // 目录加载完成
                            if (!chapterBeanList.isEmpty()) {
                                BookshelfHelp.delChapterList(book.getNoteUrl());
                                callback.onCategoryFinish(chapterBeanList);
                            }
                            // 加载并显示当前章节
                            skipToChapter(book.getDurChapter(), book.getDurChapterPage());
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (e instanceof WebBook.NoSourceThrowable) {
                                mPageView.autoChangeSource();
                            } else {
                                durDhapterError(e.getMessage());
                            }
                        }
                    });*/
        }
    }


    public void changeSourceFinish(Book bookShelfBean) {
        if (bookShelfBean == null) {
            openChapter(book.getHisttoryChapterNum());
        } else {
            this.book = bookShelfBean;
            refreshChapterList();
        }
    }

    /*@SuppressLint("DefaultLocale")
    private synchronized void loadContent(final int chapterIndex) {
        if (downloadingChapterList.size() >= 20) return;
        if (chapterIndex >= callback.getChapterList().size()
                || DownloadingList(listHandle.CHECK, callback.getChapterList().get(chapterIndex).getUrl()))
            return;
        if (null != book && callback.getChapterList().size() > 0) {
            Observable.create((ObservableOnSubscribe<Integer>) e -> {
                if (shouldRequestChapter(chapterIndex)) {
                    DownloadingList(listHandle.ADD, callback.getChapterList().get(chapterIndex).getUrl());
                    e.onNext(chapterIndex);
                }
                e.onComplete();
            })
                    .flatMap(index -> WebBookModel.getInstance().getBookContent(book, callback.getChapterList().get(chapterIndex), null))
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<BookContentBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onNext(BookContentBean bookContentBean) {
                            DownloadingList(listHandle.REMOVE, bookContentBean.getDurChapterUrl());
                            finishContent(bookContentBean.getDurChapterIndex());
                        }

                        @Override
                        public void onError(Throwable e) {
                            DownloadingList(listHandle.REMOVE, callback.getChapterList().get(chapterIndex).getDurChapterUrl());
                            if (chapterIndex == book.getDurChapter()) {
                                if (e instanceof WebBook.NoSourceThrowable) {
                                    mPageView.autoChangeSource();
                                } else if (e instanceof VipThrowable) {
                                    callback.vipPop();
                                } else {
                                    durDhapterError(e.getMessage());
                                }
                            }
                        }
                    });
        }
    }*/

    /**
     * 编辑下载列表
     */
    private synchronized boolean DownloadingList(listHandle editType, String value) {
        if (editType == listHandle.ADD) {
            downloadingChapterList.add(value);
            return true;
        } else if (editType == listHandle.REMOVE) {
            downloadingChapterList.remove(value);
            return true;
        } else {
            return downloadingChapterList.indexOf(value) != -1;
        }
    }

    /**
     * 章节下载完成
     */
    private void finishContent(int chapterIndex) {
        if (chapterIndex == mCurChapterPos) {
            super.parseCurChapter();
        }
        if (chapterIndex == mCurChapterPos - 1) {
            super.parsePrevChapter();
        }
        if (chapterIndex == mCurChapterPos + 1) {
            super.parseNextChapter();
        }
    }

    /**
     * 刷新当前章节
     */
    @SuppressLint("DefaultLocale")
    /*public void refreshDurChapter() {
        if (callback.getChapterList().isEmpty()) {
            updateChapter();
            return;
        }
        if (callback.getChapterList().size() - 1 < mCurChapterPos) {
            mCurChapterPos = callback.getChapterList().size() - 1;
        }
        BookshelfHelp.delChapter(BookshelfHelp.getCachePathName(book.getBookInfoBean().getName(), book.getTag()),
                mCurChapterPos, callback.getChapterList().get(mCurChapterPos).getDurChapterName());
        skipToChapter(mCurChapterPos, 0);
    }*/

    @Override
    protected String getChapterContent(Chapter chapter) {
        File file = new File(APPCONST.BOOK_CACHE_PATH + book.getId()
                + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY);
        if (!file.exists()) return null;
        byte[] contentByte = FileUtils.getBytes(file);
        return new String(contentByte, StandardCharsets.UTF_8);
        //return BookshelfHelp.getChapterCache(book, chapter);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected boolean noChapterData(Chapter chapter) {
        //return !BookshelfHelp.isChapterCached(book.getBookInfoBean().getName(), book.getTag(), chapter, book.isAudio());
        return !ChapterService.isChapterCached(book.getId(), chapter.getTitle());
    }

    /*private boolean shouldRequestChapter(Integer chapterIndex) {
        return NetworkUtils.isNetWorkAvailable() && noChapterData(callback.getChapterList().get(chapterIndex));
    }*/

    // 装载上一章节的内容
    @Override
    void parsePrevChapter() {
        /*if (mCurChapterPos >= 1) {
            loadContent(mCurChapterPos - 1);
        }*/
        super.parsePrevChapter();
    }

    // 装载当前章内容。
    @Override
    void parseCurChapter() {
        /*for (int i = mCurChapterPos; i < Math.min(mCurChapterPos + 5, book.getChapterTotalNum()); i++) {
            loadContent(i);
        }*/
        super.parseCurChapter();
    }

    // 装载下一章节的内容
    @Override
    void parseNextChapter() {
        /*for (int i = mCurChapterPos; i < Math.min(mCurChapterPos + 5, book.getChapterTotalNum()); i++) {
            loadContent(i);
        }*/
        super.parseNextChapter();
    }

    @Override
    public void updateChapter() {
        /*mPageView.getActivity().toast("目录更新中");
        WebBookModel.getInstance().getChapterList(book)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<BookChapterBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<BookChapterBean> chapterBeanList) {
                        isChapterListPrepare = true;

                        if (chapterBeanList.size() > callback.getChapterList().size()) {
                            mPageView.getActivity().toast("更新完成,有新章节");
                            callback.onCategoryFinish(chapterBeanList);
                        } else {
                            mPageView.getActivity().toast("更新完成,无新章节");
                        }

                        // 加载并显示当前章节
                        skipToChapter(book.getDurChapter(), book.getDurChapterPage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        durDhapterError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });*/
    }

    @Override
    public void closeBook() {
        super.closeBook();
    }

    public enum listHandle {
        ADD, REMOVE, CHECK
    }
}
