package com.example.choose;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dou361.dialogui.DialogUIUtils;
import com.guoqi.highlightview.Component;
import com.guoqi.highlightview.Guide;
import com.guoqi.highlightview.GuideBuilder;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button start;
    private TextView option_1, option_2, option_3;
    private TextView title_str;
    private ImageView circle;
    private ImageView image;
    private LinearLayout ll;
    private DialogInterface dialogInterface;

    private int bt_start_click_count = 0;
    private float now_x = 0.0f;
    private float sum_x = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        start = findViewById(R.id.start);
        option_1 = findViewById(R.id.option_1);
        option_2 = findViewById(R.id.option_2);
        option_3 = findViewById(R.id.option_3);
        title_str = findViewById(R.id.title_str);
        image = findViewById(R.id.image);
        ll = findViewById(R.id.ll);
        circle = findViewById(R.id.circle);
        circle.setImageBitmap(draw_circle(12));
        start.setOnClickListener(this);
        option_1.setOnClickListener(listener);
        option_2.setOnClickListener(listener);
        option_3.setOnClickListener(listener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                if (bt_start_click_count % 2 == 0) {
                    //球位置摆放
                    PropertyValuesHolder px = PropertyValuesHolder.ofFloat("translationX", 0, 0);
                    PropertyValuesHolder py = PropertyValuesHolder.ofFloat("translationY", 0, 0);
                    ObjectAnimator.ofPropertyValuesHolder(circle, px, py).setDuration(10).start();
                    circle.setVisibility(View.VISIBLE);
                    start.setText(getString(R.string.Start));
                    option_1.setTextColor(0xFF868686);
                    option_2.setTextColor(0xFF868686);
                    option_3.setTextColor(0xFF868686);
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                circle_action.obtainMessage(Constant.CIRCLE_ACTION_START).sendToTarget();
                                for (int i = 1; i < 26; i++) {
                                    circle_action.obtainMessage(Constant.CIRCLE_ACTIONING, i).sendToTarget();
                                    sleep(i % 2 == 0 ? Constant.long_action_date : Constant.short_action_date);
                                }
                                circle_action.obtainMessage(Constant.CIRCLE_ACTION_END).sendToTarget();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                bt_start_click_count++;
                break;
        }
    }

    private Handler circle_action = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.CIRCLE_ACTIONING:
                    int count = (int) msg.obj;
                    PropertyValuesHolder px;
                    PropertyValuesHolder py;
                    if (count % 2 == 1) {
                        px = PropertyValuesHolder.ofFloat("translationX", sum_x, sum_x);
                    } else {
                        Random random = new Random();
                        int number = random.nextInt(2);
                        if (number == 0) {
                            if (sum_x == -((image.getWidth() / 5) * 2))
                                now_x = sum_x + (image.getWidth() / 5);
                            else
                                now_x = sum_x - (image.getWidth() / 5);
                        } else {
                            if (sum_x == ((image.getWidth() / 5) * 2))
                                now_x = sum_x - (image.getWidth() / 5);
                            else
                                now_x = sum_x + (image.getWidth() / 5);
                        }
                        px = PropertyValuesHolder.ofFloat("translationX", sum_x, now_x);
                        sum_x = now_x;
                    }
                    if (count == 25)
                        py = PropertyValuesHolder.ofFloat("translationY", (count - 1) * 50, (count + 1) * 50);
                    else
                        py = PropertyValuesHolder.ofFloat("translationY", (count - 1) * 50, count * 50);
                    ObjectAnimator.ofPropertyValuesHolder(circle, px, py).setDuration(count % 2 == 0 ? Constant.long_action_date : Constant.short_action_date).start();
                    break;
                case Constant.CIRCLE_ACTION_END:
                    if (sum_x == -((image.getWidth() / 5) * 2))
                        option_1.setTextColor(0xFF0000FF);
                    else if (sum_x == ((image.getWidth() / 5) * 2))
                        option_3.setTextColor(0xFF0000FF);
                    else
                        option_2.setTextColor(0xFF0000FF);
                    viewEnabled(true);
                    start.setText(getString(R.string.Encore));
                    break;
                case Constant.CIRCLE_ACTION_START:
                    sum_x = 0.0f;
                    now_x = 0.0f;
                    viewEnabled(false);
                    start.setText(getString(R.string.Actioning));
                    break;

            }
        }
    };

    private Handler draw_path = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.DRAW_PATH_START:
                    dialogInterface = DialogUIUtils.showLoadingVertical(MainActivity.this, getString(R.string.Drawpathing), true, false, false).show();
                    break;
                case Constant.DRAW_PATH_END:
                    dialogInterface.dismiss();
                    image.setImageBitmap(draw_path());
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) circle.getLayoutParams();
                    params.setMargins(image.getWidth() / 2 - (circle.getWidth() / 2), image.getTop() + 50 - (circle.getHeight() / 2), 0, 0);
                    circle.setLayoutParams(params);
                    SharedPreferences sp = getSharedPreferences("SHIDE", Context.MODE_PRIVATE);
                    if (sp.getBoolean("KEY", true)) {
                        showHighLight(ll);
                        sp.edit().putBoolean("KEY", false).commit();
                    }
                    break;
            }
        }
    };

    /**
     * @param r 圆的半径
     * @return
     */
    private Bitmap draw_circle(int r) {
        Bitmap bitmap;
        //画笔
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setAlpha(150);
        mPaint.setStrokeWidth(18);
        bitmap = Bitmap.createBitmap(2 * r, 2 * r, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawCircle(r, r, r, mPaint);
        return bitmap;
    }

    //绘画出路径图
    private Bitmap draw_path() {
        Bitmap bitmap;
        //画笔
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(150);
        mPaint.setStrokeWidth(18);


        int width = image.getWidth();
        int height = image.getHeight();
        int w_unit = width / 5;
        int h_unit = 50;
        int w_center = width / 2;

        //画布
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
//        canvas.drawRGB(255, 255, 255);

        int count = 1;
        canvas.drawLine(w_center, h_unit * count, w_center, h_unit * (++count), mPaint);

        canvas.drawLine(w_center, h_unit * count, w_center - w_unit, h_unit * (count + 1), mPaint);
        canvas.drawLine(w_center, h_unit * count, w_center + w_unit, h_unit * (++count), mPaint);

        for (int i = 0; i < 5; i++) {
            canvas.drawLine(w_center + w_unit, h_unit * count, w_center + w_unit, h_unit * (count + 1), mPaint);
            canvas.drawLine(w_center - w_unit, h_unit * count, w_center - w_unit, h_unit * (++count), mPaint);

            canvas.drawLine(w_center - w_unit, h_unit * count, w_center - w_unit * 2, h_unit * (count + 1), mPaint);
            canvas.drawLine(w_center - w_unit, h_unit * count, w_center, h_unit * (count + 1), mPaint);
            canvas.drawLine(w_center + w_unit, h_unit * count, w_center, h_unit * (count + 1), mPaint);
            canvas.drawLine(w_center + w_unit, h_unit * count, w_center + w_unit * 2, h_unit * (++count), mPaint);

            canvas.drawLine(w_center - w_unit * 2, h_unit * count, w_center - w_unit * 2, h_unit * (count + 1), mPaint);
            canvas.drawLine(w_center, h_unit * count, w_center, h_unit * (count + 1), mPaint);
            canvas.drawLine(w_center + w_unit * 2, h_unit * count, w_center + w_unit * 2, h_unit * (++count), mPaint);

            canvas.drawLine(w_center - w_unit * 2, h_unit * count, w_center - w_unit, h_unit * (count + 1), mPaint);
            canvas.drawLine(w_center, h_unit * count, w_center - w_unit, h_unit * (count + 1), mPaint);
            canvas.drawLine(w_center, h_unit * count, w_center + w_unit, h_unit * (count + 1), mPaint);
            canvas.drawLine(w_center + w_unit * 2, h_unit * count, w_center + w_unit, h_unit * (++count), mPaint);
        }

        canvas.drawLine(w_center + w_unit, h_unit * count, w_center + w_unit, h_unit * (count + 1), mPaint);
        canvas.drawLine(w_center - w_unit, h_unit * count, w_center - w_unit, h_unit * (++count), mPaint);

        canvas.drawLine(w_center - w_unit, h_unit * count, w_center - w_unit * 2, h_unit * (count + 1), mPaint);
        canvas.drawLine(w_center - w_unit, h_unit * count, w_center, h_unit * (count + 1), mPaint);
        canvas.drawLine(w_center + w_unit, h_unit * count, w_center, h_unit * (count + 1), mPaint);
        canvas.drawLine(w_center + w_unit, h_unit * count, w_center + w_unit * 2, h_unit * (++count), mPaint);

        canvas.drawLine(w_center - w_unit * 2, h_unit * count, w_center - w_unit * 2, h_unit * (count + 1), mPaint);
        canvas.drawLine(w_center, h_unit * count, w_center, h_unit * (count + 1), mPaint);
        canvas.drawLine(w_center + w_unit * 2, h_unit * count, w_center + w_unit * 2, h_unit * (++count), mPaint);

        return bitmap;
    }

    //双击处理
    private boolean waitDouble = true;
    private static final int DOUBLE_CLICK_TIME = 350; //两次单击的时间间隔
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final TextView tv = (TextView) v;
            if (waitDouble == true) {
                waitDouble = false;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(DOUBLE_CLICK_TIME);
                            if (waitDouble == false) {
                                waitDouble = true;
                                //单击响应事件
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            } else {
                waitDouble = true;
                // 双击响应事件
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getString(R.string.Change) + tv.getTag() + getString(R.string.Text));
                final EditText et = new EditText(MainActivity.this);
                et.setText(tv.getText());
                et.setTextSize(20);
                builder.setView(et);
                builder.setPositiveButton(getString(R.string.Sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = et.getText().toString().trim();
                        if (!TextUtils.isEmpty(str)) {
                            tv.setText(str);
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.Change_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.Cancel), null);
                Dialog dialog = builder.create();
                dialog.show();
            }
        }
    };

    public void viewEnabled(Boolean b) {
        start.setEnabled(b);
        option_1.setEnabled(b);
        option_2.setEnabled(b);
        option_3.setEnabled(b);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = getSharedPreferences("RECORD", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("OPTION_1", option_1.getText() + "");
        editor.putString("OPTION_2", option_2.getText() + "");
        editor.putString("OPTION_3", option_3.getText() + "");
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sp = getSharedPreferences("RECORD", Context.MODE_PRIVATE);
        option_1.setText(sp.getString("OPTION_1", getString(R.string.OPTION_1)));
        option_2.setText(sp.getString("OPTION_2", getString(R.string.OPTION_2)));
        option_3.setText(sp.getString("OPTION_3", getString(R.string.OPTION_3)));
        start.setText(getString(R.string.setout));

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    draw_path.obtainMessage(Constant.DRAW_PATH_START).sendToTarget();
                    sleep(1000);
                    draw_path.obtainMessage(Constant.DRAW_PATH_END).sendToTarget();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        circle.setVisibility(View.INVISIBLE);
    }

    public void showHighLight(final View v) {
        GuideBuilder builder = new GuideBuilder();
        builder
                //设置要高亮显示的View
                .setTargetView(v)
                //设置遮罩透明度(0-255)
                .setAlpha(180)
                //设置遮罩形状 默认矩形Component.ROUNDRECT,可选圆形
                .setHighTargetGraphStyle(Component.ROUNDRECT)
                //设置高亮区域圆角
                .setHighTargetCorner(100)
                //是否覆盖目标View,默认flase
                .setOverlayTarget(false)
                //设置进出动画
                .setEnterAnimationId(android.R.anim.fade_in)
                .setExitAnimationId(android.R.anim.fade_out)
                //外部是否可点击取消遮罩,true为不可取消
                .setOutsideTouchable(false);

        builder.setOnVisibilityChangedListener(new GuideBuilder.OnVisibilityChangedListener() {
            @Override
            public void onShown() {
                //显示遮罩时触发
            }

            @Override
            public void onDismiss() {
                //遮罩消失时触发
            }
        });
        builder.addComponent(new MyComponent());
        Guide guide = builder.createGuide();
        //检测定位非0
        guide.setShouldCheckLocInWindow(false);
        //显示
        guide.show(this);
        //设置遮罩监听
    }

}
