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

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import androidx.annotation.NonNull;


import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.databinding.DialogAudioPlayerBinding;
import xyz.fycz.myreader.model.audio.ReadAloudService;
import xyz.fycz.myreader.ui.activity.ReadActivity;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.AudioMngHelper;
import xyz.fycz.myreader.widget.page.PageLoader;

public class AudioPlayerDialog extends Dialog{
    private static final String TAG="AudioPlayerDialog";

    private DialogAudioPlayerBinding binding;

    private PageLoader mPageLoader;
    private ReadActivity mReadActivity;
    private boolean aloudNextPage;
    public ReadAloudService.Status aloudStatus = ReadAloudService.Status.STOP;
    private Handler mHandler = new Handler();

    private int volume;
    private int pitch;
    private int speechRate;
    private int timer;

    public AudioPlayerDialog(@NonNull ReadActivity context, PageLoader mPageLoader) {
        super(context);
        mReadActivity = context;
        this.mPageLoader = mPageLoader;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogAudioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpWindow();
        initData();
        initWidget();
        initCLick();
        readAloud();
    }

    @Override
    protected void onStart() {
        super.onStart();
        volume = AudioMngHelper.getInstance().get100CurrentVolume();
        binding.sbVolumeProgress.setProgress(volume);
        if (!ReadAloudService.running){
            readAloud();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ReadAloudService.running){
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return addOrSubVolume(true);
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return addOrSubVolume(false);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    private void initData(){
        volume = AudioMngHelper.getInstance().get100CurrentVolume();
        pitch = SharedPreUtils.getInstance().getInt("readPitch", 10);
        speechRate = SharedPreUtils.getInstance().getInt("speechRate", 10);
        timer = SharedPreUtils.getInstance().getInt("timer", 10);
        AudioMngHelper.getInstance().setVoiceStep100(5);
    }

    private void initWidget() {
        binding.sbVolumeProgress.setProgress(volume);
        binding.sbPitchProgress.setProgress(pitch);
        binding.sbSpeechRateProgress.setProgress(speechRate);
        SeekBarChangeListener seekBarChangeListener = new SeekBarChangeListener();
        binding.sbVolumeProgress.setOnSeekBarChangeListener(seekBarChangeListener);
        binding.sbPitchProgress.setOnSeekBarChangeListener(seekBarChangeListener);
        binding.sbSpeechRateProgress.setOnSeekBarChangeListener(seekBarChangeListener);
        binding.ivResetSetting.setOnClickListener(v ->{
            pitch = 10;
            speechRate = 10;
            binding.sbPitchProgress.setProgress(pitch);
            binding.sbSpeechRateProgress.setProgress(speechRate);
            SharedPreUtils.getInstance().putInt("readPitch", pitch);
            SharedPreUtils.getInstance().putInt("speechRate", speechRate);
            if (ReadAloudService.running) {
                ReadAloudService.pause(mReadActivity);
                ReadAloudService.resume(mReadActivity);
            }
        });
    }

    private void initCLick() {
        binding.ivReadPlayStop.setOnClickListener(this::onClick);
        binding.ivReadLastParagraph.setOnClickListener(this::onClick);
        binding.ivReadNextParagraph.setOnClickListener(this::onClick);
        binding.ivGoTtsSetting.setOnClickListener(this::onClick);
        binding.ivReadStop.setOnClickListener(this::onClick);
        binding.ivReadTimer.setOnClickListener(this::onClick);
        binding.ivReadHome.setOnClickListener(this::onClick);
    }

    public void onClick(View view){
        int id = view.getId();
        if (id == R.id.iv_go_tts_setting) {
            ReadAloudService.toTTSSetting(mReadActivity);
        } else if (id == R.id.iv_read_play_stop) {
            if (ReadAloudService.running) {
                if (aloudStatus == ReadAloudService.Status.PLAY) {
                    ReadAloudService.pause(mReadActivity);
                } else {
                    ReadAloudService.resume(mReadActivity);
                }
            } else {
                readAloud();
            }
        } else if (id == R.id.iv_read_last_paragraph) {
            ReadAloudService.lastP(mReadActivity);
        } else if (id == R.id.iv_read_next_paragraph) {
            ReadAloudService.nextP(mReadActivity);
        } else if (id == R.id.iv_read_timer) {
            View timer = LayoutInflater.from(getContext()).inflate(R.layout.dialog_hour_minute_picker, null, false);
            NumberPicker hourPicker = timer.findViewById(R.id.hour_picker);
            int hour = this.timer / 60;
            int minute = this.timer % 60;
            hourPicker.setMaxValue(5);
            hourPicker.setMinValue(0);
            hourPicker.setValue(hour);
            NumberPicker minutePicker = timer.findViewById(R.id.minute_picker);
            minutePicker.setMaxValue(59);
            minutePicker.setMinValue(0);
            minutePicker.setValue(minute);
            MyAlertDialog.build(mReadActivity)
                    .setTitle("定时停止")
                    .setView(timer)
                    .setPositiveButton("确定", (dialog, which) -> {
                        this.timer = hourPicker.getValue() * 60 + minutePicker.getValue();
                        SharedPreUtils.getInstance().putInt("timer", this.timer);
                        ReadAloudService.setTimer(mReadActivity, this.timer);
                        ToastUtils.showInfo("朗读将在" + hourPicker.getValue() + "时" + minutePicker.getValue() + "分钟后停止！");
                    }).setNegativeButton("取消", null)
                    .show();
        } else if (id == R.id.iv_read_stop) {
            ReadAloudService.stop(mReadActivity);
            dismiss();
        } else if (id == R.id.iv_read_home) {
            dismiss();
            mReadActivity.toggleMenu(true, true);
        }
    }

    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            int id = seekBar.getId();
            if (id == R.id.sb_volume_progress) {
                AudioMngHelper.getInstance().setVoice100(progress);
            } else if (id == R.id.sb_pitch_progress) {
                SharedPreUtils.getInstance().putInt("readPitch", progress);
                if (ReadAloudService.running) {
                    ReadAloudService.pause(mReadActivity);
                    ReadAloudService.resume(mReadActivity);
                }
            } else if (id == R.id.sb_speech_rate_progress) {
                SharedPreUtils.getInstance().putInt("speechRate", progress);
                if (ReadAloudService.running) {
                    ReadAloudService.pause(mReadActivity);
                    ReadAloudService.resume(mReadActivity);
                }
            }
        }
    }

    /**
     * 设置Dialog显示的位置
     */
    private void setUpWindow() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = getContext().getResources().getDisplayMetrics().widthPixels;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.dialogWindowAnim;
        lp.dimAmount = 0f;
        window.setAttributes(lp);
        window.getDecorView().setBackgroundColor(getContext().getResources().getColor(R.color.read_menu_bg));
    }


    public void readAloud() {
        mPageLoader.resetReadAloudParagraph();
        aloudNextPage = false;
        String unReadContent = mPageLoader.getUnReadContent();
        ReadAloudService.setReadEvent(new ReadEvent());
        ReadAloudService.play(getContext(), unReadContent,
                mPageLoader.getCollBook().getName(),
                mPageLoader.getChapterCategory().get(mPageLoader.getChapterPos()).getTitle(),
                mPageLoader.getPagePos());
    }

    public boolean addOrSubVolume(boolean isAdd){
        volume = isAdd ? AudioMngHelper.getInstance().addVoice100() : AudioMngHelper.getInstance().subVoice100();
        binding.sbVolumeProgress.setProgress(volume);
        return true;
    }

    private class ReadEvent implements ReadAloudService.ReadEvent {
        @Override
        public void onEventOccur(String tag, Object event) {
            switch (tag){
                case ReadAloudService.READ_ALOUD_START:
                    aloudNextPage = true;
                    if (mPageLoader != null) {
                        mHandler.post(() -> mPageLoader.readAloudStart((Integer) event));
                    }
                    break;
                case ReadAloudService.READ_ALOUD_NUMBER:
                    if (mPageLoader != null && aloudNextPage) {
                        mHandler.post(() ->  mPageLoader.readAloudLength((Integer) event));
                    }
                    break;
                case ReadAloudService.ALOUD_STATE:
                    updateAloudState((ReadAloudService.Status) event);
                    break;
                case ReadAloudService.ALOUD_TIMER:
                    break;

            }
        }

        private void updateAloudState(ReadAloudService.Status status) {
            aloudStatus = status;
            if (!App.isDestroy(mReadActivity)) {
                mReadActivity.autoPageStop();
            }
            switch (status) {
                case NEXT:
                    if (mPageLoader == null) {
                        ReadAloudService.stop(mReadActivity);
                        break;
                    }
                    if (!mPageLoader.skipNextChapter()) {
                        ReadAloudService.stop(mReadActivity);
                    }
                    break;
                case PLAY:
                    binding.ivReadPlayStop.setImageResource(R.drawable.ic_stop);
                    break;
                case PAUSE:
                    binding.ivReadPlayStop.setImageResource(R.drawable.ic_play);
                    break;
                default:
                    binding.ivReadPlayStop.setImageResource(R.drawable.ic_play);
                    mPageLoader.skipToPage(mPageLoader.getPagePos());
            }
        }
    }
}
