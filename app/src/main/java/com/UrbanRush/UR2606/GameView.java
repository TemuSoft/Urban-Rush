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
    long game_over_time, last_open_hatch_intersection_time;

    int life_remain = 3;
    private int xSpeed, ySpeed;
    private Context context;

    int active_open_hatch_index;
    int tap_x = -1, tap_y = -1;
    int max_h, max_man_y_jum;
    long last_animate_time = System.currentTimeMillis();
    long last_jump_down_time;
    int animation = 200, duration = 1000;
    int man_index = 0;
    boolean on_jumping_up, on_jumping_down;
    ArrayList<Bitmap> houses = new ArrayList<>();
    ArrayList<Bitmap> man = new ArrayList<>();
    Bitmap road, iron, jigsaw, tree, hatch_close, hatch_open;
    int r_y, r_w, r_h, move_left = -1, move_up = 0;
    int m_x, m_y, m_c_x, tree_w, tree_h;
    int hatch_w_o, hatch_h_o, hatch_w_c, hatch_h_c;
    int man_w, man_h, man_ground_original, man_ground;
    int iron_y, iron_w, iron_h;
    int jigsaw_y, jigsaw_w, jigsaw_h;
    int IRON = 0, JIGSAW = 1, TREE = 2, HATCH_OPEN = 3, HATCH_CLOSE = 4;

    ArrayList<Integer> road_x = new ArrayList<>();
    ArrayList<ArrayList<Integer>> obstacle_data = new ArrayList<>();
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
        tree = BitmapFactory.decodeResource(res, R.drawable.tree);
        hatch_open = BitmapFactory.decodeResource(res, R.drawable.hatch_open);
        hatch_close = BitmapFactory.decodeResource(res, R.drawable.hatch_close);

        init_size(res);

        iron = Bitmap.createScaledBitmap(iron, iron_w, iron_h, false);
        jigsaw = Bitmap.createScaledBitmap(jigsaw, jigsaw_w, jigsaw_h, false);
        road = Bitmap.createScaledBitmap(road, r_w, r_h, false);
        tree = Bitmap.createScaledBitmap(tree, tree_w, tree_h, false);
        hatch_open = Bitmap.createScaledBitmap(hatch_open, hatch_w_o, hatch_h_o, false);
        hatch_close = Bitmap.createScaledBitmap(hatch_close, hatch_w_c, hatch_h_c, false);

        setSpeed();
        for (int i = 0; i < 3; i++)
            add_road();

        for (int i = 0; i < 5; i++) {
            add_house();
            add_obstacle();
        }
    }

    private void add_obstacle() {
        int type = random.nextInt(5);
        type = 3;
        int s = obstacle_data.size();
        int more = random.nextBoolean() ? random.nextInt(screenX / 4) + screenX / 4 : 0;
        int gap = screenX / 4 + random.nextInt(screenX / 4) + more;
        int direction = (random.nextBoolean() ? -1 : 1);
        int x;
        ArrayList<Integer> data = new ArrayList<>();

        if (move_left == -1) {
            if (s > 0) {
                x = obstacle_data.get(s - 1).get(0) + gap;
            } else {
                x = screenX + gap;
            }
            data.add(x);
            data.add(direction);
            data.add(type);
            obstacle_data.add(data);

        } else if (move_left == 1) {
            if (s > 0) {
                x = obstacle_data.get(0).get(0) - gap;
            } else {
                x = -gap - iron_w;
            }
            data.add(x);
            data.add(direction);
            data.add(type);
            obstacle_data.add(0, data);
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

        int max_h_i_j = r_h * 3 / 4;
        iron_w = iron.getWidth();
        iron_h = iron.getHeight();
        if (iron_h > max_h_i_j) {
            iron_w = iron_w * max_h_i_j / iron_h;
            iron_h = max_h_i_j;
        }
        jigsaw_w = iron.getWidth();
        jigsaw_h = iron.getHeight();
        if (jigsaw_h > max_h_i_j) {
            jigsaw_w = jigsaw_w * max_h_i_j / jigsaw_h;
            jigsaw_h = max_h_i_j;
        }

        tree_w = tree.getWidth();
        tree_h = tree.getHeight();
        if (tree_h > max_h) {
            tree_w = tree_w * max_h / tree_h;
            tree_h = max_h;
        }

        int max_hatch_h = tree_h / 4;
        hatch_w_o = hatch_open.getWidth();
        hatch_h_o = hatch_open.getHeight();
        if (hatch_h_o > max_hatch_h) {
            hatch_w_o = hatch_w_o * max_hatch_h / hatch_h_o;
            hatch_h_o = max_hatch_h;
        }

        hatch_w_c = hatch_close.getWidth();
        hatch_h_c = hatch_close.getHeight();
        if (hatch_h_c > max_hatch_h) {
            hatch_w_c = hatch_w_c * max_hatch_h / hatch_h_c;
            hatch_h_c = max_hatch_h;
        }

        jigsaw_y = screenY - r_h / 2 - jigsaw_h / 2;
        man_ground = screenY - r_h / 3;
        man_ground_original = man_ground;

        man_w = man.get(man_index).getWidth();
        man_h = man.get(man_index).getHeight();
//        m_c_x = r_h * 3;
        m_c_x = screenX / 2;
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

        for (int i = 0; i < obstacle_data.size(); i++) {
            int x = obstacle_data.get(i).get(0);
            int direction = obstacle_data.get(i).get(1);
            int type = obstacle_data.get(i).get(2);
            int w = iron_w;
            if (type == JIGSAW)
                w = jigsaw_w;
            else if (type == TREE)
                w = tree_w;
            else if (type == HATCH_OPEN)
                w = hatch_w_o;
            else if (type == HATCH_CLOSE)
                w = hatch_w_c;
            boolean iii = x > screenX || x + w < 0;
            if (iii) continue;

            canvas.save();
            if (type == IRON) {
                canvas.scale(direction, 1, x + iron_w / 2, iron_y + iron_h / 2);
                canvas.drawBitmap(iron, x, iron_y, paint);
            } else if (type == JIGSAW) {
                canvas.scale(direction, 1, x + jigsaw_w / 2, jigsaw_y + jigsaw_h / 2);
                canvas.drawBitmap(jigsaw, x, jigsaw_y, paint);
            } else if (type == HATCH_OPEN) {
                int y = man_ground_original - hatch_h_o;
                canvas.scale(direction, 1, x + hatch_w_o / 2, y + hatch_h_o / 2);
                canvas.drawBitmap(hatch_open, x, y, paint);
            } else if (type == HATCH_CLOSE) {
                int y = man_ground_original - hatch_h_c;
                canvas.scale(direction, 1, x + hatch_w_c / 2, y + hatch_h_c / 2);
                canvas.drawBitmap(hatch_close, x, y, paint);
            }
            canvas.restore();
        }

        canvas.save();
        canvas.scale(move_left * -1, 1, m_x + man_w / 2, m_y + man_h / 2);
        if (last_open_hatch_intersection_time != 0) {
            canvas.translate(0, r_h);
            canvas.scale(move_left * -1, -1, m_x + man_w / 2, m_y + man_h / 2);
            canvas.rotate(90, m_x + man_w / 2, m_y + man_h / 2);
        }
        canvas.drawBitmap(man.get(man_index), m_x, m_y, paint);
        canvas.restore();

        for (int i = 0; i < obstacle_data.size(); i++) {
            int x = obstacle_data.get(i).get(0);
            int direction = obstacle_data.get(i).get(1);
            int type = obstacle_data.get(i).get(2);
            int w = iron_w;
            if (type == JIGSAW)
                w = jigsaw_w;
            else if (type == TREE)
                w = tree_w;
            else if (type == HATCH_OPEN)
                w = hatch_w_o;
            else if (type == HATCH_CLOSE)
                w = hatch_w_c;
            boolean iii = x > screenX || x + w < 0;
            if (iii) continue;

            canvas.save();

            if (type == TREE) {
                int y = man_ground_original - tree_h;
                canvas.scale(direction, 1, x + tree_w / 2, y + tree_h / 2);
                canvas.drawBitmap(tree, x, y, paint);
            }
            canvas.restore();
        }
    }

    private void setSpeed() {
        xSpeed = screenX / 300;
        ySpeed = screenY / 300;
    }

    public void update() {
        int m_h = man_h / 6;
        if (life_remain < 1 || m_x + m_h < 0 || m_x + man_w - m_h > screenX) {
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

        if (last_open_hatch_intersection_time != 0) {
            m_c_x += xSpeed * move_left;

            if (last_open_hatch_intersection_time + duration / 2 < System.currentTimeMillis()) {
                last_open_hatch_intersection_time = 0;

                life_remain--;
                obstacle_data.remove(active_open_hatch_index);
                add_obstacle();
            }
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

            if (last_open_hatch_intersection_time == 0)
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
        for (int i = 0; i < obstacle_data.size(); i++) {
            int x = obstacle_data.get(i).get(0);

            x += xSpeed * move_left;
            obstacle_data.get(i).set(0, x);
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

            int w = iron_w;
            int type = obstacle_data.get(0).get(2);
            if (type == JIGSAW)
                w = jigsaw_w;
            else if (type == TREE)
                w = tree_w;
            else if (type == HATCH_OPEN)
                w = hatch_w_o;
            else if (type == HATCH_CLOSE)
                w = hatch_w_c;

            if (obstacle_data.get(0).get(0) + w < 0) {
                obstacle_data.remove(0);
                add_obstacle();
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

            if (obstacle_data.get(obstacle_data.size() - 1).get(0) > screenX) {
                obstacle_data.remove(obstacle_data.size() - 1);
                add_obstacle();
            }

            if (house_data.get(house_data.size() - 1).get(0) > screenX) {
                house_data.remove(house_data.size() - 1);
                add_house();
            }
        }
    }

    private void check_intersection() {
        for (int i = 0; i < obstacle_data.size(); i++) {
            int x = obstacle_data.get(i).get(0);
            int type = obstacle_data.get(i).get(2);
            int w;
            Rect rect = null;
            if (type == IRON) {
                w = iron_w;
                rect = new Rect(x, iron_y, x + iron_w, iron_y + iron_h);
            } else if (type == JIGSAW) {
                w = jigsaw_w;
                rect = new Rect(x, jigsaw_y, x + jigsaw_w, jigsaw_y + jigsaw_h);
            } else if (type == HATCH_OPEN) {
                w = hatch_w_c;
                rect = new Rect(x, jigsaw_y, x + jigsaw_w, jigsaw_y + jigsaw_h);
            }

            if (rect == null)
                continue;

            if (Rect.intersects(getPlayerCollision(), rect)) {
                if (type == HATCH_OPEN) {
                    if (last_open_hatch_intersection_time == 0) {
                        last_open_hatch_intersection_time = System.currentTimeMillis();
                        man_index = 0;
                        active_open_hatch_index = i;
                    }
                } else {
                    life_remain--;
                    obstacle_data.remove(i);
                    add_obstacle();
                }

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