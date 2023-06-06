package com.example.musicbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SongChangeListener {
    private final List<MusicList> musicLists = new ArrayList<>();
    private RecyclerView musicRecyclerView;
    private MediaPlayer mediaPlayer;
    private TextView endTime,startTime;
    private boolean isPlaying =false;
    private SeekBar playerSeekBar;
    private ImageView playPauseImg;
    private Timer timer;
    private MusicAdaper musicAdaper;

    private int currentSongListPosition =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decodeView =  getWindow().getDecorView();
        int options = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decodeView.setSystemUiVisibility(options);
        setContentView(R.layout.activity_main);




        musicRecyclerView =findViewById(R.id.musicRecyclerView);
        final CardView playPauseCard  = findViewById(R.id.playPauseCard);
        playPauseImg = findViewById(R.id.playPauseImg);
        final ImageView nextBtn = findViewById(R.id.nextBtn);
        final ImageView prevBtn = findViewById(R.id.previousBtn);
        playerSeekBar = findViewById(R.id.playerSeekBar);

        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);

        musicRecyclerView.setHasFixedSize(true);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(this));



        mediaPlayer =new MediaPlayer();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getMusicFiles();

        }else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},11);
            }
            else {
                getMusicFiles();
            }
        }
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nextSongListPosition = currentSongListPosition+1;

                if(nextSongListPosition>=musicLists.size()){
                    nextSongListPosition=0;
                }
                musicLists.get(currentSongListPosition).setPlaying(false);
                musicLists.get(nextSongListPosition).setPlaying(true);


                musicAdaper.updateList(musicLists);

                musicRecyclerView.scrollToPosition(nextSongListPosition);
                onChanged(nextSongListPosition);
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int prevSongListPosition = currentSongListPosition+1;

                if(prevSongListPosition<0){
                    prevSongListPosition=musicLists.size()-1;
                }
                musicLists.get(currentSongListPosition).setPlaying(false);
                musicLists.get(prevSongListPosition).setPlaying(true);


                musicAdaper.updateList(musicLists);

                musicRecyclerView.scrollToPosition(prevSongListPosition);
                onChanged(prevSongListPosition);

            }
        });


        playPauseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    isPlaying=false;
                    mediaPlayer.pause();
                    playPauseImg.setImageResource(R.drawable.play_icon);
                }
                else {
                    isPlaying=true;
                    mediaPlayer.start();
                    playPauseImg.setImageResource(R.drawable.pause_btn);
                }
            }
        });
        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    if(isPlaying){
                        int seekPoition = progress*1000;
                        mediaPlayer.seekTo(progress);
                    }
                    else {
                        mediaPlayer.seekTo(0);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void getMusicFiles(){
        ContentResolver contentResolver= getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null,MediaStore.Audio.Media.DATA+" LIKE?",new String[]{"%.mp3%"},null);


        if (cursor==null){
            Toast.makeText(this,"Something went Wrong!!!",Toast.LENGTH_SHORT).show();
        } else if (!cursor.moveToNext() ) {
            Toast.makeText(this,"no music found",Toast.LENGTH_SHORT).show();

        }
        else {
            while(cursor.moveToNext()){
                final String getMusicFileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                final String getArtistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                long cursorId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                Uri musicFileuri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,cursorId);
                String getDuration = "00:00";


                if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.Q){
                    getDuration= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION));
                }
                final  MusicList musicList = new MusicList(getMusicFileName,getArtistName,getDuration,false,musicFileuri);
                musicLists.add(musicList);
            }
            musicAdaper= new MusicAdaper(musicLists,MainActivity.this);
            musicRecyclerView.setAdapter( musicAdaper);
        }
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11) { // Update the request code to 11
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMusicFiles();
            } else {
                Toast.makeText(this, "Permission declined by user", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus){
            View decodeView =  getWindow().getDecorView();
            int options = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decodeView.setSystemUiVisibility(options);
        }
    }

    @Override
    public void onChanged(int position) {
        currentSongListPosition=position;
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            mediaPlayer.reset();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    mediaPlayer.setDataSource(MainActivity.this,musicLists.get(position).getMusicFile());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,"Unable to play track",Toast.LENGTH_SHORT).show();
                }
            }
        }).start();

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                final int getTotalDuration =mp.getDuration();

                String generationDuration =String.format(Locale.getDefault(),"%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(getTotalDuration),
                        TimeUnit.MILLISECONDS.toSeconds(getTotalDuration),
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getTotalDuration)));

                endTime.setText(generationDuration);
                isPlaying=true;
                mp.start();

                playerSeekBar.setMax(getTotalDuration);
                playPauseImg.setImageResource(R.drawable.pause_btn);
            }
        });
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int getCurrentDurartion = mediaPlayer.getCurrentPosition();

                        String generationDuration =String.format(Locale.getDefault(),"%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(getCurrentDurartion),
                                TimeUnit.MILLISECONDS.toSeconds(getCurrentDurartion),
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrentDurartion)));

                        playerSeekBar.setProgress(getCurrentDurartion);

                        startTime.setText(generationDuration);
                    }
                });



            }
        }, 1000,1000);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.reset();


                timer.purge();
                timer.cancel();

                isPlaying=false;
                playPauseImg.setImageResource(R.drawable.play_icon);
                playerSeekBar.setProgress(0);
            }
        });
    }
}