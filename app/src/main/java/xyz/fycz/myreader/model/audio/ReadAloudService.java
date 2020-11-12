package xyz.fycz.myreader.model.audio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.activity.ReadActivity;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/2.
 * 朗读服务
 */
public class ReadAloudService extends Service {
    private static final String TAG = ReadAloudService.class.getSimpleName();
    public static final String ActionStartService = "startService";
    public static final String ActionDoneService = "doneService";
    public static final String ActionNewReadAloud = "newReadAloud";
    public static final String ActionPauseService = "pauseService";
    public static final String ActionResumeService = "resumeService";
    public static final String ActionLastPService = "lastPService";
    public static final String ActionNextPService = "nextPService";
    private static final String ActionReadActivity = "readActivity";
    private static final String ActionSetTimer = "updateTimer";
    private static final String ActionSetProgress = "setProgress";
    private static final String ActionUITimerStop = "UITimerStop";
    private static final String ActionUITimerRemaining = "UITimerRemaining";
    public final static String ALOUD_STATE = "aloud_state";
    public final static String ALOUD_TIMER = "aloud_timer";
    public final static String READ_ALOUD_NUMBER = "readAloudNumber";
    public final static String READ_ALOUD_START = "readAloudStart";
    private static final int notificationId = 3222;
    public static final int maxTimeMinute = 360;
    public static Boolean running = false;
    private TextToSpeech textToSpeech;
    private TextToSpeech textToSpeech_ui;
    private HashMap mParams;
    private Boolean ttsInitSuccess = false;
    private Boolean speak = true;
    private Boolean pause = false;
    private List<String> contentList = new ArrayList<>();
    private int nowSpeak;
    private static int timeMinute = 0;
    private boolean timerEnable = false;
    private BroadcastReceiver broadcastReceiver;
    private SharedPreferences preference;
    private int speechRate;
    private int pitch;
    private String title;
    private String text;
    private boolean fadeTts;
    private Handler handler = new Handler();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable dsRunnable;
    private int readAloudNumber;
    private int progress;
    private static ReadEvent mReadEvent;

    /**
     * 朗读
     */
    public static void play(Context context, String content, String title, String text, int progress) {
        Intent readAloudIntent = new Intent(context, ReadAloudService.class);
        readAloudIntent.setAction(ActionNewReadAloud);
        readAloudIntent.putExtra("content", content);
        readAloudIntent.putExtra("title", title);
        readAloudIntent.putExtra("text", text);
        readAloudIntent.putExtra("progress", progress);
        context.startService(readAloudIntent);
    }

    /**
     * @param context 停止
     */
    public static void stop(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionDoneService);
            context.startService(intent);
        }
    }

    /**
     * @param context 暂停
     */
    public static void pause(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionPauseService);
            context.startService(intent);
        }
    }

    /**
     * @param context 继续
     */
    public static void resume(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionResumeService);
            context.startService(intent);
        }
    }

    /**
     * @param context 上一段
     */
    public static void lastP(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionLastPService);
            context.startService(intent);
        }
    }

    /**
     * @param context 下一段
     */
    public static void nextP(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionNextPService);
            context.startService(intent);
        }
    }

    public static void setTimer(Context context, int minute) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionSetTimer);
            intent.putExtra("minute", minute);
            context.startService(intent);
        }
    }

    public static void setProgress(Context context, int progress) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionSetProgress);
            intent.putExtra("progress", progress);
            context.startService(intent);
        }
    }

    public static void tts_ui_timer_stop(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionUITimerStop);
            context.startService(intent);
        }
    }

    public static void tts_ui_timer_remaining(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionUITimerRemaining);
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        preference = SharedPreUtils.getInstance().getSharedReadable();
        fadeTts = preference.getBoolean("fadeTTS", false);
        dsRunnable = this::doDs;
        initBroadcastReceiver();
        updateNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                String sText;
                switch (action) {
                    case ActionDoneService:
                        stopSelf();
                        break;
                    case ActionPauseService:
                        pauseReadAloud(true);
                        break;
                    case ActionResumeService:
                        resumeReadAloud();
                        break;
                    case ActionSetTimer:
                        updateTimer(intent.getIntExtra("minute", 10));
                        break;
                    case ActionNewReadAloud:
                        newReadAloud(intent.getStringExtra("content"),
                                intent.getStringExtra("title"),
                                intent.getStringExtra("text"),
                                intent.getIntExtra("progress", 0));
                        break;
                    case ActionUITimerStop:
                        sText = getString(R.string.read_aloud_timerstop);
                        textToSpeech_ui.speak(sText,TextToSpeech.QUEUE_FLUSH, mParams);
                        break;
                    case ActionUITimerRemaining:
                        if (timeMinute > 0 && timeMinute <= maxTimeMinute) {
                            if (timeMinute<=60) {
                                sText = getString(R.string.read_aloud_timerremaining, timeMinute);
                            }
                            else {
                                int hours = timeMinute / 60;
                                int minutes = timeMinute % 60;
                                sText = getString(R.string.read_aloud_timerremaininglong, hours, minutes);
                            }
                        } else {
                            sText = getString(R.string.read_aloud_timerstop);
                        }
                        pauseReadAloud(false);
                        textToSpeech_ui.speak(sText,TextToSpeech.QUEUE_FLUSH, mParams);
                        resumeReadAloud();
                        break;
                    case ActionLastPService:
                        lastReadAloud();
                        break;
                    case ActionNextPService:
                        nextReadAloud();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initTTS() {
        if (textToSpeech == null)
            textToSpeech = new TextToSpeech(MyApplication.getmContext(), new TTSListener());
        if (textToSpeech_ui == null)
            textToSpeech_ui = new TextToSpeech(MyApplication.getmContext(), new TTSUIListener());
        if (mParams == null) {
            mParams = new HashMap();
            mParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM, "3");
        }
    }

    private void newReadAloud(String content, String title, String text, int progress) {
        if (TextUtils.isEmpty(content)) {
            stopSelf();
            return;
        }
        this.text = text;
        this.title = title;
        this.progress = progress;
        nowSpeak = 0;
        readAloudNumber = 0;
        contentList.clear();
        initTTS();
        String[] splitSpeech = content.split("\n");
        for (String aSplitSpeech : splitSpeech) {
            if (!isEmpty(aSplitSpeech)) {
                contentList.add(aSplitSpeech);
            }
        }
        if (speak) {
            speak = false;
            pause = false;
            playTTS();
        }
    }

    public void playTTS() {
        updateNotification();
        if (fadeTts) {
            handler.postDelayed(this::playTTSN, 200);
        } else {
            playTTSN();
        }
    }

    public void playTTSN() {
        if (contentList.size() < 1) {
            postEvent(ALOUD_STATE, Status.NEXT);
            return;
        }
        if (ttsInitSuccess && !speak) {
            speak = !speak;
            postEvent(ALOUD_STATE, Status.PLAY);
            updateNotification();
            initSpeechRateAndPitch();
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "content");
            for (int i = nowSpeak; i < contentList.size(); i++) {
                if (i == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_FLUSH, null, "content");
                    } else {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_FLUSH, map);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_ADD, null, "content");
                    } else {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_ADD, map);
                    }
                }
            }
        }
    }

    public static void toTTSSetting(Context context) {
        //跳转到文字转语音设置界面
        try {
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    private void initSpeechRateAndPitch() {
        if (speechRate != preference.getInt("speechRate", 10)) {
            speechRate = preference.getInt("speechRate", 10);
            float speechRateF = (float) speechRate / 10;
            textToSpeech.setSpeechRate(speechRateF);
        }
        if (pitch != preference.getInt("readPitch", 10)) {
            pitch = preference.getInt("readPitch", 10);
            float pitchF = (float) pitch / 10;
            textToSpeech.setPitch(pitchF);
        }
    }

    /**
     * @param pause true 暂停, false 失去焦点
     */
    private void pauseReadAloud(Boolean pause) {
        this.pause = pause;
        speak = false;
        updateNotification();
        if (fadeTts) {
            handler.postDelayed(() -> textToSpeech.stop(), 300);
        } else {
            textToSpeech.stop();
        }
        postEvent(ALOUD_STATE, Status.PAUSE);
    }

    /**
     * 恢复朗读
     */
    private void resumeReadAloud() {
        updateTimer(0);
        pause = false;
        updateNotification();

        playTTS();

        postEvent(ALOUD_STATE, Status.PLAY);
    }

    /**
     * 上一段
     */
    private void lastReadAloud(){
        if (nowSpeak > 0) {
            pauseReadAloud(true);
            nowSpeak--;
            readAloudNumber -= contentList.get(nowSpeak).length() - 1;
            resumeReadAloud();
        }
    }

    /**
     * 下一段
     */
    private void nextReadAloud(){
        if (nowSpeak < contentList.size() - 1) {
            pauseReadAloud(true);
            readAloudNumber += contentList.get(nowSpeak).length() + 1;
            nowSpeak++;
            resumeReadAloud();
        }
    }


    private void updateTimer(int minute) {
        timeMinute = minute;
        if (timeMinute > maxTimeMinute) {
            timerEnable = false;
            handler.removeCallbacks(dsRunnable);
            timeMinute = 0;
            updateNotification();
        } else if (timeMinute <= 0) {
            if (timerEnable) {
                handler.removeCallbacks(dsRunnable);
                stopSelf();
            }
        } else {
            timerEnable = true;
            updateNotification();
            handler.removeCallbacks(dsRunnable);
            handler.postDelayed(dsRunnable, 60000);
        }
    }

    private void doDs() {
        if (!pause) {
            timeMinute--;
            if (timeMinute == 0) {
                stopSelf();
            } else if (timeMinute > 0) {
                handler.postDelayed(dsRunnable, 60000);
            }
        }
        updateNotification();
    }

    private PendingIntent getReadBookActivityPendingIntent() {
        Intent intent = new Intent(this, ReadActivity.class);
        intent.setAction(ReadAloudService.ActionReadActivity);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 更新通知
     */
    private void updateNotification() {
        if (text == null)
            text = getString(R.string.read_aloud_s);
        String nTitle;
        if (pause) {
            nTitle = getString(R.string.read_aloud_pause);
        } else if (timeMinute > 0 && timeMinute <= maxTimeMinute) {
            if (timeMinute<=60) {
                nTitle = getString(R.string.read_aloud_timer, timeMinute);
            }
            else {
                int hours = timeMinute / 60;
                int minutes = timeMinute % 60;
                nTitle = getString(R.string.read_aloud_timerlong, hours, minutes);
            }
        } else {
            nTitle = getString(R.string.read_aloud_t);
        }
        nTitle += ": " + title;
        postEvent(ALOUD_TIMER, nTitle);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, APPCONST.channelIdRead)
                .setSmallIcon(R.drawable.ic_volume_up)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_read))
                .setOngoing(true)
                .setContentTitle(nTitle)
                .setContentText(text)
                .setContentIntent(getReadBookActivityPendingIntent());
        builder.addAction(R.drawable.ic_last, getString(R.string.last), getThisServicePendingIntent(ActionLastPService));
        if (pause) {
            builder.addAction(R.drawable.ic_play_24dp, getString(R.string.resume), getThisServicePendingIntent(ActionResumeService));
        } else {
            builder.addAction(R.drawable.ic_pause_24dp, getString(R.string.pause), getThisServicePendingIntent(ActionPauseService));
        }
        builder.addAction(R.drawable.ic_next, getString(R.string.next), getThisServicePendingIntent(ActionNextPService));

        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), getThisServicePendingIntent(ActionDoneService));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2, 3));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        startForeground(notificationId, notification);
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
        stopForeground(true);
        handler.removeCallbacks(dsRunnable);
        postEvent(ALOUD_STATE, Status.STOP);
        unregisterReceiver(broadcastReceiver);
        clearTTS();
    }

    private void clearTTS() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        if (textToSpeech_ui != null) {
            textToSpeech_ui.stop();
            textToSpeech_ui.shutdown();
            textToSpeech_ui = null;
        }
    }


    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                    pauseReadAloud(true);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    private final class TTSListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int i) {
            if (i == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.CHINA);
                textToSpeech.setOnUtteranceProgressListener(new ttsUtteranceListener());
                ttsInitSuccess = true;
                playTTS();
            } else {
                ToastUtils.showError("TTS初始化失败！");
                ReadAloudService.this.stopSelf();
            }
        }
    }

    private final class TTSUIListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int i) {
            if (i == TextToSpeech.SUCCESS) {
                textToSpeech_ui.setLanguage(Locale.CHINA);
            }
        }
    }

    /**
     * 朗读监听
     */
    private class ttsUtteranceListener extends UtteranceProgressListener {

        @Override
        public void onStart(String s) {
            postEvent(READ_ALOUD_START, readAloudNumber + 1);
            postEvent(READ_ALOUD_NUMBER, readAloudNumber + 1);
        }

        @Override
        public void onDone(String s) {
            readAloudNumber = readAloudNumber + contentList.get(nowSpeak).length() + 1;
            nowSpeak = nowSpeak + 1;
            if (nowSpeak >= contentList.size()) {
                postEvent(ALOUD_STATE, Status.NEXT);
            }
        }

        @Override
        public void onError(String s) {
            pauseReadAloud(true);
            postEvent(ALOUD_STATE, Status.PAUSE);
        }

        @Override
        public void onRangeStart(String utteranceId, int start, int end, int frame) {
            super.onRangeStart(utteranceId, start, end, frame);
            postEvent(READ_ALOUD_NUMBER, readAloudNumber + start);
        }
    }


    private void postEvent(String tag, Object event){
        if (mReadEvent != null) {
            mReadEvent.onEventOccur(tag, event);
        }
    }

    public static void setReadEvent(ReadEvent readEvent){
        mReadEvent = readEvent;
    }

    public enum Status {
        PLAY, STOP, PAUSE, NEXT
    }

    public interface ReadEvent{
        void onEventOccur(String tag, Object event);
    }
}
