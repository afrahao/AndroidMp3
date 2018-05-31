package com.example.afra.myandroidmp3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mStart;
    private ImageView mNext;
    private ImageView mPrevious;
    private ImageView mStop;
    private TextView mMusicName;
    private TextView mSingerName;
    private SeekBar seekBar;
    private ActivityReceiver mActivityReceiver;
    public static final String CTL_ACTION = "CTL_ACTION";
    public static final String UPDATE_ACTION = "UPDATE_ACTION";

    //定义音乐播放状态，0x11代表没有播放，0x12代表正在播放，0x13代表暂停
    int status = 0x11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStart = (ImageView) findViewById(R.id.play);
        mNext = (ImageView) findViewById(R.id.next);
        mStop = (ImageView) findViewById(R.id.download);
        mPrevious = (ImageView) findViewById(R.id.previous);
        mMusicName = (TextView) findViewById(R.id.music_name);
        mSingerName = (TextView) findViewById(R.id.stateText);
        seekBar = findViewById(R.id.seekBar);

        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPrevious.setOnClickListener(this);

        mActivityReceiver = new ActivityReceiver();
        //创建IntentFilter
        IntentFilter filter = new IntentFilter();
        //指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        //注册BroadcastReceiver
        registerReceiver(mActivityReceiver, filter);
        MusicService ms = new MusicService();
        ms.setSeekBar(seekBar);
        Intent intent = new Intent(MainActivity.this, ms.getClass());
        //启动后台Service
        startService(intent);
    }

    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取Intent中的update消息，update代表播放状态
            int update = intent.getIntExtra("update", -1);
            //获取Intent中的current消息，current代表当前正在播放的歌曲
            int current = intent.getIntExtra("current", -1);
            String name = intent.getStringExtra("name");
            String singer = intent.getStringExtra("singer");
            if (current >= 0){
                mMusicName.setText(name);
                mSingerName.setText(singer);
            }
            switch (update){
                case 0x11:
                    mStart.setBackgroundResource(R.drawable.play);
                    status = 0x11;
                    break;
                //控制系统进入播放状态
                case 0x12:
                    //在播放状态下设置使用暂停图标
                    mStart.setBackgroundResource(R.drawable.pause);
                    status = 0x12;
                    break;
                case 0x13:
                    //在暂停状态下设置使用播放图标
                    mStart.setBackgroundResource(R.drawable.play);
                    status = 0x13;
                    break;

            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(CTL_ACTION);
        switch (v.getId()){
            case R.id.play:
                intent.putExtra("control", 1);
                break;
            case R.id.next:
                intent.putExtra("control", 3);
                break;
            case R.id.previous:
                intent.putExtra("control", 4);
                break;
            case R.id.download:
                intent.putExtra("control", 2);
                break;
        }
        //发送广播，将被Service中的BroadcastReceiver接收到
        sendBroadcast(intent);
    }
}
