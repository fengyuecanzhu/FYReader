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

package com.kongzue.dialogx.interfaces;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.kongzue.dialogx.DialogX;

import static com.kongzue.dialogx.DialogX.ERROR_INIT_TIPS;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/10/8 17:00
 */
public abstract class OnBindView<D> {
    int layoutResId;
    View customView;
    
    public OnBindView(int layoutResId) {
        if (BaseDialog.getContext() == null) {
            DialogX.error(ERROR_INIT_TIPS);
            return;
        }
        this.layoutResId = layoutResId;
        customView = LayoutInflater.from(BaseDialog.getContext()).inflate(layoutResId, new RelativeLayout(BaseDialog.getContext()), false);
    }
    
    public OnBindView(View customView) {
        this.customView = customView;
    }
    
    public abstract void onBind(D dialog, View v);
    
    public int getLayoutResId() {
        return layoutResId;
    }
    
    public OnBindView<D> setLayoutResId(int layoutResId) {
        this.layoutResId = layoutResId;
        return this;
    }
    
    public View getCustomView() {
        return customView;
    }
    
    public OnBindView<D> setCustomView(View customView) {
        this.customView = customView;
        return this;
    }
    
    public void clean() {
        layoutResId = 0;
        customView = null;
    }
    
    public OnBindView<D> bindParent(ViewGroup parentView) {
        if (customView == null) return this;
        if (customView.getParent() != null) {
            if (customView.getParent()==parentView){
                return this;
            }
            ((ViewGroup) customView.getParent()).removeView(customView);
        }
        ViewGroup.LayoutParams lp = parentView.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        parentView.addView(customView, lp);
        return this;
    }
    
    public OnBindView<D> bindParent(ViewGroup parentView, BaseDialog dialog) {
        if (customView == null) return this;
        if (customView.getParent() != null) {
            if (customView.getParent()==parentView){
                return this;
            }
            ((ViewGroup) customView.getParent()).removeView(customView);
        }
        ViewGroup.LayoutParams lp = customView.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        parentView.addView(customView, lp);
        onBind((D) dialog, customView);
        return this;
    }
}
