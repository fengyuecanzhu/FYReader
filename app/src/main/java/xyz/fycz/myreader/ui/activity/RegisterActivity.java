package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import com.google.android.material.textfield.TextInputLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.model.backup.UserService;
import xyz.fycz.myreader.base.BaseActivity2;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.util.CodeUtil;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fengyue
 * @date 2020/9/18 22:37
 */
public class RegisterActivity extends BaseActivity2 {
    @BindView(R.id.et_username)
    TextInputLayout etUsername;
    @BindView(R.id.et_password)
    TextInputLayout etPassword;
    @BindView(R.id.et_rp_password)
    TextInputLayout etRpPassword;
    @BindView(R.id.et_captcha)
    TextInputLayout etCaptcha;
    @BindView(R.id.iv_captcha)
    ImageView ivCaptcha;
    @BindView(R.id.bt_register)
    Button btRegister;
    @BindView(R.id.tv_register_tip)
    TextView tvRegisterTip;
    @BindView(R.id.cb_agreement)
    CheckBox cbAgreement;
    @BindView(R.id.tv_agreement)
    TextView tvAgreement;
    private String code;
    private String username = "";
    private String password = "";
    private String rpPassword = "";
    private String inputCode = "";

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    createCaptcha();
                    break;
                case 2:
                    showTip((String) msg.obj);
                    break;
                case 3:
                    tvRegisterTip.setVisibility(View.GONE);
                    break;
            }
        }
    };


    @Override
    protected int getContentId() {
        return R.layout.activity_register;
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("注册");
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        mHandler.sendMessage(mHandler.obtainMessage(1));
        etUsername.requestFocus();
        etUsername.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                StringUtils.isNotChinese(s);
                username = s.toString();
                if (username.length() < 6 || username.length() >14){
                    mHandler.sendMessage(mHandler.obtainMessage(2, "用户名必须在6-14位之间"));
                } else if(!username.substring(0, 1).matches("^[A-Za-z]$")){
                    mHandler.sendMessage(mHandler.obtainMessage(2,
                            "用户名只能以字母开头"));
                }else if(!username.matches("^[A-Za-z0-9-_]+$")){
                    mHandler.sendMessage(mHandler.obtainMessage(2,
                            "用户名只能由数字、字母、下划线、减号组成"));
                }else {
                    mHandler.sendMessage(mHandler.obtainMessage(3));
                }
                checkNotNone();
            }
        });

        etPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                password = s.toString();
                if (password.length() < 8 || password.length() > 16){
                    mHandler.sendMessage(mHandler.obtainMessage(2, "密码必须在8-16位之间"));
                } else if(password.matches("^\\d+$")){
                    mHandler.sendMessage(mHandler.obtainMessage(2, "密码不能是纯数字"));
                } else {
                    mHandler.sendMessage(mHandler.obtainMessage(3));
                }
                checkNotNone();
            }
        });

        etRpPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                rpPassword = s.toString();
                if (!rpPassword.equals(password)){
                    mHandler.sendMessage(mHandler.obtainMessage(2, "两次输入的密码不一致"));
                } else {
                    mHandler.sendMessage(mHandler.obtainMessage(3));
                }
                checkNotNone();
            }
        });

        etCaptcha.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputCode = s.toString().trim().toLowerCase();
                if (!inputCode.equals(code.toLowerCase())){
                    mHandler.sendMessage(mHandler.obtainMessage(2, "验证码错误"));
                } else {
                    mHandler.sendMessage(mHandler.obtainMessage(3));
                }
                checkNotNone();
            }
        });

        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    protected void initClick() {
        super.initClick();
        ivCaptcha.setOnClickListener(v -> mHandler.sendMessage(mHandler.obtainMessage(1)));

        btRegister.setOnClickListener(v -> {
            if (!username.matches("^[A-Za-z][A-Za-z0-9]{5,13}$")){
                DialogCreator.createTipDialog(this, "用户名格式错误",
                        "用户名必须在6-14位之间\n用户名只能以字母开头\n用户名只能由数字、字母、下划线、减号组成");
            }else if(password.matches("^\\d+$") || !password.matches("^.{8,16}$")){
                DialogCreator.createTipDialog(this, "密码格式错误",
                        "密码必须在8-16位之间\n密码不能是纯数字");
            }else if(!password.equals(rpPassword)){
                DialogCreator.createTipDialog(this, "重复密码错误",
                        "两次输入的密码不一致");
            }else if(!inputCode.trim().toLowerCase().equals(code.toLowerCase())){
                DialogCreator.createTipDialog(this, "验证码错误");
            }else if(!cbAgreement.isChecked()){
                DialogCreator.createTipDialog(this, "请勾选同意《用户服务协议》");
            }else {
                ProgressDialog dialog = DialogCreator.createProgressDialog(this, null, "正在注册...");
                Map<String, String> userRegisterInfo = new HashMap<>();
                userRegisterInfo.put("username", username);
                userRegisterInfo.put("password", password);
                UserService.register(userRegisterInfo, new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        String[] info = ((String) o).split(":");
                        int result = Integer.parseInt(info[0].trim());
                        if (result == 101){
                            UserService.writeUsername(username);
                            ToastUtils.showSuccess(info[1]);
                            finish();
                        }else {
                            ToastUtils.showWarring(info[1]);
                        }
                        dialog.dismiss();
                    }
                    @Override
                    public void onError(Exception e) {
                        ToastUtils.showError("注册失败：\n" + e.getLocalizedMessage());
                        dialog.dismiss();
                    }
                });
            }
            mHandler.sendMessage(mHandler.obtainMessage(1));
        });

    }

    public void createCaptcha() {
        code = CodeUtil.getInstance().createCode();
        Bitmap codeBitmap = CodeUtil.getInstance().createBitmap(code);
        ivCaptcha.setImageBitmap(codeBitmap);
    }

    public void showTip(String tip) {
        tvRegisterTip.setVisibility(View.VISIBLE);
        tvRegisterTip.setText(tip);
    }

    public void checkNotNone(){
        if ("".equals(username) || "".equals(password) || "".equals(rpPassword) || "".equals(inputCode)){
            btRegister.setEnabled(false);
        }else {
            btRegister.setEnabled(true);
        }
    }

}
