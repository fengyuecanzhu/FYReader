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

package xyz.fycz.myreader.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.FragmentWebdavSettingBinding;
import xyz.fycz.myreader.model.storage.Restore;
import xyz.fycz.myreader.model.storage.WebDavHelp;
import xyz.fycz.myreader.ui.activity.MainActivity;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * @author fengyue
 * @date 2021/1/9 12:08
 */
public class WebDavFragment extends BaseFragment {

    private FragmentWebdavSettingBinding binding;

    private String webdavUrl;
    private String webdavAccount;
    private String webdavPassword;
    private int restoreNum;
    private int sortType;
    private String[] sortTypeArr = new String[]{"逆序", "顺序"};

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentWebdavSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        webdavUrl = SharedPreUtils.getInstance().getString("webdavUrl", APPCONST.DEFAULT_WEB_DAV_URL);
        webdavAccount = SharedPreUtils.getInstance().getString("webdavAccount", "");
        webdavPassword = SharedPreUtils.getInstance().getString("webdavPassword", "");
        restoreNum = SharedPreUtils.getInstance().getInt("restoreNum", 30);
        sortType = SharedPreUtils.getInstance().getInt("sortType");
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);
        binding.tvWebdavUrl.setText(webdavUrl);
        binding.tvWebdavAccount.setText(StringHelper.isEmpty(webdavAccount) ? "请输入WebDav账号" : webdavAccount);
        binding.tvWebdavPassword.setText(StringHelper.isEmpty(webdavPassword) ? "请输入WebDav授权密码" : "************");
        binding.tvRestoreNum.setText(getString(R.string.cur_restore_list_num, restoreNum));
        binding.tvRestoreSort.setText(sortType == 0 ? sortTypeArr[0] : sortTypeArr[1]);
    }

    @Override
    protected void initClick() {
        super.initClick();
        final String[] webdavTexts = new String[3];
        binding.webdavSettingWebdavUrl.setOnClickListener(v -> {
            MyAlertDialog.createInputDia(getContext(), getString(R.string.webdav_url),
                    "", webdavUrl.equals(APPCONST.DEFAULT_WEB_DAV_URL) ?
                            "" : webdavUrl, InputType.TYPE_CLASS_TEXT,true, 100,
                    text -> webdavTexts[0] = text,
                    (dialog, which) -> {
                        webdavUrl = webdavTexts[0];
                        binding.tvWebdavUrl.setText(webdavUrl);
                        SharedPreUtils.getInstance().putString("webdavUrl", webdavUrl);
                        dialog.dismiss();
                    }, null, "恢复默认", (dialog, which) -> {
                        webdavUrl = APPCONST.DEFAULT_WEB_DAV_URL;
                        binding.tvWebdavUrl.setText(webdavUrl);
                        SharedPreUtils.getInstance().putString("webdavUrl", webdavUrl);
                    });
        });
        binding.webdavSettingWebdavAccount.setOnClickListener(v -> {
            MyAlertDialog.createInputDia(getContext(), getString(R.string.webdav_account),
                    "", webdavAccount, true, 100,
                    text -> webdavTexts[1] = text,
                    (dialog, which) -> {
                        webdavAccount = webdavTexts[1];
                        binding.tvWebdavAccount.setText(webdavAccount);
                        SharedPreUtils.getInstance().putString("webdavAccount", webdavAccount);
                        dialog.dismiss();
                    });
        });
        binding.webdavSettingWebdavPassword.setOnClickListener(v -> {
            MyAlertDialog.createInputDia(getContext(), getString(R.string.webdav_password),
                    "", webdavPassword, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    true, 100,
                    text -> webdavTexts[2] = text,
                    (dialog, which) -> {
                        webdavPassword = webdavTexts[2];
                        binding.tvWebdavPassword.setText("************");
                        SharedPreUtils.getInstance().putString("webdavPassword", webdavPassword);
                        dialog.dismiss();
                    });
        });
        binding.webdavSettingWebdavRestore.setOnClickListener(v -> {
            Single.create((SingleOnSubscribe<ArrayList<String>>) emitter -> {
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

        binding.llRestoreNum.setOnClickListener(v -> {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_number_picker, null);
            NumberPicker threadPick = view.findViewById(R.id.number_picker);
            threadPick.setMaxValue(100);
            threadPick.setMinValue(10);
            threadPick.setValue(restoreNum);
            threadPick.setOnScrollListener((view1, scrollState) -> {

            });
            MyAlertDialog.build(getContext())
                    .setTitle("最大显示数")
                    .setView(view)
                    .setPositiveButton("确定", (dialog, which) -> {
                        restoreNum = threadPick.getValue();
                        SharedPreUtils.getInstance().putInt("restoreNum", restoreNum);
                        binding.tvRestoreNum.setText(getString(R.string.cur_restore_list_num, restoreNum));
                    }).setNegativeButton("取消", null)
                    .show();
        });

        binding.llRestoreSort.setOnClickListener(v -> {
            BottomMenu.show(getString(R.string.sort_type), sortTypeArr)
                    .setSelection(sortType)
                    .setOnMenuItemClickListener((dialog, text, which) -> {
                        sortType = which;
                        SharedPreUtils.getInstance().putInt("sortType", which);
                        binding.tvRestoreSort.setText(sortType == 0 ? sortTypeArr[0] : sortTypeArr[1]);
                        return false;
                    }).setCancelButton(R.string.cancel);
        });
    }
}
