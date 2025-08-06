package com.UrbanRush.UR2606;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class LeaderBoardActivity extends AppCompatActivity {
    private LinearLayout menu, layout_vertical;
    private TextView number, name, best_time;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isMute, soundMute;
    private Intent intent;
    private LayoutInflater inflate;
    private Random random;
    private String lang;
    private int game_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPreferences = getSharedPreferences("u95reushUR26", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isMute = sharedPreferences.getBoolean("isMute", false);
        soundMute = sharedPreferences.getBoolean("soundMute", false);
        lang = sharedPreferences.getString("lang", "");
        game_time = sharedPreferences.getInt("game_time", 0);

        setContentView(R.layout.activity_leader_board);

        inflate = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        layout_vertical = findViewById(R.id.layout_vertical);
        menu = findViewById(R.id.menu);

        number = findViewById(R.id.number);
        name = findViewById(R.id.name);
        best_time = findViewById(R.id.best_time);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Player.button(soundMute);

                intent = new Intent(LeaderBoardActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        load_UI_view();
    }

    private void load_UI_view() {
        layout_vertical.removeAllViews();

        ArrayList<Integer> data = new ArrayList<>();
        data.add(game_time);

        for (int i = 0; i < 5; i++) {
            int value = 120 + random.nextInt(game_time + 180);
            if (random.nextInt(100) == 49)
                value = random.nextInt(game_time);

            if (game_time == value)
                value += 1;
            data.add(random.nextInt(value));
        }
        data.sort(Collections.reverseOrder());
        int index = data.indexOf(game_time);
        data.remove(index);

        number.setText("#" + (index + 1));
        name.setText("You");
        best_time.setText(Player.convert(game_time));

        for (int i = 0; i < data.size(); i++) {
            View card = inflate.inflate(R.layout.card, null);
            TextView number = card.findViewById(R.id.number);
            TextView name = card.findViewById(R.id.name);
            TextView best_time = card.findViewById(R.id.best_time);

            if (i == index + 1)
                continue;

            number.setText("#" + (i + 1));
            name.setText("Player " + (i + 1));
            best_time.setText(Player.convert(data.get(i)));

            layout_vertical.addView(card);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (!isMute)
            Player.all_screens.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMute = sharedPreferences.getBoolean("isMute", false);
        if (!isMute)
            Player.all_screens.start();
    }

    @Override
    public void onBackPressed() {
        return;
    }
}