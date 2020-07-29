package xyz.fycz.myreader.ui.user;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.backup.UserService;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.util.AppInfoUtils;
import xyz.fycz.myreader.util.CodeUtil;
import xyz.fycz.myreader.util.CyptoUtils;
import xyz.fycz.myreader.util.TextHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fengyue
 * @date 2020/4/26 20:31
 */

public class RegisterPresenter implements BasePresenter {
    private RegisterActivity mRegisterActivity;
    private String code;
    private String username = "";
    private String password = "";
    private String rpPassword = "";
    private String inputCode = "";

    public RegisterPresenter(RegisterActivity mRegisterActivity) {
        this.mRegisterActivity = mRegisterActivity;
    }

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
                    mRegisterActivity.getTvRegisterTip().setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    public void start() {
        mHandler.sendMessage(mHandler.obtainMessage(1));
        mRegisterActivity.getTvTitleText().setText("注册");
        mRegisterActivity.getEtUsername().requestFocus();
        mRegisterActivity.getLlTitleBack().setOnClickListener(v -> mRegisterActivity.finish());

        mRegisterActivity.getEtUsername().addTextChangedListener(new TextWatcher() {
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

        mRegisterActivity.getEtPassword().addTextChangedListener(new TextWatcher() {
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

        mRegisterActivity.getEtRpPassword().addTextChangedListener(new TextWatcher() {
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

        mRegisterActivity.getEtCaptcha().addTextChangedListener(new TextWatcher() {
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

        mRegisterActivity.getIvCaptcha().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendMessage(mHandler.obtainMessage(1));
            }
        });

        mRegisterActivity.getBtRegister().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!username.matches("^[A-Za-z][A-Za-z0-9]{5,13}$")){
                    DialogCreator.createTipDialog(mRegisterActivity, "用户名格式错误",
                            "用户名必须在6-14位之间\n用户名只能以字母开头\n用户名只能由数字、字母、下划线、减号组成");
                }else if(password.matches("^\\d+$") || !password.matches("^.{8,16}$")){
                    DialogCreator.createTipDialog(mRegisterActivity, "密码格式错误",
                            "密码必须在8-16位之间\n密码不能是纯数字");
                }else if(!password.equals(rpPassword)){
                    DialogCreator.createTipDialog(mRegisterActivity, "重复密码错误",
                            "两次输入的密码不一致");
                }else if(!inputCode.trim().toLowerCase().equals(code.toLowerCase())){
                    DialogCreator.createTipDialog(mRegisterActivity, "验证码错误");
                }else if(!mRegisterActivity.getCbAgreement().isChecked()){
                    DialogCreator.createTipDialog(mRegisterActivity, "请勾选同意《用户服务协议》");
                }else {
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
                                mRegisterActivity.finish();
                            }
                            TextHelper.showText(info[1]);
                        }
                        @Override
                        public void onError(Exception e) {
                            TextHelper.showText("注册失败：\n" + e.getLocalizedMessage());
                        }
                    });
                }
                mHandler.sendMessage(mHandler.obtainMessage(1));
            }
        });

        mRegisterActivity.getTvAgreement().setMovementMethod(LinkMovementMethod.getInstance());
    }


    public void createCaptcha() {
        code = CodeUtil.getInstance().createCode();
        Bitmap codeBitmap = CodeUtil.getInstance().createBitmap(code);
        mRegisterActivity.getIvCaptcha().setImageBitmap(codeBitmap);
    }

    public void showTip(String tip) {
        mRegisterActivity.getTvRegisterTip().setVisibility(View.VISIBLE);
        mRegisterActivity.getTvRegisterTip().setText(tip);
    }

    public void checkNotNone(){
        if ("".equals(username) || "".equals(password) || "".equals(rpPassword) || "".equals(inputCode)){
            mRegisterActivity.getBtRegister().setEnabled(false);
        }else {
            mRegisterActivity.getBtRegister().setEnabled(true);
        }
    }

}
