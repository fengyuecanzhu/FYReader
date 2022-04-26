/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.DialogLoadingBinding;
import xyz.fycz.myreader.util.ToastUtils;

import static org.jetbrains.anko.AnkoContextKt.setContentView;

/**
 * @author fengyue
 * @date 2021/4/22 17:19
 */
public class LoadingDialog extends Dialog {

    private static final String TAG = "LoadingDialog";

    private DialogLoadingBinding binding;

    private String mMessage;
    //private int mImageId;
    private boolean mCancelable;
    private RotateAnimation mRotateAnimation;
    private long cancelTime;
    private OnCancelListener mOnCancelListener;

    public LoadingDialog(@NonNull Context context, String message, OnCancelListener onCancelListener) {
        this(context, R.style.LoadingDialog, message, false, onCancelListener);
    }

    public LoadingDialog(@NonNull Context context, int themeResId, String message, boolean cancelable, OnCancelListener onCancelListener) {
        super(context, themeResId);
        mMessage = message;
        //mImageId = imageId;
        mCancelable = cancelable;
        mOnCancelListener = onCancelListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        // 设置窗口大小
        WindowManager windowManager = getWindow().getWindowManager();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha = 0.3f;
        attributes.width = screenWidth / 3;
        attributes.height = attributes.width;
        getWindow().setAttributes(attributes);
        setCancelable(mCancelable);

        binding.tvLoading.setText(mMessage);
        //iv_loading.setImageResource(mImageId);
        binding.ivLoading.measure(0, 0);
        mRotateAnimation = new RotateAnimation(0, 360, binding.ivLoading.getMeasuredWidth() / 2f,
                binding.ivLoading.getMeasuredHeight() / 2f);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setDuration(1000);
        mRotateAnimation.setRepeatCount(-1);
    }

    public void setmMessage(String mMessage) {
        this.mMessage = mMessage;
        binding.tvLoading.setText(mMessage);
    }

    @Override
    public void show() {
        super.show();
        binding.ivLoading.startAnimation(mRotateAnimation);
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            mRotateAnimation.cancel();
            super.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (mCancelable) {
            super.onBackPressed();
            dismiss();
            mOnCancelListener.cancel();
        } else {
            if (System.currentTimeMillis() - cancelTime > APPCONST.exitConfirmTime) {
                ToastUtils.showInfo("再按一次取消");
                cancelTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
                dismiss();
                mOnCancelListener.cancel();
            }
        }
    }


    public interface OnCancelListener {
        void cancel();
    }
}
