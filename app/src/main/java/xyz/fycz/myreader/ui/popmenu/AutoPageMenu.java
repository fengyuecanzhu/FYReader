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

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.databinding.MenuAutoPageBinding;
import xyz.fycz.myreader.entity.Setting;

public class AutoPageMenu extends FrameLayout {

    private MenuAutoPageBinding binding;

    public AutoPageMenu(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AutoPageMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AutoPageMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = MenuAutoPageBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setListener(Callback callback) {
        Setting setting = SysManager.getSetting();
        binding.sbAutoScrollProgress.setProgress(110 - setting.getAutoScrollSpeed());
        binding.tvAutoScrollSpeed.setText(String.format("翻页速度：%s %%", 110 - setting.getAutoScrollSpeed()));
        binding.sbAutoScrollProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.tvAutoScrollSpeed.setText(String.format("翻页速度：%s %%", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                int speed = 110 - progress;
                setting.setAutoScrollSpeed(speed);
                SysManager.saveSetting(setting);
                callback.onSpeedChange();
            }
        });
        binding.tvExitAutoPage.setOnClickListener(v -> callback.onExitClick());
    }

    public void setNavigationBarHeight(int height) {
        binding.vwNavigationBar.getLayoutParams().height = height;
    }

    public interface Callback{
        void onSpeedChange();
        void onExitClick();
    }
}
