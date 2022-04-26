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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;

/**
 * @author fengyue
 * @date 2021/2/10 18:25
 */
public abstract class BaseSourceAdapter extends BaseListAdapter<BookSource> {
    //记录item是否被选中的Map
    private HashMap<BookSource, Boolean> mCheckMap = new HashMap<>();
    private int mCheckedCount = 0;

    @Override
    public void refreshItems(List<BookSource> list) {
        mCheckMap.clear();
        for (BookSource bookSource : list) {
            mCheckMap.put(bookSource, false);
        }
        super.refreshItems(list);
    }

    @Override
    public void addItem(BookSource value) {
        mCheckMap.put(value, false);
        super.addItem(value);
    }

    @Override
    public void addItem(int index, BookSource value) {
        mCheckMap.put(value, false);
        super.addItem(index, value);
    }

    @Override
    public void addItems(List<BookSource> values) {
        for (BookSource bookSource : values) {
            mCheckMap.put(bookSource, false);
        }
        super.addItems(values);
    }

    @Override
    public void removeItem(BookSource value) {
        mCheckMap.remove(value);
        super.removeItem(value);
    }

    @Override
    public void removeItems(List<BookSource> value) {
        for (BookSource bookSource : value) {
            mCheckMap.remove(bookSource);
            --mCheckedCount;
        }
        super.removeItems(value);
    }

    //设置点击切换
    public void setCheckedItem(int pos) {
        BookSource bookSource = getItem(pos);
        boolean isSelected = mCheckMap.get(bookSource);
        if (isSelected) {
            mCheckMap.put(bookSource, false);
            --mCheckedCount;
        } else {
            mCheckMap.put(bookSource, true);
            ++mCheckedCount;
        }
        notifyItemChanged(pos);
    }

    public void setCheckedAll(boolean isChecked) {
        Set<Map.Entry<BookSource, Boolean>> entrys = mCheckMap.entrySet();
        mCheckedCount = 0;
        for (Map.Entry<BookSource, Boolean> entry : entrys) {
            //如果选中，则增加点击的数量
            if (isChecked) {
                ++mCheckedCount;
            }
            entry.setValue(isChecked);
        }
        notifyDataSetChanged();
    }

    public void reverseChecked() {
        Set<Map.Entry<BookSource, Boolean>> entrys = mCheckMap.entrySet();
        for (Map.Entry<BookSource, Boolean> entry : entrys) {
            //如果选中，则减少点击的数量
            if (entry.getValue()) {
                mCheckedCount--;
            }else {
                mCheckedCount++;
            }
            entry.setValue(!entry.getValue());
        }
        notifyDataSetChanged();
    }

    public boolean getItemIsChecked(int pos) {
        BookSource bookSource = getItem(pos);
        return mCheckMap.get(bookSource);
    }

    public List<BookSource> getCheckedBookSources() {
        List<BookSource> bookSources = new ArrayList<>();
        Set<Map.Entry<BookSource, Boolean>> entrys = mCheckMap.entrySet();
        for (Map.Entry<BookSource, Boolean> entry : entrys) {
            if (entry.getValue()) {
                bookSources.add(entry.getKey());
            }
        }
        return bookSources;
    }

    public int getCheckedCount() {
        return mCheckedCount;
    }

    public HashMap<BookSource, Boolean> getCheckMap() {
        return mCheckMap;
    }
}
