package com.ex.player;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.ex.libplayer.controller.ExPlayerController;
import com.ex.libplayer.enu.EnumPlayStatus;
import com.ex.libplayer.listener.OnPlayListener;
import com.ex.libplayer.player.Player;
import com.ex.libplayer.view.ExPlayView;

public class MainActivity extends AppCompatActivity {


    private static final String URL = "https://download-a.akamaihd.net/files/media_periodical/ac/jwb_E_201509_01_r720P.mp4";
    private ExPlayView exPlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPlayView();
        setBtnNext();
    }

    private void setBtnNext(){
        Button btNext = findViewById(R.id.bt_next);
        btNext.setOnClickListener(v -> {
            if(exPlayView != null) {
                exPlayView.setDataSource(URL, "JW1");
                exPlayView.restart();
            }
        });
    }

    private void initPlayView(){
        exPlayView = findViewById(R.id.ex_play_view);
        exPlayView.setDataSource(URL, "JW");
        ExPlayerController  controller = new ExPlayerController(this);
        controller.setBtnFullScreenVisibility(false);
        controller.setBtnMoreVisibility(false);
        exPlayView.setController(controller);
        exPlayView.prepare(Player.ENGINE_EXO);
        exPlayView.setOnPlayListener(new OnPlayListener() {
            @Override
            public void onPlayerSizeChanged(int videoWidth, int videoHeight) {

            }

            @Override
            public void onPlayerEngineChanged(int mode) {

            }

            @Override
            public void onPlayerStatusChanged(EnumPlayStatus status) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        exPlayView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        exPlayView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exPlayView.release();
    }
}
