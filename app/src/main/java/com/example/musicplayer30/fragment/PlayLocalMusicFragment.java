package com.example.musicplayer30.fragment;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer30.R;
import com.example.musicplayer30.adapter.MusicAdapter;
import com.example.musicplayer30.bean.Song;
import com.example.musicplayer30.utils.AudioUtils;
import com.example.musicplayer30.utils.RequestPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class PlayLocalMusicFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "PlayLocalMusicFragment";

    private ImageView mSkipPrevImageView;
    private ImageView mSkipNextImageView;
    private ImageView mPlayOrPauseImageView;
    private ImageView mPlayModeImageView;
    private RecyclerView mMusicRecyclerView;
    private SeekBar mSeekBar;

    private MusicAdapter mMusicAdapter;
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private List<Song> mSongList = new ArrayList<>();

    private int songIndex = -1;

    private final int PLAY_QUEUE = R.drawable.ic_queue;
    private final int REPEAT_ONE = R.drawable.ic_repeat_one;
    private final int PLAY_RANDOM = R.drawable.ic_random;


    @DrawableRes
    private int playMode = PLAY_RANDOM;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        mMusicAdapter = new MusicAdapter(getContext(), mSongList);
    }


    public static PlayLocalMusicFragment newInstance() {

        Bundle args = new Bundle();

        PlayLocalMusicFragment fragment = new PlayLocalMusicFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_local_music, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSkipPrevImageView = view.findViewById(R.id.skip_previous_image_view);
        mSkipNextImageView = view.findViewById(R.id.skip_next_image_view);
        mPlayOrPauseImageView = view.findViewById(R.id.play_or_pause_image_view);
        mPlayModeImageView = view.findViewById(R.id.play_mode_image_view);
        mMusicRecyclerView = view.findViewById(R.id.music_recycler_view);
        mSeekBar = view.findViewById(R.id.seek_bar);


        RequestPermissions.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                new RequestPermissions.OnPermissionsRequestListener() {
                    @Override
                    public void onGranted() {
                        loadMusicList();
                        initEvent();
                    }

                    @Override
                    public void onDenied(List<String> deniedList) {
                        Toast.makeText(getActivity(), "Failed", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void loadMusicList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Song> songs = AudioUtils.getAllSongs(getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMusicAdapter.addSongs(songs);
                    }
                });
            }
        }).start();
    }


    private void initEvent() {
        mMusicRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMusicRecyclerView.setAdapter(mMusicAdapter);
        mMusicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onClick(Song song) {
                playMusic(song);
            }
        });

        changePlayMode();

        mSeekBar.setOnSeekBarChangeListener(this);
        mSkipNextImageView.setOnClickListener(this);
        mSkipPrevImageView.setOnClickListener(this);
        mPlayOrPauseImageView.setOnClickListener(this);
        mPlayModeImageView.setOnClickListener(this);
    }


    private void playMusic(Song song) {
        try {
            mMediaPlayer.reset();

            mMediaPlayer.setDataSource(song.getFileUrl());
            mMediaPlayer.prepare();

            songIndex = mSongList.indexOf(song);

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setTitle(song.getTitle());
            activity.getSupportActionBar().setSubtitle(song.getSinger() + " - " + song.getAlbum());

            mMediaPlayer.start();
            mPlayOrPauseImageView.setImageResource(R.drawable.ic_pause);

            mSeekBar.setMax(song.getDuration());

            new Thread(mRunnable).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.skip_previous_image_view:
                if (!mMediaPlayer.isPlaying()) return;

                if (songIndex == 0) songIndex = mSongList.size() - 1;
                else songIndex--;


                playMusic(mSongList.get(songIndex));
                break;
            case R.id.skip_next_image_view:
                playNextSong();
                break;
            case R.id.play_or_pause_image_view:
                if (mMediaPlayer.isPlaying()) {
                    mPlayOrPauseImageView.setImageResource(R.drawable.ic_play);
                    mMediaPlayer.pause();
                } else {
                    if (songIndex == -1 && !mSongList.isEmpty()) {
                        if (playMode == PLAY_RANDOM)
                            songIndex = new Random().nextInt(mSongList.size());
                        else
                            songIndex = 0;
                        playMusic(mSongList.get(songIndex));
                    }
                    mPlayOrPauseImageView.setImageResource(R.drawable.ic_pause);
                    mMediaPlayer.start();

                    new Thread(mRunnable).start();
                }
                break;
            case R.id.play_mode_image_view:
                if (playMode == PLAY_QUEUE) playMode = REPEAT_ONE;
                else if (playMode == REPEAT_ONE) playMode = PLAY_RANDOM;
                else if (playMode == PLAY_RANDOM) playMode = PLAY_QUEUE;
                changePlayMode();
                break;
        }
    }

    private void playNextSong() {
        if (!mMediaPlayer.isPlaying()) return;

        if (playMode == PLAY_RANDOM) {
            songIndex = new Random().nextInt(mSongList.size());
        } else {
            if (songIndex == mSongList.size() - 1) songIndex = 0;
            else songIndex++;
        }
        playMusic(mSongList.get(songIndex));
    }


    private void changePlayMode() {
        if (playMode == REPEAT_ONE) mMediaPlayer.setLooping(true);
        else if (playMode == PLAY_QUEUE) {
            mMediaPlayer.setLooping(false);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playMusic(mSongList.get((songIndex + 1) % mSongList.size()));
                }
            });
        } else if (playMode == PLAY_RANDOM) {
            mMediaPlayer.setLooping(false);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playMusic(mSongList.get(new Random().nextInt(mSongList.size())));
                }
            });
        }
        mPlayModeImageView.setImageResource(playMode);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RequestPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (mMediaPlayer.isPlaying()) {
                mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
            }
        }
    };


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        if (songIndex == -1) {
            seekBar.setProgress(0);
            return;
        }

        if (seekBar.getProgress() == seekBar.getMax()) {
            if (playMode != REPEAT_ONE) {
                playNextSong();
            }
            seekBar.setProgress(0);
            return;
        }

        mMediaPlayer.seekTo(seekBar.getProgress());
        if (!mMediaPlayer.isPlaying())
            mMediaPlayer.start();
    }
}
