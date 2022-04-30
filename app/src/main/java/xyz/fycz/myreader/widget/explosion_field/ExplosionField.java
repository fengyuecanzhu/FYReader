/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */
package xyz.fycz.myreader.widget.explosion_field;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class ExplosionField extends View {

    private long customDuration = ExplosionAnimator.DEFAULT_DURATION;
    private int idPlayAnimationEffect = 0;
    private OnAnimatorListener mZAnimatorListener;
    private OnClickListener mOnClickListener;

    private List<ExplosionAnimator> mExplosions = new ArrayList<>();
    private int[] mExpandInset = new int[2];

    public ExplosionField(Context context) {
        super(context);
        init();
    }

    public ExplosionField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExplosionField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        Arrays.fill(mExpandInset, Utils.dp2Px(32));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (ExplosionAnimator explosion : mExplosions) {
            explosion.draw(canvas);
        }
    }

    public void playSoundAnimationEffect(int id) {
        this.idPlayAnimationEffect = id;
    }

    public void setCustomDuration(long customDuration) {
        this.customDuration = customDuration;
    }

    public void addActionEvent(OnAnimatorListener ievents) {
        this.mZAnimatorListener = ievents;
    }


    public void expandExplosionBound(int dx, int dy) {
        mExpandInset[0] = dx;
        mExpandInset[1] = dy;
    }

    public void explode(Bitmap bitmap, Rect bound, long startDelay) {
        explode(bitmap, bound, startDelay, null);
    }

    public void explode(Bitmap bitmap, Rect bound, long startDelay, final View view) {
        long currentDuration = customDuration;
        final ExplosionAnimator explosion = new ExplosionAnimator(this, bitmap, bound);
        explosion.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mExplosions.remove(animation);
                if (view != null) {
                    view.setScaleX(1);
                    view.setScaleY(1);
                    view.setAlpha(1);
                    view.setOnClickListener(mOnClickListener);//set event

                }
            }
        });
        explosion.setStartDelay(startDelay);
        explosion.setDuration(currentDuration);
        mExplosions.add(explosion);
        explosion.start();
    }

    public void explode(View view) {
        explode(view, false);
    }

    public void explode(final View view, Boolean restartState) {

        Rect r = new Rect();
        view.getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
//        getLocationInWindow(location);
//        view.getLocationInWindow(location);
        r.offset(-location[0], -location[1]);
        r.inset(-mExpandInset[0], -mExpandInset[1]);
        int startDelay = 100;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            Random random = new Random();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationX((random.nextFloat() - 0.5f) * view.getWidth() * 0.05f);
                view.setTranslationY((random.nextFloat() - 0.5f) * view.getHeight() * 0.05f);
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (idPlayAnimationEffect != 0)
                    MediaPlayer.create(getContext(), idPlayAnimationEffect).start();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mZAnimatorListener != null) {
                    mZAnimatorListener.onAnimationEnd(animator, ExplosionField.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                Log.i("PRUEBA", "CANCEL");
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                Log.i("PRUEBA", "REPEAT");
            }
        });

        animator.start();
        view.animate().setDuration(150).setStartDelay(startDelay).scaleX(0f).scaleY(0f).alpha(0f).start();
        if (restartState)
            explode(Utils.createBitmapFromView(view), r, startDelay, view);
        else
            explode(Utils.createBitmapFromView(view), r, startDelay);

    }

    public void clear() {
        mExplosions.clear();
        invalidate();
    }

    public static ExplosionField attach2Window(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        ExplosionField explosionField = new ExplosionField(activity);
        rootView.addView(explosionField, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return explosionField;
    }

    public void setOnClickListener(OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }


}
