package com.UrbanRush.UR2606;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;

import java.util.HashMap;
import java.util.Map;

public class Player {

    private static SharedPreferences sharedPreferences;
	private static boolean onVibrating;
    private static Vibrator v;
    private static LayoutInflater inflate;
    private static AlertDialog.Builder builder;
    public static MediaPlayer all_screens, button, music;

    public static void all_screens(Context context, int audio) {
        all_screens = MediaPlayer.create(context, audio);
        all_screens.setLooping(true);
    }

    public static void button(Context context, int audio) {
        button = MediaPlayer.create(context, audio);
        button.setLooping(false);
    }

    public static void music(Context context, int audio) {
        music = MediaPlayer.create(context, audio);
        music.setLooping(false);
    }


    public static void StopAll() {
        try {
            all_screens.pause();
            button.pause();
            music.pause();
        } catch (Exception e) {

        }
    }

    public static void button(boolean soundMute) {
        if (!soundMute)
            button.start();
    }

    public static String convert(long seconds) {
        long minutes = seconds / 60;
        long second = seconds % 60;

        return String.format("%02d:%02d", minutes, second);
    }
}