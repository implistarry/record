package com.impl.recoder.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.impl.recoder.R;
import com.impl.recoder.util.LogUtil;


public class RecordAudioView extends AppCompatButton {

    private static final String TAG = "RecordAudioView";

    private Context context;
    private IRecordAudioListener recordAudioListener;
    private AudioRecordManager audioRecordManager;
    private boolean isCanceled;
    private float downPointY;
    private static final float DEFAULT_SLIDE_HEIGHT_CANCEL = 150;
    private boolean isRecording;


    public RecordAudioView(Context context) {
        super(context);
        initView(context);
    }

    public RecordAudioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RecordAudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        audioRecordManager = AudioRecordManager.getInstance();
    }

    public void resetStatus() {
        isHasRecorded = false;
        isPlaying = false;
        recordPath = null;
        setBackgroundResource(R.drawable.ar_record_audio_record_btn_selector_vv);
        setSelected(false);
    }

    boolean isRecordWithoutPress = false;
    boolean isHasRecorded = false;
    boolean isPlaying = false;
    String recordPath;

    public void setIsRecordWithoutPress(boolean isRecordWithoutPress) {
        this.isRecordWithoutPress = isRecordWithoutPress;
        if (isRecordWithoutPress) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isHasRecorded) {
                        if (!isRecording) {
                            setBackgroundResource(R.drawable.ar_record_audio_record_btn_selector_vv);
                            setSelected(true);
                            startRecordAudio();
                        } else {
                            stopRecordAudio();
                        }
                    } else {
                        //????????????,??????/??????
                        if (!isPlaying) {
                            if (recordAudioListener != null) {
                                isPlaying = true;
                                recordAudioListener.onRecordPlaying();
                                setBackgroundResource(R.mipmap.chat_audio_record_playing);
                                AudioPlayer.getInstance().startPlay(recordPath, new AudioPlayer.Callback() {
                                    @Override
                                    public void onCompletion(Boolean success) {
                                        if (AudioPlayer.getInstance().isPlaying())
                                            AudioPlayer.getInstance().stopPlay();
                                        recordAudioListener.onRecordPlayingFinish();
                                        setBackgroundResource(R.mipmap.chat_audio_record_finish);
                                        isPlaying = false;
                                    }
                                });
                            }
                        } else {
                            AudioPlayer.getInstance().stopPlay();
                            recordAudioListener.onRecordPlayingFinish();
                            setBackgroundResource(R.mipmap.chat_audio_record_finish);
                            isPlaying = false;
                        }

                    }
                }
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.d(TAG, "onTouchEvent");
        super.onTouchEvent(event);
        if (recordAudioListener != null) {
            //????????????
            if (!isRecordWithoutPress) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setSelected(true);
                        downPointY = event.getY();
                        recordAudioListener.onFingerPress();
                        startRecordAudio();
                        break;
                    case MotionEvent.ACTION_UP:
                        setSelected(false);
                        onFingerUp();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        onFingerMove(event);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        isCanceled = true;
                        onFingerUp();
                        break;
                    default:

                        break;
                }
            }
        }
        return true;
    }

    /**
     * ????????????,????????????????????????????????????????????????
     */
    private void onFingerUp() {
        if (isRecording) {
            if (isCanceled) {
                isRecording = false;
                audioRecordManager.cancelRecord();
                recordAudioListener.onRecordCancel();
            } else {
                stopRecordAudio();
            }
        }
    }

    private void onFingerMove(MotionEvent event) {
        float currentY = event.getY();
        isCanceled = checkCancel(currentY);
        if (isCanceled) {
            recordAudioListener.onSlideTop();
        } else {
            recordAudioListener.onFingerPress();
        }
    }

    private boolean checkCancel(float currentY) {
        return downPointY - currentY >= DEFAULT_SLIDE_HEIGHT_CANCEL;
    }

    /**
     * ????????????ready??????,????????????ready???????????????
     */
    private void startRecordAudio() throws RuntimeException {
        boolean isPrepare = recordAudioListener.onRecordPrepare();
        if (isPrepare) {
            String audioFileName = recordPath = recordAudioListener.onRecordStart();
            LogUtil.d(TAG, "startRecordAudio() has prepared.");
            //????????????????????????
            try {
                audioRecordManager.init(audioFileName);
                audioRecordManager.startRecord();
                isRecording = true;
            } catch (Exception e) {
                this.recordAudioListener.onRecordCancel();
            }
        }
    }

    /**
     * ????????????
     */
    private void stopRecordAudio() throws RuntimeException {
        if (isRecording) {
            LogUtil.d(TAG, "stopRecordAudio()");
            try {
                isRecording = false;
                audioRecordManager.stopRecord();
                if (isRecordWithoutPress) {
                    this.recordAudioListener.onRecordFinish();
                    isHasRecorded = true;
                    setBackgroundResource(R.mipmap.chat_audio_record_finish);
                } else {
                    this.recordAudioListener.onRecordStop();
                }
            } catch (Exception e) {
                this.recordAudioListener.onRecordCancel();
            }
        }
    }

    /**
     * ????????????IRecordAudioStatus,??????????????????????????????????????????,????????????????????????
     *
     * @param recordAudioListener
     */
    public void setRecordAudioListener(IRecordAudioListener recordAudioListener) {
        this.recordAudioListener = recordAudioListener;
    }

    public void invokeStop() {
        onFingerUp();
    }

    public interface IRecordAudioListener {
        boolean onRecordPrepare();

        String onRecordStart();

        /**
         * ?????????????????????
         *
         * @return
         */
        boolean onRecordFinish();

        void onRecordPlaying();

        void onRecordPlayingFinish();

        /**
         * ?????????????????????
         *
         * @return
         */
        boolean onRecordStop();

        boolean onRecordCancel();

        void onSlideTop();

        void onFingerPress();
    }
}
