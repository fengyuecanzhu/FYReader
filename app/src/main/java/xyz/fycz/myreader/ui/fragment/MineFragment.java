package xyz.fycz.myreader.ui.fragment;

import android.annotation.SuppressLint;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.model.backup.BackupAndRestore;
import xyz.fycz.myreader.model.backup.UserService;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.ui.activity.AboutActivity;
import xyz.fycz.myreader.ui.activity.LoginActivity;
import xyz.fycz.myreader.ui.activity.MoreSettingActivity;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.NetworkUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * @author fengyue
 * @date 2020/9/13 13:20
 */
public class MineFragment extends BaseFragment {
    @BindView(R.id.mine_rl_user)
    RelativeLayout mRlUser;
    @BindView(R.id.tv_user)
    TextView mTvUser;
    @BindView(R.id.mine_rl_backup)
    RelativeLayout mRlBackup;
    @BindView(R.id.mine_rl_syn)
    RelativeLayout mRlSyn;
    @BindView(R.id.mine_rl_setting)
    RelativeLayout mRlSetting;
    @BindView(R.id.mine_rl_theme_mode)
    RelativeLayout mRlThemeMode;
    @BindView(R.id.tv_theme_mode_select)
    TextView tvThemeModeSelect;
    @BindView(R.id.mine_rl_about)
    RelativeLayout mRlAbout;

    private boolean isLogin;
    private BackupAndRestore mBackupAndRestore;
    private Setting mSetting;
    private String[] webSynMenu;
    private String[] backupMenu;
    private AlertDialog themeModeDia;
    private int themeMode;
    private String[] themeModeArr;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    mTvUser.setText("登录/注册");
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
    protected int getContentId() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        isLogin = UserService.isLogin();
        mBackupAndRestore = new BackupAndRestore();
        mSetting = SysManager.getSetting();
        webSynMenu = new String[]{
                MyApplication.getmContext().getString(R.string.menu_backup_webBackup),
                MyApplication.getmContext().getString(R.string.menu_backup_webRestore),
                MyApplication.getmContext().getString(R.string.menu_backup_autoSyn)
        };
        backupMenu = new String[]{
                MyApplication.getmContext().getResources().getString(R.string.menu_backup_backup),
                MyApplication.getmContext().getResources().getString(R.string.menu_backup_restore),
        };
        themeMode = MyApplication.getApplication().isNightFS() ? 0 : mSetting.isDayStyle() ? 1 : 2;
        themeModeArr = getResources().getStringArray(R.array.theme_mode_arr);
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);
        if (isLogin) {
            mTvUser.setText(UserService.readUsername());
        }
        tvThemeModeSelect.setText(themeModeArr[themeMode]);
    }

    @Override
    protected void initClick() {
        super.initClick();
        mRlUser.setOnClickListener(v -> {
            if (isLogin) {
                DialogCreator.createCommonDialog(getActivity(), "退出登录", "确定要退出登录吗？"
                        , true, (dialog, which) -> {
                            File file = MyApplication.getApplication().getFileStreamPath("userConfig.fy");
                            if (file.delete()) {
                                ToastUtils.showSuccess("退出成功");
                                isLogin = false;
                                mHandler.sendEmptyMessage(1);
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                getActivity().startActivityForResult(intent, APPCONST.REQUEST_LOGIN);
                            } else {
                                ToastUtils.showError("退出失败(Error：file.delete())");
                            }
                        }, (dialog, which) -> dialog.dismiss());
            } else {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivityForResult(intent, APPCONST.REQUEST_LOGIN);
            }
        });
        mRlBackup.setOnClickListener(v -> {
            AlertDialog bookDialog = MyAlertDialog.build(getContext())
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
            bookDialog.show();
        });
        mRlSyn.setOnClickListener(v -> {
            if (!UserService.isLogin()) {
                ToastUtils.showWarring("请先登录！");
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivityForResult(loginIntent, APPCONST.REQUEST_LOGIN);
                return;
            }
            if (mSetting.isAutoSyn()) {
                webSynMenu[2] = MyApplication.getmContext().getString(R.string.menu_backup_autoSyn) + "已开启";
            } else {
                webSynMenu[2] = MyApplication.getmContext().getString(R.string.menu_backup_autoSyn) + "已关闭";
            }
            MyAlertDialog.build(getContext())
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
                    .show();
        });
        mRlSetting.setOnClickListener(v -> {
            Intent settingIntent = new Intent(getActivity(), MoreSettingActivity.class);
            startActivity(settingIntent);
        });
        mRlThemeMode.setOnClickListener(v -> {
            if (themeModeDia != null) {
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
                                        SharedPreUtils.getInstance().putBoolean("isNightFS", true);
                                        break;
                                    case 1:
                                        SharedPreUtils.getInstance().putBoolean("isNightFS", false);
                                        mSetting.setDayStyle(true);
                                        SysManager.saveSetting(mSetting);
                                        break;
                                    case 2:
                                        SharedPreUtils.getInstance().putBoolean("isNightFS", false);
                                        mSetting.setDayStyle(false);
                                        SysManager.saveSetting(mSetting);
                                        break;
                                }
                                dialog.dismiss();
                                tvThemeModeSelect.setText(themeModeArr[themeMode]);
                                MyApplication.getApplication().initNightTheme();
                            })
                    .setNegativeButton("取消", null)
                    .create();
            themeModeDia.show();

        });

        mRlAbout.setOnClickListener(v -> {
            Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
            startActivity(aboutIntent);
        });

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
        DialogCreator.createCommonDialog(getContext(), "确认备份吗?", "新备份会替换原有备份！", true,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (mBackupAndRestore.backup("localBackup")) {
                        DialogCreator.createTipDialog(getContext(), "备份成功，备份文件路径：" + APPCONST.BACKUP_FILE_DIR);
                    } else {
                        DialogCreator.createTipDialog(getContext(), "未给予储存权限，备份失败！");
                    }
                }, (dialogInterface, i) -> dialogInterface.dismiss());
    }

    /**
     * 恢复
     */
    private void restore() {
        DialogCreator.createCommonDialog(getContext(), "确认恢复吗?", "恢复书架会覆盖原有书架！", true,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (mBackupAndRestore.restore("localBackup")) {
                        mHandler.sendMessage(mHandler.obtainMessage(7));
//                            DialogCreator.createTipDialog(mMainActivity,
//                                    "恢复成功！\n注意：本功能属于实验功能，书架恢复后，书籍初次加载时可能加载失败，返回重新加载即可！");
                        mSetting = SysManager.getSetting();
                        ToastUtils.showSuccess("书架恢复成功！");
                    } else {
                        DialogCreator.createTipDialog(getContext(), "未找到备份文件或未给予储存权限，恢复失败！");
                    }
                }, (dialogInterface, i) -> dialogInterface.dismiss());
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
        String synTime = spb.getString("synTime");
        if (!nowTimeStr.equals(synTime) || !isAutoSyn) {
            MyApplication.getApplication().newThread(() -> {
                if (UserService.webBackup()) {
                    spb.putString("synTime", nowTimeStr);
                    if (!isAutoSyn) {
                        DialogCreator.createTipDialog(getContext(), "成功将书架同步至网络！");
                    }
                } else {
                    if (!isAutoSyn) {
                        DialogCreator.createTipDialog(getContext(), "同步失败，请重试！");
                    }
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
                    MyApplication.getApplication().newThread(() -> {
                        if (UserService.webRestore()) {
                            mHandler.sendMessage(mHandler.obtainMessage(7));
//                                    DialogCreator.createTipDialog(mMainActivity,
//                                            "恢复成功！\n注意：本功能属于实验功能，书架恢复后，书籍初次加载时可能加载失败，返回重新加载即可！");、
                            mSetting = SysManager.getSetting();
                            ToastUtils.showSuccess("成功将书架从网络同步至本地！");
                        } else {
                            DialogCreator.createTipDialog(getContext(), "未找到同步文件，同步失败！");
                        }
                    });
                }, (dialogInterface, i) -> dialogInterface.dismiss());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case APPCONST.REQUEST_LOGIN:
                    assert data != null;
                    isLogin = data.getBooleanExtra("isLogin", false);
                    if (isLogin) {
                        mTvUser.setText(UserService.readUsername());
                    }
                    break;
            }
        }
    }

    public boolean isRecreate() {
        return unbinder == null;
    }
}
