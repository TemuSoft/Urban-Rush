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
    boolean isPlaying = true, game_over;
    long game_over_time;

    int life_remain = 3;
    private int xSpeed, ySpeed;
    private Context context;

    int tap_x = -1, tap_y = -1;
    int max_h, max_man_y_jum;
    long last_animate_time = System.currentTimeMillis();
    long last_jump_down_time;
    int animation = 200, duration = 1000;
    int man_index = 0;
    boolean on_jumping_up, on_jumping_down;
    ArrayList<Bitmap> houses = new ArrayList<>();
    ArrayList<Bitmap> man = new ArrayList<>();
    Bitmap road, iron, jigsaw;
    int r_y, r_w, r_h, move_left = -1, move_up = 0;
    int m_x, m_y, m_c_x;
    int man_w, man_h, man_ground_original, man_ground;
    int iron_y, iron_w, iron_h;
    int jigsaw_y, jigsaw_w, jigsaw_h;

    ArrayList<Integer> road_x = new ArrayList<>();
    ArrayList<ArrayList<Integer>> iron_data = new ArrayList<>();
    ArrayList<ArrayList<Integer>> jigsaw_data = new ArrayList<>();
    ArrayList<ArrayList<Integer>> house_data = new ArrayList<>();

    public GameView(Context mContext, int scX, int scY, Resources res, int level_amount) {
        super(mContext);
        screenX = scX;
        screenY = scY;
        resources = res;
        context = mContext;
        random = new Random();

        road = BitmapFactory.decodeResource(res, R.drawable.road);
        iron = BitmapFactory.decodeResource(res, R.drawable.iron);
        jigsaw = BitmapFactory.decodeResource(res, R.drawable.jigsaw);

        init_size(res);

        iron = Bitmap.createScaledBitmap(iron, iron_w, iron_h, false);
        jigsaw = Bitmap.createScaledBitmap(jigsaw, jigsaw_w, jigsaw_h, false);
        road = Bitmap.createScaledBitmap(road, r_w, r_h, false);

        setSpeed();
        for (int i = 0; i < 3; i++)
            add_road();

        for (int i = 0; i < 5; i++) {
            add_house();
            add_iron();
            add_jigsaw();
        }
    }

    private void init_size(Resources res) {
        r_w = road.getWidth();
        r_h = road.getHeight();
        r_y = screenY - r_h;
        max_h = screenY - r_h * 2;

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

        int max_h = r_h * 3 / 4;
        iron_w = iron.getWidth();
        iron_h = iron.getHeight();
        if (iron_h > max_h) {
            iron_w = iron_w * max_h / iron_h;
            iron_h = max_h;
        }
        jigsaw_w = iron.getWidth();
        jigsaw_h = iron.getHeight();
        if (jigsaw_h > max_h) {
            jigsaw_w = jigsaw_w * max_h / jigsaw_h;
            jigsaw_h = max_h;
        }

        jigsaw_y = screenY - r_h / 2 - jigsaw_h / 2;
        man_ground = screenY - r_h / 3;
        man_ground_original = man_ground;

        man_w = man.get(man_index).getWidth();
        man_h = man.get(man_index).getHeight();
        m_c_x = r_h * 3;
        m_x = m_c_x - man_w / 2;
        m_y = man_ground - man_h;
        max_man_y_jum = screenY - r_h * 2 - man_h;
        iron_y = m_y;
    }

    private void add_house() {
        int s = house_data.size();
        int index = random.nextInt(houses.size());
        int y = r_y - houses.get(index).getHeight();
        int x;

        ArrayList<Integer> data = new ArrayList<>();

        int gap = houses.get(index).getWidth() / 2 + random.nextInt(houses.get(index).getWidth() / 2);
        if (move_left == -1) {
            if (s > 0) {
                int last_house_x = house_data.get(s - 1).get(0);
                int last_house_index = house_data.get(s - 1).get(2);
                int last_house_width = houses.get(last_house_index).getWidth();
                x = last_house_x + last_house_width + gap;
            } else {
                x = 0;
            }
            data.add(x);
            data.add(y);
            data.add(index);
            house_data.add(data);
        } else if (move_left == 1) {
            if (s > 0) {
                int first_house_x = house_data.get(0).get(0);
                int new_house_width = houses.get(index).getWidth();
                x = first_house_x - new_house_width - gap;
            } else {
                x = -houses.get(index).getWidth() - gap;
            }
            data.add(x);
            data.add(y);
            data.add(index);
            house_data.add(0, data);
        }
    }

    private void add_road() {
        int s = road_x.size();
        if (move_left == -1) {
            int last_x = 0;
            if (s > 0) last_x = road_x.get(s - 1) + r_w;
            road_x.add(last_x);
        } else if (move_left == 1) {
            int first_x = 0;
            if (s > 0) first_x = road_x.get(0) - r_w;
            road_x.add(0, first_x);
        }
    }

    private void add_iron() {
        int s = iron_data.size();
        int gap = screenX + random.nextInt(screenX);
        int direction = (random.nextBoolean() ? -1 : 1);
        int x;
        ArrayList<Integer> data = new ArrayList<>();

        if (move_left == -1) {
            if (s > 0) {
                x = iron_data.get(s - 1).get(0) + gap;
            } else {
                x = screenX + gap;
            }
            data.add(x);
            data.add(direction);
            iron_data.add(data);

        } else if (move_left == 1) {
            if (s > 0) {
                x = iron_data.get(0).get(0) - gap;
            } else {
                x = -gap - iron_w;
            }
            data.add(x);
            data.add(direction);
            iron_data.add(0, data);
        }
    }

    private void add_jigsaw() {
        int s = jigsaw_data.size();
        int gap = screenX + random.nextInt(screenX);
        int direction = (random.nextBoolean() ? -1 : 1);
        int x;
        ArrayList<Integer> data = new ArrayList<>();

        if (move_left == -1) {
            if (s > 0) {
                x = jigsaw_data.get(s - 1).get(0) + gap;
            } else {
                x = screenX + gap;
            }
            data.add(x);
            data.add(direction);
            jigsaw_data.add(data);

        } else if (move_left == 1) {
            if (s > 0) {
                x = jigsaw_data.get(0).get(0) - gap;
            } else {
                x = -gap - jigsaw_w;
            }
            data.add(x);
            data.add(direction);
            jigsaw_data.add(0, data);
        }
    }

    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        canvas.drawColor(Color.TRANSPARENT);

        for (int i = 0; i < road_x.size(); i++) {
            int x = road_x.get(i);
            if (x > screenX || x + r_w < 0) continue;

            canvas.drawBitmap(road, x, r_y, paint);
        }
        for (int i = 0; i < house_data.size(); i++) {
            int x = house_data.get(i).get(0);
            int y = house_data.get(i).get(1);
            int index = house_data.get(i).get(2);

            if (x > screenX || x + houses.get(index).getWidth() < 0) continue;

            canvas.drawBitmap(houses.get(index), x, y, paint);
        }

        canvas.drawRect(0, max_man_y_jum, screenX, max_man_y_jum + 10, paint);

        canvas.save();
        canvas.scale(move_left * -1, 1, m_x + man_w / 2, m_y + man_h / 2);
        canvas.drawBitmap(man.get(man_index), m_x, m_y, paint);
        canvas.restore();

        for (int i = 0; i < iron_data.size(); i++) {
            int x = iron_data.get(i).get(0);
            int direction = iron_data.get(i).get(1);

            if (x > screenX || x + iron_w < 0) continue;

            canvas.save();
            canvas.scale(direction, 1, x + iron_w / 2, iron_y + iron_h / 2);
            canvas.drawBitmap(iron, x, iron_y, paint);
            canvas.restore();
        }
        for (int i = 0; i < jigsaw_data.size(); i++) {
            int x = jigsaw_data.get(i).get(0);
            int direction = jigsaw_data.get(i).get(1);

            if (x > screenX || x + jigsaw_w < 0) continue;

            canvas.save();
            canvas.scale(direction, 1, x + jigsaw_w / 2, jigsaw_y + jigsaw_h / 2);
            canvas.drawBitmap(jigsaw, x, jigsaw_y, paint);
            canvas.restore();
        }
    }

    private void setSpeed() {
        xSpeed = screenX / 300;
        ySpeed = screenY / 300;
    }

    public void update() {
        if (life_remain < 1) {
            game_over = true;
            game_over_time = System.currentTimeMillis();
        }

        manage_man();
        move_bitmap();
        check_removed();
        check_intersection();

        if (on_jumping_down && last_jump_down_time + duration * 2 < System.currentTimeMillis()) {
            on_jumping_down = false;
            last_jump_down_time = 0;
        }

        invalidate();
    }

    private void manage_man() {
        man_w = man.get(man_index).getWidth();
        man_h = man.get(man_index).getHeight();
        m_x = m_c_x - man_w / 2;
        m_y = man_ground - man_h;

        if (on_jumping_up) {
            man_ground += ySpeed * 3 * move_up;

            if (man_ground - man_h < max_man_y_jum) move_up = 1;
            else if (man_ground >= man_ground_original) {
                man_ground = man_ground_original;
                move_up = 0;
                on_jumping_up = false;
            }
        }

        if (last_animate_time + animation < System.currentTimeMillis()) {
            last_animate_time = System.currentTimeMillis();
            man_index++;

            if (!on_jumping_down) {
                if (man_index > 7) man_index = 0;
            } else {
                // on down && dump animation will be handle here
                man_index = 8;
            }
        }
    }

    private void move_bitmap() {
        for (int i = 0; i < road_x.size(); i++) {
            int x = road_x.get(i);
            x += xSpeed * move_left;
            road_x.set(i, x);
        }
        for (int i = 0; i < iron_data.size(); i++) {
            int x = iron_data.get(i).get(0);

            x += xSpeed * move_left;
            iron_data.get(i).set(0, x);
        }
        for (int i = 0; i < jigsaw_data.size(); i++) {
            int x = jigsaw_data.get(i).get(0);

            x += xSpeed * move_left;
            jigsaw_data.get(i).set(0, x);
        }
        for (int i = 0; i < house_data.size(); i++) {
            int x = house_data.get(i).get(0);

            x += xSpeed * move_left;
            house_data.get(i).set(0, x);
        }
    }

    private void check_removed() {
        if (move_left == -1) {
            if (road_x.get(0) + r_w < 0) {
                road_x.remove(0);
                add_road();
            }

            if (iron_data.get(0).get(0) + iron_w < 0) {
                iron_data.remove(0);
                add_iron();
            }

            if (jigsaw_data.get(0).get(0) + jigsaw_w < 0) {
                jigsaw_data.remove(0);
                add_jigsaw();
            }

            if (house_data.get(0).get(0) + houses.get(house_data.get(0).get(2)).getWidth() < 0) {
                house_data.remove(0);
                add_house();
            }
        } else if (move_left == 1) {
            if (road_x.get(road_x.size() - 1) > screenX) {
                road_x.remove(road_x.size() - 1);
                add_road();
            }

            if (iron_data.get(iron_data.size() - 1).get(0) > screenX) {
                iron_data.remove(iron_data.size() - 1);
                add_iron();
            }

            if (jigsaw_data.get(jigsaw_data.size() - 1).get(0) > screenX) {
                jigsaw_data.remove(jigsaw_data.size() - 1);
                add_jigsaw();
            }

            if (house_data.get(house_data.size() - 1).get(0) > screenX) {
                house_data.remove(house_data.size() - 1);
                add_house();
            }
        }
    }

    private void check_intersection() {
        for (int i = 0; i < iron_data.size(); i++) {
            int x = iron_data.get(i).get(0);
            Rect iron = new Rect(x, iron_y, x + iron_w, iron_y + iron_h);

            if (Rect.intersects(getPlayerCollision(), iron)) {
                life_remain--;
                iron_data.remove(i);
                add_iron();
                break;
            }
        }
        for (int i = 0; i < jigsaw_data.size(); i++) {
            int x = jigsaw_data.get(i).get(0);
            Rect jigsaw = new Rect(x, jigsaw_y, x + jigsaw_w, jigsaw_y + jigsaw_h);

            if (Rect.intersects(getPlayerCollision(), jigsaw)) {
                life_remain--;
                jigsaw_data.remove(i);
                add_jigsaw();
                break;
            }
        }
    }

    private Rect getPlayerCollision() {
        int m_w = man_w / 6;
        int m_h = man_h / 10;
        return new Rect(m_x + m_w, m_y + m_h, m_x + man_w - m_w, m_y + man_h - m_h);
    }
}