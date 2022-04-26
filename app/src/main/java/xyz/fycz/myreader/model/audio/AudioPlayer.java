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

package xyz.fycz.myreader.model.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class AudioPlayer {
    private static String TAG = "AudioPlayer";
    private final int SAMPLE_RATE = 16000;
    private boolean playing = false;
    private LinkedBlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue();

    // 初始化播放器
    private int iMinBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

    private AudioTrack audioTrack;
    private byte[] tempData;

    private Thread ttsPlayerThread;


    AudioPlayer(){
        Log.i(TAG,"init...");
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO
                , AudioFormat.ENCODING_PCM_16BIT,
                iMinBufSize*10, AudioTrack.MODE_STREAM);
        playing = true;
        ttsPlayerThread = new Thread(() -> {
            while (playing) {
                tempData = audioQueue.poll();
                if (tempData == null) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                        Log.d(TAG, "audioTrack.play");
                        audioTrack.play();
                    }
                    Log.d(TAG, "audioTrack.write");
                    audioTrack.write(tempData, 0, tempData.length);
                }
            }
            Log.d(TAG, "released!");
        });
        ttsPlayerThread.start();
    }

    /**
     * 设置要播放的音频数据
     * @param data 音频数组
     */
    public void setAudioData(byte[] data){
        Log.d(TAG, "data enqueue.");
        audioQueue.offer(data);
        //非阻塞
    }

    /**
     * 停止播放，并释放资源，此对象无法再用来播放
     */
    public void stop(){
        release();
        stopPlay();
    }

    /**
     * 释放资源，此对象无法再用来播放，
     */
    public void release() {
        playing = false;
        Log.d(TAG, "releasing...");
    }

    /**
     * 停止当前播放，并清空未播放的数据。当再用setAudioData()设置新数据时才会播放
     */
    public void stopPlay() {
        audioQueue.clear();
        audioTrack.pause();
        audioTrack.flush();
        Log.d(TAG, "paused.");
    }

}
