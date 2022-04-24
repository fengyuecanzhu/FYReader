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
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;

import com.kongzue.dialogx.dialogs.BottomMenu;

/**
 * @author fengyue
 * @date 2020/8/29 21:08
 */
public class MultiChoiceDialog {
    ListView itemList = null;
    Button selectAll = null;
    int checkedCount;

    public AlertDialog create(Context context, String title, CharSequence[] items,
                              boolean[] checkedItems, int checkedCount,
                              DialogInterface.OnClickListener positiveListener,
                              DialogInterface.OnClickListener negativeListener,
                              DialogCreator.OnMultiDialogListener onMultiDialogListener) {
        this.checkedCount = checkedCount;
        int itemsCount = checkedItems.length;
        AlertDialog multiChoiceDialog = MyAlertDialog.build(context)
                .setTitle(title)
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                    if (onMultiDialogListener != null) {
                        onMultiDialogListener.onItemClick(dialog, which, isChecked);
                    }
                    if(isChecked){
                        this.checkedCount++;
                    }else {
                        this.checkedCount--;
                    }
                    if (this.checkedCount == itemsCount) {
                        selectAll.setText("取消全选");
                    } else {
                        selectAll.setText("全选");
                    }
                })
                .setPositiveButton("确定", positiveListener)
                .setNegativeButton("取消", negativeListener)
                .setNeutralButton("全选", null).create();

        multiChoiceDialog.show();
        itemList = multiChoiceDialog.getListView();
        selectAll = multiChoiceDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if (this.checkedCount == itemsCount) {
            selectAll.setText("取消全选");
        } else {
            selectAll.setText("全选");
        }
        selectAll.setOnClickListener(v1 -> {
            if (this.checkedCount == itemsCount) {
                selectAll.setText("全选");
                this.checkedCount = 0;
                for (int i = 0; i < itemsCount; i++) {
                    checkedItems[i] = false;
                    itemList.setItemChecked(i, false);
                }
                if (onMultiDialogListener != null) {
                    onMultiDialogListener.onSelectAll(false);
                }
            } else {
                this.checkedCount = itemsCount;
                selectAll.setText("取消全选");
                for (int i = 0; i < itemsCount; i++) {
                    checkedItems[i] = true;
                    itemList.setItemChecked(i, true);
                }
                if (onMultiDialogListener != null) {
                    onMultiDialogListener.onSelectAll(true);
                }
            }
        });

        return multiChoiceDialog;
    }
}
