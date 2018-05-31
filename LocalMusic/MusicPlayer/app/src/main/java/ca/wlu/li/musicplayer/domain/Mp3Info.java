package ca.wlu.li.musicplayer.domain;

/**
 * Created by windows on 2018/5/16.
 */

public class Mp3Info {
    private long id;
    private String title;
    private String album;
    private long duration;
    private long size;
    private String artist;
    private String url;

    public Mp3Info(){

    }

    public Mp3Info(long pId, String pTitle){
        id = pId;
        title = pTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
