package ca.wlu.li.musicplayer.activity;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.wlu.li.musicplayer.R;
import ca.wlu.li.musicplayer.custom.LrcView;
import ca.wlu.li.musicplayer.domain.AppConstant;
import ca.wlu.li.musicplayer.domain.Mp3Info;
import ca.wlu.li.musicplayer.service.PlayerService;
import ca.wlu.li.musicplayer.utils.MusicUtil;

/**
 * 播放音乐界面
 * 从主界面传递过来歌曲的Id、歌曲名、歌手、歌曲路径、播放状态
 */
public class PlayerActivity extends AppCompatActivity {

    @BindView(R.id.repeat_music)
    Button repeatBtn;
    @BindView(R.id.shuffle_music)
    Button shuffleBtn;
    @BindView(R.id.musicTitle)
    TextView musicTitle;
    @BindView(R.id.musicArtist)
    TextView musicArtist;
    @BindView(R.id.coverImage)
    ImageView coverImage;
    @BindView(R.id.audioTrack)
    SeekBar music_progressBar;
    @BindView(R.id.current_progress)
    TextView currentProgress;
    @BindView(R.id.final_progress)
    TextView finalProgress;
    @BindView(R.id.play_music)
    Button playBtn;
    @BindView(R.id.next_music)
    Button nextBtn;
    @BindView(R.id.previous_music)
    Button previousBtn;
    @BindView(R.id.play_queue)
    Button queueBtn;
    @BindView(R.id.search_music)
    Button searchBtn;

    private MusicUtil musicUtil;

    private String title;       //歌曲标题
    private String artist;      //歌曲艺术家
    private String url;         //歌曲路径
    private int listPosition;   //播放歌曲在mp3Infos的位置
    private int currentTime;    //当前歌曲播放时间
    private int duration;       //歌曲长度
    private int flag;           //播放标识

    private int repeatState;
    private final int isCurrentRepeat = 1; // 单曲循环
    private final int isAllRepeat = 2;      // 全部循环
    private final int isNoneRepeat = 3;     // 无重复播放
    private boolean isPlaying;              // 正在播放
    private boolean isPause;                // 暂停
    private boolean isNoneShuffle;           // 顺序播放
    private boolean isShuffle;          // 随机播放

    private List<Mp3Info> mp3Infos;
    private PlayerService myService;
    public static LrcView lrcView; // 自定义歌词视图
    //final Intent intent = new Intent(this, PlayerService.class) ;

    private PlayerReceiver playerReceiver;
    public static final String UPDATE_ACTION = "com.lmy.action.UPDATE_ACTION";  //更新动作
    public static final String CTL_ACTION = "com.lmy.action.CTL_ACTION";        //控制动作
    public static final String MUSIC_CURRENT = "com.lmy.action.MUSIC_CURRENT";  //音乐当前时间改变动作
    public static final String MUSIC_DURATION = "com.lmy.action.MUSIC_DURATION";//音乐播放长度改变动作
    public static final String MUSIC_PLAYING = "com.lmy.action.MUSIC_PLAYING";  //音乐正在播放动作
    public static final String REPEAT_ACTION = "com.lmy.action.REPEAT_ACTION";  //音乐重复播放动作
    public static final String SHUFFLE_ACTION = "com.lmy.action.SHUFFLE_ACTION";//音乐随机播放动作

    /*ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((PlayerService.DownLoadBinder)service).getService();
*//*
            // 回调接口
            myService.setOnProgressListener(new OnProgressListener() {
                @Override
                public void onProgress(int progress) {
                    progressBar.setProgress(progress);
                }
            });*//*
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        musicUtil = new MusicUtil(PlayerActivity.this);
        mp3Infos = musicUtil.getMp3Infos();

        playerReceiver = new PlayerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION);
        filter.addAction(MUSIC_CURRENT);
        filter.addAction(MUSIC_DURATION);
        registerReceiver(playerReceiver, filter);

        /*Intent intent = new Intent(this, PlayerService.class);
        // 标志位BIND_AUTO_CREATE是的服务中onCreate得到执行,onStartCommand不会执行
        bindService(intent,conn, Context.BIND_AUTO_CREATE);*/

    }

    /**
     * 在OnResume中初始化和接收Activity数据
     */
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        title = bundle.getString("title");
        artist = bundle.getString("artist");
        url = bundle.getString("url");
        listPosition = bundle.getInt("listPosition");
        repeatState = bundle.getInt("repeatState");
        isShuffle = bundle.getBoolean("shuffleState");
        flag = bundle.getInt("MSG");
        currentTime = bundle.getInt("currentTime");
        duration = bundle.getInt("duration");
        initView();
    }

    /**
     * 初始化界面
     */
    public void initView() {
        musicTitle.setText(title);
        musicArtist.setText(artist);
        music_progressBar.setProgress(currentTime);
        music_progressBar.setMax(duration);
        switch (repeatState) {
            case isCurrentRepeat: // 单曲循环
                shuffleBtn.setClickable(false);
                repeatBtn.setBackgroundResource(R.drawable.repeat_current_selector);
                break;
            case isAllRepeat: // 全部循环
                shuffleBtn.setClickable(false);
                repeatBtn.setBackgroundResource(R.drawable.repeat_all_selector);
                break;
            case isNoneRepeat: // 无重复
                shuffleBtn.setClickable(true);
                repeatBtn.setBackgroundResource(R.drawable.repeat_none_selector);
                break;
        }
        if (isShuffle) {
            isNoneShuffle = false;
            shuffleBtn.setBackgroundResource(R.drawable.shuffle_selector);
            repeatBtn.setClickable(false);
        } else {
            isNoneShuffle = true;
            shuffleBtn.setBackgroundResource(R.drawable.shuffle_none_selector);
            repeatBtn.setClickable(true);
        }
        if (flag == AppConstant.PlayerMsg.PLAYING_MSG) { //如果播放信息是正在播放
            Toast.makeText(PlayerActivity.this, "正在播放--" + title,Toast.LENGTH_SHORT).show();
        } else if (flag == AppConstant.PlayerMsg.PLAY_MSG) { //如果是点击列表播放歌曲的话
            play();
        }
        playBtn.setBackgroundResource(R.drawable.play_selector);
        isPlaying = true;
        isPause = false;
    }

    /**
     * 反注册广播
     */
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(playerReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.repeat_music, R.id.shuffle_music, R.id.play_music, R.id.next_music, R.id.previous_music, R.id.play_queue, R.id.search_music})
    public void onViewClicked(View view) {
        Intent intent = new Intent(this, PlayerService.class);
        // 标志位BIND_AUTO_CREATE是的服务中onCreate得到执行,onStartCommand不会执行
       // bindService(intent,conn, Context.BIND_AUTO_CREATE);
        switch (view.getId()) {
            case R.id.repeat_music:
                if (repeatState == isNoneRepeat) {
                    repeat_one();
                    shuffleBtn.setClickable(false); //是随机播放变为不可点击状态
                    repeatState = isCurrentRepeat;
                } else if (repeatState == isCurrentRepeat) {
                    repeat_all();
                    shuffleBtn.setClickable(false);
                    repeatState = isAllRepeat;
                } else if (repeatState == isAllRepeat) {
                    repeat_none();
                    shuffleBtn.setClickable(true);
                    repeatState = isNoneRepeat;
                }
                intent = new Intent(REPEAT_ACTION);
                switch (repeatState) {
                    case isCurrentRepeat: // 单曲循环
                        repeatBtn
                                .setBackgroundResource(R.drawable.repeat_current_selector);
                        Toast.makeText(PlayerActivity.this, R.string.repeat_current,
                                Toast.LENGTH_SHORT).show();


                        intent.putExtra("repeatState", isCurrentRepeat);
                        sendBroadcast(intent);
                        break;
                    case isAllRepeat: // 全部循环
                        repeatBtn
                                .setBackgroundResource(R.drawable.repeat_all_selector);
                        Toast.makeText(PlayerActivity.this, R.string.repeat_all,
                                Toast.LENGTH_SHORT).show();
                        intent.putExtra("repeatState", isAllRepeat);
                        sendBroadcast(intent);
                        break;
                    case isNoneRepeat: // 无重复
                        repeatBtn
                                .setBackgroundResource(R.drawable.repeat_none_selector);
                        Toast.makeText(PlayerActivity.this, R.string.repeat_none,
                                Toast.LENGTH_SHORT).show();
                        intent.putExtra("repeatState", isNoneRepeat);
                        break;
                }
                break;
            case R.id.shuffle_music:
                Intent shuffleIntent = new Intent(SHUFFLE_ACTION);
                if (isNoneShuffle) {            //如果当前状态为非随机播放，点击按钮之后改变状态为随机播放
                    shuffleBtn
                            .setBackgroundResource(R.drawable.shuffle_selector);
                    Toast.makeText(PlayerActivity.this, R.string.shuffle,
                            Toast.LENGTH_SHORT).show();
                    isNoneShuffle = false;
                    isShuffle = true;
                    shuffleMusic();
                    repeatBtn.setClickable(false);
                    shuffleIntent.putExtra("shuffleState", true);
                    sendBroadcast(shuffleIntent);
                } else if (isShuffle) {
                    shuffleBtn
                            .setBackgroundResource(R.drawable.shuffle_none_selector);
                    Toast.makeText(PlayerActivity.this, R.string.shuffle_none,
                            Toast.LENGTH_SHORT).show();
                    isShuffle = false;
                    isNoneShuffle = true;
                    repeatBtn.setClickable(true);
                    shuffleIntent.putExtra("shuffleState", false);
                    sendBroadcast(shuffleIntent);
                }
                break;
            case R.id.play_music:
                if (isPlaying) {
                    playBtn.setBackgroundResource(R.drawable.pause_selector);
                    intent.setAction("com.lmy.media.MUSIC_SERVICE");
                    intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
                    startService(intent);
                    isPlaying = false;
                    isPause = true;

                } else if (isPause) {
                    playBtn.setBackgroundResource(R.drawable.play_selector);
                    intent.setAction("com.lmy.media.MUSIC_SERVICE");
                    intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);
                    startService(intent);
                    isPause = false;
                    isPlaying = true;
                }
                break;
            case R.id.next_music:
                next_music();
                break;
            case R.id.previous_music:
                previous_music();
                break;
            case R.id.play_queue:
                break;
            case R.id.search_music:
                break;
        }
    }

    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            switch(seekBar.getId()) {
                case R.id.audioTrack:
                    if (fromUser) {
                        audioTrackChange(progress); // 用户控制进度的改变
                    }
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }

    /**
     * 播放音乐
     */
    public void play() {
        //开始播放的时候为顺序播放
        repeat_none();
        Intent intent = new Intent(this, PlayerService.class);
        // 标志位BIND_AUTO_CREATE是的服务中onCreate得到执行,onStartCommand不会执行
       // bindService(intent,conn, Context.BIND_AUTO_CREATE);
        intent.setAction("com.lmy.media.MUSIC_SERVICE");
        intent.putExtra("url", url);
        intent.putExtra("listPosition", listPosition);
        intent.putExtra("MSG", flag);
        startService(intent);
    }


    /**
     * 随机播放
     */
    public void shuffleMusic() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 4);
        sendBroadcast(intent);
    }

    public void audioTrackChange(int progress) {
        Intent intent = new Intent();
        intent.setAction("com.lmy.media.MUSIC_SERVICE");
        intent.putExtra("url", url);
        intent.putExtra("listPosition", listPosition);
        if (isPause) {
            intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
        } else {
            intent.putExtra("MSG", AppConstant.PlayerMsg.PROGRESS_CHANGE);
        }
        intent.putExtra("progress", progress);
        startService(intent);
    }

    /**
     * 单曲循环
     */
    public void repeat_one() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 1);
        sendBroadcast(intent);
    }

    /**
     * 全部循环
     */
    public void repeat_all() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 2);
        sendBroadcast(intent);
    }

    /**
     * 顺序播放
     */
    public void repeat_none() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 3);
        sendBroadcast(intent);
    }

    /**
     * 上一首
     */
    public void previous_music() {
        playBtn.setBackgroundResource(R.drawable.play_selector);
        listPosition = listPosition - 1;
        if (listPosition >= 0) {
            Mp3Info mp3Info = mp3Infos.get(listPosition);   //上一首MP3
            musicTitle.setText(mp3Info.getTitle());
            musicArtist.setText(mp3Info.getArtist());
            url = mp3Info.getUrl();
            Intent intent = new Intent(this, PlayerService.class);
            // 标志位BIND_AUTO_CREATE是的服务中onCreate得到执行,onStartCommand不会执行
          //  bindService(intent,conn, Context.BIND_AUTO_CREATE);
            intent.setAction("com.lmy.media.MUSIC_SERVICE");
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("MSG", AppConstant.PlayerMsg.PRIVIOUS_MSG);
            startService(intent);
        } else {
            Toast.makeText(PlayerActivity.this, "没有上一首了", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 下一首
     */
    public void next_music() {
        playBtn.setBackgroundResource(R.drawable.play_selector);
        listPosition = listPosition + 1;
        if (listPosition <= mp3Infos.size() - 1) {
            Mp3Info mp3Info = mp3Infos.get(listPosition);
            url = mp3Info.getUrl();
            musicTitle.setText(mp3Info.getTitle());
            musicArtist.setText(mp3Info.getArtist());
            Intent intent = new Intent(this, PlayerService.class);
            // 标志位BIND_AUTO_CREATE是的服务中onCreate得到执行,onStartCommand不会执行
           // bindService(intent,conn, Context.BIND_AUTO_CREATE);
            intent.setAction("com.lmy.media.MUSIC_SERVICE");
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
            startService(intent);
        } else {
            Toast.makeText(PlayerActivity.this, "没有下一首了", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 用来接收从service传回来的广播的内部类
     */
    public class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MUSIC_CURRENT)) {
                currentTime = intent.getIntExtra("currentTime", -1);
                currentProgress.setText(MusicUtil.formatTime(currentTime));
                music_progressBar.setProgress(currentTime);
                //playBtn.setBackgroundResource(R.drawable.play_selector);
            } else if (action.equals(MUSIC_DURATION)) {
                int duration = intent.getIntExtra("duration", -1);
                music_progressBar.setMax(duration);
                finalProgress.setText(MusicUtil.formatTime(duration));
            } else if (action.equals(UPDATE_ACTION)) {
                //获取Intent中的current消息，current代表当前正在播放的歌曲
                listPosition = intent.getIntExtra("current", -1);
                url = mp3Infos.get(listPosition).getUrl();
                if (listPosition >= 0) {
                    musicTitle.setText(mp3Infos.get(listPosition).getTitle());
                    musicArtist.setText(mp3Infos.get(listPosition).getArtist());
                }
                if (listPosition == 0) {
                    finalProgress.setText(MusicUtil.formatTime(mp3Infos.get(listPosition).getDuration()));
                    playBtn.setBackgroundResource(R.drawable.pause_selector);
                    isPause = true;
                }
            }
        }

    }
}
