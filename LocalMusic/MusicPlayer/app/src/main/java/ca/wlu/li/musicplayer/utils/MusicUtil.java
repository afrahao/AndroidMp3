package ca.wlu.li.musicplayer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ca.wlu.li.musicplayer.domain.Mp3Info;

/**
 * Created by windows on 2018/5/18.
 * 工具类
 */

public class MusicUtil {
    private ContentResolver contentResolver ;
    private Context mContext;
    public MusicUtil(Context context){
        this.mContext = context;
    }

    public List<Mp3Info> getMp3Infos() {
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
        if(cursor == null) {
            Log.d("cursorInfo", "这儿呢 MusicUtil cursor == null.");
        }else{
                for (int i = 0; i < cursor.getCount(); i++) {
                    Mp3Info mp3Info = new Mp3Info();
                    cursor.moveToNext();
                    long id = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Audio.Media._ID));   //音乐id
                    String title = cursor.getString((cursor
                            .getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题
                    String artist = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家
                    long duration = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Audio.Media.DURATION));//时长
                    long size = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Audio.Media.SIZE));  //文件大小
                    String url = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.DATA));              //文件路径
                    int isMusic = cursor.getInt(cursor
                            .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐
                    if (isMusic != 0) {     //只把音乐添加到集合当中
                        mp3Info.setId(id);
                        mp3Info.setTitle(title);
                        mp3Info.setArtist(artist);
                        mp3Info.setDuration(duration);
                        mp3Info.setSize(size);
                        mp3Info.setUrl(url);
                        mp3Infos.add(mp3Info);
                    }
                }
            }
        return mp3Infos;
    }

    /**
     * 往List集合中添加Map对象数据，每一个Map对象存放一首音乐的所有属性
     */
    public static List<HashMap<String, String>> getMusicMaps(
            List<Mp3Info> mp3Infos) {
        List<HashMap<String, String>> mp3list = new ArrayList<HashMap<String, String>>();
        for (Iterator iterator = mp3Infos.iterator(); iterator.hasNext();) {
            Mp3Info mp3Info = (Mp3Info) iterator.next();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", mp3Info.getTitle());
            map.put("Artist", mp3Info.getArtist());
            map.put("duration", formatTime(mp3Info.getDuration()));
            map.put("size", String.valueOf(mp3Info.getSize()));
            map.put("url", mp3Info.getUrl());
            mp3list.add(map);
        }
        return mp3list;
    }

    /**
     * 格式化时间，将毫秒转换为分:秒格式
     */
    public static String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }


}
