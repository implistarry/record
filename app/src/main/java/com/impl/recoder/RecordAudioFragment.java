package com.impl.recoder;

import static com.impl.recoder.util.ToastUtilKt.toastShortMessage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.impl.recoder.recordmp3.AudioRecordDataSource;
import com.impl.recoder.recordmp3.AudioRecordMp3;
import com.impl.recoder.recordmp3.Decibel;
import com.impl.recoder.widget.recordviews.VerticalLineMoveAudioRecordView;
import com.impl.recoder.widget.verticalseekbar.VerticalSeekBar;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.runtime.Permission;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * ClassName: RecordAudioFragment
 * Description:
 */
public class RecordAudioFragment extends BaseAudioRecordFragment implements View.OnClickListener {

    /**
     * 最长录音时间 分钟
     */
    public static final int RECORD_TIME_IN_MINUTES = 5;
    public static final int MAX_RECORD_DECIBEL = 80;
    public static final int MIN_RECORD_DECIBEL = 35;

    /**
     * 最短录制时长 3秒
     */
    public static final int MIN_RECORD_SECOND = 3;
    private RecordStatus recordStatus;
    private RecordCallBack recordCallBack;
    private long recordTimeInMillis;
    private boolean isPermissionsGranted;
    private AudioRecordMp3 audioRecordMp3;
    private float recordDecibel;

    VerticalLineMoveAudioRecordView audioRecordView;
    TextView tvDuration;
    TextView tvDelete;
    TextView tvRecord;
    TextView tvPlay;
    TextView tvComplete;
    TextView tvStatus;
    RelativeLayout setSound;
    ImageView iconSound;
    RelativeLayout verticalSeekBarLayout;
    VerticalSeekBar seekbar;
    ImageView redDot;

    private SimpleExoPlayer simpleExoPlayer;
    private SimpleExoPlayer simpleBGMExoPlayer;
    private long centerLineTime;


    private ArrayList<Decibel> decibelList = new ArrayList<>();


    public static RecordAudioFragment newInstance() {
        return new RecordAudioFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordStatus = RecordStatus.None;
        AndPermission.with(this)
                .runtime()
                .permission(Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.RECORD_AUDIO
                )
                .rationale(new Rationale<List<String>>() {
                    @Override
                    public void showRationale(Context context, List<String> data, RequestExecutor executor) {
                    }
                })
                .onGranted(permissions -> {
                    isPermissionsGranted = true;
                })
                .onDenied(permissions -> {
                    isPermissionsGranted = false;
                    toastShortMessage("未获取到文件读写权限,可能某些功能无法正常使用");
                })
                .start();
        //获取音频服务
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            //打开麦克风
            audioManager.setMicrophoneMute(false);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(getLayoutResId(), container, false);
    }

    protected int getLayoutResId() {
        return R.layout.dialogfragment_record_audio;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Objects.requireNonNull(getView()).setTag(2);
        audioRecordView = view.findViewById(R.id.audio_record_view);
        tvDuration = view.findViewById(R.id.tv_duration);
        tvDelete = view.findViewById(R.id.tv_delete);
        tvRecord = view.findViewById(R.id.tv_record);
        tvPlay = view.findViewById(R.id.tv_play);
        redDot = view.findViewById(R.id.red_dot);
        tvComplete = view.findViewById(R.id.tv_complete);
        tvStatus = view.findViewById(R.id.status);
        verticalSeekBarLayout = view.findViewById(R.id.VerticalSeekBar);
        setSound = view.findViewById(R.id.set_sound);
        iconSound = view.findViewById(R.id.icon_sound);
        seekbar = view.findViewById(R.id.seekbar);

        tvDelete.setOnClickListener(this);
        tvRecord.setOnClickListener(this);
        tvPlay.setOnClickListener(this);
        setSound.setOnClickListener(this);
        initView();
    }


    @Override
    public void onResume() {
        super.onResume();
        audioRecordView.setRecordCallBack(recordCallBack);
    }

    @Override
    public void onPause() {
        super.onPause();
        audioRecordMp3.stopRecord();
        audioRecordView.stopPlayRecord();
        audioRecordView.setRecordCallBack(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioRecordView != null) {
            audioRecordView.setRecordCallBack(null);
        }
        if (simpleExoPlayer != null) {
            stopPlay();
            simpleExoPlayer.release();
        }
        if (simpleBGMExoPlayer != null) {
            simpleBGMExoPlayer.stop();
            simpleBGMExoPlayer.release();
        }
    }

    private boolean isSonudOn = false;

    protected void initView() {
        initAudioRecorder();
        initPlayer();

        initBgmPlayer();

        initListener();
        renderUIByRecordStatus();
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                simpleBGMExoPlayer.setVolume(progress / 100f);
                if (progress == 0) {
                    isSonudOn = false;
                    iconSound.setImageResource(R.mipmap.sound_off);
                } else {
                    if (!isSonudOn) {
                        isSonudOn = true;
                        iconSound.setImageResource(R.mipmap.sound_on);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onClick(View view) {
//        if (FastClickLimitUtil.isFastClick()) {
//            return;
//        }
        int viewId = view.getId();
        if (viewId == R.id.tv_delete) {
            //删除
            AudioRecordDataSource.getInstance().deleteRecordFile();
            recordStatus = RecordStatus.None;
            audioRecordMp3.onRelease();
            audioRecordView.reset();
            renderUIByRecordStatus();
        } else if (viewId == R.id.tv_record) {
            //录音
            switch (recordStatus) {
                case None:
                case PauseRecording:
                case PausePlaying:
                    startRecord();
                    break;
                case Recording:
                    stopRecord();
                    break;
                case Playing:
                    audioRecordView.stopPlayRecord();
                    startRecord();
                    break;
                default:
                    break;
            }
        } else if (viewId == R.id.tv_play) {
            //播放
            switch (recordStatus) {
                case None:
                case Recording:
                    break;
                case PauseRecording:
                case PausePlaying:
                case FinishRecording:
                    //小于300毫秒从头开始播放
                    if (Math.abs(centerLineTime - recordTimeInMillis) < 300) {
                        centerLineTime = 0;
                    }
                    audioRecordView.startPlayRecord(centerLineTime);
                    break;
                case Playing:
                    audioRecordView.stopPlayRecord();
                    break;
                default:
                    break;
            }
        } else if (viewId == R.id.set_sound) {
            verticalSeekBarLayout.setVisibility(verticalSeekBarLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }


    public void renderUIByRecordStatus() {
        if (tvDelete == null || tvPlay == null || tvRecord == null) {
            return;
        }

        tvRecord.setEnabled(recordTimeInMillis <= TimeUnit.MINUTES.toMillis(RECORD_TIME_IN_MINUTES) - 300);
        tvDelete.setEnabled(true);
        tvPlay.setEnabled(true);

        switch (recordStatus) {
            case None:
                tvRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_record_continue, 0, 0);
                tvRecord.setText("点击录制");
                tvDelete.setVisibility(View.INVISIBLE);
                tvComplete.setVisibility(View.INVISIBLE);
                tvPlay.setVisibility(View.GONE);
                redDot.setVisibility(View.GONE);
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("点击录制");
                tvDuration.setText("00:00");
                break;
            case Recording:
                audioRecordView.setVisibility(View.VISIBLE);
                tvRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.record_start, 0, 0);
                tvRecord.setText("点击暂停");
                tvPlay.setVisibility(View.GONE);
                redDot.setVisibility(View.VISIBLE);
                tvDelete.setVisibility(View.INVISIBLE);
                tvComplete.setVisibility(View.INVISIBLE);
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("正在录制");
//                tvPlay.setEnabled(false);
//                tvDelete.setEnabled(false);
                break;
            case PausePlaying:
            case PauseRecording:
                tvRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_record_continue, 0, 0);
                tvRecord.setText("继续录制");
                tvPlay.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.try_lisen, 0, 0, 0);
                tvDelete.setVisibility(View.VISIBLE);
                tvComplete.setVisibility(View.VISIBLE);
                tvPlay.setVisibility(View.VISIBLE);
                redDot.setVisibility(View.GONE);
                tvStatus.setVisibility(View.GONE);
                break;
            case Playing:
//                tvRecord.setEnabled(false);
//                tvDelete.setEnabled(false);
                tvPlay.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.try_lisenting, 0, 0, 0);
                tvDelete.setVisibility(View.VISIBLE);
                tvPlay.setVisibility(View.VISIBLE);
                redDot.setVisibility(View.GONE);
                tvStatus.setVisibility(View.GONE);
                break;
            case FinishRecording:
                tvRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.selector_record_continue, 0, 0);
//                tvRecord.setEnabled(false);
                break;
        }
        setCanUseAudio();
    }

    private void setCanUseAudio() {
        if (fragmentInteraction != null) {
            boolean status = recordStatus != RecordStatus.Recording;
            boolean length = TimeUnit.SECONDS.toMillis(MIN_RECORD_SECOND) < recordTimeInMillis;
            fragmentInteraction.canUseAudio(status && length);
        }
    }


    private void initAudioRecorder() {

        audioRecordMp3 = new AudioRecordMp3(AudioRecordDataSource.getInstance().getRecordFile(), new AudioRecordMp3.RecordMp3Listener() {
            @Override
            public void onStartRecord() {
                audioRecordView.startRecord();
                recordStatus = RecordStatus.Recording;
                renderUIByRecordStatus();
            }

            @Override
            public void onStopRecord() {
                if (audioRecordView == null) {
                    return;
                }
                audioRecordView.stopRecord();
                recordStatus = RecordStatus.PauseRecording;
                renderUIByRecordStatus();
                setCanUseAudio();
            }

            @Override
            public void onDeletedLastRecord() {
                audioRecordView.deleteLastRecord();
            }

            @Override
            public void onRecordDecibel(float decibel) {
                recordDecibel = decibel;
            }

        });
    }


    private void initPlayer() {
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(getContext()).build();//new DefaultBandwidthMeter();
        ExoTrackSelection.Factory videoTackSelectionFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(getContext(), videoTackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        //2. 创建ExoPlayer
        simpleExoPlayer = new SimpleExoPlayer.Builder(getContext())
                .setBandwidthMeter(bandwidthMeter)
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .build();
    }

    private void initListener() {
        recordCallBack = new RecordCallBack() {

            @Override
            public float getSamplePercent() {
                double percent;
                if (recordDecibel >= MAX_RECORD_DECIBEL) {
                    percent = 1f;
                } else if (recordDecibel <= MIN_RECORD_DECIBEL) {
                    percent = 0.01f;
                } else {
                    int max = MAX_RECORD_DECIBEL - MIN_RECORD_DECIBEL;
                    percent = (recordDecibel - MIN_RECORD_DECIBEL) / max;
                }
                BigDecimal bd = new BigDecimal(percent);
                decibelList.add(new Decibel(bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()));
                return (float) percent;
            }

            @Override
            public void onScroll(long centerStartTimeMillis) {

            }

            @Override
            public void onRecordCurrent(long centerStartTimeMillis, long recordTimeInMillis) {
                RecordAudioFragment.this.recordTimeInMillis = recordTimeInMillis;
                if (tvDuration != null) {
                    tvDuration.setText(formatDuration(TimeUnit.MILLISECONDS.toSeconds(recordTimeInMillis)));
                }
            }

            @Override
            public void onFinishPlayingRecord() {
                recordStatus = RecordStatus.PausePlaying;
                centerLineTime = 0;
                renderUIByRecordStatus();
            }

            @Override
            public void onCenterLineTime(long centerLineTime) {
                RecordAudioFragment.this.centerLineTime = centerLineTime;

            }

            @Override
            public void onStartRecord() {
            }

            @Override
            public void onStopRecord() {
            }

            @Override
            public void onFinishRecord() {
                recordStatus = RecordStatus.FinishRecording;
                renderUIByRecordStatus();
                setCanUseAudio();
                if (tvDuration != null) {
                    tvDuration.setText(formatDuration(TimeUnit.MINUTES.toSeconds(RECORD_TIME_IN_MINUTES)));
                }
            }

            @Override
            public void onStartPlayRecord(long timeMillis) {
                recordStatus = RecordStatus.Playing;
                play(timeMillis);
                renderUIByRecordStatus();
            }

            @Override
            public void onStopPlayRecode() {
                recordStatus = RecordStatus.PausePlaying;
                stopPlay();
                renderUIByRecordStatus();
            }

        };
    }

    public static String formatDuration(long seconds) {
        if (seconds != 0 && seconds % 3600 == 0) {
            return String.format(Locale.getDefault(),
                    "%02d:%02d:%02d",
                    seconds % 3600,
                    (seconds % 3600) / 60,
                    seconds % 60);
        } else {
            return String.format(Locale.getDefault(),
                    "%02d:%02d",
                    (seconds % 3600) / 60,
                    seconds % 60);
        }
    }

    @SuppressLint("CheckResult")
    private void startRecord() {
        if (!isPermissionsGranted) {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.READ_EXTERNAL_STORAGE,
                            Permission.WRITE_EXTERNAL_STORAGE,
                            Permission.RECORD_AUDIO
                    )
                    .rationale(new Rationale<List<String>>() {
                        @Override
                        public void showRationale(Context context, List<String> data, RequestExecutor executor) {
                        }
                    })
                    .onGranted(permissions -> {
                        isPermissionsGranted = true;
                    })
                    .onDenied(permissions -> {
                        isPermissionsGranted = false;
                       toastShortMessage("未获取到文件读写权限,可能某些功能无法正常使用");
                    })
                    .start();
        } else {
            audioRecordMp3.startAudioRecord();
        }

    }


    private void stopRecord() {
        audioRecordMp3.stopRecord();
    }

    public void play(long timeMillis) {
        if (timeMillis < 0) {
            timeMillis = 0;
        }
        // MediaSource代表要播放的媒体。
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(new FileDataSourceFactory()).createMediaSource(Uri.fromFile(AudioRecordDataSource.getInstance().getRecordFile()));
        //Prepare the player with the source.
        simpleExoPlayer.prepare(mediaSource);
        simpleExoPlayer.seekTo(timeMillis);
        simpleExoPlayer.setPlayWhenReady(true);

    }

    private void stopPlay() {
        simpleExoPlayer.stop();
    }


    /**
     * 录音采样频率
     *
     * @return 采样频率
     */
    public int getRecordSamplingFrequency() {
        return audioRecordView.getRecordSamplingFrequency();
    }

    /**
     * 录音时长
     *
     * @return 录音时长
     */
    public long getRecordTimeInMillis() {
        return recordTimeInMillis;
    }

    /**
     * 获取采样分贝点
     *
     * @return 采样分贝点
     */
    public ArrayList<Decibel> getDecibelList() {
        return decibelList;
    }

    /**
     * 点击返回按钮
     */
    public void onFinishRelease() {
        audioRecordView.setRecordCallBack(null);
        stopRecord();
        audioRecordView.reset();
        audioRecordMp3.onRelease();
        //删除
        AudioRecordDataSource.getInstance().onRelease();
    }

    public boolean hasRecorded() {
        //删除
        return recordTimeInMillis > 0;
    }

    FragmentInteraction fragmentInteraction;

    public void setFragmentInteraction(FragmentInteraction fragmentInteraction) {
        this.fragmentInteraction = fragmentInteraction;
    }

    public interface FragmentInteraction {
        /**
         * 音频是否可用
         *
         * @param canUseAudio 是否可用
         */
        void canUseAudio(boolean canUseAudio);
    }

    private void initBgmPlayer() {
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(getContext()).build();//new DefaultBandwidthMeter();
        ExoTrackSelection.Factory videoTackSelectionFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(getContext(), videoTackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        //2. 创建ExoPlayer
        simpleBGMExoPlayer = new SimpleExoPlayer.Builder(getContext())
                .setBandwidthMeter(bandwidthMeter)
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                getActivity(),
                Util.getUserAgent(getActivity(), App.instance.getPackageName())
        );
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse("https://vv-app.oss-cn-beijing.aliyuncs.com/%E4%BD%A0%E6%9B%BE%E6%98%AF%E5%B0%91%E5%B9%B4.mp3"));
        simpleBGMExoPlayer.setVolume(1);
        simpleBGMExoPlayer.prepare(mediaSource);
        simpleBGMExoPlayer.setPlayWhenReady(true);
    }
}
