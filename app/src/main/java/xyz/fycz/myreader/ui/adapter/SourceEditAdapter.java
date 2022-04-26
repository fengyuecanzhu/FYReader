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

package xyz.fycz.myreader.ui.adapter;

import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.entity.sourceedit.EditEntity;
import xyz.fycz.myreader.ui.adapter.holder.SourceEditHolder;

/**
 * @author fengyue
 * @date 2021/2/9 10:08
 */
public class SourceEditAdapter extends BaseListAdapter<EditEntity> {
    @Override
    protected IViewHolder<EditEntity> createViewHolder(int viewType) {
        return new SourceEditHolder();
    }
}
