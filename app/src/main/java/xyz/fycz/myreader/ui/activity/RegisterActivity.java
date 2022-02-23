package xyz.fycz.myreader.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.LinkMovementMethod;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.MyTextWatcher;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivityRegisterBinding;
import xyz.fycz.myreader.model.user.Result;
import xyz.fycz.myreader.model.user.User;
import xyz.fycz.myreader.model.user.UserService;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.LoadingDialog;
import xyz.fycz.myreader.util.CodeUtil;
import xyz.fycz.myreader.util.CyptoUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

/**
 * @author fengyue
 * @date 2020/9/18 22:37
 */
public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding binding;

    private String code;
    private String username = "";
    private String password = "";
    private String email = "";
    private String emailCode = "";
    private String keyc = "";
    private String inputCode = "";
    private LoadingDialog dialog;
    private Disposable disp;

    @Override
    protected void bindView() {
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("注册");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        dialog = new LoadingDialog(this, "正在注册", () -> {
            if (disp != null) {
                disp.dispose();
            }
        });
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        createCaptcha();
        binding.etUsername.requestFocus();
        binding.etUsername.getEditText().addTextChangedListener(new MyTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                StringUtils.isNotChinese(s);
                username = s.toString();
                if (username.length() < 6 || username.length() > 14) {
                    showTip("用户名必须在6-14位之间");
                } else if (!username.substring(0, 1).matches("^[A-Za-z]$")) {
                    showTip("用户名只能以字母开头");
                } else if (!username.matches("^[A-Za-z0-9-_]+$")) {
                    showTip("用户名只能由数字、字母、下划线、减号组成");
                } else {
                    binding.tvRegisterTip.setVisibility(View.GONE);
                }
                checkNotNone();
            }
        });

        binding.etPassword.getEditText().addTextChangedListener(new MyTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                password = s.toString();
                if (password.length() < 8 || password.length() > 16) {
                    showTip("密码必须在8-16位之间");
                } else if (password.matches("^\\d+$")) {
                    showTip("密码不能是纯数字");
                } else {
                    binding.tvRegisterTip.setVisibility(View.GONE);
                }
                checkNotNone();
            }
        });

        binding.etEmail.getEditText().addTextChangedListener(new MyTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                email = s.toString();
                if (!email.matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$")) {
                    showTip("邮箱格式错误");
                } else {
                    binding.tvRegisterTip.setVisibility(View.GONE);
                }
                checkNotNone();
            }
        });

        binding.etEmailCode.getEditText().addTextChangedListener(new MyTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                emailCode = s.toString().trim();
                checkNotNone();
            }
        });

        binding.etCaptcha.getEditText().addTextChangedListener(new MyTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                inputCode = s.toString().trim().toLowerCase();
                if (!inputCode.equals(code.toLowerCase())) {
                    showTip("验证码错误");
                } else {
                    binding.tvRegisterTip.setVisibility(View.GONE);
                }
                checkNotNone();
            }
        });

        binding.cbAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.ivCaptcha.setOnClickListener(v -> createCaptcha());
        binding.tvGetEmailCode.setOnClickListener(v -> {
            if (!email.matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$")) {
                ToastUtils.showWarring("请正确输入邮箱");
                return;
            }
            dialog.show();
            dialog.setmMessage("正在发送");
            UserService.INSTANCE.sendEmail(email, "reg", keyc).subscribe(new MySingleObserver<Result>() {
                @Override
                public void onSubscribe(Disposable d) {
                    addDisposable(d);
                    disp = d;
                }

                @Override
                public void onSuccess(@NonNull Result result) {
                    if (result.getCode() == 106) {
                        ToastUtils.showSuccess("验证码发送成功");
                        keyc = result.getResult().toString();
                        timeDown(60);
                    } else {
                        ToastUtils.showWarring(result.getResult().toString());
                    }
                    dialog.dismiss();
                }

                @Override
                public void onError(Throwable e) {
                    ToastUtils.showError("验证码发送失败：\n" + e.getLocalizedMessage());
                    dialog.dismiss();
                }
            });
        });

        binding.btRegister.setOnClickListener(v -> {
            if (!username.matches("^[A-Za-z][A-Za-z0-9]{5,13}$")) {
                DialogCreator.createTipDialog(this, "用户名格式错误",
                        "用户名必须在6-14位之间\n用户名只能以字母开头\n用户名只能由数字、字母、下划线、减号组成");
            } else if (password.matches("^\\d+$") || !password.matches("^.{8,16}$")) {
                DialogCreator.createTipDialog(this, "密码格式错误",
                        "密码必须在8-16位之间\n密码不能是纯数字");
            } else if (!email.matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$")) {
                DialogCreator.createTipDialog(this, "邮箱格式错误",
                        "电子邮箱的正确写法为：用户名@邮箱网站.com(.cn等)");
            } else if ("".equals(keyc)) {
                DialogCreator.createTipDialog(this, "请先获取邮箱验证码");
            } else if (emailCode.length() < 6) {
                DialogCreator.createTipDialog(this, "请输入6位邮箱验证码");
            } else if (!inputCode.trim().equalsIgnoreCase(code)) {
                DialogCreator.createTipDialog(this, "验证码错误");
            } else if (!binding.cbAgreement.isChecked()) {
                DialogCreator.createTipDialog(this, "请勾选同意《用户服务协议》");
            } else {
                dialog.show();
                dialog.setmMessage("正在注册");
                User user = new User(username, CyptoUtils.encode(APPCONST.KEY, password), email);
                UserService.INSTANCE.register(user, emailCode, keyc).subscribe(new MySingleObserver<Result>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                        disp = d;
                    }

                    @Override
                    public void onSuccess(@NonNull Result result) {
                        if (result.getCode() == 101) {
                            UserService.INSTANCE.writeUsername(user.getUserName());
                            ToastUtils.showSuccess(result.getResult().toString());
                            finish();
                        } else {
                            ToastUtils.showWarring(result.getResult().toString());
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showError("注册失败：\n" + e.getLocalizedMessage());
                        dialog.dismiss();
                        createCaptcha();
                    }
                });
            }
        });
    }

    public void createCaptcha() {
        code = CodeUtil.getInstance().createCode();
        Bitmap codeBitmap = CodeUtil.getInstance().createBitmap(code);
        binding.ivCaptcha.setImageBitmap(codeBitmap);
    }

    public void showTip(String tip) {
        binding.tvRegisterTip.setVisibility(View.VISIBLE);
        binding.tvRegisterTip.setText(tip);
    }

    private void timeDown(int time) {
        if (time == 0) {
            binding.tvGetEmailCode.setText(getString(R.string.re_get_email_code, ""));
            binding.tvGetEmailCode.setEnabled(true);
        } else {
            binding.tvGetEmailCode.setEnabled(false);
            String timeStr = "(" + time + ")";
            binding.tvGetEmailCode.setText(getString(R.string.re_get_email_code, timeStr));
            App.getHandler().postDelayed(() -> timeDown(time - 1), 1000);
        }
    }

    public void checkNotNone() {
        binding.btRegister.setEnabled(!"".equals(username) &&
                !"".equals(password) &&
                !"".equals(email) &&
                !"".equals(emailCode) &&
                !"".equals(inputCode));
    }

    @Override
    protected void onDestroy() {
        dialog.dismiss();
        super.onDestroy();
    }
}
