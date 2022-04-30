/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.adapter.holder;

import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;

/**
 * @author fengyue
 * @date 2020/8/17 15:07
 */
public class CatalogHolder extends ViewHolderImpl<Chapter> {
    private TextView tvTitle;
    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_chapter_title_item;
    }

    @Override
    public void initView() {
        tvTitle = findById(R.id.tv_chapter_title);
    }

    @Override
    public void onBind(RecyclerView.ViewHolder holder, Chapter data, int pos) {
        if (ChapterService.isChapterCached(data) || data.getEnd() > 0) {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.drawable.selector_category_load), null, null, null);
        } else {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.drawable.selector_category_unload), null, null, null);
        }
        tvTitle.setText(data.getTitle());
    }
}
