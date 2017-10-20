package com.akshathjain.bookworm.fragments;

/*
Name: Akshath Jain
Date: 10/17/17
Purpose: Audio player fragment
 */

import android.app.Fragment;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.akshathjain.bookworm.generic.AudioBook;
import com.akshathjain.bookworm.R;
import com.akshathjain.bookworm.async.ArchiveRetriever;
import com.akshathjain.bookworm.async.QueryFinished;
import com.akshathjain.bookworm.generic.Chapter;
import com.bumptech.glide.Glide;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class Player extends Fragment {
    private AudioBook book;
    private ImageView playPause;
    private ImageView trackNext;
    private ImageView trackPrevious;
    private SeekBar seekBar;
    private TextView title;
    private TextView chapter;
    private ImageView thumbnail;
    private boolean isMusicPlaying = false;
    private MediaPlayer mediaPlayer;
    private Chapter currentChapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_audio_player, container, false);

        playPause = layout.findViewById(R.id.track_play_pause);
        trackNext = layout.findViewById(R.id.track_next);
        trackPrevious = layout.findViewById(R.id.track_previous);
        seekBar = layout.findViewById(R.id.track_seekbar);
        title = layout.findViewById(R.id.track_title);
        chapter = layout.findViewById(R.id.track_chapter);
        thumbnail = layout.findViewById(R.id.track_thumbnail);
        mediaPlayer = new MediaPlayer();

        //get the book information and then get the chapter information
        Bundle args = getArguments();
        this.book = (AudioBook) args.getSerializable("AUDIO_BOOK");
        ArchiveRetriever streamGetter = new ArchiveRetriever(book);
        streamGetter.addOnCompleted(new QueryFinished<Void>() {
            @Override
            public void onQueryFinished(Void aVoid) {
                currentChapter = book.getCurrentChapter();
                setupSeekBar();
                setupMediaPlayer(currentChapter.getUrl());
            }
        });
        streamGetter.execute(book.getChaptersURL());

        //set book information
        title.setText(book.getTitle()); //bind title
        Glide.with(this).load(book.getThumbnailURL()).transition(withCrossFade()).into(thumbnail); //bind thumbnail

        //setup onclick listeners
        setupOnClickListeners();

        return layout;
    }

    //function to set up on click listeners
    private void setupOnClickListeners() {
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseMusic();
            }
        });
    }

    //function to setup mediaplayer / wakelock functionality
    private void setupMediaPlayer(String url) {
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    playPauseMusic();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //function to start playing music
    private void playPauseMusic() {
        isMusicPlaying = !isMusicPlaying;
        if (isMusicPlaying) {
            playPause.setImageResource(R.drawable.ic_pause_black_24dp);
            mediaPlayer.start();
        } else {
            playPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            mediaPlayer.pause();
        }
    }

    private void setupSeekBar(){
        final Handler handler = new Handler();
        seekBar.setMax((int)currentChapter.getRuntime());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    seekBar.setProgress((int)(mediaPlayer.getCurrentPosition() / 500.0));
                }
                handler.postDelayed(this, 500);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean userTouched = false;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(userTouched) {
                    if(mediaPlayer != null) {
                        playPauseMusic();
                        mediaPlayer.seekTo(i * 1000);
                        playPauseMusic();
                    }
                    userTouched = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userTouched = true;
            }
        });
    }
}
