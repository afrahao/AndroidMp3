package ca.wlu.li.musicplayer.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ca.wlu.li.musicplayer.R;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.wlu.li.musicplayer.service.MusicService;

public class TestActivity extends AppCompatActivity {

    @BindView(R.id.coverImage)
    ImageView coverImage;
    @BindView(R.id.stateText)
    TextView stateText;
    @BindView(R.id.playingTime)
    TextView playingTime;
    @BindView(R.id.seekBar)
    SeekBar seekBar;
    @BindView(R.id.totalTime)
    TextView totalTime;
    @BindView(R.id.isPlayButton)
    Button isPlayButton;
    @BindView(R.id.stopButton)
    Button stopButton;
    @BindView(R.id.pathText)
    TextView pathText;

    //Context mContext;
    private int flag = 0;
    private MusicService musicService;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");

    public android.os.Handler handler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getOverflowMenu();
        setContentView(R.layout.activity_main);

        //绑定初始化ButterKnife
        ButterKnife.bind(this);
        musicService = new MusicService();
        // mContext = this;
    }

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };

    private void bindServiceConection(){
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // isPlayButton.setOnClickListener(new myOnClickListener());
        }
    };

    @OnClick({R.id.isPlayButton, R.id.stopButton})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.isPlayButton:
                //playMusic();
                break;
            case R.id.stopButton:
                //stopMusic();
                break;
        }
    }

    /*public void playMusic(){
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext,R.raw.sound_file_1);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
        Uri myUri = C:/Users/windows/Music; // initialize Uri here
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(mContext, myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
