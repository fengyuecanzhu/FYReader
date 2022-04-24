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

package xyz.fycz.myreader.ui.fragment;



import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.ui.adapter.FileSystemAdapter;

import java.io.File;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/8/12 20:02
 * FileSystemActivity的基础Fragment类
 */

public abstract class BaseFileFragment extends BaseFragment {

    protected FileSystemAdapter mAdapter;
    protected OnFileCheckedListener mListener;
    protected boolean isCheckedAll;

    //设置当前列表为全选
    public void setCheckedAll(boolean checkedAll){
        if (mAdapter == null) return;

        isCheckedAll = checkedAll;
        mAdapter.setCheckedAll(checkedAll);
    }

    public void setChecked(boolean checked){
        isCheckedAll = checked;
    }

    //当前fragment是否全选
    public boolean isCheckedAll() {
        return isCheckedAll;
    }

    //获取被选中的数量
    public int getCheckedCount(){
        if (mAdapter == null) return 0;
        return mAdapter.getCheckedCount();
    }

    //获取被选中的文件列表
    public List<File> getCheckedFiles(){
        return mAdapter != null ? mAdapter.getCheckedFiles() : null;
    }

    //获取文件的总数
    public int getFileCount(){
        return mAdapter != null ? mAdapter.getItemCount() : null;
    }

    //获取可点击的文件的数量
    public int getCheckableCount(){
        if (mAdapter == null) return 0;
        return mAdapter.getCheckableCount();
    }

    /**
     * 删除选中的文件
     */
    public void deleteCheckedFiles(){
        //删除选中的文件
        List<File> files = getCheckedFiles();
        //删除显示的文件列表
        mAdapter.removeItems(files);
        //删除选中的文件
        for (File file : files){
            if (file.exists()){
                file.delete();
            }
        }
    }

    //设置文件点击监听事件
    public void setOnFileCheckedListener(OnFileCheckedListener listener){
        mListener = listener;
    }

    //文件点击监听
    public interface OnFileCheckedListener {
        void onItemCheckedChange(boolean isChecked);
        void onCategoryChanged();
    }
}
