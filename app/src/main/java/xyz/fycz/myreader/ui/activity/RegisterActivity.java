package xyz.fycz.myreader.ui.activity;

/**
 * @author fengyue
 * @date 2020/4/26 18:49
 */

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.material.textfield.TextInputLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.ui.presenter.RegisterPresenter;


@SuppressLint("Registered")
public class RegisterActivity extends BaseActivity{
    @BindView(R.id.ll_title_back)
    LinearLayout llTitleBack;
    @BindView(R.id.tv_title_text)
    TextView tvTitleText;
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
    private RegisterPresenter mRegisterPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        setStatusBar(R.color.white, false);
        mRegisterPresenter = new RegisterPresenter(this);
        mRegisterPresenter.start();
    }

    public LinearLayout getLlTitleBack() {
        return llTitleBack;
    }

    public TextView getTvTitleText() {
        return tvTitleText;
    }

    public EditText getEtUsername() {
        return etUsername.getEditText();
    }

    public EditText getEtPassword() {
        return etPassword.getEditText();
    }

    public EditText getEtRpPassword() {
        return etRpPassword.getEditText();
    }

    public EditText getEtCaptcha() {
        return etCaptcha.getEditText();
    }

    public ImageView getIvCaptcha() {
        return ivCaptcha;
    }

    public Button getBtRegister() {
        return btRegister;
    }

    public TextView getTvRegisterTip() {
        return tvRegisterTip;
    }

    public CheckBox getCbAgreement() {
        return cbAgreement;
    }

    public TextView getTvAgreement() {
        return tvAgreement;
    }
}
