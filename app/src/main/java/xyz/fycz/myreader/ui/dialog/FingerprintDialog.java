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

package xyz.fycz.myreader.ui.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import javax.crypto.Cipher;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * @author fengyue
 * @date 2021/1/9 14:50
 */
@TargetApi(23)
public class FingerprintDialog extends DialogFragment {
    private FingerprintManager fingerprintManager;

    private CancellationSignal mCancellationSignal;

    private Cipher mCipher;

    private TextView errorMsg;

    private boolean isUnlock;//是否解锁

    private AppCompatActivity mActivity;

    private OnAuthenticated onAuthenticated;

    private OnCancelListener onCancelListener;

    public FingerprintDialog(AppCompatActivity activity, boolean isUnlock, OnAuthenticated onAuthenticated) {
        mActivity = activity;
        this.isUnlock = isUnlock;
        this.onAuthenticated = onAuthenticated;
    }


    /**
     * 标识是否是用户主动取消的认证。
     */
    private boolean isSelfCancelled;

    public void setCipher(Cipher cipher) {
        mCipher = cipher;
    }

    public void setOnCancelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fingerprintManager = getContext().getSystemService(FingerprintManager.class);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.alertDialogTheme);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);
        errorMsg = v.findViewById(R.id.error_msg);
        TextView verifyFingerprint = v.findViewById(R.id.verify_fingerprint);
        verifyFingerprint.setText(isUnlock ? R.string.verify_fingerprint : R.string.verify_has_fingerprint);
        TextView cancel = v.findViewById(R.id.cancel);
        TextView usePwd = v.findViewById(R.id.use_pwd);
        if (isUnlock){
            usePwd.setVisibility(View.VISIBLE);
            usePwd.setOnClickListener(v1 ->{
                dismiss();
                stopListening();
                MyAlertDialog.showPrivatePwdInputDia(mActivity, needGoTo -> {
                    onAuthenticated.onSuccess(needGoTo);
                }, () ->{
                    if (onCancelListener != null) {
                        onCancelListener.cancel();
                    }
                });
            });
        }else {
            usePwd.setVisibility(View.GONE);
        }
        cancel.setOnClickListener(v1 -> {
            dismiss();
            stopListening();
            if (onCancelListener != null) {
                onCancelListener.cancel();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 开始指纹认证监听
        startListening(mCipher);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止指纹认证监听
        stopListening();
    }

    private void startListening(Cipher cipher) {
        isSelfCancelled = false;
        mCancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                if (!isSelfCancelled) {
                    errorMsg.setText(errString);
                    if (errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                        ToastUtils.showError((String) errString);
                        dismiss();
                        if (onCancelListener != null) {
                            onCancelListener.cancel();
                        }
                    }
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                errorMsg.setText(helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                ToastUtils.showSuccess("指纹认证成功");
                onAuthenticated.onSuccess(true);
                dismiss();
            }

            @Override
            public void onAuthenticationFailed() {
                errorMsg.setText("指纹认证失败，请再试一次");
            }
        }, null);
    }

    private void stopListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            isSelfCancelled = true;
        }
    }

    public interface OnAuthenticated{
        void onSuccess(boolean needGoTo);
    }

    public interface OnCancelListener{
        void cancel();
    }
}
