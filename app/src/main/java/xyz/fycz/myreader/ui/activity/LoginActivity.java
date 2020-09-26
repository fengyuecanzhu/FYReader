package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import com.google.android.material.textfield.TextInputLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.backup.UserService;
import xyz.fycz.myreader.base.BaseActivity2;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.util.CodeUtil;
import xyz.fycz.myreader.util.CyptoUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fengyue
 * @date 2020/9/18 22:27
 */
public class LoginActivity extends BaseActivity2 implements TextWatcher {
    @BindView(R.id.et_user)
    TextInputLayout user;
    @BindView(R.id.et_password)
    TextInputLayout password;
    @BindView(R.id.bt_login)
    Button loginBtn;
    @BindView(R.id.tv_register)
    TextView tvRegister;
    @BindView(R.id.et_captcha)
    TextInputLayout etCaptcha;
    @BindView(R.id.iv_captcha)
    ImageView ivCaptcha;

    private String code;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    loginBtn.setEnabled(true);
                    break;
                case 2:
                    createCaptcha();
                    break;
            }
        }
    };

    @Override
    protected int getContentId() {
        return R.layout.activity_login;
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
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        mHandler.sendMessage(mHandler.obtainMessage(2));
        String username = UserService.readUsername();
        user.getEditText().setText(username);
        user.getEditText().requestFocus(username.length());
        //监听内容改变 -> 控制按钮的点击状态
        user.getEditText().addTextChangedListener(this);
        password.getEditText().addTextChangedListener(this);
        etCaptcha.getEditText().addTextChangedListener(this);
    }

    @Override
    protected void initClick() {
        super.initClick();
        ivCaptcha.setOnClickListener(v -> mHandler.sendMessage(mHandler.obtainMessage(2)));

        loginBtn.setOnClickListener(v -> {
            mHandler.sendMessage(mHandler.obtainMessage(2));
            if (!code.toLowerCase().equals(etCaptcha.getEditText().getText().toString().toLowerCase())){
                DialogCreator.createTipDialog(this, "验证码错误！");
                return;
            }
            if (!NetworkUtils.isNetWorkAvailable()) {
                ToastUtils.showError("无网络连接！");
                return;
            }
            ProgressDialog dialog = DialogCreator.createProgressDialog(this, null, "正在登陆...");
            loginBtn.setEnabled(false);
            final String loginName = user.getEditText().getText().toString().trim();
            String loginPwd = password.getEditText().getText().toString();
            final Map<String, String> userLoginInfo = new HashMap<>();
            userLoginInfo.put("loginName", loginName);
            userLoginInfo.put("loginPwd", CyptoUtils.encode(APPCONST.KEY, loginPwd));
            //验证用户名和密码
            UserService.login(userLoginInfo, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    String result = (String) o;
                    String[] info = result.split(":");
                    int resultCode = Integer.parseInt(info[0].trim());
                    String resultName = info[1].trim();
                    //最后输出结果
                    if (resultCode == 102) {
                        UserService.writeConfig(userLoginInfo);
                        UserService.writeUsername(loginName);
                        Intent intent = new Intent();
                        intent.putExtra("isLogin", true);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        ToastUtils.showSuccess(resultName);
                    } else {
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                        dialog.dismiss();
                        ToastUtils.showWarring(resultName);
                    }

                }

                @Override
                public void onError(Exception e) {
                    ToastUtils.showError("登录失败\n" + e.getLocalizedMessage());
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                    dialog.dismiss();
                }
            });

        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    public void createCaptcha() {
        code = CodeUtil.getInstance().createCode();
        Bitmap codeBitmap = CodeUtil.getInstance().createBitmap(code);
        ivCaptcha.setImageBitmap(codeBitmap);
    }


    /**
     * 当有控件获得焦点focus 自动弹出键盘
     * 1. 点击软键盘的enter键 自动收回键盘
     * 2. 代码控制 InputMethodManager
     *    requestFocus
     *    showSoftInput:显示键盘 必须先让这个view成为焦点requestFocus
     *
     *    hideSoftInputFromWindow 隐藏键盘
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            //隐藏键盘
            //1.获取系统输入的管理器
            InputMethodManager inputManager =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            //2.隐藏键盘
            inputManager.hideSoftInputFromWindow(user.getWindowToken(),0);

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
        if (user.getEditText().getText().toString().length() > 0 &&
                password.getEditText().getText().toString().length() > 0 &&
                etCaptcha.getEditText().getText().toString().length() > 0){
            //按钮可以点击
            loginBtn.setEnabled(true);
        }else{
            //按钮不能点击
            loginBtn.setEnabled(false);
        }
    }
}
