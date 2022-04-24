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

package xyz.fycz.myreader.ui.adapter.holder;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.zhy.view.flowlayout.TagFlowLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.adapter.BookTagAdapter;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.BookApi;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.widget.CoverImageView;

/**
 * @author fengyue
 * @date 2021/7/22 9:34
 */
public class FindBookHolder extends ViewHolderImpl<Book> {
    private List<String> tagList = new ArrayList<>();

    private CoverImageView ivBookImg;
    private TextView tvBookName;
    private TagFlowLayout tflBookTag;
    private TextView tvDesc;
    private TextView tvAuthor;
    private TextView tvSource;
    private TextView tvNewestChapter;

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_search_book;
    }

    @Override
    public void initView() {
        ivBookImg = findById(R.id.iv_book_img);
        tvBookName = findById(R.id.tv_book_name);
        tflBookTag = findById(R.id.tfl_book_tag);
        tvAuthor = findById(R.id.tv_book_author);
        tvDesc = findById(R.id.tv_book_desc);
        tvSource = findById(R.id.tv_book_source);
        tvNewestChapter = findById(R.id.tv_book_newest_chapter);
        getItemView().setBackgroundColor(getContext().getResources().getColor(R.color.colorForeground));
    }

    @Override
    public void onBind(RecyclerView.ViewHolder holder, Book data, int pos) {
        BookSource source = BookSourceManager.getBookSourceByStr(data.getSource());
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(source);
        if (StringHelper.isEmpty(data.getImgUrl())) {
            data.setImgUrl("");
        }
        if (!App.isDestroy((Activity) getContext())) {
            ivBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), data.getImgUrl()), data.getName(), data.getAuthor());
        }
        tvBookName.setText(data.getName());
        if (!StringHelper.isEmpty(data.getAuthor())) {
            tvAuthor.setText(data.getAuthor());
        }else {
            tvAuthor.setText("");
        }
        initTagList(data);
        if (!StringHelper.isEmpty(data.getNewestChapterTitle())) {
            tvNewestChapter.setText(getContext().getString(R.string.newest_chapter, data.getNewestChapterTitle()));
        } else {
            data.setNewestChapterTitle("");
            tvNewestChapter.setText("");
        }
        if (!StringHelper.isEmpty(data.getDesc())) {
            tvDesc.setText(String.format("简介:%s", data.getDesc()));
        } else {
            data.setDesc("");
            tvDesc.setText("");
        }
        if (!StringHelper.isEmpty(source.getSourceName()) && !"未知书源".equals(source.getSourceName()))
            tvSource.setText(String.format("书源:%s", source.getSourceName()));
        if (needGetInfo(data) && rc instanceof BookInfoCrawler) {
            Log.i(data.getName(), "initOtherInfo");
            BookInfoCrawler bic = (BookInfoCrawler) rc;
            BookApi.getBookInfo(data, bic).compose(RxUtils::toSimpleSingle)
                    .subscribe(new MyObserver<Book>() {
                        @Override
                        public void onNext(@NotNull Book book) {
                            initOtherInfo(book, rc);
                        }
                    });
        }
        setAnimation(holder.itemView, pos);
    }

    private void initOtherInfo(Book book, ReadCrawler rc) {
        //简介
        if (StringHelper.isEmpty(tvDesc.getText().toString())) {
            tvDesc.setText(String.format("简介:%s", book.getDesc()));
        }
        if (StringHelper.isEmpty(tvNewestChapter.getText().toString())) {
            tvNewestChapter.setText(getContext().getString(R.string.newest_chapter, book.getNewestChapterTitle()));
        }
        if (!StringHelper.isEmpty(book.getAuthor())) {
            tvAuthor.setText(book.getAuthor());
        }
        //图片
        if (!App.isDestroy((Activity) getContext())) {
            ivBookImg.load(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), book.getImgUrl()), book.getName(), book.getAuthor());
        }
    }

    private void initTagList(Book data) {
        tagList.clear();
        String type = data.getType();
        if (!StringHelper.isEmpty(type))
            tagList.add(type);
        String wordCount = data.getWordCount();
        if (!StringHelper.isEmpty(wordCount))
            tagList.add(wordCount);
        String status = data.getStatus();
        if (!StringHelper.isEmpty(status))
            tagList.add(status);
        if (tagList.size() == 0) {
            tflBookTag.setVisibility(View.GONE);
        } else {
            tflBookTag.setVisibility(View.VISIBLE);
            tflBookTag.setAdapter(new BookTagAdapter(getContext(), tagList, 11));
        }
    }
    private boolean needGetInfo(Book bookBean) {
        if (StringHelper.isEmpty(bookBean.getAuthor())) return true;
        if (StringHelper.isEmpty(bookBean.getType())) return true;
        if (StringHelper.isEmpty(bookBean.getDesc())) return true;
        if (StringHelper.isEmpty(bookBean.getNewestChapterTitle())) return true;
        return StringHelper.isEmpty(bookBean.getImgUrl());
    }
    /**
     * Here is the key method to apply the animation
     */
    protected void setAnimation(View viewToAnimate, int position){
        // If the bound view wasn't previously displayed on screen, it's animated
        Animation animation =
                AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.anim_recycle_item);
        viewToAnimate.startAnimation(animation);
    }
}
