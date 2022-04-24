/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.widget.animation;

import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.entity.Setting;

/**
 * @author fengyue
 * @date 2020/11/19
 */

public class AutoPageAnim extends PageAnimation {
    private static final String TAG = "AutoPageAnim";

    private Rect mSrcRect, mDestRect, mNextSrcRect,mNextDestRect;
    private Bitmap mCurBitmap;
    private Bitmap mNextBitmap;
    private GradientDrawable mBackShadowDrawableLR;
    private Setting setting = SysManager.getSetting();

    public AutoPageAnim(int w, int h, View view, OnPageChangeListener listener) {
        this(w, h, 0, 0,0, view, listener);
    }

    public AutoPageAnim(int w, int h, int marginWidth, int marginTop, int marginBottom, View view, OnPageChangeListener listener) {
        super(w, h, marginWidth, marginTop,marginBottom, view, listener);
        //创建图片
        mCurBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);
        mNextBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);

        mSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
        int[] mBackShadowColors = new int[]{0x66000000, 0x00000000};
        mBackShadowDrawableLR = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, mBackShadowColors);
        mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    /**
     * 转换页面，在显示下一章的时候，必须首先调用此方法
     */
    public void changePage() {
        Bitmap bitmap = mCurBitmap;
        mCurBitmap = mNextBitmap;
        mNextBitmap = bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        int dis = (int) mTouchY;
        //计算bitmap截取的区域
        mSrcRect.top = dis;
        //计算bitmap在canvas显示的区域
        mDestRect.top = dis;
        //计算下一页bitmap截取的区域
        mNextSrcRect.bottom = dis;
        //计算下一页bitmap在canvas显示的区域
        mNextDestRect.bottom = dis;
        canvas.drawBitmap(mNextBitmap, mNextSrcRect, mNextDestRect, null);
        canvas.drawBitmap(mCurBitmap, mSrcRect, mDestRect, null);
        addShadow(dis, canvas);
    }

    //添加阴影
    public void addShadow(int top, Canvas canvas) {
        mBackShadowDrawableLR.setBounds(0, top, mScreenWidth, top + 30);
        mBackShadowDrawableLR.draw(canvas);
    }

    @Override
    public void scrollAnim() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            setTouchPoint(x, y);
            mView.postInvalidate();
        }
    }

    @Override
    public void abortAnim() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            isRunning = false;
            setTouchPoint(mScroller.getFinalX(), mScroller.getFinalY());
            mView.postInvalidate();
        }
    }

    @Override
    public void startAnim() {
        int dy = (int) (mScreenHeight);
        mScroller.startScroll(0, 0, 0, dy, setting.getAutoScrollSpeed() * 1000);
    }

    @Override
    public Bitmap getBgBitmap() {
        return mNextBitmap;
    }

    @Override
    public Bitmap getNextBitmap() {
        return mNextBitmap;
    }
}
