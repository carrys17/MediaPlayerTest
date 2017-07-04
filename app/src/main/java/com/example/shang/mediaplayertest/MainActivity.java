package com.example.shang.mediaplayertest;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// 不用布局文件，直接自己动态加载
public class MainActivity extends AppCompatActivity {

    private LinearLayout layout;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer; // 示波器
    private Equalizer mEqualizer;  // 均衡控制器
    private BassBoost mBassBoost;  // 重低音控制器
    private PresetReverb mPresetReverb; // 预设音场控制器
    private List<Short> reverbNames = new ArrayList<>();
    private List<String> reverbVals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置音乐声音
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        mMediaPlayer = MediaPlayer.create(this,R.raw.zzz);

        setupVisualizer(); // 初始化示波器
        setupEqualizer();  // 初始化均衡控制器
        setupBassBoost();  // 初始化重低音控制器
        setupPresetReverb(); // 初始化预设音场控制器

        mMediaPlayer.start();
    }

    private void setupVisualizer() {
        final MyVisualizerView myVisualizerView = new MyVisualizerView(this);
        myVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int)(120f * getResources().getDisplayMetrics().density)));
        layout.addView(myVisualizerView);
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                myVisualizerView.updateVisualizer(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

            }
        },Visualizer.getMaxCaptureRate()/2,true,false);
        mVisualizer.setEnabled(true);
    }

    private void setupEqualizer() {
        mEqualizer = new Equalizer(0,mMediaPlayer.getAudioSessionId());
        mEqualizer.setEnabled(true);

        TextView title = new TextView(this);
        title.setText("均衡器:");
        layout.addView(title);
        //获取均衡控制器的最小和最大值
        final short min = mEqualizer.getBandLevelRange()[0];
        short max = mEqualizer.getBandLevelRange()[1];
        short brands = mEqualizer.getNumberOfBands();
        for (short i = 0; i <brands;i++){
            TextView fretx = new TextView(this);
            fretx.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            fretx.setGravity(Gravity.CENTER_HORIZONTAL);
            fretx.setText(mEqualizer.getCenterFreq(i)/1000+" Hz");
            layout.addView(fretx);

            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            TextView tx1 = new TextView(this);
            tx1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tx1.setText(min/100 +" dB");

            TextView tx2 = new TextView(this);
            tx2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tx2.setText(max/100 +" dB");

            SeekBar bar = new SeekBar(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            bar.setLayoutParams(params);
            bar.setMax(max - min);
            bar.setProgress(mEqualizer.getBandLevel(i));
            final short brand = i;
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mEqualizer.setBandLevel(brand,(short)(progress+min));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            ll.addView(tx1);
            ll.addView(bar);
            ll.addView(tx2);

            layout.addView(ll);
        }
    }

    private void setupBassBoost() {
        mBassBoost = new BassBoost(0,mMediaPlayer.getAudioSessionId());
        mBassBoost.setEnabled(true);
        TextView bb = new TextView(this);
        bb.setText("重低音: ");
        layout.addView(bb);

        SeekBar bar = new SeekBar(this);
        bar.setMax(1000);
        bar.setProgress(0);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBassBoost.setStrength((short) progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        layout.addView(bar);
    }

    private void setupPresetReverb() {
        mPresetReverb = new PresetReverb(0,mMediaPlayer.getAudioSessionId());
        mPresetReverb.setEnabled(true);
        TextView xx = new TextView(this);
        xx.setText("音场：");
        layout.addView(xx);

        for (short i = 0; i< mEqualizer.getNumberOfPresets();i++){
            reverbNames.add(i);
            reverbVals.add(mEqualizer.getPresetName(i));
        }

        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,reverbVals));
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresetReverb.setPreset(reverbNames.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        layout.addView(sp);
    }


}
