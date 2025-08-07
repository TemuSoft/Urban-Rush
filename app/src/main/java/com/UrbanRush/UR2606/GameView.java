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

public class GameView extends View {
    private SharedPreferences sharedPreferences;
    private int screenX, screenY;
    private Resources resources;
    private Random random;
    boolean isPlaying = true;

    int life_remain = 3;
    private int xSpeed, ySpeed;
    private Context context;


    int max_h;
    long last_animate_time = System.currentTimeMillis();
    int animation = 150;
    int man_index = 0;
    boolean on_running = true;
    ArrayList<Bitmap> houses = new ArrayList<>();
    ArrayList<Bitmap> man = new ArrayList<>();
    Bitmap road;
    int r_y, r_w, r_h, move_left = -1;
    int m_x, m_y, m_c_x;
    ArrayList<Integer> road_x = new ArrayList<>();
    ArrayList<ArrayList<Integer>> house_data = new ArrayList<>();

    public GameView(Context mContext, int scX, int scY, Resources res, int level_amount) {
        super(mContext);
        screenX = scX;
        screenY = scY;
        resources = res;
        context = mContext;
        random = new Random();

        road = BitmapFactory.decodeResource(res, R.drawable.road);
        r_w = road.getWidth();
        r_h = road.getHeight();
        r_y = screenY - r_h;
        max_h = screenY - r_h * 2;

        m_c_x = r_h * 3;

        for (int i = 0; i < 9; i++) {
            int img = context.getResources().getIdentifier("home_" + i, "drawable", context.getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(res, img);
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            if (h > max_h) {
                w = w * max_h / h;
                h = max_h;
            }
            houses.add(Bitmap.createScaledBitmap(bitmap, w, h, false));
        }
        for (int i = 0; i < 9; i++) {
            int img = context.getResources().getIdentifier("man_" + i, "drawable", context.getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(res, img);
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            if (h > max_h) {
                w = w * max_h / h;
                h = max_h;
            }
            man.add(Bitmap.createScaledBitmap(bitmap, w, h, false));
        }

        road = Bitmap.createScaledBitmap(road, r_w, r_h, false);

        setSpeed();
        for (int i = 0; i < 2; i++)
            add_road();

        for (int i = 0; i < 5; i++)
            add_house();
    }

    private void add_house() {
        int s = house_data.size();
        int index = random.nextInt(houses.size());
        int x = random.nextInt(houses.get(index).getWidth());
        int y = r_y - houses.get(index).getHeight();
        if (s > 0) {
            int ii = house_data.get(s - 1).get(2);
            x += house_data.get(s - 1).get(0) + houses.get(ii).getWidth() + houses.get(index).getWidth() / 2 + random.nextInt(houses.get(index).getWidth() / 2);
        }

        ArrayList<Integer> data = new ArrayList<>();
        data.add(x);
        data.add(y);
        data.add(index);
        house_data.add(data);
    }

    private void add_road() {
        int s = road_x.size();
        int x = 0;
        if (s > 0)
            x += road_x.get(s - 1) + r_w;

        road_x.add(x);
    }

    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        canvas.drawColor(Color.TRANSPARENT);


        for (int i = 0; i < road_x.size(); i++) {
            int x = road_x.get(i);
            if (x > screenX || x + r_w < 0)
                continue;

            canvas.drawBitmap(road, x, r_y, paint);
        }

        for (int i = 0; i < house_data.size(); i++) {
            int x = house_data.get(i).get(0);
            int y = house_data.get(i).get(1);
            int index = house_data.get(i).get(2);

            if (x > screenX || x + houses.get(index).getWidth() < 0)
                continue;

            canvas.drawBitmap(houses.get(index), x, y, paint);
        }

        int x = m_c_x - man.get(man_index).getWidth() / 2;
        int y = screenY - r_h / 3 - man.get(man_index).getHeight();
        canvas.drawBitmap(man.get(man_index), x, y, paint);
    }

    private void setSpeed() {
        xSpeed = screenX / 300;
        ySpeed = screenY / 300;
    }

    public void update() {
        for (int i = 0; i < road_x.size(); i++) {
            int x = road_x.get(i);
            x += xSpeed * move_left;
            road_x.set(i, x);

            if (x + r_w <= screenX)
                add_road();

            if (x + r_w < 0) {
                road_x.remove(i);
                add_road();
                break;
            }
        }

        for (int i = 0; i < house_data.size(); i++) {
            int x = house_data.get(i).get(0);
            int y = house_data.get(i).get(1);
            int index = house_data.get(i).get(2);

            x += xSpeed * move_left;
            house_data.get(i).set(0, x);


            if (x + houses.get(index).getWidth() <= screenX)
                add_house();

            if (x + houses.get(index).getWidth() < 0) {
                house_data.remove(i);
                add_house();
                break;
            }
        }

        if (last_animate_time + animation < System.currentTimeMillis()) {
            last_animate_time = System.currentTimeMillis();
            man_index++;

            if (on_running) {
                if (man_index == 7)
                    man_index = 0;
            } else {
                // on down animation will be handle here
                man_index = 8;
            }
        }

        invalidate();
    }
}