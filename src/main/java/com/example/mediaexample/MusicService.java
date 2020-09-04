package com.example.mediaexample;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songs;
    private int position;
    private IBinder musicBinder = new MusicBinder();
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }
    @Override
    public void onCreate(){
        position = 0;
        // Khởi tạo một bộ phát đa phương tiện mới.
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();

        rand = new Random();
    }

    public  void initMusicPlayer(){

        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public  void setList(ArrayList<Song> songs){
        this.songs = songs;
    }

    public  class  MusicBinder extends Binder{
        MusicService getService(){
            return  MusicService.this;
        }
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mediaPlayer.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    // để media play trong background ngay cả khi ứng dụng không ở trên màn hình,
    // là khi bạn muốn media tiếp tục chạy khi bạn sử dụng 1 ứng dụng khác,
    // hiện thị Notification
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification.Builder nBuilder = new Notification.Builder(this);
        nBuilder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_close_black_24dp)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification notif = nBuilder.getNotification();
        startForeground(NOTIFY_ID, notif);
    }

    @Override
    public  boolean onUnbind(Intent intent){
        mediaPlayer.stop();
        mediaPlayer.release();
        return  false;
    }

    public  void playSong(){
        mediaPlayer.reset();
        Song playSong = songs.get(position);
        songTitle = playSong.getTitle();
        int currentSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error starting data source", e);
        }
        mediaPlayer.prepareAsync();
    }

    public void setSong(int songIndex) {
        this.position = songIndex;
    }

    public int getPosn(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDur(){
        return mediaPlayer.getDuration();
    }

    public boolean isPng(){
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mediaPlayer.pause();
    }

    public void seek(int posn){
        mediaPlayer.seekTo(posn);
    }

    public void go(){
        mediaPlayer.start();
    }

    public void playPrev() {
        position--;
        if (position < 0) position = songs.size() - 1;
        playSong();
    }

    public void playNext() {
        if (shuffle) {
            int newSongPos = position;
            while (newSongPos == position) {
                newSongPos = rand.nextInt(songs.size());
            }
            position = newSongPos;
        } else {
            position++;
            if (position >= songs.size()) position = 0;
        }
        playSong();
    }
    public void setShuffle() {
        if (shuffle) shuffle = false;
        else shuffle = true;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }


}
