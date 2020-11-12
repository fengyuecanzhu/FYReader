package xyz.fycz.myreader.model.audio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import xyz.fycz.myreader.util.ToastUtils;

import static android.text.TextUtils.isEmpty;

public class ReadService extends Service {
    private static final String TAG = "ReadService";
    public static Boolean running = false;
    public static final String ActionNewRead = "newRead";
    private Boolean speak = true;
    private Boolean pause = false;
    private List<String> contentList = new ArrayList<>();
    private int nowSpeak;
    private int speechRate;
    private String title;
    private String text;
    private int readAloudNumber;
    private int progress;
    private Boolean ttsInitSuccess = false;
    private TextToSpeech textToSpeech;
    private HashMap mParams;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ActionNewRead:
                        newReadAloud(intent.getStringExtra("content"),
                                intent.getStringExtra("title"),
                                intent.getStringExtra("text"),
                                intent.getIntExtra("progress", 0));
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initTTS() {
        if (textToSpeech == null)
            textToSpeech = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.CHINA);
                    textToSpeech.setOnUtteranceProgressListener(new ttsUtteranceListener());
                    ttsInitSuccess = true;
                    playTTS();
                } else {
                    ToastUtils.showError("TTS初始化失败！");
                    ReadService.this.stopSelf();
                }
            });
        if (mParams == null) {
            mParams = new HashMap();
            mParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM, "3");
        }
    }

    /**
     * 朗读监听
     */
    private class ttsUtteranceListener extends UtteranceProgressListener {

        @Override
        public void onStart(String s) {
            //updateMediaSessionPlaybackState();
            //RxBus.get().post(RxBusTag.READ_ALOUD_START, readAloudNumber + 1);
            //RxBus.get().post(RxBusTag.READ_ALOUD_NUMBER, readAloudNumber + 1);
        }

        @Override
        public void onDone(String s) {
            readAloudNumber = readAloudNumber + contentList.get(nowSpeak).length() + 1;
            nowSpeak = nowSpeak + 1;
            if (nowSpeak >= contentList.size()) {
                //RxBus.get().post(RxBusTag.ALOUD_STATE, Status.NEXT);
            }
        }

        @Override
        public void onError(String s) {
            //pauseReadAloud(true);
            //RxBus.get().post(RxBusTag.ALOUD_STATE, Status.PAUSE);
        }

        @Override
        public void onRangeStart(String utteranceId, int start, int end, int frame) {
            super.onRangeStart(utteranceId, start, end, frame);
            //RxBus.get().post(RxBusTag.READ_ALOUD_NUMBER, readAloudNumber + start);
        }
    }
    /**
     * 朗读
     */
    public static void play(Context context, String content, String title, String text, int progress) {
        Intent readAloudIntent = new Intent(context, ReadService.class);
        readAloudIntent.setAction(ActionNewRead);
        readAloudIntent.putExtra("content", content);
        readAloudIntent.putExtra("title", title);
        readAloudIntent.putExtra("text", text);
        readAloudIntent.putExtra("progress", progress);
        context.startService(readAloudIntent);
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

    private void playTTS() {
        if (contentList.size() < 1) {
            //RxBus.get().post(RxBusTag.ALOUD_STATE, Status.NEXT);
            return;
        }
        if (ttsInitSuccess && !speak) {
            speak = !speak;
            //RxBus.get().post(RxBusTag.ALOUD_STATE, Status.PLAY);
            //updateNotification();
            //initSpeechRate();
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


    public enum Status {
        PLAY, STOP, PAUSE, NEXT
    }
}
