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

package xyz.fycz.myreader.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.kongzue.dialogx.dialogs.BottomMenu;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.BookGroup;
import xyz.fycz.myreader.greendao.service.BookGroupService;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * @author fengyue
 * @date 2021/1/8 23:58
 */
public class BookGroupDialog {
    private ArrayList<BookGroup> mBookGroups = new ArrayList<>();//书籍分组
    private CharSequence[] mGroupNames;//书籍分组名称
    private final BookGroupService mBookGroupService;
    private Context mContext;

    public BookGroupDialog(Context context) {
        this.mBookGroupService = BookGroupService.getInstance();
        mContext = context;
    }

    public ArrayList<BookGroup> getmBookGroups() {
        return mBookGroups;
    }

    public CharSequence[] getmGroupNames() {
        return mGroupNames;
    }

    public int getGroupSize() {
        return mBookGroups.size();
    }

    //初始化书籍分组
    public void initBookGroups(boolean isAdd) {
        mBookGroups.clear();
        mBookGroups.addAll(mBookGroupService.getAllGroups());
        boolean openPrivate = SharedPreUtils.getInstance().getBoolean("openPrivate");
        if (openPrivate) {
            String privateGroupId = SharedPreUtils.getInstance().getString("privateGroupId");
            mBookGroups.remove(BookGroupService.getInstance().getGroupById(privateGroupId));
        }
        mGroupNames = new CharSequence[isAdd ? mBookGroups.size() + 1 : mBookGroups.size()];
        for (int i = 0; i < mBookGroups.size(); i++) {
            BookGroup bookGroup = mBookGroups.get(i);
            String groupName = bookGroup.getName();
//            mGroupNames[i] = groupName.getBytes().length > 20 ? groupName.substring(0, 8) + "···" : groupName;
            mGroupNames[i] = groupName;
        }
        if (isAdd) {
            mGroupNames[mBookGroups.size()] = "+ 新建分组";
        }
    }


    /**
     * 加入分组
     *
     * @param book
     */
    public void addGroup(Book book, OnGroup onGroup) {
        List<Book> books = new ArrayList<>();
        books.add(book);
        addGroup(books, onGroup);
    }

    /**
     * 加入批量分组
     *
     * @param mSelectBooks
     * @param onGroup
     */
    public void addGroup(List<Book> mSelectBooks, OnGroup onGroup) {
        initBookGroups(true);
        showSelectGroupDia((dialog, which) -> {
            if (which < mBookGroups.size()) {
                BookGroup bookGroup = mBookGroups.get(which);
                for (Book book : mSelectBooks) {
                    if (!bookGroup.getId().equals(book.getGroupId())) {
                        book.setGroupId(bookGroup.getId());
                        book.setGroupSort(0);
                    }
                }
                BookService.getInstance().updateBooks(mSelectBooks);
                ToastUtils.showSuccess("成功将《" + mSelectBooks.get(0).getName() + "》"
                        + (mSelectBooks.size() > 1 ? "等" : "")
                        + "加入[" + bookGroup.getName() + "]分组");
                if (onGroup != null) onGroup.change();
            } else if (which == mBookGroups.size()) {
                showAddOrRenameGroupDia(false, true, 0, onGroup);
            }
        });
    }

    /**
     * 添加/重命名分组对话框
     * @param isRename
     * @param isAddGroup 是否在将书籍添加分组对话框中调用
     * @param groupNum
     * @param onGroup
     */
    public void showAddOrRenameGroupDia(boolean isRename, boolean isAddGroup, int groupNum, OnGroup onGroup) {
        BookGroup bookGroup = !isRename ? new BookGroup() : mBookGroups.get(groupNum);
        String oldName = bookGroup.getName();
        int maxLen = 20;
        MyAlertDialog.showFullInputDia(mContext, !isRename ? "新建分组" : "重命名分组", "请输入分组名",
                isRename ? oldName : "", null, true, maxLen, newGroupName -> {
                    for (CharSequence oldGroupName : mGroupNames) {
                        if (oldGroupName.equals(newGroupName)) {
                            ToastUtils.showWarring("分组[" + newGroupName + "]已存在，无法" + (!isRename ? "添加！" : "重命名！"));
                            return;
                        }
                    }
                    bookGroup.setName(newGroupName);
                    if (!isRename) {
                        mBookGroupService.addBookGroup(bookGroup);
                    } else {
                        mBookGroupService.updateEntity(bookGroup);
                        if (SharedPreUtils.getInstance().getString(mContext.getString(R.string.curBookGroupName), "").equals(oldName)) {
                            SharedPreUtils.getInstance().putString(mContext.getString(R.string.curBookGroupName), newGroupName);
                            if (onGroup != null) onGroup.change();
                        }
                    }
                    ToastUtils.showSuccess("成功" +
                            (!isRename ? "添加分组[" : "成功将[" + oldName + "]重命名为[")
                            + bookGroup.getName() + "]");
                    initBookGroups(false);
                    if (isAddGroup) {
                        if (onGroup != null) onGroup.addGroup(bookGroup);
                    }
                });
    }

    /**
     * 删除分组对话框
     */
    public void showDeleteGroupDia(OnGroup onGroup) {
        boolean[] checkedItems = new boolean[mGroupNames.length];
        new MultiChoiceDialog().create(mContext, "删除分组", mGroupNames
                , checkedItems, 0, (dialog, which) -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            mBookGroupService.deleteEntity(mBookGroups.get(i));
                            sb.append(mBookGroups.get(i).getName()).append("、");
                        }
                    }
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.lastIndexOf("、"));
                    }
                    if (mBookGroupService.getGroupById(SharedPreUtils.getInstance().getString(mContext.getString(R.string.curBookGroupId), "")) == null) {
                        SharedPreUtils.getInstance().putString(mContext.getString(R.string.curBookGroupId), "");
                        SharedPreUtils.getInstance().putString(mContext.getString(R.string.curBookGroupName), "");
                        onGroup.change();
                    }
                    ToastUtils.showSuccess("分组[" + sb.toString() + "]删除成功！");
                }, null, null);
    }

    //显示选择书籍对话框
    public void showSelectGroupDia(DialogInterface.OnClickListener onClickListener) {
        /*MyAlertDialog.build(mContext)
                .setTitle("选择分组")
                .setItems(mGroupNames, onClickListener)
                .setCancelable(false)
                .setPositiveButton("取消", null)
                .show();*/
        BottomMenu.show("选择分组", mGroupNames)
                .setOnMenuItemClickListener((dialog, text, which) -> {
                    onClickListener.onClick(null, which);
                    return false;
                }).setCancelButton(R.string.cancel);
    }

    public abstract static class OnGroup {
        //当前正使用分组名称改变回调
        public abstract void change();

        //回调
        public void addGroup(BookGroup group) {

        }
    }
}
