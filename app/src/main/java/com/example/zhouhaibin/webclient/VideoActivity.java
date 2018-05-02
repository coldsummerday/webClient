package com.example.zhouhaibin.webclient;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;



import org.w3c.dom.Text;

import java.util.TimeZone;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;


public class VideoActivity extends AppCompatActivity implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {




    private String source_url = "http://192.168.199.244:8081/cloud/第十课.mkv";
    private Uri uri;
    private ProgressBar pb;
    private TextView downloadRateView, loadRateView;
    private MediaController mediaController;
    private AudioManager audioManager;
    private GestureDetector gestureDetector;
    private ImageView mOperationBg;
    private View mVolumeBrightnessLayout;
    private View mSeekBarLayout;

    private TextView seekbarLeftTextView;
    private TextView seekbarRightTextView;
    private SeekBar seekBar;
    private ImageView seekBarView;


    /*最大声音*/
    private int maxVolume;

    /*当前声音*/
    private int currentVolume = -1;
    //当前亮度
    private float mBrightness = -1f;

    private long currentPosition, duration;

    /**
     * 控制界面显示时长,10s,超过就隐藏
     **/
    public static final int CONTROLPANEL_KEEPTIME = 10;

    private VideoView mVideoView;
    Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        //获取播放url:

        Intent intent = getIntent();
        source_url = intent.getStringExtra("Source");

        if (Vitamio.isInitialized(this)) {
            initView();
            initData();
        }

        gestureDetector = new GestureDetector(this, new CustomGestureListener());
    }

    //初始化控件
    private void initView() {
        mVideoView = (VideoView) findViewById(R.id.buffer);
        mVolumeBrightnessLayout = findViewById(R.id.operation_volume);
        mOperationBg = (ImageView) findViewById(R.id.operation_bg);
        seekbarLeftTextView = (TextView)findViewById(R.id.mediacontroller_time_current);
        seekbarRightTextView = (TextView)findViewById(R.id.mediacontroller_time_total);
        seekBar = (SeekBar)findViewById(R.id.mediacontroller_seekbar);
        pb = (ProgressBar) findViewById(R.id.probar);
        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    //初始化数据
    private void initData() {
        Log.d("vedioActivity",source_url);
        uri = Uri.parse(source_url);
        mVideoView.setVideoURI(uri);//设置视频播放地址
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);//高画质
        mVideoView.requestFocus();
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnBufferingUpdateListener(this);
        mHandler = new Handler();

        //加载完毕监听
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
                //获取播放时间
                duration = mediaPlayer.getDuration();
                seekbarRightTextView.setText(formatTime(duration));
                seekBar.setMax(new Long(mediaPlayer.getDuration()).intValue());
                //设置进度条监听
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    //进度条抬起事件
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mediaPlayer.seekTo(seekBar.getProgress());
                        seekbarLeftTextView.setText(formatTime(new Integer(seekBar.getProgress()).longValue()));
                    }
                });
            }
        });

        //当前播放进度跟踪
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mVideoView != null ){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(mVideoView != null && mVideoView.isPlaying()){
                                seekbarLeftTextView.setText(formatTime(mVideoView.getCurrentPosition()));
                                seekBar.setProgress(new Long(mVideoView.getCurrentPosition()).intValue());
                            }
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            //开始缓存，暂停播放
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    pb.setVisibility(View.VISIBLE);
                    downloadRateView.setText("");
                    loadRateView.setText("");
                    downloadRateView.setVisibility(View.VISIBLE);
                    loadRateView.setVisibility(View.VISIBLE);
                    setSeekBarVisible();
                }

                break;
            //缓存完成，继续播放
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                pb.setVisibility(View.GONE);
                downloadRateView.setVisibility(View.GONE);
                loadRateView.setVisibility(View.GONE);
                setSeekBarGone();
                break;

            //显示 下载速度
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                downloadRateView.setText("" + extra + "kb/s" + "  ");

                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        loadRateView.setText(percent + "%");
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //屏幕切换时，设置全屏
        if (mVideoView != null){
            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        }
        super.onConfigurationChanged(newConfig);
    }

    private void setSeekBarVisible(){
        seekbarLeftTextView.setVisibility(View.VISIBLE);
        seekbarRightTextView.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
    }
    private void setSeekBarGone(){
        seekbarLeftTextView.setVisibility(View.GONE);
        seekbarRightTextView.setVisibility(View.GONE);
        seekBar.setVisibility(View.GONE);
    }

    private void endGesture() {
        currentVolume = -1;
        mDismissHandler.removeMessages(0);
        mDismissHandler.sendEmptyMessageDelayed(0, 500);
    }
    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        /*
        * 双击操作
        * */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            } else {
                mVideoView.start();
            }

            return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            //上下滑动
            if (Math.abs(distanceY) / Math.abs(distanceX) > 3) {
                float mOldX = e1.getX();
                float mOldY = e1.getY();
                int y = (int) e2.getRawY();
                Display disp = getWindowManager().getDefaultDisplay();
                int windowHeight = disp.getHeight();
                int windowWidth = disp.getWidth();
                if (mOldX > windowWidth * 3.0 / 4.0) {// 右边滑动 屏幕 3/4
                    onVolumeSlide((mOldY - y) / windowHeight);
                } else if (mOldX < windowWidth * 1.0 / 4.0) {// 左边滑动 屏幕 1/4
                    onBrightnessSlide((mOldY - y) / windowHeight);
                }

            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //当手势结束，并且是单击结束时，控制器隐藏/显示
            toggleMediaControlsVisiblity();
            return super.onSingleTapConfirmed(e);
        }

        /*
         * 滑动操作
         * */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            currentPosition = mVideoView.getCurrentPosition();

            /*向左滑*/
            if (e1.getX() - e2.getX() > 120) {

                if (currentPosition < 10000) {
                    currentPosition = 0;
                    mVideoView.seekTo(currentPosition);
                } else {
                    mVideoView.seekTo(currentPosition - 10000);
                }

            } else if (e2.getX() - e1.getX() > 120) {
            /*向右滑*/
                if (currentPosition + 10000 > duration) {
                    currentPosition = duration;
                    mVideoView.seekTo(currentPosition);
                } else {
                    mVideoView.seekTo(currentPosition + 10000);
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    //单击的时候唤出
    private void toggleMediaControlsVisiblity(){
        if(seekBar.getVisibility()==View.VISIBLE){
           setSeekBarGone();
        }else {
            setSeekBarVisible();
        }
    }

    /*
    * 定时隐藏
    * */
    private Handler mDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            mVolumeBrightnessLayout.setVisibility(View.GONE);
        }
    };

    /**
     * 滑动改变声音大小
     */
    private void onVolumeSlide(float percent) {

        if (currentVolume == -1) {
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume < 0)
                currentVolume = 0;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_volumn_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int value = currentVolume + (int) (percent * maxVolume);
        if (value > maxVolume) {
            value = maxVolume;
        } else if (value < 0) {
            value = 0;
        }
        if (value >= 10) {
            mOperationBg.setImageResource(R.drawable.volmn_100);
        } else if (value >= 5 && value < 10) {
            mOperationBg.setImageResource(R.drawable.volmn_60);
        } else if (value > 0 && value < 5) {
            mOperationBg.setImageResource(R.drawable.volmn_30);
        } else {
            mOperationBg.setImageResource(R.drawable.volmn_no);
        }

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);

    }

    //滑动控制亮度
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = VideoActivity.this.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = VideoActivity.this.getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        VideoActivity.this.getWindow().setAttributes(lpa);
        //不同的亮度加载不同的图片
        if (lpa.screenBrightness * 100 >= 90) {
            mOperationBg.setImageResource(R.drawable.light_100);
        } else if (lpa.screenBrightness * 100 >= 80 && lpa.screenBrightness * 100 < 90) {
            mOperationBg.setImageResource(R.drawable.light_90);
        } else if (lpa.screenBrightness * 100 >= 70 && lpa.screenBrightness * 100 < 80) {
            mOperationBg.setImageResource(R.drawable.light_80);
        } else if (lpa.screenBrightness * 100 >= 60 && lpa.screenBrightness * 100 < 70) {
            mOperationBg.setImageResource(R.drawable.light_70);
        } else if (lpa.screenBrightness * 100 >= 50 && lpa.screenBrightness * 100 < 60) {
            mOperationBg.setImageResource(R.drawable.light_60);
        } else if (lpa.screenBrightness * 100 >= 40 && lpa.screenBrightness * 100 < 50) {
            mOperationBg.setImageResource(R.drawable.light_50);
        } else if (lpa.screenBrightness * 100 >= 30 && lpa.screenBrightness * 100 < 40) {
            mOperationBg.setImageResource(R.drawable.light_40);
        } else if (lpa.screenBrightness * 100 >= 20 && lpa.screenBrightness * 100 < 20) {
            mOperationBg.setImageResource(R.drawable.light_30);
        } else if (lpa.screenBrightness * 100 >= 10 && lpa.screenBrightness * 100 < 20) {
            mOperationBg.setImageResource(R.drawable.light_20);
        }

    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Log.d("key","back");
                break;

            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onResume() {
        //强制横屏
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();

        if (mVideoView != null) {
            mVideoView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }


        /*
     * 毫秒转化时分秒毫秒
     */
        public  String formatTime(Long ms) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");//初始化Formatter的转换格式。

            String hms = formatter.format(ms - TimeZone.getDefault().getRawOffset());
            return hms;
        }

}
