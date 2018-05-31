package com.example.afra.myandroidmp3;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.SeekBar;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by afra on 2018/5/27.
 */

public class MusicService extends Service{

    MyReceiver serviceReceiver;
    AssetManager mAssetManager;
    String[] musics = new String[]{"http://abv.cn/music/光辉岁月.mp3", "http://47.95.10.11/music/Problem.mp3", "http://47.95.10.11/music/Shape_of_You.mp3"};
    String[] musicNames = new String[]{"光辉岁月", "problem", "shape of you"};
    String[] singerNames = new String[]{"aaaa", "Ariana Grande", "Ed Sheeran"};
    MediaPlayer mMediaPlayer;
    private List<Music> musicList = new ArrayList<Music>();
    private static SeekBar seekBar; // 拖动条
    private Timer mTimer = new Timer(); // 计时器

    int status = 0x11;
    int current = 0; // 记录当前正在播放的音乐


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setSeekBar(SeekBar seekBar)
    {
        this.seekBar = seekBar;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAssetManager = getAssets();
        serviceReceiver = new MyReceiver();
        //创建IntentFilter
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.CTL_ACTION);
        registerReceiver(serviceReceiver, filter);
        //创建MediaPlayer
        mMediaPlayer = new MediaPlayer();
        //为MediaPlayer播放完成事件绑定监听器
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                current++;
                if (current >= 3) {
                    current = 0;
                }
                //发送广播通知Activity更改文本框
                Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
                sendIntent.putExtra("current", current);
                //发送广播，将被Activity中的BroadcastReceiver接收到
                sendBroadcast(sendIntent);
                //准备并播放音乐
                prepareAndPlay(musics[current]);
            }
        });
        InitMusicList();
        mTimer.schedule(timerTask, 0, 1000);
    }



    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int control = intent.getIntExtra("control", -1);
            switch (control){
                case 1: // 播放或暂停
                    //原来处于没有播放状态
                    if (status ==0x11){
                        //准备播放音乐
                        prepareAndPlay(musics[current]);
                        status = 0x12;
                    }
                    //原来处于播放状态
                    else if (status == 0x12){
                        //暂停
                        mMediaPlayer.pause();
                        status = 0x13; // 改变为暂停状态
                    }
                    //原来处于暂停状态
                    else if (status == 0x13){
                        //播放
                        mMediaPlayer.start();
                        status = 0x12; // 改变状态
                    }
                    break;
                //停止声音
                case 2:
                    //如果原来正在播放或暂停
                    if (status == 0x12 || status == 0x13){
                        //停止播放
                        mMediaPlayer.stop();
                        status = 0x11;
                    }
                    break;
                case 3:
                    current++;
                    if (current >= 3) {
                        current = 0;
                    }
                    mMediaPlayer.stop();
                    seekBar.setProgress(0);
                    prepareAndPlay(musics[current]);
                    status = 0x12;
                    break;
                case 4:
                    current--;
                    if (current < 0) {
                        current = 3;
                    }
                    mMediaPlayer.stop();
                    seekBar.setProgress(0);
                    prepareAndPlay(musics[current]);
                    status = 0x12;
                    break;

            }
            //广播通知Activity更改图标、文本框
            Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
            sendIntent.putExtra("update", status);
            sendIntent.putExtra("current", current);
            sendIntent.putExtra("name", musicNames[current]);
            sendIntent.putExtra("singer", singerNames[current]);
            //发送广播，将被Activity中的BroadcastReceiver接收到
            sendBroadcast(sendIntent);
        }
    }

    // 计时器
    TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            if (mMediaPlayer == null)
                return;
            if (mMediaPlayer.isPlaying()) {
                handler.sendEmptyMessage(0); // 发送消息
            }
        }
    };

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int position = mMediaPlayer.getCurrentPosition();
            int duration = mMediaPlayer.getDuration();
            if (duration > 0) {
                // 计算进度（获取进度条最大刻度*当前音乐播放位置 / 当前音乐时长）
                long pos = seekBar.getMax() * position / duration;
                seekBar.setProgress((int) pos);
            }
        };
    };

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        super.onDestroy();
    }

    private void prepareAndPlay(String music) {
        try {
            mMediaPlayer.reset();
            //使用MediaPlayer加载指定的声音文件
            mMediaPlayer.setDataSource(music);
            System.out.println(music);
            mMediaPlayer.prepareAsync(); // 准备声音
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start(); // 播放
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void InitMusicList()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = Util.getConnection();
                System.out.println("All users info:");

                PreparedStatement psmt = null;
                ResultSet rs = null;
                conn = Util.getConnection();
                StringBuffer sql = new StringBuffer();
                sql.append("select * from music");
                try {
                    psmt = conn.prepareStatement(sql.toString());
                    rs = psmt.executeQuery();
                    while (rs.next()) {
                        Music music = new Music();
                        music.setMName(rs.getString("mName"));
                        music.setMUrl(rs.getString("mUrl"));
                        music.setMArtist(rs.getString("mArtist"));
                        musicList.add(music);
                        System.out.println(rs.getString("mName") + "  " + rs.getString("mUrl") + "  " + rs.getString("mArtist"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}