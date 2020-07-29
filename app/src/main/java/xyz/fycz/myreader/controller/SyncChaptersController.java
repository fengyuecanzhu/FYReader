package xyz.fycz.myreader.controller;

import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;

import java.util.ArrayList;



public class SyncChaptersController {

    private ArrayList<Chapter> mLocalChapters;//本地章节
    private ArrayList<Chapter> mNetChapters;//网络章节
    private Book mBook;//书
    private ChapterService mChapterService;

    public SyncChaptersController(ArrayList<Chapter> localChapters,ArrayList<Chapter> netChapters,Book book){
        mLocalChapters = localChapters;
        mNetChapters = netChapters;
        mBook = book;
        mChapterService = new ChapterService();
    }

    /**
     * 开始同步
     */
    public void sync(){

    }

    /**
     * 更新所有章节
     *
     * @param newChapters
     */
    private void updateAllOldChapterData(ArrayList<Chapter> newChapters) {
        int i;
        for (i = 0; i < mLocalChapters.size() && i < newChapters.size(); i++) {
            Chapter oldChapter = mLocalChapters.get(i);
            Chapter newChapter = newChapters.get(i);
            if (!oldChapter.getTitle().equals(newChapter.getTitle())) {
                oldChapter.setTitle(newChapter.getTitle());
                oldChapter.setUrl(newChapter.getUrl());
                oldChapter.setContent(null);
                mChapterService.updateEntity(oldChapter);
            }
        }
        if (mLocalChapters.size() < newChapters.size()) {
            for (int j = mLocalChapters.size(); j < newChapters.size(); j++) {

                mLocalChapters.add(newChapters.get(j));
                mChapterService.addChapter(newChapters.get(j), null);
            }
        } else if (mLocalChapters.size() > newChapters.size()) {
            for (int j = newChapters.size(); j < mLocalChapters.size(); j++) {
                mChapterService.deleteEntity(mLocalChapters.get(j));
            }
            mLocalChapters.subList(0, newChapters.size());
        }
    }

}
