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

package xyz.fycz.myreader.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivityLoginBinding;
import xyz.fycz.myreader.model.user.Result;
import xyz.fycz.myreader.model.user.User;
import xyz.fycz.myreader.model.user.UserService;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.LoadingDialog;
import xyz.fycz.myreader.util.CodeUtil;
import xyz.fycz.myreader.util.CyptoUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

/**
 * @author fengyue
 * @date 2020/9/18 22:27
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements TextWatcher {

    private String code;
    private Disposable loginDisp;
    private LoadingDialog dialog;
    private User user;

    @Override
    protected void bindView() {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("登录");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        dialog = new LoadingDialog(this, "正在登录", () -> {
            if (loginDisp != null) {
                loginDisp.dispose();
            }
        });
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        createCaptcha();
        String username = UserService.INSTANCE.readUsername();
        binding.etUser.getEditText().setText(username);
        binding.etUser.getEditText().requestFocus(username.length());
        //监听内容改变 -> 控制按钮的点击状态
        binding.etUser.getEditText().addTextChangedListener(this);
        binding.etPassword.getEditText().addTextChangedListener(this);
        binding.etCaptcha.getEditText().addTextChangedListener(this);
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.ivCaptcha.setOnClickListener(v -> createCaptcha());

        binding.btLogin.setOnClickListener(v -> {
            if (!code.equalsIgnoreCase(binding.etCaptcha.getEditText().getText().toString())) {
                DialogCreator.createTipDialog(this, "验证码错误！");
                return;
            }
            if (!NetworkUtils.isNetWorkAvailable()) {
                ToastUtils.showError("无网络连接！");
                return;
            }
            binding.btLogin.setEnabled(false);
            final String loginName = binding.etUser.getEditText().getText().toString().trim();
            String loginPwd = binding.etPassword.getEditText().getText().toString();
            user = new User(loginName, CyptoUtils.encode(APPCONST.KEY, loginPwd));
            dialog.show();
            UserService.INSTANCE.login(user).subscribe(new MySingleObserver<Result>() {
                @Override
                public void onSubscribe(Disposable d) {
                    addDisposable(d);
                    loginDisp = d;
                }

                @Override
                public void onSuccess(@NonNull Result result) {
                    if (result.getCode() == 102) {
                        loginSuccess();
                        ToastUtils.showSuccess(result.getResult().toString());
                    } else if (result.getCode() == 109) {
                        user.setUserName(GsonExtensionsKt.getGSON()
                                .fromJson(result.getResult().toString(), User.class)
                                .getUserName());
                        loginSuccess();
                        ToastUtils.showSuccess("登录成功");
                    } else if (result.getCode() == 301) {
                        Intent intent = new Intent(LoginActivity.this, AuthEmailActivity.class);
                        BitIntentDataManager.getInstance().putData(intent, user);
                        startActivityForResult(intent, APPCONST.REQUEST_AUTH_EMAIL);
                        ToastUtils.showWarring(result.getResult().toString());
                    } else {
                        ToastUtils.showWarring(result.getResult().toString());
                    }
                    binding.btLogin.setEnabled(false);
                    dialog.dismiss();
                }

                @Override
                public void onError(Throwable e) {
                    ToastUtils.showError("登录失败\n" + e.getLocalizedMessage());
                    binding.btLogin.setEnabled(false);
                    dialog.dismiss();
                    createCaptcha();
                    if (App.isDebug()) e.printStackTrace();
                }
            });
        });

        binding.tvForgotPwd.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AuthEmailActivity.class);
            startActivity(intent);
        });

        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginSuccess() {
        UserService.INSTANCE.writeConfig(user);
        UserService.INSTANCE.writeUsername(user.getUserName());
        Intent intent = new Intent();
        intent.putExtra("isLogin", true);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void createCaptcha() {
        code = CodeUtil.getInstance().createCode();
        Bitmap codeBitmap = CodeUtil.getInstance().createBitmap(code);
        binding.ivCaptcha.setImageBitmap(codeBitmap);
    }


    /**
     * 当有控件获得焦点focus 自动弹出键盘
     * 1. 点击软键盘的enter键 自动收回键盘
     * 2. 代码控制 InputMethodManager
     * requestFocus
     * showSoftInput:显示键盘 必须先让这个view成为焦点requestFocus
     * <p>
     * hideSoftInputFromWindow 隐藏键盘
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //隐藏键盘
            //1.获取系统输入的管理器
            InputMethodManager inputManager =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            //2.隐藏键盘
            inputManager.hideSoftInputFromWindow(binding.etUser.getWindowToken(), 0);

            //3.取消焦点
            View focusView = getCurrentFocus();
            if (focusView != null) {
                focusView.clearFocus(); //取消焦点
            }

            //getCurrentFocus().clearFocus();

            //focusView.requestFocus();//请求焦点
        }
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        //禁止输入中文
        StringUtils.isNotChinese(s);
        //判断两个输入框是否有内容
        if (binding.etUser.getEditText().getText().toString().length() > 0 &&
                binding.etPassword.getEditText().getText().toString().length() > 0 &&
                binding.etCaptcha.getEditText().getText().toString().length() > 0) {
            //按钮可以点击
            binding.btLogin.setEnabled(true);
        } else {
            //按钮不能点击
            binding.btLogin.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == APPCONST.REQUEST_AUTH_EMAIL) {
                loginSuccess();
            }
        }
    }

    @Override
    protected void onDestroy() {
        dialog.dismiss();
        super.onDestroy();
    }
}
