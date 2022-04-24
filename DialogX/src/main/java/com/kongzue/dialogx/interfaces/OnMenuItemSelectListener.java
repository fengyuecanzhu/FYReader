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

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2021/4/11 19:18
 */
public abstract class OnMenuItemSelectListener<D> implements OnMenuItemClickListener<D> {
    
    /**
     * 警告：不建议重写此方法！
     * 如果选择使用 OnMenuItemSelectListener 作为 BottomMenu 的回调，那么点击 Item 后，菜单默认不应该关闭，
     * 若选择自行处理菜单点击 onClick，那么请务必 return true 作为返回值，
     * 否则不会处理 onOneItemSelect 或 onMultiItemSelect 事件。
     *
     * @param dialog BottomMenu实例
     * @param text   菜单文本
     * @param index  菜单索引值
     * @return return true：拦截自动关闭对话框；return false：点击后关闭对话框
     */
    @Deprecated
    @Override
    public boolean onClick(D dialog, CharSequence text, int index) {
        return true;
    }
    
    public void onOneItemSelect(D dialog, CharSequence text, int which) {
    
    }
    
    public void onMultiItemSelect(D dialog, CharSequence[] text, int[] indexArray) {
    
    }
    
}
