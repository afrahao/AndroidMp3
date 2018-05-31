package ca.wlu.li.musicplayer.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.wlu.li.musicplayer.R;
import ca.wlu.li.musicplayer.domain.AppConstant;
import ca.wlu.li.musicplayer.domain.Mp3Info;
import ca.wlu.li.musicplayer.service.PlayerService;
import ca.wlu.li.musicplayer.utils.MusicUtil;

public class MusicListActivity extends AppCompatActivity {
    @BindView(R.id.previous_music)
    Button previousMusic;
    @BindView(R.id.repeat_music)
    Button repeatMusic;
    @BindView(R.id.play_music)
    Button playMusic;
    @BindView(R.id.shuffle_music)
    Button shuffleMusic;
    @BindView(R.id.next_music)
    Button nextMusic;
    @BindView(R.id.playing)
    Button playing;
    @BindView(R.id.music_title_txt)
    TextView musicTitle;
    @BindView(R.id.music_duration_txt)
    TextView musicDuration;
    @BindView(R.id.music_list)
    ListView mMusiclist;
    @BindView(R.id.music_album)
    ImageView musicAlbum;


    private final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private List<Mp3Info> mp3Infos;
    private ListAdapter mAdapter;
    private MusicUtil musicUtil;

    private int repeatState;        //循环标识
    private final int isCurrentRepeat = 1; // 单曲循环
    private final int isAllRepeat = 2; // 全部循环
    private final int isNoneRepeat = 3; // 无重复播放
    private boolean isFirstTime = true;
    private boolean isPlaying; // 正在播放
    private boolean isPause; // 暂停
    private boolean isNoneShuffle = true; // 顺序播放
    private boolean isShuffle = false; // 随机播放

    private int listPosition = 0;   //标识列表位置
    private int currentTime;
    private int duration;

    private HomeReceiver homeReceiver;  //自定义的广播接收器
    private PlayerService myService;

    //一系列动作
    public static final String UPDATE_ACTION = "com.lmy.action.UPDATE_ACTION";
    public static final String CTL_ACTION = "com.lmy.action.CTL_ACTION";
    public static final String MUSIC_CURRENT = "com.lmy.action.MUSIC_CURRENT";
    public static final String MUSIC_DURATION = "com.lmy.action.MUSIC_DURATION";
    public static final String REPEAT_ACTION = "com.lmy.action.REPEAT_ACTION";
    public static final String SHUFFLE_ACTION = "com.lmy.action.SHUFFLE_ACTION";

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_layout);
        ButterKnife.bind(this);

        checkPermission();
        musicUtil = new MusicUtil(getApplicationContext());
        mp3Infos = musicUtil.getMp3Infos();
        mMusiclist = (ListView) findViewById(R.id.music_list);
        mMusiclist.setOnItemClickListener(new MusicListItemClickListener());
        setListAdpter(MusicUtil.getMusicMaps(mp3Infos));
        repeatState = isNoneRepeat; // 初始状态为无重复播放状态

        homeReceiver = new HomeReceiver();
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        // 指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        filter.addAction(MUSIC_CURRENT);
        filter.addAction(MUSIC_DURATION);
        filter.addAction(REPEAT_ACTION);
        filter.addAction(SHUFFLE_ACTION);
        // 注册BroadcastReceiver
        registerReceiver(homeReceiver, filter);

        /*Intent intent = new Intent(this, PlayerService.class);
        // 标志位BIND_AUTO_CREATE是的服务中onCreate得到执行,onStartCommand不会执行
        bindService(intent,conn, Context.BIND_AUTO_CREATE);*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意使用read
                //startGetImageThread();
            } else {
                //用户不同意，自行处理即可
                Toast.makeText(this, "您拒绝了此应用对读取本地音乐文件权限的申请！将无法使用app", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermission() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            Activity activty = this;
            ActivityCompat.requestPermissions(activty, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
    }

    @OnClick({R.id.previous_music, R.id.repeat_music, R.id.play_music, R.id.shuffle_music, R.id.next_music, R.id.playing})
    public void onViewClicked(View view) {
        //Intent intent = new Intent();
        final Intent intent = new Intent(this, PlayerService.class);
        /*ServiceConnection coon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Toast.makeText(MusicListActivity.this, "没有绑定Service",
                        Toast.LENGTH_SHORT).show();
            }
        };
        bindService(intent, coon, Service.BIND_AUTO_CREATE);*/
        switch (view.getId()) {
            case R.id.previous_music:
                //playMusic.setBackgroundResource(R.drawable.playing_selector);
                isFirstTime = false;
                isPlaying = true;
                isPause = false;
                previous_music();
                break;
            case R.id.repeat_music:
                if (repeatState == isNoneRepeat) {
                    repeat_one();
                    shuffleMusic.setClickable(false);
                    repeatState = isCurrentRepeat;
                } else if (repeatState == isCurrentRepeat) {
                    repeat_all();
                    shuffleMusic.setClickable(false);
                    repeatState = isAllRepeat;
                } else if (repeatState == isAllRepeat) {
                    repeat_none();
                    shuffleMusic.setClickable(true);
                    repeatState = isNoneRepeat;
                }
                switch (repeatState) {
                    case isCurrentRepeat: // 单曲循环
                        repeatMusic.setBackgroundResource(R.drawable.repeat_current_selector);
                        Toast.makeText(MusicListActivity.this, R.string.repeat_current,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case isAllRepeat: // 全部循环
                        repeatMusic.setBackgroundResource(R.drawable.repeat_all_selector);
                        Toast.makeText(MusicListActivity.this, R.string.repeat_all,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case isNoneRepeat: // 无重复
                        repeatMusic.setBackgroundResource(R.drawable.repeat_none_selector);
                        Toast.makeText(MusicListActivity.this, R.string.repeat_none,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case R.id.play_music:
                if (isFirstTime) {
                    playMusic.setBackgroundResource(R.drawable.pause_selector);
                    play();
                    isFirstTime = false;
                    isPlaying = true;
                    isPause = false;
                } else {
                    if (isPlaying) {
                        playMusic.setBackgroundResource(R.drawable.pause_selector);
                        intent.setAction("com.lmy.media.MUSIC_SERVICE");
                        intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
                        startService(intent);
                        isPlaying = false;
                        isPause = true;

                    } else if (isPause) {
                        playMusic.setBackgroundResource(R.drawable.play_selector);
                        intent.setAction("com.lmy.media.MUSIC_SERVICE");
                        intent.putExtra("MSG",
                                AppConstant.PlayerMsg.CONTINUE_MSG);
                        startService(intent);
                        isPause = false;
                        isPlaying = true;
                    }
                }
                break;
            case R.id.shuffle_music:
                if (isNoneShuffle) {
                    shuffleMusic.setBackgroundResource(R.drawable.shuffle_selector);
                    Toast.makeText(MusicListActivity.this, R.string.shuffle,
                            Toast.LENGTH_SHORT).show();
                    isNoneShuffle = false;
                    isShuffle = true;
                    shuffleMusic();
                    repeatMusic.setClickable(false);
                } else if (isShuffle) {
                    shuffleMusic.setBackgroundResource(R.drawable.shuffle_none_selector);
                    Toast.makeText(MusicListActivity.this, R.string.shuffle_none,
                            Toast.LENGTH_SHORT).show();
                    isShuffle = false;
                    isNoneShuffle = true;
                    repeatMusic.setClickable(true);
                }
                break;
            case R.id.next_music:
                //playMusic.setBackgroundResource(R.drawable.playing_selector);
                isFirstTime = false;
                isPlaying = true;
                isPause = false;
                next_music();
                break;
            case R.id.playing:
                Mp3Info mp3Info = mp3Infos.get(listPosition);
                Intent in = new Intent(MusicListActivity.this, PlayerActivity.class);
                in.putExtra("title", mp3Info.getTitle());
                in.putExtra("url", mp3Info.getUrl());
                in.putExtra("artist", mp3Info.getArtist());
                in.putExtra("listPosition", listPosition);
                in.putExtra("currentTime", currentTime);
                in.putExtra("duration", duration);
                in.putExtra("MSG", AppConstant.PlayerMsg.PLAYING_MSG);
                startActivity(in);
                break;
        }
    }

    private void shuffleMusic() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 4);
        sendBroadcast(intent);
    }

    private void repeat_none() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 3);
        sendBroadcast(intent);
    }

    private void repeat_all() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 2);
        sendBroadcast(intent);
    }

    private void repeat_one() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 1);
        sendBroadcast(intent);
    }

    private void previous_music() {
        listPosition = listPosition - 1;
        if (listPosition >= 0) {
            playMusic.setBackgroundResource(R.drawable.playing_selector);
            Mp3Info mp3Info = mp3Infos.get(listPosition);
            musicTitle.setText(mp3Info.getTitle());
            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction("com.lmy.media.MUSIC_SERVICE");
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("MSG", AppConstant.PlayerMsg.PRIVIOUS_MSG);
            startService(intent);
        } else {
            Toast.makeText(MusicListActivity.this, "没有上一首了", Toast.LENGTH_SHORT).show();
        }
    }

    private void next_music() {
        listPosition = listPosition + 1;
        if (listPosition <= mp3Infos.size() - 1) {
            playMusic.setBackgroundResource(R.drawable.playing_selector);
            Mp3Info mp3Info = mp3Infos.get(listPosition);
            musicTitle.setText(mp3Info.getTitle());
            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction("com.lmy.media.MUSIC_SERVICE");
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
            startService(intent);
        } else {
            Toast.makeText(MusicListActivity.this, "没有下一首了", Toast.LENGTH_SHORT).show();
        }
    }

    public void play() {
        Mp3Info mp3Info = mp3Infos.get(listPosition);
        musicTitle.setText(mp3Info.getTitle());
        Intent intent = new Intent(this, PlayerService.class);
        /*ServiceConnection coon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Toast.makeText(MusicListActivity.this, "没有绑定Service",
                        Toast.LENGTH_SHORT).show();
            }
        };*/
        intent.setAction("com.lmy.media.MUSIC_SERVICE");
        intent.putExtra("listPosition", 0);
        intent.putExtra("url", mp3Info.getUrl());
        intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
        startService(intent);
    }

    public void playMusic(int listPosition) {
        if (mp3Infos != null) {
            Mp3Info mp3Info = mp3Infos.get(listPosition);
            musicTitle.setText(mp3Info.getTitle());
            Intent intent = new Intent(MusicListActivity.this, PlayerActivity.class);
            intent.putExtra("title", mp3Info.getTitle());
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("artist", mp3Info.getArtist());
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("currentTime", currentTime);
            intent.putExtra("repeatState", repeatState);
            intent.putExtra("shuffleState", isShuffle);
            intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
            startActivity(intent);
        }
    }

    /*public void musicListItemDialog() {
        String[] menuItems = new String[]{"播放音乐","设为铃声","查看详情"};
        ListView menuList = new ListView(MusicListActivity.this);
        menuList.setCacheColorHint(Color.TRANSPARENT);
        menuList.setDividerHeight(1);
        menuList.setAdapter(new ArrayAdapter<String>(HomeActivity.this, R.layout.context_dialog_layout, R.id.dialogText, menuItems));
        menuList.setLayoutParams(new LayoutParams(ConstantUtil.getScreen(MusicListActivity.this)[0] / 2, LayoutParams.WRAP_CONTENT));


        final CustomDialog customDialog = new CustomDialog.Builder(HomeActivity.this).setTitle(R.string.operation).setView(menuList).create();
        customDialog.show();

        menuList.setOnItemClickListener( new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                customDialog.cancel();
                customDialog.dismiss();
            }

        });
    }*/

    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
        /**
         * 点击列表播放音乐
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            listPosition = position; // 获取列表点击的位置
            isFirstTime = false;
            playMusic(listPosition); // 播放音乐
        }
        /*@Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (mp3Infos != null) {
                Mp3Info mp3Info = mp3Infos.get(position);
                Log.d("mp3Info-->", mp3Info.toString());
                Intent intent = new Intent();
                intent.putExtra("url", mp3Info.getUrl());
                intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
                intent.setClass(MusicListActivity.this, PlayerService.class);
                startService(intent);       //启动服务
            }
        }*/
    }

    /**
     * 填充列表
     */
    public void setListAdpter(List<HashMap<String, String>> mp3Infos) {
        mAdapter = new SimpleAdapter(this, mp3Infos,
                R.layout.music_list_item_layout, new String[]{"title", "Artist", "duration"},
                new int[]{R.id.music_title, R.id.music_Artist, R.id.music_duration});
        mMusiclist.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        //unbindService(conn);
        super.onDestroy();
        unregisterReceiver(homeReceiver);
    }

    /**
     * 反注册广播
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    //自定义的BroadcastReceiver，负责监听从Service传回来的广播
    public class HomeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MUSIC_CURRENT)) {
                //currentTime代表当前播放的时间
                currentTime = intent.getIntExtra("currentTime", -1);
                musicDuration.setText(MusicUtil.formatTime(currentTime));
            } else if (action.equals(MUSIC_DURATION)) {
                duration = intent.getIntExtra("duration", -1);
            } else if (action.equals(UPDATE_ACTION)) {
                //获取Intent中的current消息，current代表当前正在播放的歌曲
                listPosition = intent.getIntExtra("current", -1);
                if (listPosition >= 0) {
                    musicTitle.setText(mp3Infos.get(listPosition).getTitle());
                }
            } else if (action.equals(REPEAT_ACTION)) {
                repeatState = intent.getIntExtra("repeatState", -1);
                switch (repeatState) {
                    case isCurrentRepeat: // 单曲循环
                        repeatMusic
                                .setBackgroundResource(R.drawable.repeat_current_selector);
                        shuffleMusic.setClickable(false);
                        break;
                    case isAllRepeat: // 全部循环
                        repeatMusic
                                .setBackgroundResource(R.drawable.repeat_all_selector);
                        shuffleMusic.setClickable(false);
                        break;
                    case isNoneRepeat: // 无重复
                        repeatMusic
                                .setBackgroundResource(R.drawable.repeat_none_selector);
                        shuffleMusic.setClickable(true);
                        break;
                }
            } else if (action.equals(SHUFFLE_ACTION)) {
                isShuffle = intent.getBooleanExtra("shuffleState", false);
                if (isShuffle) {
                    isNoneShuffle = false;
                    shuffleMusic.setBackgroundResource(R.drawable.shuffle_selector);
                    repeatMusic.setClickable(false);
                } else {
                    isNoneShuffle = true;
                    shuffleMusic.setBackgroundResource(R.drawable.shuffle_none_selector);
                    repeatMusic.setClickable(true);
                }
            }
        }

    }
}
