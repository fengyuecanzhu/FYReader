package xyz.fycz.myreader.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.interfaces.OnBindView;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.service.BookGroupService;
import xyz.fycz.myreader.util.CyptoUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.FingerprintUtils;

/**
 * @author fengyue
 * @date 2020/9/20 9:48
 */
public class MyAlertDialog {
    public static AlertDialog.Builder build(Context context) {
        return new AlertDialog.Builder(context, R.style.alertDialogTheme);
    }

    public static AlertDialog createInputDia(Context context, String title, String hint, String initText,
                                             Integer inputType, boolean cancelable, int maxLen, onInputChangeListener oic,
                                             DialogInterface.OnClickListener posListener) {
        return createInputDia(context, title, hint, initText, inputType, cancelable, maxLen, oic, posListener, null, null, null);
    }

    public static AlertDialog createInputDia(Context context, String title, String hint, String initText,
                                             Integer inputType, boolean cancelable, int maxLen, onInputChangeListener oic,
                                             DialogInterface.OnClickListener posListener,
                                             DialogInterface.OnClickListener negListener, String neutralBtn,
                                             DialogInterface.OnClickListener neutralListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.edit_dialog, null);
        TextInputLayout textInputLayout = view.findViewById(R.id.text_input_lay);

        textInputLayout.setCounterMaxLength(maxLen);
        EditText editText = textInputLayout.getEditText();
        editText.setHint(hint);
        if (inputType != null) editText.setInputType(inputType);
        if (!StringHelper.isEmpty(initText)) editText.setText(initText);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        App.getHandler().postDelayed(() -> imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED), 220);
        AlertDialog inputDia;
        if (neutralBtn == null) {
            inputDia = build(context)
                    .setTitle(title)
                    .setView(view)
                    .setCancelable(cancelable)
                    .setPositiveButton("确认", (dialog, which) -> {
                        posListener.onClick(dialog, which);
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        if (negListener != null) {
                            negListener.onClick(dialog, which);
                        }
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .show();
        } else {
            inputDia = build(context)
                    .setTitle(title)
                    .setView(view)
                    .setCancelable(cancelable)
                    .setNeutralButton(neutralBtn, (dialog, which) -> {
                        neutralListener.onClick(dialog, which);
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .setPositiveButton("确认", (dialog, which) -> {
                        posListener.onClick(dialog, which);
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        if (negListener != null) {
                            negListener.onClick(dialog, which);
                        }
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .show();
        }
        Button posBtn = inputDia.getButton(AlertDialog.BUTTON_POSITIVE);
        posBtn.setEnabled(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = editText.getText().toString();
                if (editText.getText().length() > 0 && editText.getText().length() <= maxLen && !text.equals(initText)) {
                    posBtn.setEnabled(true);
                } else {
                    posBtn.setEnabled(false);
                }
                oic.onChange(text);
            }
        });
        return inputDia;
    }

    public static AlertDialog createInputDia(Context context, String title, String hint, String initText,
                                             boolean cancelable, int maxLen, onInputChangeListener oic,
                                             DialogInterface.OnClickListener posListener) {
        return createInputDia(context, title, hint, initText, InputType.TYPE_CLASS_TEXT, cancelable, maxLen, oic, posListener);
    }


    /**
     * 验证隐私密码对话框
     *
     * @param activity
     * @param onVerify
     */
    public static void showPrivateVerifyDia(AppCompatActivity activity, OnVerify onVerify) {
        showPrivateVerifyDia(activity, onVerify, null);
    }
    public static void showPrivateVerifyDia(AppCompatActivity activity, OnVerify onVerify, OnCancel onCancel) {
        boolean openPrivate = SharedPreUtils.getInstance().getBoolean("openPrivate");
        boolean openFingerprint = SharedPreUtils.getInstance().getBoolean("openFingerprint");
        if (openPrivate) {
            if (openFingerprint) {
                FingerprintDialog fd = new FingerprintDialog(activity, true, onVerify::success);
                fd.setCancelable(false);
                fd.setCipher(FingerprintUtils.initCipher());
                fd.setOnCancelListener(() -> {
                    if (onCancel != null) {
                        onCancel.cancel();
                    }
                });
                if (!App.isDestroy(activity))
                    fd.show(activity.getSupportFragmentManager(), "fingerprint");
            } else {
                showPrivatePwdInputDia(activity, onVerify, onCancel);
            }
        } else {
            onVerify.success(false);
        }
    }


    /**
     * 输入隐私密码对话框
     * @param activity
     * @param onVerify
     */
    public static void showPrivatePwdInputDia(AppCompatActivity activity, OnVerify onVerify){
        showPrivatePwdInputDia(activity, onVerify, null);
    }
    public static void showPrivatePwdInputDia(AppCompatActivity activity, OnVerify onVerify, OnCancel onCancel){
        final String[] pwd = new String[1];
        String pwds = SharedPreUtils.getInstance().getString("privatePwd");
        MyAlertDialog.createInputDia(activity, activity.getString(R.string.input_private_pwd),
                "", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                true, 12,
                text -> pwd[0] = text,
                (dialog, which) -> {
                    String pwde = CyptoUtils.encode(APPCONST.KEY, pwd[0]);
                    if (pwde.equals(pwds)) {
                        onVerify.success(true);
                    } else {
                        ToastUtils.showError("密码错误");
                        if (onCancel != null) {
                            onCancel.cancel();
                        }
                    }
                }, (dialog, which) -> {
                    if (onCancel != null) {
                        onCancel.cancel();
                    }
                },"忘记密码", (dialog, which) -> {
                    DialogCreator.createCommonDialog(activity, "忘记密码",
                            "忘记密码无法找回！\n您可点击确定按钮关闭私密书架并删除私密书架所有书籍，确定关闭吗？",
                            false, (dialog1, which1) -> {
                                BookGroupService.getInstance().deletePrivateGroup();
                                SharedPreUtils.getInstance().putBoolean("openPrivate", false);
                                SharedPreUtils.getInstance().putBoolean("openFingerprint", false);
                                SharedPreUtils.getInstance().putString("privatePwd", "");
                                ToastUtils.showSuccess("私密书架已关闭");
                                onVerify.success(false);
                            }, (dialog1, which1) -> {
                                if (onCancel != null) {
                                    onCancel.cancel();
                                }
                            });
                });
    }

    public static void showTipDialogWithLink(Context context, int msgId){
        showTipDialogWithLink(context,"提示", msgId);
    }
    public static void showTipDialogWithLink(Context context, String title, int msgId){
        /*TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.dialog_textview, null);
        view.setText(msgId);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        build(context).setTitle(title).setView(view).setPositiveButton("知道了", null).show();*/
        BottomDialog.show(title, new OnBindView<BottomDialog>(R.layout.dialog_textview){
            @Override
            public void onBind(BottomDialog dialog, View v) {
                TextView view = (TextView) v;
                view.setText(msgId);
                view.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }).setCancelButton("知道了");
    }

    public interface OnVerify {
        void success(boolean needGoTo);
    }

    public interface OnCancel {
        void cancel();
    }

    public interface onInputChangeListener {
        void onChange(String text);
    }
}
