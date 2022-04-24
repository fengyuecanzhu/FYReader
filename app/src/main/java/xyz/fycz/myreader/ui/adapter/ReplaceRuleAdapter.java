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

package xyz.fycz.myreader.ui.adapter;

import android.widget.Filter;
import android.widget.Filterable;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.greendao.entity.ReplaceRuleBean;
import xyz.fycz.myreader.ui.adapter.holder.ReplaceRuleHolder;
import xyz.fycz.myreader.util.help.StringHelper;

/**
 * @author fengyue
 * @date 2021/1/19 9:51
 */
public class ReplaceRuleAdapter extends BaseListAdapter<ReplaceRuleBean> {
    private AppCompatActivity activity;
    private List<ReplaceRuleBean> beans;
    private OnSwipeListener onSwipeListener;

    public ReplaceRuleAdapter(AppCompatActivity activity, List<ReplaceRuleBean> beans, OnSwipeListener onSwipeListener) {
        this.activity = activity;
        this.beans = beans;
        this.onSwipeListener = onSwipeListener;
    }

    @Override
    protected IViewHolder<ReplaceRuleBean> createViewHolder(int viewType) {
        return new ReplaceRuleHolder(activity, onSwipeListener);
    }

    public void removeItem(int pos) {
        mList.remove(pos);
        notifyItemRemoved(pos);
        if (pos != mList.size())
            notifyItemRangeChanged(pos, mList.size() - pos);
    }

    public void toTop(int which, ReplaceRuleBean bean) {
        mList.remove(bean);
        notifyItemInserted(0);
        mList.add(0, bean);
        notifyItemRemoved(which);
        notifyItemRangeChanged(0, which + 1);
    }

    public void setBeans(List<ReplaceRuleBean> beans) {
        this.beans = beans;
    }

    /**
     * 过滤器，实现搜索
     *
     * @return
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                List<ReplaceRuleBean> mFilterList = new ArrayList<>();
                if (charString.isEmpty()) {
                    //没有过滤的内容，则使用源数据
                    mFilterList = beans;
                } else {
                    for (ReplaceRuleBean bean : beans) {
                        //这里根据需求，添加匹配规则
                        if (StringHelper.isEmpty(bean.getReplaceSummary())) {
                            if (bean.getRegex().contains(charString) ||
                                    bean.getReplacement().contains(charString)) {
                                mFilterList.add(bean);
                            }
                        } else {
                            if (bean.getReplaceSummary().contains(charString)) {
                                mFilterList.add(bean);
                            }
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilterList;
                return filterResults;
            }

            //把过滤后的值返回出来
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                refreshItems((List<ReplaceRuleBean>) results.values);
            }
        };
    }

    public interface OnSwipeListener {
        void onDel(int which, ReplaceRuleBean bean);

        void onTop(int which, ReplaceRuleBean bean);
    }
}
