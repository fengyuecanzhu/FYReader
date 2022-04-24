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

package com.kongzue.dialogx.util.views;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.annotation.RequiresApi;

import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.interfaces.BottomMenuListViewTouchEvent;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/10/6 23:42
 */
public class BottomDialogListView extends ListView {
    
    private BottomMenuListViewTouchEvent bottomMenuListViewTouchEvent;
    
    public BottomDialogListView(Context context) {
        super(context);
    }
    
    public BottomDialogListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public BottomDialogListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    private BottomDialog.DialogImpl dialogImpl;
    
    public BottomDialogListView(BottomDialog.DialogImpl dialog, Context context) {
        super(context);
        dialogImpl = dialog;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(dip2px(55)*size+size, MeasureSpec.EXACTLY));
    
        //super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(expandSpec, MeasureSpec.AT_MOST));
    }
    
    private int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    private int mPosition;
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int actionMasked = ev.getActionMasked() & MotionEvent.ACTION_MASK;
        
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            if (bottomMenuListViewTouchEvent != null) {
                bottomMenuListViewTouchEvent.down(ev);
            }
            /*mPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
            return super.dispatchTouchEvent(ev);*/
        }
        
        if (actionMasked == MotionEvent.ACTION_MOVE) {
            if (bottomMenuListViewTouchEvent != null) {
                bottomMenuListViewTouchEvent.move(ev);
            }
            /*setPressed(pointToPosition((int) ev.getX(), (int) ev.getY()) == mPosition);
            invalidate();
            return true;*/
        }
        if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
            if (bottomMenuListViewTouchEvent != null) {
                bottomMenuListViewTouchEvent.up(ev);
            }
            /*if (pointToPosition((int) ev.getX(), (int) ev.getY()) != mPosition) {
                return true;
            }*/
        }
        
        return super.dispatchTouchEvent(ev);
    }
    
    public BottomMenuListViewTouchEvent getBottomMenuListViewTouchEvent() {
        return bottomMenuListViewTouchEvent;
    }
    
    private int size =1;
    
    @Override
    public void setAdapter(ListAdapter adapter) {
        size = adapter.getCount();
        super.setAdapter(adapter);
    }
    
    public BottomDialogListView setBottomMenuListViewTouchEvent(BottomMenuListViewTouchEvent bottomMenuListViewTouchEvent) {
        this.bottomMenuListViewTouchEvent = bottomMenuListViewTouchEvent;
        return this;
    }
}
