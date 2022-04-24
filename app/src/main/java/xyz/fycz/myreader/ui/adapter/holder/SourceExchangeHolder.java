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

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.dialog.SourceExchangeDialog;

/**
 * @author fengyue
 * @date 2020/9/30 18:43
 */
public class SourceExchangeHolder extends ViewHolderImpl<Book> {
    TextView sourceTvTitle;
    TextView sourceTvChapter;
    ImageView sourceIv;
    private SourceExchangeDialog dialog;

    public SourceExchangeHolder(SourceExchangeDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_change_source;
    }

    @Override
    public void initView() {
        sourceTvTitle = findById(R.id.tv_source_name);
        sourceTvChapter = findById(R.id.tv_lastChapter);
        sourceIv = findById(R.id.iv_checked);
    }

    @Override
    public void onBind(RecyclerView.ViewHolder holder, Book data, int pos) {
        sourceTvTitle.setText(BookSourceManager.getSourceNameByStr(data.getSource()));
        sourceTvChapter.setText(data.getNewestChapterTitle());
        if ((data.getInfoUrl() != null && data.getInfoUrl().equals(dialog.getmShelfBook().getInfoUrl())||
                data.getChapterUrl() != null && data.getChapterUrl().equals(dialog.getmShelfBook().getChapterUrl()))&&
                (data.getSource() != null && data.getSource().equals(dialog.getmShelfBook().getSource()))) {
            sourceIv.setVisibility(View.VISIBLE);
            dialog.setSourceIndex(pos);
        } else {
            sourceIv.setVisibility(View.GONE);
        }
    }
}
