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

package xyz.fycz.myreader.ui.fragment;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kongzue.dialogx.dialogs.BottomMenu;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.FragmentMineBinding;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.model.storage.BackupRestoreUi;
import xyz.fycz.myreader.model.storage.Restore;
import xyz.fycz.myreader.model.storage.WebDavHelp;
import xyz.fycz.myreader.model.user.Result;
import xyz.fycz.myreader.model.user.User;
import xyz.fycz.myreader.model.user.UserService;
import xyz.fycz.myreader.ui.activity.AboutActivity;
import xyz.fycz.myreader.ui.activity.AdSettingActivity;
import xyz.fycz.myreader.ui.activity.BookSourceActivity;
import xyz.fycz.myreader.ui.activity.DonateActivity;
import xyz.fycz.myreader.ui.activity.LoginActivity;
import xyz.fycz.myreader.ui.activity.MainActivity;
import xyz.fycz.myreader.ui.activity.MoreSettingActivity;
import xyz.fycz.myreader.ui.activity.ReadRecordActivity;
import xyz.fycz.myreader.ui.activity.RemoveAdActivity;
import xyz.fycz.myreader.ui.activity.UserInfoActivity;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.LoadingDialog;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.AdUtils;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.RxUtils;

/**
 * @author fengyue
 * @date 2020/9/13 13:20
 */
public class MineFragment extends BaseFragment {

    private FragmentMineBinding binding;

    private boolean isLogin;
    private Setting mSetting;
    private String[] webSynMenu;
    private String[] backupMenu;
    //    private AlertDialog themeModeDia;
    private int themeMode;
    private String[] themeModeArr;
    private User user;
    private Disposable disp;
    private LoadingDialog dialog;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    binding.tvUser.setText("登录/注册");
                    break;
                case 2:
                    backup();
                    break;
                case 3:
                    restore();
                    break;
            }
        }
    };

    public MineFragment() {
    }

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentMineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        user = UserService.INSTANCE.readConfig();
        isLogin = user != null && !TextUtils.isEmpty(user.getUserName());
        mSetting = SysManager.getSetting();
        webSynMenu = new String[]{
                App.getmContext().getString(R.string.menu_backup_webBackup),
                App.getmContext().getString(R.string.menu_backup_webRestore),
                App.getmContext().getString(R.string.menu_backup_autoSyn)
        };
        backupMenu = new String[]{
                App.getmContext().getResources().getString(R.string.menu_backup_backup),
                App.getmContext().getResources().getString(R.string.menu_backup_restore),
        };
        themeMode = App.getApplication().isNightFS() ? 0 : mSetting.isDayStyle() ? 1 : 2;
        themeModeArr = getResources().getStringArray(R.array.theme_mode_arr);
        dialog = new LoadingDialog(getContext(), "正在同步", (LoadingDialog.OnCancelListener) () -> {
            if (disp != null) {
                disp.dispose();
            }
        });
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);
        if (isLogin) {
            binding.tvUser.setText(user.getUserName());
        }
        binding.tvThemeModeSelect.setText(themeModeArr[themeMode]);
        initShowMode();
        AdUtils.checkHasAd(true, true).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(@NonNull Boolean aBoolean) {
                        if (aBoolean && AdUtils.getAdConfig().getRemoveAdTime() > 0){
                            binding.mineRlRemoveAd.setVisibility(View.VISIBLE);
                        }else {
                            binding.mineRlRemoveAd.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        binding.mineRlRemoveAd.setVisibility(View.GONE);
                    }
                });
    }

    private void initShowMode() {
        int showMode = SharedPreUtils.getInstance().getInt("mineShowMode", 0);
        switch (showMode){
            case 1:
                binding.mineLlUser.setVisibility(View.GONE);
                binding.mineLlCloud.setVisibility(View.VISIBLE);
                break;
            case 2:
                binding.mineLlUser.setVisibility(View.VISIBLE);
                binding.mineLlCloud.setVisibility(View.VISIBLE);
                break;
            case 3:
                binding.mineLlUser.setVisibility(View.GONE);
                binding.mineLlCloud.setVisibility(View.GONE);
                break;
            default:
                binding.mineLlUser.setVisibility(View.VISIBLE);
                binding.mineLlCloud.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.mineRlUser.setOnClickListener(v -> {
            if (isLogin) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                startActivityForResult(intent, APPCONST.REQUEST_LOGOUT);
            } else {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivityForResult(intent, APPCONST.REQUEST_LOGIN);
            }
        });
        binding.mineRlSyn.setOnClickListener(v -> {
            if (!isLogin) {
                ToastUtils.showWarring("请先登录！");
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivityForResult(loginIntent, APPCONST.REQUEST_LOGIN);
                return;
            }
            if (mSetting.isAutoSyn()) {
                webSynMenu[2] = App.getmContext().getString(R.string.menu_backup_autoSyn) + "已开启";
            } else {
                webSynMenu[2] = App.getmContext().getString(R.string.menu_backup_autoSyn) + "已关闭";
            }
            /*MyAlertDialog.build(getContext())
                    .setTitle(getActivity().getString(R.string.menu_bookcase_syn))
                    .setItems(webSynMenu, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                synBookcaseToWeb(false);
                                break;
                            case 1:
                                webRestore();
                                break;
                            case 2:
                                String tip = "";
                                if (mSetting.isAutoSyn()) {
                                    mSetting.setAutoSyn(false);
                                    tip = "每日自动同步已关闭！";
                                } else {
                                    mSetting.setAutoSyn(true);
                                    tip = "每日自动同步已开启！";
                                }
                                SysManager.saveSetting(mSetting);
                                ToastUtils.showSuccess(tip);
                                break;
                        }
                    })
                    .setNegativeButton(null, null)
                    .setPositiveButton(null, null)
                    .show();*/
            BottomMenu.show("同步书架", webSynMenu)
                    .setOnMenuItemClickListener((dialog1, text, which) -> {
                        switch (which) {
                            case 0:
                                synBookcaseToWeb(false);
                                break;
                            case 1:
                                webRestore();
                                break;
                            case 2:
                                String tip = "";
                                if (mSetting.isAutoSyn()) {
                                    mSetting.setAutoSyn(false);
                                    tip = "每日自动同步已关闭！";
                                } else {
                                    mSetting.setAutoSyn(true);
                                    tip = "每日自动同步已开启！";
                                }
                                SysManager.saveSetting(mSetting);
                                ToastUtils.showSuccess(tip);
                                break;
                        }
                        return false;
                    }).setCancelButton(R.string.cancel);
        });

        binding.mineRlWebdav.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MoreSettingActivity.class);
            intent.putExtra(APPCONST.WEB_DAV, true);
            startActivity(intent);
        });

        binding.mineRlSynWebdav.setOnClickListener(v -> {
            String account = SharedPreUtils.getInstance().getString("webdavAccount");
            String password = SharedPreUtils.getInstance().getString("webdavPassword");
            if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
                ToastUtils.showWarring("请先配置WebDav账户");
                binding.mineRlWebdav.performClick();
                return;
            }
            Single.create((SingleOnSubscribe<ArrayList<String>>) emitter -> {
                ToastUtils.showInfo("正在连接WebDav服务器...");
                emitter.onSuccess(WebDavHelp.INSTANCE.getWebDavFileNames());
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MySingleObserver<ArrayList<String>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            addDisposable(d);
                        }

                        @Override
                        public void onSuccess(ArrayList<String> strings) {
                            if (!WebDavHelp.INSTANCE.showRestoreDialog(strings, new Restore.CallBack() {
                                @Override
                                public void restoreSuccess() {
                                    SysManager.regetmSetting();
                                    ToastUtils.showSuccess("成功将书架从网络同步至本地！");
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                    startActivity(new Intent(getContext(), MainActivity.class));
                                }

                                @Override
                                public void restoreError(@NotNull String msg) {
                                    ToastUtils.showError(msg);
                                }
                            })) {
                                ToastUtils.showWarring("WebDav服务端没有备份或WebDav配置错误");
                            }
                        }
                    });
        });
        binding.mineRlBookSource.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), BookSourceActivity.class));
        });

        binding.mineRlBackup.setOnClickListener(v -> {
            /*AlertDialog bookDialog = MyAlertDialog.build(getContext())
                    .setTitle(getContext().getResources().getString(R.string.menu_bookcase_backup))
                    .setItems(backupMenu, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                mHandler.sendMessage(mHandler.obtainMessage(2));
                                break;
                            case 1:
                                mHandler.sendMessage(mHandler.obtainMessage(3));
                                break;
                        }
                    })
                    .setNegativeButton(null, null)
                    .setPositiveButton(null, null)
                    .create();
            bookDialog.show();*/
            BottomMenu.show(getContext().getResources().getString(R.string.menu_bookcase_backup), backupMenu)
                    .setOnMenuItemClickListener((dialog, text, which) -> {
                        switch (which) {
                            case 0:
                                mHandler.sendMessage(mHandler.obtainMessage(2));
                                break;
                            case 1:
                                mHandler.sendMessage(mHandler.obtainMessage(3));
                                break;
                        }
                        return false;
                    }).setCancelButton(R.string.cancel);
        });

        binding.mineRlReadRecord.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ReadRecordActivity.class));
        });

        binding.mineRlSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MoreSettingActivity.class);
            startActivityForResult(intent, APPCONST.REQUEST_SETTING);
        });
        binding.mineRlThemeMode.setOnClickListener(v -> {
            /*if (themeModeDia != null) {
                themeModeDia.show();
                return;
            }
            themeModeDia = MyAlertDialog.build(getContext())
                    .setTitle("主题模式")
                    .setSingleChoiceItems(themeModeArr, themeMode
                            , (dialog, which) -> {
                                if (themeMode == which) {
                                    dialog.dismiss();
                                    return;
                                }
                                themeMode = which;
                                switch (which) {
                                    case 0:
                                        SharedPreUtils.getInstance().putBoolean(getString(R.string.isNightFS), true);
                                        break;
                                    case 1:
                                        SharedPreUtils.getInstance().putBoolean(getString(R.string.isNightFS), false);
                                        mSetting.setDayStyle(true);
                                        SysManager.saveSetting(mSetting);
                                        break;
                                    case 2:
                                        SharedPreUtils.getInstance().putBoolean(getString(R.string.isNightFS), false);
                                        mSetting.setDayStyle(false);
                                        SysManager.saveSetting(mSetting);
                                        break;
                                }
                                dialog.dismiss();
                                binding.tvThemeModeSelect.setText(themeModeArr[themeMode]);
                                App.getApplication().initNightTheme();
                            })
                    .setNegativeButton("取消", null)
                    .create();
            themeModeDia.show();*/
            BottomMenu.show("主题模式", themeModeArr)
                    .setSelection(themeMode)
                    .setOnMenuItemClickListener((dialog, text, which) -> {
                        if (themeMode == which) {
                            return false;
                        }
                        themeMode = which;
                        switch (which) {
                            case 0:
                                SharedPreUtils.getInstance().putBoolean(getString(R.string.isNightFS), true);
                                break;
                            case 1:
                                SharedPreUtils.getInstance().putBoolean(getString(R.string.isNightFS), false);
                                mSetting.setDayStyle(true);
                                SysManager.saveSetting(mSetting);
                                break;
                            case 2:
                                SharedPreUtils.getInstance().putBoolean(getString(R.string.isNightFS), false);
                                mSetting.setDayStyle(false);
                                SysManager.saveSetting(mSetting);
                                break;
                        }
                        dialog.dismiss();
                        binding.tvThemeModeSelect.setText(themeModeArr[themeMode]);
                        App.getApplication().initNightTheme();
                        return false;
                    }).setCancelButton(R.string.cancel);
        });

        binding.mineRlRemoveAd.setOnClickListener(v -> startActivity(new Intent(getActivity(), RemoveAdActivity.class)));

        binding.mineRlAbout.setOnClickListener(v -> {
            Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
            startActivity(aboutIntent);
        });

        binding.mineRlFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://qun.qq.com/qqweb/qunpro/share?_wv=3&_wwv=128&inviteCode=2aP6ZQ&from=246610&biz=ka"));
            startActivity(intent);
        });

        binding.mineRlDonate.setOnClickListener(v -> startActivity(new Intent(getActivity(), DonateActivity.class)));
    }

    @Override
    protected void processLogic() {
        super.processLogic();
    }

    /**
     * 备份
     */
    private void backup() {
        ArrayList<Book> mBooks = (ArrayList<Book>) BookService.getInstance().getAllBooks();
        if (mBooks.size() == 0) {
            ToastUtils.showWarring("当前书架无任何书籍，无法备份！");
            return;
        }
        /*StoragePermissionUtils.request(this, (permissions, all) -> {
            DialogCreator.createCommonDialog(getContext(), "确认备份吗?", "新备份会替换原有备份！", true,
                    (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    *//*if (mBackupAndRestore.backup("localBackup")) {
                        DialogCreator.createTipDialog(getContext(), "备份成功，备份文件路径：" + APPCONST.BACKUP_FILE_DIR);
                    } else {
                        DialogCreator.createTipDialog(getContext(), "未给予储存权限，备份失败！");
                    }*//*
                        Backup.INSTANCE.backup(App.getmContext(), APPCONST.BACKUP_FILE_DIR, new Backup.CallBack() {
                            @Override
                            public void backupSuccess() {
                                DialogCreator.createTipDialog(getContext(), "备份成功，备份文件路径：" + APPCONST.BACKUP_FILE_DIR);
                            }

                            @Override
                            public void backupError(@io.reactivex.annotations.NonNull String msg) {
                                DialogCreator.createTipDialog(getContext(), "未给予储存权限，备份失败！\n" + msg);
                            }
                        }, false);
                    }, (dialogInterface, i) -> dialogInterface.dismiss());
        });*/
        BackupRestoreUi.INSTANCE.backup(getActivity());
    }

    /**
     * 恢复
     */
    private void restore() {
        /*StoragePermissionUtils.request(this, (permissions, all) -> {
            DialogCreator.createCommonDialog(getContext(), "确认恢复吗?", "恢复书架会覆盖原有书架！", true,
                    (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    *//*if (mBackupAndRestore.restore("localBackup")) {
                        mHandler.sendMessage(mHandler.obtainMessage(7));
//                            DialogCreator.createTipDialog(mMainActivity,
//                                    "恢复成功！\n注意：本功能属于实验功能，书架恢复后，书籍初次加载时可能加载失败，返回重新加载即可！");
                        mSetting = SysManager.getSetting();
                        ToastUtils.showSuccess("书架恢复成功！");
                    } else {
                        DialogCreator.createTipDialog(getContext(), "未找到备份文件或未给予储存权限，恢复失败！");
                    }*//*
                        Restore.INSTANCE.restore(APPCONST.BACKUP_FILE_DIR, new Restore.CallBack() {
                            @Override
                            public void restoreSuccess() {
                                mHandler.sendMessage(mHandler.obtainMessage(7));
//                            DialogCreator.createTipDialog(mMainActivity,
//                                    "恢复成功！\n注意：本功能属于实验功能，书架恢复后，书籍初次加载时可能加载失败，返回重新加载即可！");
                                SysManager.regetmSetting();
                                ToastUtils.showSuccess("书架恢复成功！");
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                                startActivity(new Intent(getContext(), MainActivity.class));
                            }

                            @Override
                            public void restoreError(@io.reactivex.annotations.NonNull String msg) {
                                DialogCreator.createTipDialog(getContext(), "未找到备份文件或未给予储存权限，恢复失败！");
                            }
                        });
                    }, (dialogInterface, i) -> dialogInterface.dismiss());
        });*/
        BackupRestoreUi.INSTANCE.restore(getActivity());
    }

    /**
     * 同步书架
     */
    private void synBookcaseToWeb(boolean isAutoSyn) {
        if (!NetworkUtils.isNetWorkAvailable()) {
            if (!isAutoSyn) {
                ToastUtils.showWarring("无网络连接！");
            }
            return;
        }
        ArrayList<Book> mBooks = (ArrayList<Book>) BookService.getInstance().getAllBooks();
        if (mBooks.size() == 0) {
            if (!isAutoSyn) {
                ToastUtils.showWarring("当前书架无任何书籍，无法同步！");
            }
            return;
        }
        Date nowTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
        String nowTimeStr = sdf.format(nowTime);
        SharedPreUtils spb = SharedPreUtils.getInstance();
        String synTime = spb.getString(getString(R.string.synTime));
        if (!nowTimeStr.equals(synTime) || !isAutoSyn) {
            dialog.show();
            UserService.INSTANCE.webBackup(user).subscribe(new MySingleObserver<Result>() {
                @Override
                public void onSubscribe(Disposable d) {
                    addDisposable(d);
                    disp = d;
                }

                @Override
                public void onSuccess(@NonNull Result result) {
                    if (result.getCode() == 104) {
                        spb.putString(getString(R.string.synTime), nowTimeStr);
                        if (!isAutoSyn) {
                            DialogCreator.createTipDialog(getContext(), "成功将书架同步至网络！");
                        }
                    } else {
                        if (!isAutoSyn) {
                            DialogCreator.createTipDialog(getContext(), "同步失败，请重试！");
                        }
                    }
                    dialog.dismiss();
                }

                @Override
                public void onError(Throwable e) {
                    if (!isAutoSyn) {
                        DialogCreator.createTipDialog(getContext(), "同步失败，请重试！\n" + e.getLocalizedMessage());
                    }
                    dialog.dismiss();
                    if (App.isDebug()) e.printStackTrace();
                }
            });
        }

    }

    /**
     * 恢复
     */
    private void webRestore() {
        if (!NetworkUtils.isNetWorkAvailable()) {
            ToastUtils.showWarring("无网络连接！");
            return;
        }
        DialogCreator.createCommonDialog(getContext(), "确认同步吗?", "将书架从网络同步至本地会覆盖原有书架！", true,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    dialog.show();
                    UserService.INSTANCE.webRestore(user).subscribe(new MySingleObserver<Result>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            addDisposable(d);
                            disp = d;
                        }

                        @Override
                        public void onSuccess(@NonNull Result result) {
                            if (result.getCode() < 200) {
                                mHandler.sendMessage(mHandler.obtainMessage(7));
//                                    DialogCreator.createTipDialog(mMainActivity,
//                                            "恢复成功！\n注意：本功能属于实验功能，书架恢复后，书籍初次加载时可能加载失败，返回重新加载即可！");、
                                SysManager.regetmSetting();
                                ToastUtils.showSuccess("成功将书架从网络同步至本地！");
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                                startActivity(new Intent(getContext(), MainActivity.class));
                            } else {
                                DialogCreator.createTipDialog(getContext(), "未找到同步文件，同步失败！");
                            }
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (App.isDebug()) e.printStackTrace();
                            DialogCreator.createTipDialog(getContext(), "未找到同步文件，同步失败！\n" + e.getLocalizedMessage());
                            dialog.dismiss();
                        }
                    });
                }, (dialogInterface, i) -> dialogInterface.dismiss());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case APPCONST.REQUEST_LOGIN:
                    if (data == null) {
                        return;
                    }
                    isLogin = data.getBooleanExtra("isLogin", false);
                    user = UserService.INSTANCE.readConfig();
                    if (isLogin && user != null) {
                        binding.tvUser.setText(user.getUserName());
                    }
                    break;
                case APPCONST.REQUEST_SETTING:
                    if (data == null) {
                        return;
                    }
                    if (data.getBooleanExtra(APPCONST.RESULT_NEED_REFRESH, false)){
                        initShowMode();
                    }
                    break;
                case APPCONST.REQUEST_LOGOUT:
                    isLogin = false;
                    mHandler.sendEmptyMessage(1);
                    break;
            }
        }
    }

    public boolean isRecreate() {
        return binding == null;
    }
}
