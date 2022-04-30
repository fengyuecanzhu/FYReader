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

package com.kongzue.dialogx.impl;

import android.animation.Animator;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/9/22 14:37
 */
public abstract class AnimatorListenerEndCallBack implements Animator.AnimatorListener {
    @Override
    public void onAnimationStart(Animator animation) {
    
    }
    
    @Override
    public abstract void onAnimationEnd(Animator animation);
    
    @Override
    public void onAnimationCancel(Animator animation) {
    
    }
    
    @Override
    public void onAnimationRepeat(Animator animation) {
    
    }
}
