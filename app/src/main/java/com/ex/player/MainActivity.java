package com.ex.player;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.ex.libplayer.fragment.PlayFragment;
import com.ex.libplayer.listener.OnPlayListener;
import com.ex.libplayer.player.Player;

public class MainActivity extends AppCompatActivity implements OnPlayListener {


    private static final String URL = "https://download-a.akamaihd.net/files/media_periodical/ac/jwb_E_201509_01_r720P.mp4";
    private PlayFragment playFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFragment();
    }

    private void initFragment(){
        playFragment = new PlayFragment(Player.ENGINE_VLC);
        playFragment.setUrl(URL);
        playFragment.setTitle("JW");
        playFragment.setOnPlayListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.frame_layout, playFragment).commit();
    }

    @Override
    public void onPlayerPrepared() {

    }

    @Override
    public void onPlayerPlaying() {

    }

    @Override
    public void onPlayerBuffering() {

    }

    @Override
    public void onPlayerPause() {

    }

    @Override
    public void onPlayerPositionChanged() {

    }

    @Override
    public void onPlayerCompleted() {

    }

    @Override
    public void onPlayerError() {

    }
}
