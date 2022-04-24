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

import com.kongzue.dialogx.dialogs.BottomMenu;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/10/9 14:54
 */
public abstract class OnIconChangeCallBack {
    
    private boolean autoTintIconInLightOrDarkMode;
    
    public OnIconChangeCallBack() {
    }
    
    public OnIconChangeCallBack(boolean autoTintIconInLightOrDarkMode) {
        this.autoTintIconInLightOrDarkMode = autoTintIconInLightOrDarkMode;
    }
    
    public abstract int getIcon(BottomMenu bottomMenu, int index, String menuText);
    
    public boolean isAutoTintIconInLightOrDarkMode() {
        return autoTintIconInLightOrDarkMode;
    }
}
