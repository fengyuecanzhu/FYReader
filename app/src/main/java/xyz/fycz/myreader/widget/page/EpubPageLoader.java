/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.widget.page;

import android.text.TextUtils;
import android.util.Log;

import net.sf.jazzlib.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

public class EpubPageLoader extends PageLoader {

    //编码类型
    private Charset mCharset;

    private Book epubBook;

    EpubPageLoader(PageView pageView, xyz.fycz.myreader.greendao.entity.Book bookShelfBean, Setting setting) {
        super(pageView, bookShelfBean, setting);
        mStatus = STATUS_PARING;
    }

    @Override
    public void refreshChapterList() {
        if (mCollBook == null) return;

        Observable.create((ObservableOnSubscribe<xyz.fycz.myreader.greendao.entity.Book>) e -> {
            File bookFile = new File(mCollBook.getChapterUrl());
            if (!bookFile.exists())
                e.onError(new FileNotFoundException("书籍源文件不存在\n" + mCollBook.getChapterUrl()));
            epubBook = readBook(bookFile);

            if (epubBook == null) {
                e.onError(new Exception("文件解析失败"));
                return;
            }
            if (TextUtils.isEmpty(mCollBook.getInfoUrl())) {
                mCollBook.setInfoUrl("UTF-8");
            }
            mCharset = Charset.forName(mCollBook.getInfoUrl());

            if (TextUtils.isEmpty(mCollBook.getImgUrl())) {
                saveCover();
            }
            e.onNext(mCollBook);
            e.onComplete();
        }).subscribeOn(Schedulers.single())
                .flatMap(this::checkChapterList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<xyz.fycz.myreader.greendao.entity.Book>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mChapterDis = d;
                    }

                    @Override
                    public void onNext(xyz.fycz.myreader.greendao.entity.Book bookShelfBean) {
                        isChapterListPrepare = true;
                        //提示目录加载完成
                        if (mPageChangeListener != null) {
                            mPageChangeListener.onCategoryFinish(mChapterList);
                        }
                        // 加载并显示当前章节
                        openChapter();
                    }

                    @Override
                    public void onError(Throwable e) {
                        error(STATUS_PARSE_ERROR, e.getMessage());
                    }
                });
    }


    public static Book readBook(File file) {
        try {
            EpubReader epubReader = new EpubReader();
            MediaType[] lazyTypes = {
                    MediatypeService.CSS,
                    MediatypeService.GIF,
                    MediatypeService.JPG,
                    MediatypeService.PNG,
                    MediatypeService.MP3,
                    MediatypeService.MP4};
            ZipFile zipFile = new ZipFile(file);
            return epubReader.readEpubLazy(zipFile, "utf-8", Arrays.asList(lazyTypes));
        } catch (Exception e) {
            return null;
        }
    }

    private void saveCover() throws IOException {
        byte[] data = epubBook.getCoverImage().getData();
        FileUtils.writeFile(data, APPCONST.COVER_DIR, mCollBook.getId() + ".jpg");
        mCollBook.setImgUrl(APPCONST.COVER_DIR + mCollBook.getId() + ".jpg");
    }

    /*private void extractScaledCoverImage() {
        try {
            byte[] data = epubBook.getCoverImage().getData();
            Bitmap rawCover = BitmapFactory.decodeByteArray(data, 0, data.length);
            int width = rawCover.getWidth();
            int height = rawCover.getHeight();
            if (width <= mVisibleWidth && width >= 0.8 * mVisibleWidth) {
                cover = rawCover;
                return;
            }
            height = Math.round(mVisibleWidth * 1.0f * height / width);
            cover = Bitmap.createScaledBitmap(rawCover, mVisibleWidth, height, true);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void drawCover(Canvas canvas, float top) {
        if (cover == null) {
            extractScaledCoverImage();
        }
        if (cover == null)
            return;
        int left = (mDisplayWidth - cover.getWidth()) / 2;
        canvas.drawBitmap(cover, left, top, mTextPaint);
    }*/

    private List<Chapter> loadChapters() {
        Metadata metadata = epubBook.getMetadata();
        if (!TextUtils.isEmpty(metadata.getFirstTitle())) {
            mCollBook.setName(metadata.getFirstTitle());
        }
        if (metadata.getAuthors().size() > 0) {
            String author = metadata.getAuthors().get(0).toString().replaceAll("^, |, $", "");
            mCollBook.setAuthor(author);
        }
        if (metadata.getDescriptions().size() > 0) {
            mCollBook.setDesc(Jsoup.parse(metadata.getDescriptions().get(0)).text());
        }
        mChapterList = new ArrayList<>();
        List<TOCReference> refs = epubBook.getTableOfContents().getTocReferences();
        if (refs == null || refs.isEmpty()) {
            List<SpineReference> spineReferences = epubBook.getSpine().getSpineReferences();
            for (int i = 0, size = spineReferences.size(); i < size; i++) {
                Resource resource = spineReferences.get(i).getResource();
                String title = resource.getTitle();
                if (TextUtils.isEmpty(title)) {
                    try {
                        Document doc = Jsoup.parse(new String(resource.getData(), mCharset));
                        Elements elements = doc.getElementsByTag("title");
                        if (elements.size() > 0) {
                            title = elements.get(0).text();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Chapter bean = new Chapter();
                bean.setNumber(i);
                bean.setBookId(mCollBook.getId());
                bean.setUrl(resource.getHref());
                if (i == 0 && title.isEmpty()) {
                    bean.setTitle("封面");
                } else {
                    bean.setTitle(title);
                }
                bean.setEnd(1);
                mChapterList.add(bean);
            }
        } else {
            parseMenu(refs, 0);
            for (int i = 0; i < mChapterList.size(); i++) {
                mChapterList.get(i).setNumber(i);
                mChapterList.get(i).setEnd(1);
            }
        }

        return mChapterList;
    }

    private void parseMenu(List<TOCReference> refs, int level) {
        if (refs == null) return;
        for (TOCReference ref : refs) {
            if (ref.getResource() != null) {
                Chapter chapter = new Chapter();
                chapter.setBookId(mCollBook.getId());
                chapter.setTitle(ref.getTitle());
                chapter.setUrl(ref.getCompleteHref());
                chapter.setId(StringHelper.getStringRandom(15));
                mChapterList.add(chapter);
            }
            if (ref.getChildren() != null && !ref.getChildren().isEmpty()) {
                parseMenu(ref.getChildren(), level + 1);
            }
        }
    }

    protected String getChapterContent(Chapter chapter) throws Exception {
        Resource resource = epubBook.getResources().getByHref(chapter.getUrl());
        StringBuilder content = new StringBuilder();
        Document doc = Jsoup.parse(new String(resource.getData(), mCharset));
        Elements elements = doc.getAllElements();
        for (Element element : elements) {
            List<TextNode> contentEs = element.textNodes();
            for (int i = 0; i < contentEs.size(); i++) {
                String text = contentEs.get(i).text().trim();
                text = StringUtils.formatHtml(text);
                if (elements.size() > 1) {
                    if (text.length() > 0) {
                        if (content.length() > 0) {
                            content.append("\r\n");
                        }
                        content.append("\u3000\u3000").append(text);
                    }
                } else {
                    content.append(text);
                }
            }
        }
        return content.toString();
    }

    private Observable<xyz.fycz.myreader.greendao.entity.Book> checkChapterList(xyz.fycz.myreader.greendao.entity.Book collBook) {
        mChapterList = ChapterService.getInstance().findBookAllChapterByBookId(mCollBook.getId());
        if (!mChapterList.isEmpty()) {
            return Observable.just(collBook);
        } else {
            return Observable.create((ObservableOnSubscribe<List<Chapter>>) e -> {
                List<Chapter> chapterList = loadChapters();
                if (!chapterList.isEmpty()) {
                    e.onNext(chapterList);
                } else {
                    e.onError(new IllegalAccessException("epubBook sub-chapter failed!"));
                }
                e.onComplete();
            })
                    .flatMap(chapterList -> {
                        mPageChangeListener.onCategoryFinish(chapterList);
                        return Observable.just(collBook);
                    })
                    .doOnNext(bookShelfBean -> {
                        // 存储章节到数据库
                        ChapterService.getInstance().addChapters(mChapterList);
                    });
        }
    }

    @Override
    public String getChapterReader(Chapter chapter) throws Exception {
        /*byte[] content = getChapterContent(chapter).getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        BufferedReader br = new BufferedReader(new InputStreamReader(bais));
        return br;*/
        return getChapterContent(chapter);
    }

    @Override
    public boolean hasChapterData(Chapter chapter) {
        return true;
    }

    /*public void updateChapter() {
        mPageView.getActivity().toast("目录更新中");
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            if (TextUtils.isEmpty(book.getBookInfoBean().getCharset())) {
                book.getBookInfoBean().setCharset("UTF-8");
            }
            mCharset = Charset.forName(book.getBookInfoBean().getCharset());
            //清除原目录
            BookshelfHelp.delChapterList(book.getNoteUrl());
            callback.getChapterList().clear();
            e.onNext(book);
            e.onComplete();
        }).flatMap(this::checkChapterList)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        mPageView.getActivity().toast("更新完成");
                        isChapterListPrepare = true;
                        // 加载并显示当前章节
                        skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        durDhapterError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }*/
}
