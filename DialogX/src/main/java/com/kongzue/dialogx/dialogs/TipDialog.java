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

package com.kongzue.dialogx.dialogs;

import android.app.Activity;

import com.kongzue.dialogx.interfaces.DialogLifecycleCallback;

import java.lang.ref.WeakReference;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/9/28 23:53
 */
public class TipDialog extends WaitDialog {
    
    protected TipDialog() {
        super();
    }
    
    public static WaitDialog show(int messageResId) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(messageResId);
        if (dialogImpl != null) {
            dialogImpl.showTip(TYPE.WARNING);
        } else {
            me().showTip(messageResId, TYPE.WARNING);
        }
        return me();
    }
    
    public static WaitDialog show(Activity activity, int messageResId) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(messageResId);
        if (dialogImpl != null && dialogImpl.bkg.getContext() == activity) {
            dialogImpl.showTip(TYPE.WARNING);
        } else {
            me().showTip(activity, messageResId, TYPE.WARNING);
        }
        return me();
    }
    
    public static WaitDialog show(CharSequence message) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(message);
        if (dialogImpl != null) {
            dialogImpl.showTip(TYPE.WARNING);
        } else {
            me().showTip(message, TYPE.WARNING);
        }
        return me();
    }
    
    public static WaitDialog show(Activity activity, CharSequence message) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(message);
        if (dialogImpl != null && dialogImpl.bkg.getContext() == activity) {
            dialogImpl.showTip(TYPE.WARNING);
        } else {
            me().showTip(activity, message, TYPE.WARNING);
        }
        return me();
    }
    
    public static WaitDialog show(int messageResId, TYPE tip) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(messageResId);
        if (dialogImpl != null) {
            dialogImpl.showTip(tip);
        } else {
            me().showTip(messageResId, tip);
        }
        return me();
    }
    
    public static WaitDialog show(Activity activity, int messageResId, TYPE tip) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(messageResId);
        if (dialogImpl != null && dialogImpl.bkg.getContext() == activity) {
            dialogImpl.showTip(tip);
        } else {
            me().showTip(activity, messageResId, tip);
        }
        return me();
    }
    
    public static WaitDialog show(CharSequence message, TYPE tip) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(message);
        if (dialogImpl != null) {
            dialogImpl.showTip(tip);
        } else {
            me().showTip(message, tip);
        }
        return me();
    }
    
    public static WaitDialog show(Activity activity, CharSequence message, TYPE tip) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(message);
        if (dialogImpl != null && dialogImpl.bkg.getContext() == activity) {
            dialogImpl.showTip(tip);
        } else {
            me().showTip(activity, message, tip);
        }
        return me();
    }
    
    public static WaitDialog show(int messageResId, TYPE tip, long duration) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(messageResId);
        me().tipShowDuration = duration;
        if (dialogImpl != null) {
            dialogImpl.showTip(tip);
        } else {
            me().showTip(messageResId, tip);
        }
        return me();
    }
    
    public static WaitDialog show(Activity activity, int messageResId, TYPE tip, long duration) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(messageResId);
        me().tipShowDuration = duration;
        if (dialogImpl != null && dialogImpl.bkg.getContext() == activity) {
            dialogImpl.showTip(tip);
        } else {
            me().showTip(activity, messageResId, tip);
        }
        return me();
    }
    
    public static WaitDialog show(CharSequence message, TYPE tip, long duration) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(message);
        me().tipShowDuration = duration;
        if (dialogImpl != null) {
            dialogImpl.showTip(tip);
        } else {
            me().showTip(message, tip);
        }
        return me();
    }
    
    public static WaitDialog show(Activity activity, CharSequence message, TYPE tip, long duration) {
        DialogImpl dialogImpl = me().dialogImpl;
        me().preMessage(message);
        me().tipShowDuration = duration;
        if (dialogImpl != null && dialogImpl.bkg.getContext() == activity) {
            dialogImpl.showTip(tip);
        } else {
            me().showTip(activity, message, tip);
        }
        return me();
    }
    
    @Override
    public String dialogKey() {
        return getClass().getSimpleName() + "(" + Integer.toHexString(hashCode()) + ")";
    }
}
