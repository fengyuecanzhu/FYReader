package xyz.fycz.myreader.ui.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import xyz.fycz.myreader.backup.UserService;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.ui.font.FontsActivity;
import xyz.fycz.myreader.util.CodeUtil;
import xyz.fycz.myreader.util.CyptoUtils;
import xyz.fycz.myreader.util.TextHelper;
import xyz.fycz.myreader.util.utils.NetworkUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fengyue
 * @date 2020/4/26 20:31
 */

public class LoginPresenter implements BasePresenter {
    private LoginActivity mLoginActivity;
    private String code;

    public LoginPresenter(LoginActivity mLoginActivity) {
        this.mLoginActivity = mLoginActivity;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mLoginActivity.getLoginBtn().setEnabled(true);
                    break;
                case 2:
                    createCaptcha();
                    break;
            }
        }
    };

    @Override
    public void start() {
        mHandler.sendMessage(mHandler.obtainMessage(2));
        mLoginActivity.getTvTitleText().setText("登录");
        mLoginActivity.getLlTitleBack().setOnClickListener(v -> mLoginActivity.finish());
        String username = UserService.readUsername();
        mLoginActivity.getUser().setText(username);
        mLoginActivity.getUser().requestFocus(username.length());
        //监听内容改变 -> 控制按钮的点击状态
        mLoginActivity.getUser().addTextChangedListener(mLoginActivity);
        mLoginActivity.getPassword().addTextChangedListener(mLoginActivity);
        mLoginActivity.getEtCaptcha().addTextChangedListener(mLoginActivity);

        mLoginActivity.getIvCaptcha().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }
        });

        mLoginActivity.getLoginBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendMessage(mHandler.obtainMessage(2));
                if (!code.toLowerCase().equals(mLoginActivity.getEtCaptcha().getText().toString().toLowerCase())){
                    DialogCreator.createTipDialog(mLoginActivity, "验证码错误！");
                    return;
                }
                if (!NetworkUtils.isNetWorkAvailable()) {
                    TextHelper.showText("无网络连接！");
                    return;
                }
                mLoginActivity.getLoginBtn().setEnabled(false);
                final String loginName = mLoginActivity.getUser().getText().toString().trim();
                String loginPwd = mLoginActivity.getPassword().getText().toString();
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
                            mLoginActivity.finish();
                        } else {
                            mHandler.sendMessage(mHandler.obtainMessage(1));
                        }
                        TextHelper.showText(resultName);
                    }

                    @Override
                    public void onError(Exception e) {
                        TextHelper.showText("登录失败\n" + e.getLocalizedMessage());
                    }
                });

            }
        });

        mLoginActivity.getTvRegister().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mLoginActivity, RegisterActivity.class);
                mLoginActivity.startActivity(intent);
            }
        });
    }

    public void createCaptcha() {
        code = CodeUtil.getInstance().createCode();
        Bitmap codeBitmap = CodeUtil.getInstance().createBitmap(code);
        mLoginActivity.getIvCaptcha().setImageBitmap(codeBitmap);
    }
}
