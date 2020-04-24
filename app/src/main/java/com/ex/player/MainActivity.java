package com.ex.player;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ex.lib.security.RSA;
import com.ex.libplayer.controller.ExPlayerController;
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
        controller.setBtnFullScreenVisibility(true);
        controller.setBtnMoreVisibility(false);
        exPlayView.setController(controller);
        exPlayView.prepare(Player.ENGINE_IJK);
    }

}
