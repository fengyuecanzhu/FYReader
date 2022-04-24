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

package xyz.fycz.myreader.ui.popmenu;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.databinding.MenuBrightnessEyeBinding;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.util.BrightUtil;

public class BrightnessEyeMenu extends FrameLayout {

    private MenuBrightnessEyeBinding binding;

    private Callback callback;

    private Activity context;

    private Setting setting = SysManager.getSetting();

    public BrightnessEyeMenu(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BrightnessEyeMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BrightnessEyeMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = MenuBrightnessEyeBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setListener(Activity activity, Callback callback) {
        this.context = activity;
        this.callback = callback;
        initListener();
    }

    public void initWidget() {
        binding.sbBrightnessProgress.setProgress(setting.getBrightProgress());
        binding.cbFollowSys.setChecked(setting.isBrightFollowSystem());
        binding.sbProtectEye.setProgress(setting.getBlueFilterPercent() - 10);
        binding.cbProtectEye.setChecked(setting.isProtectEye());
        binding.sbProtectEye.setEnabled(binding.cbProtectEye.isChecked());
    }

    private void initListener() {
        //设置亮度
        binding.sbBrightnessProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    BrightUtil.setBrightness((AppCompatActivity) context, progress);
                    binding.cbFollowSys.setChecked(false);
                    setting.setBrightProgress(progress);
                    setting.setBrightFollowSystem(false);
                    SysManager.saveSetting(setting);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //亮度跟随系统
        binding.cbFollowSys.setOnClickListener(v -> {
            binding.cbFollowSys.setChecked(!binding.cbFollowSys.isChecked());
            if (binding.cbFollowSys.isChecked()) {
                BrightUtil.followSystemBright((AppCompatActivity) context);
                setting.setBrightFollowSystem(true);
            } else {
                BrightUtil.setBrightness((AppCompatActivity) context, setting.getBrightProgress());
                setting.setBrightFollowSystem(false);
            }
            SysManager.saveSetting(setting);
        });

        //设置蓝光过滤
        binding.sbProtectEye.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setting.setBlueFilterPercent(progress + 10);
                    SysManager.saveSetting(setting);
                    callback.upProtectEye();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //是否开启护眼
        binding.cbProtectEye.setOnClickListener(v -> {
            binding.cbProtectEye.setChecked(!binding.cbProtectEye.isChecked());
            binding.sbProtectEye.setEnabled(binding.cbProtectEye.isChecked());
            setting.setProtectEye(binding.cbProtectEye.isChecked());
            SysManager.saveSetting(setting);
            callback.onProtectEyeChange();
        });
    }

    public void setNavigationBarHeight(int height) {
        binding.vwNavigationBar.getLayoutParams().height = height;
    }

    public interface Callback {
        void onProtectEyeChange();

        void upProtectEye();
    }
}
