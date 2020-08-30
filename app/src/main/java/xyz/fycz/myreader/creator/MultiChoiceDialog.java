package xyz.fycz.myreader.creator;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;

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
        AlertDialog multiChoiceDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                    onMultiDialogListener.onItemClick(dialog, which, isChecked);
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
                onMultiDialogListener.onSelectAll(false);
            } else {
                this.checkedCount = itemsCount;
                selectAll.setText("取消全选");
                for (int i = 0; i < itemsCount; i++) {
                    checkedItems[i] = true;
                    itemList.setItemChecked(i, true);
                }
                onMultiDialogListener.onSelectAll(true);
            }
        });

        return multiChoiceDialog;
    }
}
