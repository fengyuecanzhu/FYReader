package xyz.fycz.myreader.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.model.audio.ReadAloudService;
import xyz.fycz.myreader.model.audio.ReadService;
import xyz.fycz.myreader.ui.activity.ReadActivity;
import xyz.fycz.myreader.util.DateHelper;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.AudioMngHelper;
import xyz.fycz.myreader.widget.page.PageLoader;

import static xyz.fycz.myreader.util.utils.StringUtils.getString;

public class AudioPlayerDialog extends Dialog {
    private static final String TAG = "AudioPlayerDialog";
    private PageLoader mPageLoader;
    private ReadActivity mReadActivity;
    private boolean aloudNextPage;
    public ReadAloudService.Status aloudStatus = ReadAloudService.Status.STOP;

    private int volume;
    private int pitch;
    private int speechRate;
    private int timer;

    @BindView(R.id.iv_reset_setting)
    TextView tvResetSetting;
    @BindView(R.id.sb_volume_progress)
    SeekBar sbVolume;
    @BindView(R.id.sb_pitch_progress)
    SeekBar sbPitch;
    @BindView(R.id.sb_speech_rate_progress)
    SeekBar sbSpeechRate;
    @BindView(R.id.iv_go_tts_setting)
    AppCompatImageView ivGoTTSSetting;
    @BindView(R.id.iv_read_last_paragraph)
    AppCompatImageView ivReadLastParagraph;
    @BindView(R.id.iv_read_play_stop)
    AppCompatImageView ivReadPlayStop;
    @BindView(R.id.iv_read_next_paragraph)
    AppCompatImageView ivReadNextParagraph;
    @BindView(R.id.iv_read_stop)
    AppCompatImageView ivReadStop;
    @BindView(R.id.iv_read_timer)
    AppCompatImageView ivReadTimer;
    @BindView(R.id.iv_read_home)
    AppCompatImageView ivReadHome;


    public AudioPlayerDialog(@NonNull ReadActivity context, PageLoader mPageLoader) {
        super(context);
        mReadActivity = context;
        this.mPageLoader = mPageLoader;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diallog_audio_player);
        ButterKnife.bind(this);
        setUpWindow();
        initData();
        initWidget();
        readAloud();
    }

    @Override
    protected void onStart() {
        super.onStart();
        volume = AudioMngHelper.getInstance().get100CurrentVolume();
        sbVolume.setProgress(volume);
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
        sbVolume.setProgress(volume);
        sbPitch.setProgress(pitch);
        sbSpeechRate.setProgress(speechRate);
        SeekBarChangeListener seekBarChangeListener = new SeekBarChangeListener();
        sbVolume.setOnSeekBarChangeListener(seekBarChangeListener);
        sbPitch.setOnSeekBarChangeListener(seekBarChangeListener);
        sbSpeechRate.setOnSeekBarChangeListener(seekBarChangeListener);
        tvResetSetting.setOnClickListener(v ->{
            pitch = 10;
            speechRate = 10;
            sbPitch.setProgress(pitch);
            sbSpeechRate.setProgress(speechRate);
            SharedPreUtils.getInstance().putInt("pitch", pitch);
            SharedPreUtils.getInstance().putInt("speechRate", speechRate);
            if (ReadAloudService.running) {
                ReadAloudService.pause(mReadActivity);
                ReadAloudService.resume(mReadActivity);
            }
        });
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
            switch (seekBar.getId()){
                case R.id.sb_volume_progress:
                    AudioMngHelper.getInstance().setVoice100(progress);
                    break;
                case R.id.sb_pitch_progress:
                    SharedPreUtils.getInstance().putInt("readPitch", progress);
                    if (ReadAloudService.running) {
                        ReadAloudService.pause(mReadActivity);
                        ReadAloudService.resume(mReadActivity);
                    }
                    break;
                case R.id.sb_speech_rate_progress:
                    SharedPreUtils.getInstance().putInt("speechRate", progress);
                    if (ReadAloudService.running) {
                        ReadAloudService.pause(mReadActivity);
                        ReadAloudService.resume(mReadActivity);
                    }
                    break;
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

    @OnClick({R.id.iv_read_play_stop, R.id.iv_read_last_paragraph,
            R.id.iv_read_next_paragraph, R.id.iv_go_tts_setting,
            R.id.iv_read_stop, R.id.iv_read_timer, R.id.iv_read_home})
    public void onClick(View view){
        //Log.d("onClick", String.valueOf(view.getId()));
        switch (view.getId()){
            case R.id.iv_go_tts_setting:
                ReadAloudService.toTTSSetting(mReadActivity);
                break;
            case R.id.iv_read_play_stop:
                if (ReadAloudService.running) {
                    if (aloudStatus == ReadAloudService.Status.PLAY) {
                        ReadAloudService.pause(mReadActivity);
                    } else {
                        ReadAloudService.resume(mReadActivity);
                    }
                }else {
                    readAloud();
                }
                break;
            case R.id.iv_read_last_paragraph:
                ReadAloudService.lastP(mReadActivity);
                break;
            case R.id.iv_read_next_paragraph:
                ReadAloudService.nextP(mReadActivity);
                break;
            case R.id.iv_read_timer:
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
                break;
            case R.id.iv_read_stop:
                ReadAloudService.stop(mReadActivity);
                dismiss();
                break;
            case R.id.iv_read_home:
                dismiss();
                mReadActivity.toggleMenu(true, true);
                break;
        }
    }


    public void readAloud() {
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
        sbVolume.setProgress(volume);
        return true;
    }

    private class ReadEvent implements ReadAloudService.ReadEvent {
        @Override
        public void onEventOccur(String tag, Object event) {
            switch (tag){
                case ReadAloudService.READ_ALOUD_START:
                    aloudNextPage = true;
                    if (mPageLoader != null) {
                        mPageLoader.readAloudStart((Integer) event);
                    }
                    break;
                case ReadAloudService.READ_ALOUD_NUMBER:
                    if (mPageLoader != null && aloudNextPage) {
                        mPageLoader.readAloudLength((Integer) event);
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
            if (!MyApplication.isDestroy(mReadActivity)) {
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
                    ivReadPlayStop.setImageResource(R.drawable.ic_stop);
                    break;
                case PAUSE:
                    ivReadPlayStop.setImageResource(R.drawable.ic_play);
                    break;
                default:
                    ivReadPlayStop.setImageResource(R.drawable.ic_play);
                    mPageLoader.skipToPage(mPageLoader.getPagePos());
            }
        }
    }
}
