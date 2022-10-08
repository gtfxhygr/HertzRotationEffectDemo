package com.ufreedom.demo;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ufreedom.floatingview.Floating;
import com.ufreedom.floatingview.FloatingBuilder;
import com.ufreedom.floatingview.FloatingElement;
import com.ufreedom.floatingview.effect.ScaleFloatingTransition;
import com.ufreedom.floatingview.effect.TranslateFloatingTransition;
import com.ufreedom.floatingview.spring.ReboundListener;
import com.ufreedom.floatingview.spring.SimpleReboundListener;
import com.ufreedom.floatingview.spring.SpringHelper;
import com.ufreedom.floatingview.transition.BaseFloatingPathTransition;
import com.ufreedom.floatingview.transition.FloatingPath;
import com.ufreedom.floatingview.transition.FloatingTransition;
import com.ufreedom.floatingview.transition.PathPosition;
import com.ufreedom.floatingview.transition.YumFloating;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Created by gtf on 2022/9/27
 */
public class MainActivity extends AppCompatActivity {
    private boolean recordWhetherALap;
    private Floating mFloating;
    int angle = 360;
    private ImageView bg;
    private int radius;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFloating = new Floating(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        initLayout();
    }

    private void initLayout() {
        bg = findViewById(R.id.icBg);
        Glide.with(this).asGif().load(R.drawable.bg).transition(withCrossFade()).into(bg);
        bg.post(new Runnable() {
            @Override
            public void run() {
                radius = (int) (bg.getWidth() * 0.4);
                startAnimation(bg);
            }
        });
    }

    private void startAnimation(final View v) {
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = new ImageView(MainActivity.this);
                int w_h = UIUtils.dip2px(MainActivity.this, 50);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(w_h, w_h));
                Glide.with(imageView.getContext()).load("https://api.multiavatar.com/Starcrasher" + new Random().nextInt(200) + ".png").apply(new RequestOptions()).placeholder(R.mipmap.ic_default_head_circle).circleCrop().into(imageView);
                FloatingElement floatingElement = new FloatingBuilder().anchorView(v).targetView(imageView).floatingTransition(new PlaneFloating()).build();
                mFloating.startFloating(floatingElement);
                startAnimation(v);
            }
        }, 1000);
    }

    public class PlaneFloating extends BaseFloatingPathTransition {
        float x, y, x1, y1, x2, y2;

        public PlaneFloating() {
            if (angle > 0) {
                angle -= 20;
            } else {
                angle = 360;
            }
            x = Double.valueOf((radius) * Math.cos(angle)).floatValue();
            y = Double.valueOf((radius) * Math.sin(angle)).floatValue();
            angle += 150;
            x1 = Double.valueOf((radius) * Math.cos(angle)).floatValue();
            y1 = Double.valueOf((radius) * Math.sin(angle)).floatValue();
            angle += 250;
            x2 = Double.valueOf((radius) * Math.cos(angle)).floatValue();
            y2 = Double.valueOf((radius) * Math.sin(angle)).floatValue();

        }

        @Override
        public FloatingPath getFloatingPath() {
            Path path = new Path();
            path.rCubicTo(x, y, x1, y1, x2, y2);
            return FloatingPath.create(path, false);
        }

        @Override
        public void applyFloating(final YumFloating yumFloating) {
            final ValueAnimator translateAnimator = ObjectAnimator.ofFloat(getStartPathPosition(), getEndPathPosition());
            translateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float value = (float) valueAnimator.getAnimatedValue();
                    long curPlayTime = translateAnimator.getCurrentPlayTime();
                    BigDecimal scale = new BigDecimal(curPlayTime).divide(new BigDecimal(5000));
                    float scale_v = (float) (scale.floatValue() + 0.1);
                    PathPosition floatingPosition = getFloatingPosition(value);
                    yumFloating.setTranslationX(floatingPosition.x);
                    yumFloating.setTranslationY(floatingPosition.y);
                    float alpha = (float) (11 - ((scale_v * 100) / 10));
                    if (scale_v < 1) {
                        yumFloating.setScaleX(scale_v);
                        yumFloating.setScaleY(scale_v);
                    } else {
                        if (alpha < 0) {
                            alpha = 0;
                        }
                        yumFloating.setAlpha(alpha);
                    }
                }
            });
            translateAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    yumFloating.setTranslationX(0);
                    yumFloating.setTranslationY(0);
                    yumFloating.setAlpha(0f);
                    yumFloating.clear();
                }
            });
            translateAnimator.setDuration(5000);
            translateAnimator.setInterpolator(new LinearInterpolator());
            translateAnimator.start();
        }
    }

}
