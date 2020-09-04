package com.example.mediaexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.MediaController;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songs;
    private RecyclerView recyclerView;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;
    private boolean paused=false, playbackPaused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songs = new ArrayList<>();
        getSongList();

        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SongAdapter(songs, MainActivity.this));

        setController();

    }



    // Method to retrieve song infos from device
    public void getSongList() {
        // Query external audio resources
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, null);

        // Iterate over results if valid
        if (musicCursor != null && musicCursor.moveToFirst()) {
            // Get columns

            do {
                int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                String url = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                int thisId = musicCursor.getInt(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(url);
                byte[] coverBytes = retriever.getEmbeddedPicture();
                Bitmap songCover = null;
                if (coverBytes!=null) //se l'array di byte non è vuoto, crea una bitmap
                    songCover = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);

                songs.add(new Song(thisId, thisTitle, thisArtist,songCover));

            }
            while (musicCursor.moveToNext());
        }
    }

    // Connect with the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setList(songs);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected  void onStart() {

        Log.e("0","kjhgf");
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this,MusicService.class);
            bindService(playIntent,musicConnection,BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    public void CickSOng(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        musicService.setSong(itemPosition);
        musicService.playSong();
        if(playbackPaused)
        {
            setController();
            playbackPaused = false;
        }
        controller.show(0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicService.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicService = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean canPause() {
        Log.e("1","kjhgf");
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        Log.e("2","kjhgf");
        return true;
    }

    @Override
    public boolean canSeekForward() {
        Log.e("3","kjhgf");
        return true;
    }
    @Override
    public int getAudioSessionId() {
        Log.e("4","kjhgf");
        return 0;
    }

    @Override
    public int getBufferPercentage() {

        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService!=null && musicBound && musicService.isPng())
            return musicService.getPosn();
        else return 0;
    }

    @Override
    public int getDuration() {
        if(musicService!=null && musicBound && musicService.isPng())
            return musicService.getDur();
        else return 0;
    }

    @Override
    public boolean isPlaying() {
        if(musicService!=null && musicBound)
            return musicService.isPng();
        return false;
    }

    @Override
    public void pause() {
        Log.e("6","kjhgf");
        playbackPaused=false;
        musicService.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public void start() {
        musicService.go();
    }

    private void setController() {
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.recyclerview));
        controller.setEnabled(true);
    }
    private void playNext(){

        musicService.playNext();
        if(playbackPaused)
        {
            setController();
            playbackPaused = false;
        }
        controller.show(0);



    }

    private void playPrev(){
        musicService.playPrev();
        if(playbackPaused)
        {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    @Override
    protected void onPause(){
        Log.e("7","kjhgf");
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
           paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }


}