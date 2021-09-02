package com.impl.recoder.widget;


import static com.impl.recoder.util.ToastUtilKt.toastLongMessage;
import static com.impl.recoder.util.ToastUtilKt.toastShortMessage;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.TextUtils;

import com.impl.recoder.App;
import com.impl.recoder.ConfigKt;
import com.impl.recoder.util.LogUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AudioPlayer {

    private static final String TAG = AudioPlayer.class.getSimpleName();
    private static AudioPlayer sInstance = new AudioPlayer();
    private static String CURRENT_RECORD_FILE = App.instance.getFilesDir() + "/record/" + "auto_";
    private static int MAGIC_NUMBER = 500;
    private static int MIN_RECORD_DURATION = 1000;
    private Callback mRecordCallback;
    private Callback mPlayCallback;

    private String mAudioRecordPath;
    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder;
    private Handler mHandler;
    private int mDuration = 0;

    private AudioPlayer() {
        mHandler = new Handler();
    }

    public static AudioPlayer getInstance() {
        return sInstance;
    }


    public void startRecord(Callback callback) {
        mRecordCallback = callback;
        try {
            mAudioRecordPath = CURRENT_RECORD_FILE + System.currentTimeMillis() + ".m4a";
            mRecorder = new MediaRecorder();
//            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            //所有android系统都支持的适中采样的频率
            mRecorder.setAudioSamplingRate(44100);
            //设置音质频率
            mRecorder.setAudioEncodingBitRate(3000000);
//            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // 使用mp4容器并且后缀改为.m4a，来兼容小程序的播放
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mRecorder.setAudioChannels(2);

            mRecorder.setOutputFile(mAudioRecordPath);
//            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.prepare();
            mRecorder.start();
            // 最大录制时间之后需要停止录制
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopInternalRecord();
                    onRecordCompleted(true);
                    mRecordCallback = null;
                    toastShortMessage("已达到最大语音长度");
                }
            }, ConfigKt.maxTime * 1000);
        } catch (Exception e) {
            LogUtil.d(TAG, "startRecord failed", e);
            stopInternalRecord();
            onRecordCompleted(false);
        }
    }

    public void stopRecord() {
        stopInternalRecord();
        onRecordCompleted(true);
        mRecordCallback = null;
    }

    private void stopInternalRecord() {
        mHandler.removeCallbacksAndMessages(null);
        if (mRecorder == null) {
            return;
        }
        mRecorder.release();
        mRecorder = null;
    }

    public void startPlay(String filePath, Callback callback) {
        mAudioRecordPath = filePath;
        mPlayCallback = callback;
        stopUpdatingCallbackWithPosition(true);
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(filePath);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopInternalPlay();
                    onPlayCompleted(true);
                }
            });
            mPlayer.prepare();
            mPlayer.start();
            mDuration = mPlayer.getDuration();
            startUpdatingCallbackWithPosition();
        } catch (Exception e) {
            LogUtil.d(TAG, "startPlay failed", e);
            toastLongMessage("语音文件已损坏或不存在");
            stopInternalPlay();
            onPlayCompleted(false);
        }
    }

    private PlayVoiceLisenter playVoiceLisenter;

    public void setPlayVoiceLisenter(PlayVoiceLisenter playVoiceLisenter) {
        this.playVoiceLisenter = playVoiceLisenter;
    }

    public void stopPlay() {
        stopInternalPlay();
        onPlayCompleted(false);
        stopUpdatingCallbackWithPosition(false);
        mPlayCallback = null;
    }

    private void stopInternalPlay() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.release();
        stopUpdatingCallbackWithPosition(false);
        mPlayer = null;
    }

    public boolean isPlaying() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    private void onPlayCompleted(boolean success) {
        if (mPlayCallback != null) {
            mPlayCallback.onCompletion(success);
        }
        mPlayer = null;
    }

    private void onRecordCompleted(boolean success) {
        if (mRecordCallback != null) {
            mRecordCallback.onCompletion(success);
        }
        mRecorder = null;
    }

    public String getPath() {
        return mAudioRecordPath;
    }

    public int getDuration() {
        if (TextUtils.isEmpty(mAudioRecordPath)) {
            return 0;
        }
        int duration = 0;
        // 通过初始化播放器的方式来获取真实的音频长度
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(mAudioRecordPath);
            mp.prepare();
            duration = mp.getDuration();
            // 语音长度如果是59s多，因为外部会/1000取整，会一直显示59'，所以这里对长度进行处理，达到四舍五入的效果
            if (duration < MIN_RECORD_DURATION) {
                duration = 0;
            } else {
                duration = duration + MAGIC_NUMBER;
            }
        } catch (Exception e) {
            LogUtil.d(TAG, "getDuration failed", e);
        }
        if (duration < 0) {
            duration = 0;
        }
        return duration;
    }

    public interface Callback {
        void onCompletion(Boolean success);
    }

    private ScheduledExecutorService mExecutor;//开启线程
    public int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 10;
    private Runnable mSeekbarPositionUpdateTask;

    /**
     * 开启线程，获取当前播放的进度
     **/
    private void startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (mSeekbarPositionUpdateTask == null) {
            mSeekbarPositionUpdateTask = new Runnable() {
                @Override
                public void run() {
                    updateProgressCallbackTask();
                }
            };
        }

        mExecutor.scheduleAtFixedRate(
                mSeekbarPositionUpdateTask,
                0,
                PLAYBACK_POSITION_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 播放完成回调监听
     **/
    private void stopUpdatingCallbackWithPosition(boolean resetUIPlaybackPosition) {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
            mSeekbarPositionUpdateTask = null;
            if (resetUIPlaybackPosition && playVoiceLisenter != null) {
                playVoiceLisenter.onProgress(0);
            }
        }
    }

    /**
     * 更新当前的进度
     **/
    private void updateProgressCallbackTask() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            try {
                float currentPosition = mPlayer.getCurrentPosition();
                if (playVoiceLisenter != null) {
                    playVoiceLisenter.onProgress((int) (currentPosition/mDuration*100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
