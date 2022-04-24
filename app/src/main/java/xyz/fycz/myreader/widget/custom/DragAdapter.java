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

package xyz.fycz.myreader.widget.custom;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by zhao on 2017/5/15.
 */

public abstract class DragAdapter extends BaseAdapter {

    /**
     *
     * @描述:当从from排序被拖到to排序时的处理方式,请对相应的数据做处理。
     *
     * @param from
     * @param to
     * @作者 [pWX273343] 2015年6月24日
     */
    public abstract void onDataModelMove(int from, int to);

    /**
     * 复制View使用的方法,默认直接使用getView方法获取
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View copyView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    /**
     * 是否启用copyView方法
     * @return true 使用copyView复制 false 使用getView直接获取镜像
     */
    public boolean isUseCopyView() {
        return false;
    }
}
