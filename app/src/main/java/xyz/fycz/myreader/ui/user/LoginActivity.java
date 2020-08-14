package xyz.fycz.myreader.ui.user;

/**
 * @author fengyue
 * @date 2020/4/26 18:49
 */

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.material.textfield.TextInputLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.util.utils.StringUtils;


@SuppressLint("Registered")
public class LoginActivity extends BaseActivity implements TextWatcher {
    @BindView(R.id.ll_title_back)
    LinearLayout llTitleBack;
    @BindView(R.id.tv_title_text)
    TextView tvTitleText;
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
    private LoginPresenter mLoginPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setStatusBar(R.color.sys_line);
        mLoginPresenter = new LoginPresenter(this);
        mLoginPresenter.start();
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


    public EditText getUser() {
        return user.getEditText();
    }

    public EditText getPassword() {
        return password.getEditText();
    }

    public Button getLoginBtn() {
        return loginBtn;
    }

    public TextView getTvTitleText() {
        return tvTitleText;
    }

    public LinearLayout getLlTitleBack() {
        return llTitleBack;
    }

    public TextView getTvRegister() {
        return tvRegister;
    }

    public EditText getEtCaptcha() {
        return etCaptcha.getEditText();
    }

    public ImageView getIvCaptcha() {
        return ivCaptcha;
    }
}
