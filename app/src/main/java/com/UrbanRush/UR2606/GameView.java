package com.UrbanRush.UR2606;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View{
    private SharedPreferences sharedPreferences;
    private int screenX, screenY;
    private Resources resources;
    private Random random;
    boolean isPlaying = true;

    int life_remain = 3;
    private int xSpeed, ySpeed;
    private Context context;

    public GameView(Context mContext, int scX, int scY, Resources res, int level_amount) {
        super(mContext);
        screenX = scX;
        screenY = scY;
        resources = res;
        context = mContext;
        random = new Random();


        setSpeed();
    }

    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        canvas.drawColor(Color.TRANSPARENT);



    }

    private void setSpeed() {
        xSpeed = screenX / 80;
        ySpeed = screenY / 80;
    }

    public void update() {

        invalidate();
    }
}