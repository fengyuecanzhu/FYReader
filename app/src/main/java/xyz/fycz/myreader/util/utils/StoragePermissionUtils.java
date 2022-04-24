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

package xyz.fycz.myreader.util.utils;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * @author fengyue
 * @date 2021/6/6 18:37
 */
public class StoragePermissionUtils {
    public static void request(Context context, OnPermissionCallback callback){
        XXPermissions.with(context)
                .permission(APPCONST.STORAGE_PERMISSIONS)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        callback.onGranted(permissions, all);
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        ToastUtils.showWarring("没有储存权限！");
                        callback.onDenied(permissions, never);
                    }
                });
    }

    public static void request(Fragment fragment, OnPermissionCallback callback){
        XXPermissions.with(fragment)
                .permission(APPCONST.STORAGE_PERMISSIONS)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        callback.onGranted(permissions, all);
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        ToastUtils.showWarring("没有储存权限！");
                        callback.onDenied(permissions, never);
                    }
                });
    }
}
