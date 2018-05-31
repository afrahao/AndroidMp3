package ca.wlu.li.musicplayer.service;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.animation.LinearInterpolator;

import ca.wlu.li.musicplayer.R;

public class MusicService extends Service {
    private int flag = 0;

    public static String which = "";
    public static int isReturnTo = 0;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static ObjectAnimator animator;

    public final IBinder binder = new MyBinder();

    public class MyBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }

    }

    public MusicService() {
        initMediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void initMediaPlayer(){
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        /*WifiLock wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();*/
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound_file_1);
        mediaPlayer.start();

    }

    public void AnimationAction(){
        if(mediaPlayer.isPlaying()){
            //set the animation
            animator.setDuration(5000);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.start();
        }
    }

    //@TargetApi(19)
    public void playOrPause(){
        flag++;
        if(flag >= 1000)
            flag = 2;

        which = "pause";

        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            animator.pause();
        }else{
            mediaPlayer.start();

            if((flag == 1)||(isReturnTo ==1)){
                animator.setDuration(5000);
                animator.setInterpolator(new LinearInterpolator());
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.start();
            }else{
                animator.resume();
            }
        }
    }

    public void stop(){
        which = "stop";
        animator.pause();
        if(mediaPlayer != null){
            mediaPlayer.pause();
            mediaPlayer.stop();
            try{
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDestroy() {
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            super.onDestroy();
        }
    }

}
