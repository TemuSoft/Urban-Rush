package com.UrbanRush.UR2606;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnTouchListener {
    private LinearLayout layout_dialog, layout_blur, layout_time, layout_progress;
    private LinearLayout layout_sound, layout_music, continues_again, home, layout_audio;
    private TextView total_time, title, text_continues_again;
    private ProgressBar progressBar1, progressBar2;
    private ImageView pause, heart_0, heart_1, heart_2;
    private TextView time;
    private LinearLayout layout_canvas;
    private LayoutInflater inflate;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isMute, soundMute;
    private Intent intent;
    private String lang;
    private AlertDialog.Builder builder;
    private Random random;
    private Handler handler;
    private GameView gameView;
    private long start_time = System.currentTimeMillis();
    private long pause_time;
    private boolean alert_is_on_pause = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPreferences = getSharedPreferences("u95reushUR26", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isMute = sharedPreferences.getBoolean("isMute", false);
        soundMute = sharedPreferences.getBoolean("soundMute", false);
        lang = sharedPreferences.getString("lang", "");

        setContentView(R.layout.activity_game);

        builder = new AlertDialog.Builder(this);
        random = new Random();
        handler = new Handler();

        pause = findViewById(R.id.pause);
        time = findViewById(R.id.time);
        heart_0 = findViewById(R.id.heart_0);
        heart_1 = findViewById(R.id.heart_1);
        heart_2 = findViewById(R.id.heart_2);

        layout_audio = findViewById(R.id.layout_audio);
        layout_sound = findViewById(R.id.layout_sound);
        layout_music = findViewById(R.id.layout_music);
        continues_again = findViewById(R.id.continues_again);
        home = findViewById(R.id.home);
        layout_progress = findViewById(R.id.layout_progress);
        layout_time = findViewById(R.id.layout_time);
        layout_blur = findViewById(R.id.layout_blur);
        layout_dialog = findViewById(R.id.layout_dialog);
        title = findViewById(R.id.title);
        text_continues_again = findViewById(R.id.text_continues_again);
        total_time = findViewById(R.id.total_time);
        progressBar1 = findViewById(R.id.progressBar1);
        progressBar2 = findViewById(R.id.progressBar2);

        layout_canvas = (LinearLayout) findViewById(R.id.layout_canvas);
        inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Player.button(soundMute);
                pause_time = System.currentTimeMillis();
                alert_is_on_pause = true;
                pauseDialog();
            }
        });

        layout_canvas.removeAllViews();
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);

        int w = point.x;
        int h = point.y;
        gameView = new GameView(this, w, h, getResources(), 0);
        gameView.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        layout_canvas.addView(gameView);

        layout_canvas.setOnTouchListener(this);
        layout_blur.setVisibility(GONE);
        layout_dialog.setVisibility(GONE);

        continues_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Player.button(soundMute);

                if (alert_is_on_pause) {
                    long gap = System.currentTimeMillis() - pause_time;
                    start_time += gap;
                    gameView.last_animate_time += gap;
                    gameView.last_jump_down_time += gap;
                    gameView.game_over_time += gap;

                    layout_blur.setVisibility(GONE);
                    layout_dialog.setVisibility(GONE);
                    gameView.isPlaying = true;
                    reloading_UI();
                } else {
                    intent = new Intent(GameActivity.this, GameActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Player.button(soundMute);

                intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void reloading_UI() {
        Runnable r = new Runnable() {
            public void run() {
                if (gameView.isPlaying) {
                    if (!gameView.game_over) gameView.update();

                    time.setText(Player.convert((int) (System.currentTimeMillis() - start_time) / 1000));
                    if (gameView.life_remain > 2) {
                        heart_0.setImageResource(R.drawable.heart_1);
                        heart_1.setImageResource(R.drawable.heart_1);
                        heart_2.setImageResource(R.drawable.heart_1);
                    } else if (gameView.life_remain > 1) {
                        heart_0.setImageResource(R.drawable.heart_1);
                        heart_1.setImageResource(R.drawable.heart_1);
                        heart_2.setImageResource(R.drawable.heart_0);
                    } else if (gameView.life_remain > 0) {
                        heart_0.setImageResource(R.drawable.heart_1);
                        heart_1.setImageResource(R.drawable.heart_0);
                        heart_2.setImageResource(R.drawable.heart_0);
                    } else if (gameView.game_over && gameView.game_over_time + gameView.duration < System.currentTimeMillis()) {
                        alert_is_on_pause = false;
                        game_over();
                    }

                    reloading_UI();
                }
            }
        };
        handler.postDelayed(r, 20);
    }

    public void pauseDialog() {
        gameView.isPlaying = false;
        layout_blur.setVisibility(VISIBLE);
        layout_dialog.setVisibility(VISIBLE);
        layout_audio.setVisibility(VISIBLE);

        text_continues_again.setText(getResources().getString(R.string.continues));
        title.setText(getResources().getString(R.string.game_paused));
        layout_time.setVisibility(GONE);
        layout_progress.setVisibility(GONE);

        layout_sound.setBackgroundResource(R.drawable.off);
        layout_music.setBackgroundResource(R.drawable.off);

        if (!isMute)
            layout_music.setBackgroundResource(R.drawable.on);

        if (!soundMute)
            layout_sound.setBackgroundResource(R.drawable.on);

        layout_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Player.button(soundMute);

                isMute = !isMute;
                if (isMute) {
                    Player.all_screens.stop();
                    layout_music.setBackgroundResource(R.drawable.off);
                } else {
                    layout_music.setBackgroundResource(R.drawable.on);
                    Player.all_screens.stop();
                }

                editor.putBoolean("isMute", isMute);
                editor.apply();
            }
        });

        layout_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Player.button(soundMute);

                soundMute = !soundMute;
                if (soundMute) {
                    Player.button.stop();
                    layout_sound.setBackgroundResource(R.drawable.off);
                } else {
                    layout_sound.setBackgroundResource(R.drawable.on);
                    Player.button.stop();
                }

                editor.putBoolean("soundMute", soundMute);
                editor.apply();
            }
        });
    }


    private void game_over() {
        gameView.isPlaying = false;
        int time_value = (int) (System.currentTimeMillis() - start_time) / 1000;
        editor.putInt("game_time", time_value);
        editor.apply();

        total_time.setText(Player.convert(time_value));
        layout_audio.setVisibility(GONE);
        layout_blur.setVisibility(VISIBLE);
        layout_dialog.setVisibility(VISIBLE);

        text_continues_again.setText(getResources().getString(R.string.again));
        title.setText(getResources().getString(R.string.game_over));
        layout_time.setVisibility(VISIBLE);
        layout_progress.setVisibility(VISIBLE);

        save_best_score();
    }

    private void save_best_score() {
        if (sharedPreferences.getInt("game_time", 0) > sharedPreferences.getInt("best_time", 0)) {
            editor.putInt("best_time", sharedPreferences.getInt("game_time", 0));
            editor.apply();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.isPlaying = false;
        if (isMute)
            Player.all_screens.pause();

        pause_time = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.isPlaying = true;
        isMute = sharedPreferences.getBoolean("isMute", false);
        if (!isMute)
            Player.all_screens.start();

        if (pause_time != 0) {
            long gap = System.currentTimeMillis() - pause_time;
            start_time += gap;
            gameView.last_animate_time += gap;
            gameView.last_jump_down_time += gap;
            gameView.game_over_time += gap;
        }

        reloading_UI();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!gameView.game_over) processActionDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!gameView.game_over) processActionMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                if (!gameView.game_over) processActionUp(x, y);
                break;
        }
        return true;
    }

    private void processActionDown(int x, int y) {
        gameView.tap_x = x;
        gameView.tap_y = y;
    }

    private void processActionUp(int xp, int yp) {
        Rect clicked = new Rect(xp, yp, xp, yp);
        int min_dix = gameView.r_h;
        int x_dif = xp - gameView.tap_x;
        int y_dif = yp - gameView.tap_y;

        if (Math.abs(x_dif) > Math.abs(y_dif) && Math.abs(x_dif) > min_dix) {
            if (x_dif > 0) gameView.move_left = -1;
            else gameView.move_left = 1;
        } else if (Math.abs(y_dif) > Math.abs(x_dif) && Math.abs(y_dif) > min_dix) {
            if (y_dif > 0) {
                gameView.move_up = 1;
                gameView.on_jumping_down = true;
                gameView.on_jumping_up = false;
                gameView.last_jump_down_time = System.currentTimeMillis();
            } else {
                gameView.move_up = -1;
                gameView.on_jumping_up = true;
                gameView.on_jumping_down = false;
            }
        }

        gameView.tap_x = -1;
        gameView.tap_y = -1;
    }

    private void processActionMove(int x, int y) {

    }
}