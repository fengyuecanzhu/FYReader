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

import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.ui.activity.FindBookActivity;

/**
 * @author fengyue
 * @date 2021/7/22 22:27
 */
public class FindSourceHolder extends ViewHolderImpl<BookSource> {

    private TextView tvName;

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_find_source;
    }

    @Override
    public void initView() {
        tvName = findById(R.id.tv_name);
    }

    @Override
    public void onBind(RecyclerView.ViewHolder holder, BookSource data, int pos) {
        tvName.setText(data.getSourceName());
    }
}
