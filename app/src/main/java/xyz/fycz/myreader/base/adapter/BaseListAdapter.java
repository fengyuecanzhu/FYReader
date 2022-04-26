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

package xyz.fycz.myreader.base.adapter;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/8/12 20:02
 */

public abstract class BaseListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final String TAG = "BaseListAdapter";
    protected static final int TYPE_OTHER = Integer.MIN_VALUE;
    /*common statement*/
    protected List<T> mList = new ArrayList<>();
    protected SparseArray<View> otherViews = new SparseArray<>();
    protected SparseIntArray otherViewPos = new SparseIntArray();
    protected OnItemClickListener mClickListener;
    protected OnItemLongClickListener mLongClickListener;

    /************************abstract area************************/
    protected abstract IViewHolder<T> createViewHolder(int viewType);


    /*************************rewrite logic area***************************************/

    @Override
    public int getItemViewType(int position) {
        int key = otherViewPos.indexOfKey(position);
        if (key >= 0) {
            return otherViewPos.valueAt(key);
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType < TYPE_OTHER + getOtherCount()) {
            return new OtherViewHolder(otherViews.get(viewType));
        }
        IViewHolder<T> viewHolder = createViewHolder(viewType);

        View view = viewHolder.createItemView(parent);
        //初始化
        RecyclerView.ViewHolder holder = new BaseViewHolder(view, viewHolder);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OtherViewHolder) return;
        //防止别人直接使用RecyclerView.ViewHolder调用该方法
        if (!(holder instanceof BaseViewHolder))
            throw new IllegalArgumentException("The ViewHolder item must extend BaseViewHolder");

        IViewHolder<T> iHolder = ((BaseViewHolder) holder).holder;
        iHolder.onBind(holder, getItem(position), position);

        //设置点击事件
        holder.itemView.setOnClickListener((v) -> {
            if (mClickListener != null) {
                mClickListener.onItemClick(v, position);
            }
            //adapter监听点击事件
            iHolder.onClick();
            onItemClick(v, position);
        });
        //设置长点击事件
        holder.itemView.setOnLongClickListener(
                (v) -> {
                    boolean isClicked = false;
                    if (mLongClickListener != null) {
                        isClicked = mLongClickListener.onItemLongClick(v, position);
                    }
                    //Adapter监听长点击事件
                    onItemLongClick(v, position);
                    return isClicked;
                }
        );
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public int getOtherCount() {
        return otherViews.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected void onItemClick(View v, int pos) {
    }

    protected void onItemLongClick(View v, int pos) {
    }

    /******************************public area***********************************/

    public void setOnItemClickListener(OnItemClickListener mListener) {
        this.mClickListener = mListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mListener) {
        this.mLongClickListener = mListener;
    }

    public synchronized void addItem(T value) {
        int oldSize = getItemSize();
        if (mList.add(value)) {
            notifyItemInserted(oldSize);
        }
    }

    public synchronized void addItem(int index, T value) {
        mList.add(index, value);
        notifyItemInserted(index);
    }

    public synchronized void addOther(int index, View view) {
        int type = TYPE_OTHER + getOtherCount();
        otherViews.put(type, view);
        otherViewPos.put(index, type);
        notifyItemInserted(index);
    }

    public synchronized void addItems(List<T> values) {
        int oldSize = getItemSize();
        if (mList.addAll(values)) {
            if (oldSize == 0) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeInserted(oldSize, values.size());
            }
        }
    }

    public synchronized void removeItem(T value) {
        int pos = mList.indexOf(value);
        if (mList.remove(value)) {
            notifyItemRemoved(pos);
            if (pos != mList.size())
                notifyItemRangeChanged(pos, mList.size() - pos);
        }
    }

    public synchronized void removeItems(List<T> value) {
        if (mList.removeAll(value)) {
            notifyDataSetChanged();
        }
    }

    public synchronized void swapItem(int oldPos, int newPos) {
        int size = getItemSize();
        if (oldPos >= 0 && oldPos < size && newPos >= 0 && newPos < size) {
            Collections.swap(mList, oldPos, newPos);
            notifyItemMoved(oldPos, newPos);
        }
    }

    public T getItem(int position) {
        return mList.get(position);
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(mList);
    }

    public int getItemSize() {
        return mList.size();
    }

    public synchronized void refreshItems(List<T> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public synchronized void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    /***************************inner class area***********************************/
    public interface OnItemClickListener {
        void onItemClick(View view, int pos);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int pos);
    }

    static class OtherViewHolder extends RecyclerView.ViewHolder {

        public OtherViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
