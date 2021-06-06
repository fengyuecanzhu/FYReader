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
