package xyz.fycz.myreader.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.FullScreenDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.kongzue.dialogx.interfaces.OnBindView;

import java.net.URISyntaxException;
import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.service.BookGroupService;
import xyz.fycz.myreader.util.CyptoUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.StatusBarUtil;
import xyz.fycz.myreader.util.download.DownloadUtil;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.FingerprintUtils;

import static android.content.Context.DOWNLOAD_SERVICE;

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
                                             boolean cancelable, int maxLen, onInputChangeListener oic,
                                             DialogInterface.OnClickListener posListener) {
        return createInputDia(context, title, hint, initText, InputType.TYPE_CLASS_TEXT, cancelable, maxLen, oic, posListener);
    }

    public static AlertDialog createInputDia(Context context, String title, String hint, String initText,
                                             Integer inputType, boolean cancelable, int maxLen, onInputChangeListener oic,
                                             DialogInterface.OnClickListener posListener,
                                             DialogInterface.OnClickListener negListener, String neutralBtn,
                                             DialogInterface.OnClickListener neutralListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.edit_text, null);
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
                oic.onChange(text);
            }
        });
        return inputDia;
    }


    public static void showFullInputDia(Context context, String title, String hint, String initText,
                                        Integer inputType, boolean cancelable, int maxLen,
                                        OnInputFinishListener posListener) {

        FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.dialog_input) {
            @Override
            public void onBind(FullScreenDialog dialog, View view) {
                TextView cancelBtn = view.findViewById(R.id.btn_cancel);
                TextView finishBtn = view.findViewById(R.id.btn_finish);
                TextView tvTitle = view.findViewById(R.id.tv_title);
                TextInputLayout textInputLayout = view.findViewById(R.id.text_input_lay);
                EditText editText = textInputLayout.getEditText();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

                tvTitle.setText(title);
                cancelBtn.setOnClickListener(v -> {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    dialog.dismiss();
                });
                finishBtn.setOnClickListener(v -> {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    posListener.finish(editText.getText().toString());
                    dialog.dismiss();
                });
                textInputLayout.setCounterMaxLength(maxLen);
                editText.setHint(hint);
                if (inputType != null) editText.setInputType(inputType);
                if (!StringHelper.isEmpty(initText)) editText.setText(initText);
                editText.requestFocus();
                App.getHandler().postDelayed(() -> imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED), 220);
                finishBtn.setEnabled(false);
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
                            finishBtn.setEnabled(true);
                        } else {
                            finishBtn.setEnabled(false);
                        }
                    }
                });
            }
        }).setCancelable(cancelable);
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
     *
     * @param activity
     * @param onVerify
     */
    public static void showPrivatePwdInputDia(AppCompatActivity activity, OnVerify onVerify) {
        showPrivatePwdInputDia(activity, onVerify, null);
    }

    public static void showPrivatePwdInputDia(AppCompatActivity activity, OnVerify onVerify, OnCancel onCancel) {
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
                }, "忘记密码", (dialog, which) -> {
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

    public static void showTipDialogWithLink(Context context, int msgId) {
        showTipDialogWithLink(context, "提示", msgId);
    }

    public static void showTipDialogWithLink(Context context, String title, int msgId) {
        /*TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.dialog_textview, null);
        view.setText(msgId);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        build(context).setTitle(title).setView(view).setPositiveButton("知道了", null).show();*/
        BottomDialog.show(title, new OnBindView<BottomDialog>(R.layout.dialog_textview) {
            @Override
            public void onBind(BottomDialog dialog, View v) {
                TextView view = (TextView) v;
                view.setText(msgId);
                view.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }).setCancelButton("知道了");
    }

    public static void showTipDialogWithLink(Context context, String title, String msg){
        BottomDialog.show(title, new OnBindView<BottomDialog>(R.layout.dialog_textview) {
            @Override
            public void onBind(BottomDialog dialog, View v) {
                TextView view = (TextView) v;
                view.setText(msg);
                view.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }).setCancelButton("取消");
    }

    public static void showPrivacyDialog(Context context, DialogInterface.OnClickListener pos, DialogInterface.OnClickListener neg) {
        TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.dialog_textview, null);
        String msg = context.getString(R.string.privacy_tip);
        int start = msg.indexOf("《隐私政策》");
        SpannableString span = new SpannableString(msg);
        AlertDialog dialog = build(context).setTitle("风月读书APP隐私政策")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("同意并继续", pos)
                .setNegativeButton("不同意", neg)
                .create();
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showFullWebViewDia(context, "file:///android_asset/PrivacyPolicy.html", false, () -> showPrivacyDialog(context, pos, neg));
                dialog.dismiss();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, start, start + 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setText(span);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        dialog.show();
    }

    public static void showFullWebViewDia(Context context, String url, boolean outBrowser, OnCancel onCancel) {
        FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.dialog_full_webview) {
            @Override
            public void onBind(final FullScreenDialog dialog, View v) {
                TextView btnBrowser = v.findViewById(R.id.btn_browser);
                TextView btnClose = v.findViewById(R.id.btn_close);
                TextView tvTitle = v.findViewById(R.id.tv_title);
                WebView webView = v.findViewById(R.id.webView);
                ProgressBar pbLoad = v.findViewById(R.id.pb_load);
                btnClose.setVisibility(outBrowser ? View.VISIBLE : View.GONE);
                btnClose.setOnClickListener(v1 -> {
                    if (onCancel != null) onCancel.cancel();
                    dialog.dismiss();
                });

                btnBrowser.setText(outBrowser ? R.string.browser : R.string.close);
                btnBrowser.setOnClickListener(v1 -> {
                    if (outBrowser) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(webView.getUrl()));
                        context.startActivity(intent);
                    } else {
                        if (onCancel != null) onCancel.cancel();
                        dialog.dismiss();
                    }
                });

                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setSupportZoom(true);
                webSettings.setAllowFileAccess(true);
                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                webSettings.setLoadsImagesAutomatically(true);
                webSettings.setDefaultTextEncodingName("utf-8");

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        try {
                            //url
                            if (url.startsWith("intent://")) {
                                Intent intent;
                                try {
                                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                    intent.addCategory("android.intent.category.BROWSABLE");
                                    intent.setComponent(null);
                                    intent.setSelector(null);
                                    List<ResolveInfo> resolves = context.getPackageManager().queryIntentActivities(intent,0);
                                    if(resolves.size()>0){
                                        context.startActivity(intent);
                                    }
                                    return true;
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                }
                            }
                            // 处理自定义scheme协议
                            if (!url.startsWith("http")) {
                                try {
                                    // 以下固定写法
                                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(url));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    // 防止没有安装的情况
                                    e.printStackTrace();
                                    ToastUtils.showError("您所打开的第三方App未安装！");
                                }
                                return true;
                            }
                            view.loadUrl(url);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                });

                webView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onProgressChanged(WebView view, int newProgress) {
                        if (newProgress < 100) {
                            pbLoad.setVisibility(View.VISIBLE);
                            pbLoad.setProgress(newProgress);
                        } else {
                            pbLoad.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        tvTitle.setText(title);
                    }

                });

                webView.setDownloadListener((url1, userAgent, contentDisposition, mimetype, contentLength) -> {
                    DownloadUtil.downloadBySystem(context, url1, contentDisposition, mimetype);
                });

                webView.loadUrl(url);
            }
        }).setOnBackPressedListener(() -> {
            if (onCancel != null) onCancel.cancel();
            return false;
        });
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

    public interface OnInputFinishListener {
        void finish(String text);
    }


}
