package com.example.musicplayer30.activity;


import androidx.fragment.app.Fragment;

import com.example.musicplayer30.R;
import com.example.musicplayer30.base.FragmentContainerActivity;
import com.example.musicplayer30.fragment.PlayLocalMusicFragment;

public class MusicPlayerActivity extends FragmentContainerActivity {
    @Override
    protected Fragment createFragment() {
        return PlayLocalMusicFragment.newInstance();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.fragment_container;
    }
}
