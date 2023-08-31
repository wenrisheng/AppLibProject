package com.wrs.module.audiorecord;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioRecordControl {

    public interface AudioRecordControlCallback {
        void receiveAudioData(short[] data, int minBufferSize);
    }

    /**
     * 采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
     */
    public static final int SAMPLE_RATE_INHZ = 8000;
//    public static final int SAMPLE_RATE_INHZ = 44100;
    /**
     * 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
     */
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
     */
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;


    private AudioRecord audioRecord;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isRecording;
    private  int minBufferSize;
private AudioRecordControlCallback callback;

    public void init(Context context) {
         minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        //构造函数参数：
        //1.记录源
        //2.采样率，以赫兹表示
        //3.音频声道描述，声道数
        //4.返回音频声道的描述，格式
        //5.写入音频数据的缓冲区的总大小（字节），小于最小值将创建失败
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (null != audioRecord) {
            closeRecord();
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
    }

    public void startRecord() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                audioRecord.startRecording();
                isRecording = true;
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

                    short[] inG711Buffer = new short[minBufferSize];
//                    byte[] outG711Buffer = new byte[minBufferSize];

                    while (isRecording) {
                        int read = audioRecord.read(inG711Buffer, 0, minBufferSize);
                        if (AudioRecord.ERROR_INVALID_OPERATION != read && null != getCallback()) {
                            getCallback().receiveAudioData(inG711Buffer, minBufferSize);
                        }
                    }

            }
        });
    }

    public void stop() {
        if (null != audioRecord) {
            audioRecord.stop();
        }

        isRecording = false;
    }

    public void closeRecord() {
        isRecording = false;
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    public AudioRecordControlCallback getCallback() {
        return callback;
    }

    public void setCallback(AudioRecordControlCallback callback) {
        this.callback = callback;
    }
}
