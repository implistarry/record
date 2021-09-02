package com.impl.recoder;

import static com.impl.recoder.util.ToastUtilKt.toastShortMessage;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.impl.recoder.recordmp3.AudioRecordDataSource;
import com.impl.recoder.util.ToastUtilKt;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.List;

/**
 * ClassName: BaseAudioRecordFragment
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-11-12  15:11
 */
public abstract class BaseAudioRecordFragment extends Fragment {
    enum RecordStatus {
        /**
         *
         */
        None,
        Recording,
        PauseRecording,
        FinishRecording,
        Playing,
        PausePlaying,;
    }

     boolean isPermissionsGranted;

      RecordStatus recordStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        recordStatus = RecordStatus.None;

        //获取音频服务
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            //打开麦克风
            audioManager.setMicrophoneMute(false);
        }

        iniFile();
    }

    private void iniFile() {
        AudioRecordDataSource.getInstance().init(getContext());
        AudioRecordDataSource.getInstance().initRecordFile();
        AudioRecordDataSource.getInstance().initNewVersionCropOutputFile();
    }
}
