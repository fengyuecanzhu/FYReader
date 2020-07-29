package xyz.fycz.myreader.widget.page;


import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.crawler.ReadCrawler;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.webapi.CommonApi;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by newbiechen on 17-5-29.
 * 网络页面加载器
 */

public class NetPageLoader extends PageLoader {
    private static final String TAG = "PageFactory";
    private ChapterService mChapterService;
    private ReadCrawler mReadCrawler;

    public NetPageLoader(PageView pageView, Book collBook, ChapterService mChapterService,
                         ReadCrawler mReadCrawler, Setting setting) {
        super(pageView, collBook, setting);
        this.mChapterService = mChapterService;
        this.mReadCrawler = mReadCrawler;
    }

    /*private List<TxtChapter> convertTxtChapter(List<Chapter> bookChapters) {
        List<TxtChapter> txtChapters = new ArrayList<>(bookChapters.size());
        for (Chapter bean : bookChapters) {
            TxtChapter chapter = new TxtChapter();
            chapter.setBookId(bean.getBookId());
            chapter.setTitle(bean.getTitle());
            chapter.setLink(bean.getUrl());
            txtChapters.add(chapter);
        }
        return txtChapters;
    }*/

    @Override
    public void refreshChapterList() {
        List<Chapter> chapters = mChapterService.findBookAllChapterByBookId(mCollBook.getId());
        if (chapters == null) return;

        // 将 BookChapter 转换成当前可用的 Chapter
//        mChapterList = convertTxtChapter(chapters);
        mChapterList = chapters;
        isChapterListPrepare = true;

        // 目录加载完成，执行回调操作。
        if (mPageChangeListener != null) {
            mPageChangeListener.onCategoryFinish(mChapterList);
        }

        // 如果章节未打开
        if (!isChapterOpen()) {
            // 打开章节
            openChapter();
        }
    }

    @Override
    protected BufferedReader getChapterReader(Chapter chapter) throws FileNotFoundException {
        File file = new File(APPCONST.BOOK_CACHE_PATH + mCollBook.getId()
                + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY);
        if (!file.exists()) return null;
        BufferedReader br = new BufferedReader(new FileReader(file));
        /*try {
            if (StringHelper.isEmpty(br.readLine())){
                getChapterContent(chapter);
                assert chapter.getContent() != null;
                //保证chapter更新完毕,暂时先这么用着
                for (int i = 0; i < 5; i++) {
                    if (!StringHelper.isEmpty(chapter.getContent())){
                        break;
                    }
                    Thread.sleep(1000);
                }
                return br;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }*/
        return br;
    }

    @Override
    protected boolean hasChapterData(Chapter chapter) {
        return ChapterService.isChapterCached(mCollBook.getId(), chapter.getTitle());
    }

    // 装载上一章节的内容
    @Override
    boolean parsePrevChapter() {
        boolean isRight = super.parsePrevChapter();

        if (mStatus == STATUS_FINISH) {
            loadPrevChapter();
        } else if (mStatus == STATUS_LOADING) {
            loadCurrentChapter();
        }
        return isRight;
    }

    // 装载当前章内容。
    @Override
    boolean parseCurChapter() {
        boolean isRight = super.parseCurChapter();

        if (mStatus == STATUS_LOADING) {
            loadCurrentChapter();
        }
        return isRight;
    }

    // 装载下一章节的内容
    @Override
    boolean parseNextChapter() {
        boolean isRight = super.parseNextChapter();

        if (mStatus == STATUS_FINISH) {
            loadNextChapter();
        } else if (mStatus == STATUS_LOADING) {
            loadCurrentChapter();
        }

        return isRight;
    }

    /**
     * 加载当前页的前面两个章节
     */
    private void loadPrevChapter() {
        if (mPageChangeListener != null) {
            int end = mCurChapterPos;
            int begin = end - 2;
            if (begin < 0) {
                begin = 0;
            }
            requestChapters(begin, end);
        }
    }

    /**
     * 加载前一页，当前页，后一页。
     */
    private void loadCurrentChapter() {
        if (mPageChangeListener != null) {
            int begin = mCurChapterPos;
            int end = mCurChapterPos;

            // 是否当前不是最后一章
            if (end < mChapterList.size()) {
                end = end + 1;
                if (end >= mChapterList.size()) {
                    end = mChapterList.size() - 1;
                }
            }

            // 如果当前不是第一章
            if (begin != 0) {
                begin = begin - 1;
                if (begin < 0) {
                    begin = 0;
                }
            }
            requestChapters(begin, end);
        }
    }

    /**
     * 加载当前页的后一个章节
     */
    private void loadNextChapter() {
        if (mPageChangeListener != null) {

            // 提示加载后两章
            int begin = mCurChapterPos + 1;
            int end = begin + 1;

            // 判断是否大于最后一章
            if (begin >= mChapterList.size()) {
                // 如果下一章超出目录了，就没有必要加载了
                return;
            }

            if (end > mChapterList.size()) {
                end = mChapterList.size() - 1;
            }
            requestChapters(begin, end);
        }
    }

    private void requestChapters(int start, int end) {
        // 检验输入值
        if (start < 0) {
            start = 0;
        }

        if (end >= mChapterList.size()) {
            end = mChapterList.size() - 1;
        }


        List<Chapter> chapters = new ArrayList<>();

        // 过滤，哪些数据已经加载了
        for (int i = start; i <= end; ++i) {
            Chapter txtChapter = mChapterList.get(i);
            if (!hasChapterData(txtChapter)) {
                chapters.add(txtChapter);
            }
        }

        if (!chapters.isEmpty()) {
            mPageChangeListener.requestChapters(chapters);
        }
    }

    /**
     * 加载章节内容
     *
     * @param chapter
     */
    public void getChapterContent(final Chapter chapter) {
        Chapter cacheChapter = mChapterService.findChapterByBookIdAndTitle(chapter.getBookId(), chapter.getTitle());
        if (cacheChapter != null && hasChapterData(chapter)) {
            chapter.setContent(APPCONST.BOOK_CACHE_PATH + chapter.getBookId()
                    + File.separator + chapter.getTitle() + FileUtils.SUFFIX_FY);
            chapter.setId(cacheChapter.getId());
            mChapterService.saveOrUpdateChapter(chapter, null);
        } else {
            CommonApi.getChapterContent(chapter.getUrl(), mReadCrawler, new ResultCallback() {
                @Override
                public void onFinish(final Object o, int code) {
//                    chapter.setContent((String) o);
                    mChapterService.saveOrUpdateChapter(chapter, (String) o);
                }
                @Override
                public void onError(Exception e) {

                }

            });
        }

    }
}

