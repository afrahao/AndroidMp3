package com.example.afra.myandroidmp3;

/**
 * Created by afra on 2018/5/27.
 */

public class Music {
    private String mName;
    private String mArtist;
    private String mUrl;
    private String picUrl1;
    private String picUrl2;

    public void setMName(String mName)
    {
        this.mName = mName;
    }
    public String getMName()
    {
        return this.mName;
    }
    public void setMArtist(String mArtist)
    {
        this.mArtist = mArtist;
    }
    public String getMArtist()
    {
        return this.mArtist;
    }
    public void setMUrl(String mUrl)
    {
        this.mUrl = mUrl;
    }
    public String getMUrl()
    {
        return this.mUrl;
    }
    public void setPicUrl1(String picUrl1)
    {
        this.picUrl1 = picUrl1;
    }
    public String getPicUrl1()
    {
        return this.picUrl1;
    }
    public void setPicUrl2(String picUrl2)
    {
        this.picUrl2 = picUrl2;
    }
    public String getPicUrl2()
    {
        return this.picUrl2;
    }
}
